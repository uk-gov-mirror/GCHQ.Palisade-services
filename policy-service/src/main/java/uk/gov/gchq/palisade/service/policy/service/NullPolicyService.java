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

package uk.gov.gchq.palisade.service.policy.service;

import uk.gov.gchq.palisade.service.policy.common.Generated;
import uk.gov.gchq.palisade.service.policy.common.policy.PolicyService;
import uk.gov.gchq.palisade.service.policy.common.rule.RecordRules;
import uk.gov.gchq.palisade.service.policy.common.rule.ResourceRules;

import java.util.Optional;

/**
 * A default do-nothing Policy Service designed to work with the Caching and Hierarchy layers.
 * Within cache TTL and cache size, the set...Rules methods will add to the cache, get...Rules will get from the cache
 * and the service will declare all Resources available unless Policy dictates otherwise.
 * After cache TTL timeout, the service will effectively be reset and empty
 */
public class NullPolicyService implements PolicyService {

    @Override
    @Generated
    public Optional<ResourceRules> getResourceRules(final String resourceId) {
        return Optional.empty();
    }

    @Override
    @Generated
    public Optional<RecordRules> getRecordRules(final String resourceId) {
        return Optional.empty();
    }

    @Override
    @Generated
    public Optional<ResourceRules> setResourceRules(final String resourceId, final ResourceRules rules) {
        return Optional.of(rules);
    }

    @Override
    @Generated
    public Optional<RecordRules> setRecordRules(final String resourceId, final RecordRules rules) {
        return Optional.of(rules);
    }
}
