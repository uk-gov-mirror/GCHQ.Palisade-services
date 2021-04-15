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
import uk.gov.gchq.palisade.service.policy.model.AuditablePolicyResourceResponse;

import static org.assertj.core.api.Assertions.assertThat;

class AuditablePolicyResourceResponseTest extends CommonTestData {

    /**
     * Test for a {@link AuditablePolicyResourceResponse} which holds a {@code PolicyRequest},
     * no exception and after the {@code Resource} has been modified. This is the expected state of the object
     * after a query for the rules applicable to the resource then these rules have been applied to the resource.
     * Test involve creating the object with the builder and then convert to the Json equivalent.
     * Takes the JSON Object, deserializes and tests against the original Object
     *
     * @throws JsonProcessingException throws if the {@link AuditablePolicyResourceResponse} object cannot be converted to a JsonContent.
     *                                 This equates to a failure to serialise or deserialise the string.
     */
    @Test
    void testAuditablePolicyResourceResponseSerialisingAndDeserialising() throws JsonProcessingException {
        var mapper = new ObjectMapper();

        var actualJson = mapper.writeValueAsString(POLICY_RESOURCE_RESPONSE);
        var actualInstance = mapper.readValue(actualJson, POLICY_RESOURCE_RESPONSE.getClass());

        assertThat(actualInstance)
                .as("Check that whilst using the objects toString method, the objects are the same")
                .isEqualTo(POLICY_RESOURCE_RESPONSE);

        assertThat(actualInstance)
                .as("Check %s using recursion", POLICY_RESOURCE_RESPONSE.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(POLICY_RESOURCE_RESPONSE);
    }

    /**
     * Test for a {@link AuditablePolicyResourceResponse} which holds a {@code PolicyRequest},
     * has and exception and after the {@code Resource} has been modified. This is the expected state of the object
     * after a query for the rules applicable to the resource then an error occurs when rules have been applied to the resource.
     * Test involve creating the object with the builder and then convert to the Json equivalent.
     * Takes the JSON Object, deserializes and tests against the original Object
     *
     * @throws JsonProcessingException throws if the {@link AuditablePolicyResourceResponse} object cannot be converted to a JsonContent.
     *                                 This equates to a failure to serialise or deserialise the string.
     */
    @Test
    void testErrorAuditablePolicyResourceResponseSerialisingAndDeserialising() throws JsonProcessingException {
        var mapper = new ObjectMapper();

        var actualJson = mapper.writeValueAsString(POLICY_RESOURCE_RESPONSE_ERROR);
        var actualInstance = mapper.readValue(actualJson, POLICY_RESOURCE_RESPONSE_ERROR.getClass());

        assertThat(actualInstance)
                .as("Check that whilst using the objects toString method, the objects are the same")
                .isEqualTo(POLICY_RESOURCE_RESPONSE_ERROR);

        assertThat(actualInstance)
                .as("Check %s using recursion", POLICY_RESOURCE_RESPONSE_ERROR.getClass().getSimpleName())
                .usingRecursiveComparison()
                .isEqualTo(POLICY_RESOURCE_RESPONSE_ERROR);
    }
}
