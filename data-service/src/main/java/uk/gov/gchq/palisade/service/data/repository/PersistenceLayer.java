/*
 * Copyright 2020 Crown Copyright
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
package uk.gov.gchq.palisade.service.data.repository;

import uk.gov.gchq.palisade.service.data.domain.AuthorisedRequestEntity;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Interface the the persistence store containing all requests for resources that have been authorised
 * by Palisade. This has been populated by some external service (attribute-masking-service) with tokens
 * and leafResources, along with the User, LeafResource, Rules and Context that must be applied before
 * returning data to the client.
 */
public interface PersistenceLayer {

    /**
     * Asynchronously retrieve the details for the given leaf resource and client token.
     *
     * @param token the client's request token
     * @param leafResourceId the leaf resource requested by the client
     * @return a {@link CompletableFuture} of the persistence access, itself returning an {@link Optional}
     * of whether such an authorisation was found and the rules to apply.
     */
    CompletableFuture<Optional<AuthorisedRequestEntity>> getAsync(final String token, final String leafResourceId);

}
