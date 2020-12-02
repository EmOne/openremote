/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.model.query.filter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.model.value.Values;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ObjectValueKeyPredicate implements ValuePredicate {

    public static final String name = "object-value-key";
    public boolean negated;
    public String key;
    public ValuePredicate value;

    public ObjectValueKeyPredicate(String key) {
        this.key = key;
    }

    @JsonCreator
    public ObjectValueKeyPredicate(@JsonProperty("key") String key, @JsonProperty("value") ValuePredicate value, @JsonProperty("negated") boolean negate) {
        this.key = key;
        this.value = value;
        negated = negate;
    }

    public ObjectValueKeyPredicate negate() {
        negated = !negated;
        return this;
    }

    public ObjectValueKeyPredicate value(ValuePredicate value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "key=" + key +
            ", negated=" + negated +
            '}';
    }

    @Override
    public Predicate<Object> asPredicate(Supplier<Long> currentMillisSupplier) {
        return obj ->
            Values.getValueCoerced(obj, ObjectNode.class).map(objectValue -> {

                boolean result = objectValue.has(key);
                return negated != result;
            }).orElse(false);
    }
}
