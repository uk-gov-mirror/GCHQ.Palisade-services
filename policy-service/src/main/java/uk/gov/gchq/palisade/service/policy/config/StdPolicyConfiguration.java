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

package uk.gov.gchq.palisade.service.policy.config;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.service.PolicyConfiguration;
import uk.gov.gchq.palisade.service.PolicyPrepopulationFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Implementation of a {@link PolicyConfiguration} that uses Spring to configure a list of policies from a yaml file
 * A container for a number of {@link StdPolicyPrepopulationFactory} builders used for creating policies by mapping {@link uk.gov.gchq.palisade.resource.Resource} to {@link uk.gov.gchq.palisade.rule.Rules}
 * These will be populated further using a {@link uk.gov.gchq.palisade.service.UserConfiguration} and {@link uk.gov.gchq.palisade.service.ResourceConfiguration}
 * These policies will be used for prepopulating the {@link uk.gov.gchq.palisade.service.policy.service.PolicyService}
 */
public class StdPolicyConfiguration implements PolicyConfiguration {

    private List<StdPolicyPrepopulationFactory> policies;

    /**
     * Constructor with 0 arguments for a standard implementation
     * of the {@link PolicyConfiguration} interface
     */
    public StdPolicyConfiguration() {
        this.policies = Collections.emptyList();
    }

    /**
     * Constructor with one argument for a standard implementation
     * of the {@link PolicyConfiguration} interface
     *
     * @param policies a {@link List} of objects implementing the {@link PolicyPrepopulationFactory} interface
     */
    public StdPolicyConfiguration(final List<StdPolicyPrepopulationFactory> policies) {
        this.policies = policies;
    }

    @Override
    @Generated
    public List<StdPolicyPrepopulationFactory> getPolicies() {
        return policies;
    }

    @Generated
    public void setPolicies(final List<StdPolicyPrepopulationFactory> policies) {
        requireNonNull(policies);
        this.policies = policies;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StdPolicyConfiguration)) {
            return false;
        }
        final StdPolicyConfiguration that = (StdPolicyConfiguration) o;
        return Objects.equals(policies, that.policies);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(policies);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", StdPolicyConfiguration.class.getSimpleName() + "[", "]")
                .add("policies=" + policies)
                .add(super.toString())
                .toString();
    }
}
