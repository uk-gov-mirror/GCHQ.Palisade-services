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

package uk.gov.gchq.palisade.service.attributemask.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import uk.gov.gchq.palisade.service.attributemask.common.resource.LeafResource;
import uk.gov.gchq.palisade.service.attributemask.common.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.attributemask.repository.AuthorisedRequestsRepository;
import uk.gov.gchq.palisade.service.attributemask.repository.JpaPersistenceLayer;
import uk.gov.gchq.palisade.service.attributemask.repository.PersistenceLayer;
import uk.gov.gchq.palisade.service.attributemask.service.AttributeMaskingAspect;
import uk.gov.gchq.palisade.service.attributemask.service.AttributeMaskingService;
import uk.gov.gchq.palisade.service.attributemask.service.LeafResourceMasker;

import java.util.Collections;
import java.util.concurrent.Executor;

/**
 * Bean configuration and dependency injection graph
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    JpaPersistenceLayer persistenceLayer(final AuthorisedRequestsRepository authorisedRequestsRepository, final @Qualifier("applicationTaskExecutor") Executor executor) {
        return new JpaPersistenceLayer(authorisedRequestsRepository, executor);
    }

    @Bean
    LeafResourceMasker simpleLeafResourceMasker() {
        // Delete all additional attributes (if a FileResource)
        return (LeafResource x) -> {
            if (x instanceof FileResource) {
                return ((FileResource) x).attributes(Collections.emptyMap());
            } else {
                return x;
            }
        };
    }

    @Bean
    AttributeMaskingService simpleAttributeMaskingService(final PersistenceLayer persistenceLayer, final LeafResourceMasker resourceMasker) {
        return new AttributeMaskingService(persistenceLayer, resourceMasker);
    }

    @Bean
    AttributeMaskingAspect attributeMaskingAspect() {
        return new AttributeMaskingAspect();
    }

    /**
     * Used so that you can create custom mapper by starting with the default and then modifying if needed
     *
     * @return a configured object mapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(SerializationFeature.CLOSE_CLOSEABLE, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule());
    }
}
