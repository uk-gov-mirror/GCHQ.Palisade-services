/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.contract.palisade.kafka;

import akka.actor.ActorSystem;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.Materializer;
import akka.stream.testkit.TestSubscriber.Probe;
import akka.stream.testkit.javadsl.TestSink;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import scala.concurrent.duration.FiniteDuration;

import uk.gov.gchq.palisade.contract.palisade.common.ContractTestData;
import uk.gov.gchq.palisade.contract.palisade.common.TestSerDesConfig;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;
import uk.gov.gchq.palisade.service.palisade.model.AuditErrorMessage;
import uk.gov.gchq.palisade.service.palisade.model.PalisadeClientRequest;
import uk.gov.gchq.palisade.service.palisade.model.PalisadeSystemResponse;
import uk.gov.gchq.palisade.service.palisade.model.StreamMarker;
import uk.gov.gchq.palisade.service.palisade.model.Token;
import uk.gov.gchq.palisade.service.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.palisade.stream.ProducerTopicConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;

/**
 * An external requirement of the service is to connect to one kafka topic.
 * The input is the requests for data that come from the client via the website
 * The downstream "request" topic is written to by this service and read by the User Service.
 */
@SpringBootTest(
        classes = PalisadeApplication.class,
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = {"akka.discovery.config.services.kafka.from-config=false"}
)
@Import({KafkaInitialiser.Config.class})
@ContextConfiguration(initializers = {KafkaInitialiser.class})
@ActiveProfiles("akka-test")
class KafkaContractTest {
    public static final String REGISTER_DATA_REQUEST = "/api/registerDataRequest";

    @SpyBean
    PalisadeService service;
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ActorSystem akkaActorSystem;
    @Autowired
    private Materializer akkaMaterialiser;
    @Autowired
    private ProducerTopicConfiguration producerTopicConfiguration;

    /**
     * Tests the rest endpoint used for mocking a kafka entry point exists and is working as expected, returns HTTP.ACCEPTED.
     * Then checks the token and headers are correct.
     */
    @Test
    @DirtiesContext
    void testRestEndpointSuccess() {
        Mockito.doReturn(ContractTestData.REQUEST_TOKEN).when(service).createToken(any());

        // Given - we are already listening to the service input
        ConsumerSettings<String, PalisadeSystemResponse> consumerSettings = ConsumerSettings
                .create(akkaActorSystem, TestSerDesConfig.requestKeyDeserialiser(), TestSerDesConfig.requestValueDeserialiser())
                .withGroupId("test-group")
                .withBootstrapServers(KafkaInitialiser.KAFKA.getBootstrapServers())
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final long recordCount = 3;

        Probe<ConsumerRecord<String, PalisadeSystemResponse>> probe = Consumer
                .atMostOnceSource(consumerSettings, Subscriptions.topics(producerTopicConfiguration.getTopics().get("output-topic").getName()))
                .runWith(TestSink.probe(akkaActorSystem), akkaMaterialiser);

        // When - we POST to the rest endpoint
        Map<String, List<String>> headers = Collections.singletonMap(Token.HEADER, Collections.singletonList(ContractTestData.REQUEST_TOKEN));
        HttpEntity<PalisadeClientRequest> entity = new HttpEntity<>(ContractTestData.REQUEST_OBJ, new LinkedMultiValueMap<>(headers));
        ResponseEntity<Void> response = restTemplate.postForEntity(REGISTER_DATA_REQUEST, entity, Void.class);

        // Then - the REST request was accepted
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        // When - results are pulled from the output stream
        Probe<ConsumerRecord<String, PalisadeSystemResponse>> resultSeq = probe.request(recordCount);
        LinkedList<ConsumerRecord<String, PalisadeSystemResponse>> results = LongStream.range(0, recordCount)
                .mapToObj(i -> resultSeq.expectNext(new FiniteDuration(20 + recordCount, TimeUnit.SECONDS)))
                .collect(Collectors.toCollection(LinkedList::new));

        // Then - the results are as expected
        // The request was written with the correct header
        assertAll("Records returned are correct",
                () -> assertThat(results)
                        .as("Check the number of results from the request probe")
                        .hasSize((int) recordCount),

                () -> assertThat(results.getFirst().headers().lastHeader(Token.HEADER).value())
                        .as("Check the byte value of the request-token header for the first result")
                        .isEqualTo(ContractTestData.REQUEST_TOKEN.getBytes()),
                () -> assertThat(results.getFirst().headers().lastHeader(StreamMarker.HEADER).value())
                        .as("Check the byte value of the stream-marker header for the first result")
                        .isEqualTo(StreamMarker.START.toString().getBytes()),

                () -> assertThat(results.getLast().headers().lastHeader(Token.HEADER).value())
                        .as("Check the byte value of the request-token header for the last result")
                        .isEqualTo(ContractTestData.REQUEST_TOKEN.getBytes()),
                () -> assertThat(results.getLast().headers().lastHeader(StreamMarker.HEADER).value())
                        .as("Check the byte value of the stream-marker header for the last result")
                        .isEqualTo(StreamMarker.END.toString().getBytes())
        );

        // Remove the START and END messages from the results
        results.removeFirst();
        results.removeLast();

        assertThat(results)
                .as("Check that there is only 1 result after removing START and END messages")
                .hasSize(1)
                .allSatisfy(result -> {
                    assertThat(result.value())
                            .as("Check the value of the returned PalisadeSystemResponse")
                            .isEqualTo(PalisadeSystemResponse.Builder.create(ContractTestData.REQUEST_OBJ));
                    assertThat(result.headers().lastHeader(Token.HEADER).value())
                            .as("Check the byte value of the request-token header")
                            .isEqualTo(ContractTestData.REQUEST_TOKEN.getBytes());
                });
    }

    /**
     * Tests the rest endpoint used for mocking a kafka entry point exists and is working as expected, returns HTTP.INTERNAL_SERVER_ERROR.
     * Then checks the token and message are correct.
     */
    @Test
    @DirtiesContext
    void testRestEndpointError() {
        Mockito.when(service.registerDataRequest(any())).thenThrow(RuntimeException.class);

        // Given - we are already listening to the service input
        ConsumerSettings<String, AuditErrorMessage> consumerSettings = ConsumerSettings
                .create(akkaActorSystem, TestSerDesConfig.errorKeyDeserialiser(), TestSerDesConfig.errorValueDeserialiser())
                .withGroupId("test-group")
                .withBootstrapServers(KafkaInitialiser.KAFKA.getBootstrapServers())
                .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        final long recordCount = 1;

        Probe<ConsumerRecord<String, AuditErrorMessage>> probe = Consumer
                .atMostOnceSource(consumerSettings, Subscriptions.topics(producerTopicConfiguration.getTopics().get("error-topic").getName()))
                .runWith(TestSink.probe(akkaActorSystem), akkaMaterialiser);

        // When - we POST to the rest endpoint
        Map<String, List<String>> headers = Collections.singletonMap(Token.HEADER, Collections.singletonList(ContractTestData.ERROR_TOKEN));
        HttpEntity<PalisadeClientRequest> entity = new HttpEntity<>(ContractTestData.REQUEST_OBJ, new LinkedMultiValueMap<>(headers));
        ResponseEntity<Void> response = restTemplate.postForEntity(REGISTER_DATA_REQUEST, entity, Void.class);

        // Then - the REST request encountered an error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        // When - results are pulled from the error stream
        Probe<ConsumerRecord<String, AuditErrorMessage>> resultSeq = probe.request(recordCount);
        LinkedList<ConsumerRecord<String, AuditErrorMessage>> results = LongStream.range(0, recordCount)
                .mapToObj(i -> resultSeq.expectNext(new FiniteDuration(20 + recordCount, TimeUnit.SECONDS)))
                .collect(Collectors.toCollection(LinkedList::new));

        // Then - the results are as expected
        // The request was written with the correct header
        assertThat(results)
                .as("Check there is only 1 result from the error probe")
                .hasSize(1)
                .allSatisfy(result -> {
                    assertThat(result.value()).usingRecursiveComparison()
                            .ignoringFieldsOfTypes(Throwable.class).ignoringFields("timestamp")
                            .as("Recursively compare the AuditErrorMessage object, ignoring the Throwable and TimeStamp values")
                            .isEqualTo(ContractTestData.ERROR_OBJ);
                    assertThat(result.value().getError())
                            .as("Check the error class within the AuditErrorMessage")
                            .isInstanceOf(Throwable.class);
                    assertThat(result.headers().lastHeader(Token.HEADER).value())
                            .as("Check the byte value of the request-token header")
                            .isEqualTo(ContractTestData.ERROR_TOKEN.getBytes());
                });
    }

}
