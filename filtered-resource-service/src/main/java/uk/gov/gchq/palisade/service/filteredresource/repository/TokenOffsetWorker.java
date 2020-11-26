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

package uk.gov.gchq.palisade.service.filteredresource.repository;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import uk.gov.gchq.palisade.service.filteredresource.repository.TokenOffsetWorker.WorkerCommand;

final class TokenOffsetWorker extends AbstractBehavior<WorkerCommand> {
    protected interface WorkerCommand {
        // Marker interface for inputs of the worker
    }

    protected interface WorkerResponse {
        // Marker interface for outputs of the worker
    }

    /**
     * A request to get the offset for a token.
     * The worker will {@link ActorRef#tell} the replyTo actor the offset once found.
     */
    protected static class GetOffset implements WorkerCommand {
        protected final String token;
        protected final ActorRef<WorkerResponse> replyTo;

        protected GetOffset(final String token, final ActorRef<WorkerResponse> replyTo) {
            this.token = token;
            this.replyTo = replyTo;
        }
    }

    /**
     * A response for this actor to send to its {@code replyTo} actor.
     * This is received by the worker when an appropriate offset if found.
     * This is both a possible input to the system {@link WorkerCommand} as well as an output {@link WorkerResponse}
     */
    protected static class SetOffset implements WorkerCommand, WorkerResponse {
        protected final String token;
        protected final Long offset;

        protected SetOffset(final String token, final Long offset) {
            this.token = token;
            this.offset = offset;
        }
    }

    /**
     * A response for this actor to send to its {@code replyTo} actor.
     * This indicates an exception was thrown by the worker while processing the request.
     *
     * @implNote This is currently only caused by the persistence store throwing an exception.
     */
    class ReportError implements WorkerResponse {
        protected final String token;
        protected final Throwable exception;

        protected ReportError(final String token, final Throwable exception) {
            this.token = token;
            this.exception = exception;
        }
    }

    private final TokenOffsetPersistenceLayer persistenceLayer;

    private TokenOffsetWorker(final ActorContext<WorkerCommand> context, final TokenOffsetPersistenceLayer persistenceLayer) {
        super(context);
        this.persistenceLayer = persistenceLayer;
    }

    static Behavior<WorkerCommand> create(final TokenOffsetPersistenceLayer persistenceLayer) {
        return Behaviors.setup(ctx -> new TokenOffsetWorker(ctx, persistenceLayer));
    }

    @Override
    public Receive<WorkerCommand> createReceive() {
        return this.onGetOffset();
    }

    private Receive<WorkerCommand> onGetOffset() {
        return newReceiveBuilder()
                // Start off in Getting mode
                .onMessage(GetOffset.class, (GetOffset getCmd) -> {
                    // Get from persistence
                    return this.persistenceLayer.findOffset(getCmd.token)
                            // If present tell self (if not, will be told in the future)
                            .<Behavior<WorkerCommand>>thenApply(optional -> {
                                optional.ifPresent(offset -> this.getContext().getSelf()
                                        .tell(new SetOffset(getCmd.token, offset)));
                                return this.onSetOffset(getCmd);
                            })
                            // If an exception is thrown reading from persistence, report the exception
                            .exceptionally(ex -> {
                                getCmd.replyTo.tell(new ReportError(getCmd.token, ex));
                                return Behaviors.stopped();
                            })
                            .join();
                })
                .build();
    }

    private Receive<WorkerCommand> onSetOffset(final GetOffset getCmd) {
        return newReceiveBuilder()
                // Switch state to Setting mode
                .onMessage(SetOffset.class, (SetOffset setOffset) -> {
                    // Tell the replyTo actor the offset that has been received
                    getCmd.replyTo.tell(setOffset);
                    // Stop this actor
                    return Behaviors.stopped();
                })
                .build();
    }

}
