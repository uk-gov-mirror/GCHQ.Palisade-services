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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import uk.gov.gchq.palisade.service.audit.exception.DeserialisationException;

import java.util.function.UnaryOperator;

/**
 * Represents information for an error that has occurred during the processing of a request. This information is
 * received by the audit-service and processed.
 * Note each of the services can potentially send an error message.  This version is for recording the information in
 * the audit service.
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableAuditErrorMessage.class)
@JsonSerialize(as = ImmutableAuditErrorMessage.class)
public interface AuditErrorMessage extends AuditMessage {

    /**
     * Helper method to create a {@link AuditErrorMessage} using a builder function.
     * This method is an alias of {@code #create(UnaryOperator)} which is useful
     * when statically imported.
     *
     * @param func The builder function
     * @return a newly created {@code AuditErrorMessage}
     */
    static AuditErrorMessage createAuditErrorMessage(final UnaryOperator<AuditErrorMessage.Builder> func) {
        return create(func);
    }

    /**
     * Helper method to create a {@link AuditErrorMessage} using a builder function.
     * This method is an alias of {@code #createAuditErrorMessage(UnaryOperator)}
     * which is useful when statically imported.
     *
     * @param func The builder function
     * @return a newly created {@code AuditErrorMessage}
     */
    static AuditErrorMessage create(final UnaryOperator<AuditErrorMessage.Builder> func) {
        return func.apply(new AuditErrorMessage.Builder()).build();
    }

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableAuditErrorMessage.Builder { // empty
    }

    @JsonProperty("error")
    JsonNode getErrorNode();

    @JsonIgnore
    @Value.Lazy
    default Throwable getError() {
        try {
            return AuditMessage.MAPPER.treeToValue(getErrorNode(), Throwable.class);
        } catch (JsonProcessingException e) {
            throw new DeserialisationException(e);
        }
    }

}

