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

package uk.gov.gchq.palisade.service.policy.common.rule;

import uk.gov.gchq.palisade.service.policy.common.Context;
import uk.gov.gchq.palisade.service.policy.common.user.User;

import java.io.Serializable;

/**
 * A Pass through rule that returns all records without filtering
 *
 * @param <T> The record to be returned
 */
public class PassThroughRule<T extends Serializable> implements Rule<T> {
    private static final long serialVersionUID = 1L;

    @Override
    public T apply(final T record, final User user, final Context context) {
        return record;
    }
}
