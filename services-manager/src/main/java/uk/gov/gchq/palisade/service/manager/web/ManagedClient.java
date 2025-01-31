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

package uk.gov.gchq.palisade.service.manager.web;

import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

/**
 * A Feign REST client for a single instance of a service
 */
@FeignClient(name = "managed-client", url = "undefined")
public interface ManagedClient {
    @GetMapping(path = "/actuator/health", produces = "application/json")
    Response getHealth(final URI url);

    @PostMapping(path = "/actuator/loggers/{module}", produces = "application/json", consumes = "application/json")
    Response setLoggers(final URI url, final @PathVariable("module") String module, final @RequestBody String configuredLevel);

    @PostMapping(path = "/actuator/shutdown")
    void shutdown(final URI url);
}
