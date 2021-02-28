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
package uk.gov.gchq.palisade.component.audit.model;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.boot.test.json.ObjectContent;
import org.springframework.test.context.ContextConfiguration;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.service.audit.model.AuditMessage;
import uk.gov.gchq.palisade.service.audit.model.AuditSuccessMessage;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JsonTest
@ContextConfiguration(classes = {AuditSuccessMessageTest.class})
class AuditSuccessMessageTest {

    @Autowired
    private JacksonTester<AuditSuccessMessage> jsonTester;

    /**
     * Grouped assertion test
     * Create the object with the builder and then convert to the Json equivalent.
     * Takes the JSON Object, deserialises and tests against the original Object
     *
     * @throws IOException throws if the {@link AuditSuccessMessage} object cannot be converted to a JsonContent.
     *                     This equates to a failure to serialise or deserialise the string.
     */
    @Test
    void testGroupedDependantSuccessMessageSerialisingAndDeserialising() throws IOException {
        Context context = new Context().purpose("testContext");
        String now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("messagesSent", "23");

        AuditSuccessMessage auditSuccessMessage = AuditSuccessMessage.createAuditSuccessMessage(b -> b
            .userId("originalUserID")
            .resourceId("testResourceId")
            .contextNode(AuditMessage.MAPPER.valueToTree(context))
            .serviceName("testServicename")
            .timestamp(now)
            .serverIP("testServerIP")
            .serverHostname("testServerHostname")
            .attributesNode(AuditMessage.MAPPER.valueToTree(attributes))
            .leafResourceId("testLeafResourceId"));

        JsonContent<AuditSuccessMessage> auditSuccessMessageJsonContent = jsonTester.write(auditSuccessMessage);
        ObjectContent<AuditSuccessMessage> auditSuccessMessageObjectContent = jsonTester.parse(auditSuccessMessageJsonContent.getJson());
        AuditSuccessMessage auditSuccessMessageObject = auditSuccessMessageObjectContent.getObject();

        assertAll("AuditSerialisingDeseralisingAndComparison",
                () -> assertAll("AuditSerialisingComparedToString",
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.userId").isEqualTo("originalUserID"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.resourceId").isEqualTo("testResourceId"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.context.contents.purpose").isEqualTo("testContext"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.serviceName").isEqualTo("testServicename"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.timestamp").isEqualTo(now),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.serverIP").isEqualTo("testServerIP"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.serverHostname").isEqualTo("testServerHostname"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.leafResourceId").isEqualTo("testLeafResourceId"),
                        () -> assertThat(auditSuccessMessageJsonContent).extractingJsonPathStringValue("$.attributes.messagesSent").isEqualTo("23")
                ),
                () -> assertAll("AuditDeserialisingComparedToObject",
                        () -> assertThat(auditSuccessMessageObject.getUserId()).isEqualTo(auditSuccessMessage.getUserId()),
                        () -> assertThat(auditSuccessMessageObject.getResourceId()).isEqualTo(auditSuccessMessage.getResourceId()),
                        () -> assertThat(auditSuccessMessageObject.getContext().getPurpose()).isEqualTo(auditSuccessMessage.getContext().getPurpose()),
                        () -> assertThat(auditSuccessMessageObject.getServiceName()).isEqualTo(auditSuccessMessage.getServiceName()),
                        () -> assertThat(auditSuccessMessageObject.getServerIP()).isEqualTo(auditSuccessMessage.getServerIP()),
                        () -> assertThat(auditSuccessMessageObject.getServerHostname()).isEqualTo(auditSuccessMessage.getServerHostname()),
                        () -> assertThat(auditSuccessMessageObject.getTimestamp()).isEqualTo(auditSuccessMessage.getTimestamp()),
                        () -> assertThat(auditSuccessMessageObject.getAttributes()).isEqualTo(auditSuccessMessage.getAttributes()),
                        () -> assertThat(auditSuccessMessageObject.getLeafResourceId()).isEqualTo(auditSuccessMessage.getLeafResourceId())
                ),
                () -> assertAll("ObjectComparison",
                        //The reconstructed stack trace wont be exactly the same due to different object hashes so equals is used here
                        () -> assertThat(auditSuccessMessageObject).usingRecursiveComparison().isEqualTo(auditSuccessMessage)
                )
        );
    }
}
