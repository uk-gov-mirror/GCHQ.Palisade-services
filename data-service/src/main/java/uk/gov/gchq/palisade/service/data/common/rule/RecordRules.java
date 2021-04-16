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

package uk.gov.gchq.palisade.service.data.common.rule;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.core.serializer.support.SerializationFailedException;

import uk.gov.gchq.palisade.service.data.common.Generated;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This class is used to encapsulate the list of references to rules that apply to a resource and is provided with a user
 * friendly message to explain what the set of rules are.
 * The rules do not necessarily exist on the classpath, so are described by a String.
 */
@JsonPropertyOrder(value = {"message", "rules"}, alphabetic = true)
public class RecordRules implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String ID_CANNOT_BE_NULL = "The id field can not be null.";
    private static final String RULE_CANNOT_BE_NULL = "The rule can not be null.";
    public static final String NO_RULES_SET = "no rules set";

    private String message;
    private LinkedHashMap<String, String> rulesMap;

    /**
     * Constructs an empty instance of Rules.
     */
    public RecordRules() {
        rulesMap = new LinkedHashMap<>();
        message = NO_RULES_SET;
    }

    /**
     * Overrides the rules with these new rules
     *
     * @param rules the rules to set
     * @return this Rules instance
     */
    @Generated
    public RecordRules rules(final Map<String, String> rules) {
        this.setRules(rules);
        return this;
    }

    @Generated
    public RecordRules addRules(final Map<String, String> rules) {
        requireNonNull(rules, "Cannot add null to the existing rules.");
        this.rulesMap.putAll(rules);
        return this;
    }

    /**
     * Sets a message.
     *
     * @param message user friendly message to explain what the set of rules are.
     * @return this Rules instance
     */
    @Generated
    public RecordRules message(final String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * Adds a rule.
     *
     * @param id   the unique rule id
     * @param rule the rule
     * @return this Rules instance
     */
    @Generated
    public RecordRules addRule(final String id, final String rule) {
        requireNonNull(id, ID_CANNOT_BE_NULL);
        requireNonNull(rule, RULE_CANNOT_BE_NULL);
        rulesMap.put(id, rule);
        return this;
    }

    @Generated
    public String getMessage() {
        return message;
    }

    @Generated
    public void setMessage(final String message) {
        requireNonNull(message);
        this.message = message;
    }

    @Generated
    public Map<String, String> getRules() {
        return rulesMap;
    }

    @SuppressWarnings({"unchecked", "java:S1452"})
    public Map<String, Rule<?>> getRuleObjects() {
        return rulesMap.entrySet().stream()
                .map(entry -> {
                    try {
                        Class<Rule<?>> clazz = (Class<Rule<?>>) Class.forName(entry.getValue());
                        Constructor<Rule<?>> constructor = clazz.getConstructor();
                        Rule<?> rule = constructor.newInstance();
                        return new SimpleImmutableEntry<>(entry.getKey(), rule);
                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new SerializationFailedException("Failed to reflect and instantiate object for rule class", e);
                    }
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Generated
    public void setRules(final Map<String, String> rulesMap) {
        requireNonNull(rulesMap);
        this.rulesMap = new LinkedHashMap<>(rulesMap);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecordRules)) {
            return false;
        }
        RecordRules other = (RecordRules) o;
        return Objects.equals(this.rulesMap, other.rulesMap);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(message, rulesMap);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", RecordRules.class.getSimpleName() + "[", "]")
                .add("message='" + message + "'")
                .add("rulesMap=" + rulesMap)
                .add(super.toString())
                .toString();
    }
}
