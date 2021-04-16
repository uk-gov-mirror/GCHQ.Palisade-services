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

package uk.gov.gchq.palisade.service.policy.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.service.policy.common.Context;
import uk.gov.gchq.palisade.service.policy.common.resource.Resource;
import uk.gov.gchq.palisade.service.policy.common.rule.RecordRules;
import uk.gov.gchq.palisade.service.policy.common.rule.ResourceRules;
import uk.gov.gchq.palisade.service.policy.common.rule.Rule;
import uk.gov.gchq.palisade.service.policy.common.user.User;

import java.util.ArrayList;

import static java.util.Objects.isNull;

/**
 * Common utility methods.
 */
public final class RulesUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(RulesUtil.class);

    /**
     * Empty constructor
     */
    private RulesUtil() {
    }

    /**
     * Applies a collection of rules to an item (record/resource).
     *
     * @param item    resource or record to filter
     * @param user    user the record is being processed for
     * @param context the additional context
     * @param rules   rules collection
     * @param <T>     record type
     * @return item with rules applied
     */
    @SuppressWarnings("unchecked") // Cast between Resource and T
    public static <T extends Resource> T applyRulesToResource(final T item, final User user, final Context context, final ResourceRules rules) {
        if (isNull(rules) || isNull(rules.getRules()) || rules.getRules().isEmpty()) {
            return item;
        }
        Resource updateItem = item;
        for (final Rule<Resource> rule : rules.getRules().values()) {
            updateItem = rule.apply(updateItem, user, context);
            if (null == updateItem) {
                break;
            }
        }
        return (T) updateItem;
    }


    public static ResourceRules mergeRules(final ResourceRules inheritedRules, final ResourceRules newRules) {
        LOGGER.debug("inheritedRules and newRules both present\n MessageInherited: {}\n MessageNew: {}\n RulesInherited: {}\n RulesNew: {}",
                inheritedRules.getMessage(), newRules.getMessage(), inheritedRules.getRules(), newRules.getRules());
        ResourceRules mergedRules = new ResourceRules();

        // Merge messages
        ArrayList<String> messages = new ArrayList<>();
        String inheritedMessage = inheritedRules.getMessage();
        String newMessage = newRules.getMessage();
        if (!inheritedMessage.equals(ResourceRules.NO_RULES_SET)) {
            messages.add(inheritedMessage);
        }
        if (!newMessage.equals(ResourceRules.NO_RULES_SET)) {
            messages.add(newMessage);
        }
        mergedRules.message(String.join(",", messages));
        LOGGER.debug("Merged messages: {} + {} -> {}", inheritedRules.getMessage(), newRules.getMessage(), mergedRules.getMessage());

        // Merge rules
        mergedRules.addRules(inheritedRules.getRules());
        mergedRules.addRules(newRules.getRules());
        LOGGER.debug("Merged rules: {} + {} -> {}", inheritedRules.getRules(), newRules.getRules(), mergedRules.getRules());

        return mergedRules;
    }

    public static RecordRules mergeRules(final RecordRules inheritedRules, final RecordRules newRules) {
        LOGGER.debug("inheritedRules and newRules both present\n MessageInherited: {}\n MessageNew: {}\n RulesInherited: {}\n RulesNew: {}",
                inheritedRules.getMessage(), newRules.getMessage(), inheritedRules.getRules(), newRules.getRules());
        RecordRules mergedRules = new RecordRules();

        // Merge messages
        ArrayList<String> messages = new ArrayList<>();
        String inheritedMessage = inheritedRules.getMessage();
        String newMessage = newRules.getMessage();
        if (!inheritedMessage.equals(RecordRules.NO_RULES_SET)) {
            messages.add(inheritedMessage);
        }
        if (!newMessage.equals(RecordRules.NO_RULES_SET)) {
            messages.add(newMessage);
        }
        mergedRules.message(String.join(",", messages));
        LOGGER.debug("Merged messages: {} + {} -> {}", inheritedRules.getMessage(), newRules.getMessage(), mergedRules.getMessage());

        // Merge rules
        mergedRules.addRules(inheritedRules.getRules());
        mergedRules.addRules(newRules.getRules());
        LOGGER.debug("Merged rules: {} + {} -> {}", inheritedRules.getRules(), newRules.getRules(), mergedRules.getRules());

        return mergedRules;
    }
}
