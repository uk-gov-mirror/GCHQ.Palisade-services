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

package uk.gov.gchq.palisade.component.filteredresource.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.GenericContainer;

import uk.gov.gchq.palisade.component.filteredresource.repository.RedisPersistenceComponentTest.Initializer;
import uk.gov.gchq.palisade.contract.filteredresource.ContractTestData;
import uk.gov.gchq.palisade.service.filteredresource.config.ApplicationConfiguration;
import uk.gov.gchq.palisade.service.filteredresource.config.AsyncConfiguration;
import uk.gov.gchq.palisade.service.filteredresource.config.RedisTtlConfiguration;
import uk.gov.gchq.palisade.service.filteredresource.model.TopicOffsetMessage;
import uk.gov.gchq.palisade.service.filteredresource.repository.offset.JpaTokenOffsetPersistenceLayer;
import uk.gov.gchq.palisade.service.filteredresource.service.OffsetEventService;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.data.redis.repositories.timeToLive.TokenOffsetEntity=1s"})
@ContextConfiguration(
        classes = {ApplicationConfiguration.class, AsyncConfiguration.class, RedisTtlConfiguration.class, JpaTokenOffsetPersistenceLayer.class},
        initializers = Initializer.class
)
@EnableAutoConfiguration
@AutoConfigureDataRedis
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("redis")
class RedisPersistenceComponentTest {

    private static final int REDIS_PORT = 6379;

    protected void cleanCache() {
        requireNonNull(redisTemplate.getConnectionFactory()).getConnection().flushAll();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        static final GenericContainer<?> REDIS = new GenericContainer<>("redis:6-alpine")
                .withExposedPorts(REDIS_PORT)
                .withReuse(true);

        @Override
        public void initialize(@NonNull final ConfigurableApplicationContext context) {
            // Start container
            REDIS.start();

            // Override Redis configuration
            String redisContainerIP = "spring.redis.host=" + REDIS.getContainerIpAddress();
            // Configure the testcontainer random port
            String redisContainerPort = "spring.redis.port=" + REDIS.getMappedPort(REDIS_PORT);
            // Override the configuration at runtime
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context, redisContainerIP, redisContainerPort);
        }
    }

    @AfterEach
    void tearDown() {
        cleanCache();
    }

    @Autowired
    private OffsetEventService service;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Test
    void testContextLoads() {
        assertThat(service).isNotNull();
        assertThat(redisTemplate).isNotNull();
    }

    @Test
    void testTopicOffsetsAreStoredInRedis() {
        // Given we have some request data
        String token = ContractTestData.REQUEST_TOKEN;
        TopicOffsetMessage request = ContractTestData.TOPIC_OFFSET_MESSAGE;

        // When a request is made to store the topic offset for a given token
        service.storeTokenOffset(token, request.commitOffset).join();

        // Then the offset is persisted in redis
        final String redisKey = "TokenOffsetEntity:" + token;
        assertThat(redisTemplate.keys(redisKey)).hasSize(1);

        // Values for the entity are correct
        final Map<Object, Object> redisHash = redisTemplate.boundHashOps(redisKey).entries();
        assertThat(redisHash)
                .containsEntry("token", ContractTestData.REQUEST_TOKEN)
                .containsEntry("offset", ContractTestData.TOPIC_OFFSET_MESSAGE.commitOffset.toString());
    }

    @Test
    void testTopicOffsetsAreEvictedAfterTtlExpires() throws InterruptedException {
        // Given we have some request data
        String token = ContractTestData.REQUEST_TOKEN;
        TopicOffsetMessage request = ContractTestData.TOPIC_OFFSET_MESSAGE;

        // When a request is made to store the topic offset for a given token
        service.storeTokenOffset(token, request.commitOffset).join();
        TimeUnit.SECONDS.sleep(1);

        // Then the offset is persisted in redis
        assertThat(redisTemplate.keys("TokenOffsetEntity:" + token)).isEmpty();
    }

}
