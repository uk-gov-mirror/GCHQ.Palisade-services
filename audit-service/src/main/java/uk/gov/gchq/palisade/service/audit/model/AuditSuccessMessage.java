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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.function.UnaryOperator;

/**
 * Represents information for a successful processing of a request which is forwarded to the audit-service.
 * Note there are three classes that effectively represent the same kind of data but represent a different
 * stage of the process:
 * uk.gov.gchq.palisade.service.audit.model.AuditSuccessMessage is the message received by the Audit Service.
 * uk.gov.gchq.palisade.service.filteredresource.model.AuditSuccessMessage is the message sent by the filtered-resource-service.
 * uk.gov.gchq.palisade.service.data.model.AuditSuccessMessage is the message sent by the data-service.
 */
@Value.Immutable
@JsonDeserialize(as = ImmutableAuditSuccessMessage.class)
@JsonSerialize(as = ImmutableAuditSuccessMessage.class)
public interface AuditSuccessMessage extends AuditMessage {

    /**
     * Helper method to create a {@link AuditSuccessMessage} using a builder
     * function. This method is an alias of {@code #create(UnaryOperator)} which is
     * useful when statically imported.
     *
     * @param func The builder function
     * @return a newly created {@code AuditSuccessMessage}
     */
    static AuditSuccessMessage createAuditSuccessMessage(final UnaryOperator<AuditSuccessMessage.Builder> func) {
        return create(func);
    }

    /**
     * Helper method to create a {@link AuditSuccessMessage} using a builder
     * function. This method is an alias of
     * {@code #createAuditErrorMessage(UnaryOperator)} which is useful when
     * statically imported.
     *
     * @param func The builder function
     * @return a newly created {@code AuditSuccessMessage}
     */
    static AuditSuccessMessage create(final UnaryOperator<AuditSuccessMessage.Builder> func) {
        return func.apply(new AuditSuccessMessage.Builder()).build();
    }

    /**
     * Exposes the generated builder outside this package
     * <p>
     * While the generated implementation (and consequently its builder) is not
     * visible outside of this package. This builder inherits and exposes all public
     * methods defined on the generated implementation's Builder class.
     */
    class Builder extends ImmutableAuditSuccessMessage.Builder { // empty
    }

    String getLeafResourceId();

}
