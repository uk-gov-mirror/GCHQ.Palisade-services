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

package uk.gov.gchq.palisade.service.attributemask.service;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.common.ConsumerGroupState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Kafka health indicator. Check that the consumer group can be accessed and is registered with the cluster,
 * if not mark the service as unhealthy.
 */
@Component("kafka")
@ConditionalOnEnabledHealthIndicator("kafka")
public class KafkaHealthIndicator implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaHealthIndicator.class);

    private final String groupId;
    private final AdminClient adminClient;

    /**
     * Requires the AdminClient to interact with Kafka
     *
     * @param groupId     of the cluster
     * @param adminClient of the cluster
     */
    public KafkaHealthIndicator(@Value("${akka.kafka.consumer.kafka-clients.group.id}") final String groupId, final AdminClient adminClient) {
        this.groupId = groupId;
        this.adminClient = adminClient;
    }

    /**
     * Health endpoint
     *
     * @return the {@code Health} object
     */
    @Override
    public Health health() {
        if (performCheck()) {
            return Health.up().withDetail("group", this.groupId).build();
        } else {
            return Health.down().withDetail("group", this.groupId).build();
        }
    }

    private boolean performCheck() {
        try {
            Map<String, ConsumerGroupDescription> groupDescriptionMap = this.adminClient.describeConsumerGroups(Collections.singletonList(this.groupId))
                    .all()
                    .get(1, TimeUnit.SECONDS);

            ConsumerGroupDescription consumerGroupDescription = groupDescriptionMap.get(this.groupId);
            LOGGER.debug("Kafka consumer group ({}) state: {}", groupId, consumerGroupDescription.state());

            if (consumerGroupDescription.state() == ConsumerGroupState.STABLE) {
                boolean assignedGroupPartition = consumerGroupDescription.members().stream()
                        .noneMatch(member -> (member.assignment() == null || member.assignment().topicPartitions().isEmpty()));
                if (!assignedGroupPartition) {
                    LOGGER.error("Failed to find kafka topic-partition assignments");
                }
                return assignedGroupPartition;
            }

        } catch (InterruptedException e) {
            LOGGER.warn("Await on future interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            LOGGER.warn("Timeout connecting to kafka", e);
        }
        return false;
    }
}
