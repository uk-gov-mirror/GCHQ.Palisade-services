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
package uk.gov.gchq.palisade.component.policy.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.palisade.component.policy.CommonTestData;
import uk.gov.gchq.palisade.service.policy.exception.NoSuchPolicyException;
import uk.gov.gchq.palisade.service.policy.model.AuditErrorMessage;
import uk.gov.gchq.palisade.service.policy.model.AuditablePolicyRecordResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.gchq.palisade.service.policy.ApplicationTestData.RESPONSE;

class AuditablePolicyRecordResponseTest extends CommonTestData {

    /**
     * Test for a {@link AuditablePolicyRecordResponse} which holds a PolicyResponse and no exception.
     * This is the scenario where the object represents a standard message that is to be passed onto the next service.
     * Create the object with the builder and then convert to the Json equivalent.
     * Takes the JSON Object, deserialises and tests against the original Object
     *
     * @throws JsonProcessingException throws if the {@link AuditablePolicyRecordResponse} object cannot be converted to a JsonContent.
     *                                 This equates to a failure to serialise or deserialize the string.
     */
    @Test
    void testAuditablePolicyRecordResponseSerializingAndDeserializing() throws JsonProcessingException {
        var mapper = new ObjectMapper();

        var actualJson = mapper.writeValueAsString(POLICY_RECORD_RESPONSE);
        var actualInstance = mapper.readValue(actualJson, POLICY_RECORD_RESPONSE.getClass());

        assertThat(actualInstance)
                .as("Check that whilst using the objects toString method, the objects are the same")
                .isEqualTo(POLICY_RECORD_RESPONSE);

        assertThat(actualInstance)
                .as("Check %s using recursion", POLICY_RECORD_RESPONSE.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(POLICY_RECORD_RESPONSE);
    }

    /**
     * Test for a {@link AuditablePolicyRecordResponse} which holds an exception and no policy response
     * This is the scenario where the object represents an error that is to be passed onto the Audit Service.
     * Create the object with the builder and then convert to the Json equivalent.
     * Takes the JSON Object, deserialises and tests against the original Object
     *
     * @throws JsonProcessingException throws if the {@link AuditablePolicyRecordResponse} object cannot be converted to a JsonContent.
     *                                 This equates to a failure to serialise or deserialize the string.
     */
    @Test
    void testAuditablePolicyRecordResponseExceptionSerialisingAndDeserialising() throws JsonProcessingException {
        var mapper = new ObjectMapper();

        var actualJson = mapper.writeValueAsString(POLICY_RECORD_RESPONSE_ERROR);
        var actualInstance = mapper.readValue(actualJson, POLICY_RECORD_RESPONSE_ERROR.getClass());

        assertThat(actualInstance)
                .as("Check that whilst using the objects toString method, the objects are the same")
                .isEqualTo(POLICY_RECORD_RESPONSE_ERROR);

        assertThat(actualInstance)
                .as("Check %s using recursion", POLICY_RECORD_RESPONSE_ERROR.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(POLICY_RECORD_RESPONSE_ERROR);
    }

    /**
     * When there is no {@link AuditErrorMessage} the {@link AuditablePolicyRecordResponse#chain(AuditErrorMessage)}
     * is expected to return the same object.
     */
    @Test
    void testChainWithoutAnException() {
        // When
        var auditablePolicyRecordResponse = AuditablePolicyRecordResponse.Builder.create()
                .withPolicyResponse(RESPONSE)
                .withNoErrors();
        var chainedResponse = auditablePolicyRecordResponse.chain(null);
        // Then
        assertAll("AuditablePolicyRecordResponse comparison after chaining with 'null' error",
                () -> assertThat(chainedResponse)
                        .as("The updated object should match the original")
                        .isEqualTo(auditablePolicyRecordResponse),

                () -> assertThat(chainedResponse)
                        .usingRecursiveComparison()
                        .as("The updated object should have the same values as the original")
                        .isEqualTo(auditablePolicyRecordResponse)
        );

    }

    /**
     * When there is no {@link AuditErrorMessage} the {@link AuditablePolicyRecordResponse#chain(AuditErrorMessage)}
     * is expected to return a new and different object with the error message {@link AuditErrorMessage} added.
     */
    @Test
    void testChainWithAnException() {
        // When
        var auditablePolicyRecordResponse = AuditablePolicyRecordResponse.Builder.create()
                .withPolicyResponse(RESPONSE)
                .withNoErrors();
        var chainedResponse = auditablePolicyRecordResponse.chain(AUDIT_ERROR_MESSAGE);
        // Then
        assertAll("AuditablePolicyRecordResponse comparison after chaining with an AuditErrorMessage",
                () -> assertThat(chainedResponse)
                        .usingRecursiveComparison()
                        .as("The updated object should not match the original")
                        .isNotEqualTo(auditablePolicyRecordResponse),

                () -> assertThat(chainedResponse.getAuditErrorMessage())
                        .usingRecursiveComparison()
                        .as("The AuditErrorMessage should have the expected values")
                        .isEqualTo(AUDIT_ERROR_MESSAGE),

                () -> assertThat(chainedResponse.getAuditErrorMessage())
                        .extracting(AuditErrorMessage::getError)
                        .as("The exception cause should be 'NoSuchPolicyException'")
                        .isInstanceOf(NoSuchPolicyException.class)
                        .as("The exception should contain the message 'No rules found for the resource'")
                        .extracting(Throwable::getMessage)
                        .isEqualTo("No rules found for the resource")
        );
    }
}
