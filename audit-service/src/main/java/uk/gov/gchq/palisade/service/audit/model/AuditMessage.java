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
package uk.gov.gchq.palisade.service.audit.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.service.audit.exception.DeserialisationException;

import java.util.Map;


/**
 * This is the parent class for Audit information.  It represents the common component of the data that has been
 * sent from each of the different services.
 */
@ImmutableStyle
public interface AuditMessage {

    ObjectMapper MAPPER = new ObjectMapper();

    String getUserId(); // Unique identifier for the user.

    String getResourceId(); // Resource that that is being asked to access.

    String getServiceName(); // service that sent the message

    String getTimestamp(); // when the message was created

    String getServerIP(); // the server IP address for the service

    String getServerHostname(); // the hostname of the server hosting the service

    @JsonProperty("context")
    JsonNode getContextNode();

    @JsonIgnore
    @Value.Lazy
    default Context getContext() {
        try {
            return AuditMessage.MAPPER.treeToValue(getContextNode(), Context.class);
        } catch (JsonProcessingException e) {
            throw new DeserialisationException(e);
        }
    }

    @JsonProperty("attributes")
    JsonNode getAttributesNode();

    @JsonIgnore
    @Value.Lazy
    default Map<String, String> getAttributes() {
        try {
            return AuditMessage.MAPPER.treeToValue(getAttributesNode(), Map.class);
        } catch (JsonProcessingException e) {
            throw new DeserialisationException(e);
        }
    }

}

