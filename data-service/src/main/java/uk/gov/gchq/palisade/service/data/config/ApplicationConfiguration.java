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
package uk.gov.gchq.palisade.service.data.config;

import akka.stream.Materializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import uk.gov.gchq.palisade.reader.HadoopDataReader;
import uk.gov.gchq.palisade.service.data.common.data.DataReader;
import uk.gov.gchq.palisade.service.data.common.data.SerialisedDataReader;
import uk.gov.gchq.palisade.service.data.repository.AuthorisedRequestsRepository;
import uk.gov.gchq.palisade.service.data.repository.JpaPersistenceLayer;
import uk.gov.gchq.palisade.service.data.repository.PersistenceLayer;
import uk.gov.gchq.palisade.service.data.service.AuditMessageService;
import uk.gov.gchq.palisade.service.data.service.AuditableDataService;
import uk.gov.gchq.palisade.service.data.service.DataService;
import uk.gov.gchq.palisade.service.data.service.SimpleDataService;

import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * Bean configuration and dependency injection graph
 */
@Configuration
public class ApplicationConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfiguration.class);

    private static final int CORE_POOL_SIZE = 6;

    /**
     * Bean for the {@link JpaPersistenceLayer}.
     * Connect the Redis or Caffeine backed repository to the persistence layer, providing an executor for any async requests
     *
     * @param requestsRepository an instance of the requests' repository, backed by either caffeine or redis (depending on profile)
     * @param executor           an async executor, preferably a {@link org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor}
     * @return a {@link JpaPersistenceLayer} wrapping the repository instance, providing async methods for getting data from persistence
     */
    @Bean
    JpaPersistenceLayer jpaPersistenceLayer(final AuthorisedRequestsRepository requestsRepository,
                                            final @Qualifier("threadPoolTaskExecutor") Executor executor) {
        return new JpaPersistenceLayer(requestsRepository, executor);
    }

    /**
     * Bean for a {@link SimpleDataService}, connecting a {@link DataReader} and {@link PersistenceLayer}.
     * These are likely the {@code HadoopDataReader} and the {@link JpaPersistenceLayer}.
     *
     * @param persistenceLayer the persistence layer for reading authorised requests
     * @param dataReader       the data reader to use for reading resource data from storage
     * @return a new {@link SimpleDataService}
     */
    @Bean
    DataService simpleDataService(final PersistenceLayer persistenceLayer,
                                  final DataReader dataReader) {
        return new SimpleDataService(persistenceLayer, dataReader);
    }

    @Bean
    AuditableDataService auditableDataService(final DataService dataService) {
        return new AuditableDataService(dataService);
    }

    @Bean
    AuditMessageService auditService(final Materializer materializer) {
        return new AuditMessageService(materializer);
    }

    /**
     * Bean implementation for {@link HadoopDataReader} which extends {@link SerialisedDataReader} and is used for setting hadoopConfigurations and reading raw data.
     *
     * @return a new instance of {@link HadoopDataReader}
     * @throws IOException ioException
     */
    @Bean
    DataReader hadoopDataReader() throws IOException {
        return new HadoopDataReader();
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

    @Bean("threadPoolTaskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setThreadNamePrefix("AppThreadPool-");
        ex.setCorePoolSize(CORE_POOL_SIZE);
        LOGGER.info("Starting ThreadPoolTaskExecutor with core = [{}] max = [{}]", ex.getCorePoolSize(), ex.getMaxPoolSize());
        return ex;
    }
}
