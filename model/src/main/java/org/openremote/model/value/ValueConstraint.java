/*
 * Copyright 2021, OpenRemote Inc.
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
package org.openremote.model.value;

import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents a constraint to apply to a value; these are based on JSR-380 validation.
 */
@JsonTypeInfo(property = "type", use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
    @JsonSubTypes.Type(ValueConstraint.Size.class),
    @JsonSubTypes.Type(ValueConstraint.Pattern.class),
    @JsonSubTypes.Type(ValueConstraint.Min.class),
    @JsonSubTypes.Type(ValueConstraint.Max.class)
})
public abstract class ValueConstraint implements Serializable {

    public static ValueConstraint[] constraints(ValueConstraint...constraints) {
        return constraints;
    }

    @JsonTypeName("size")
    public static class Size extends ValueConstraint {

        protected Integer min;
        protected Integer max;

        @JsonCreator
        public Size(@JsonProperty("min") Integer min, @JsonProperty("max") Integer max) {
            this.min = min;
            this.max = max;
        }

        public Optional<Integer> getMin() {
            return Optional.ofNullable(min);
        }

        public Optional<Integer> getMax() {
            return Optional.ofNullable(max);
        }

        public Size setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    @JsonTypeName("min")
    public static class Min extends ValueConstraint {
        protected Number min;

        @JsonCreator
        public Min(@JsonProperty("min") Number min) {
            this.min = min;
        }

        public Number getMin() {
            return min;
        }

        public Min setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    @JsonTypeName("max")
    public static class Max extends ValueConstraint {
        protected Number max;

        @JsonCreator
        public Max(@JsonProperty("max") Number max) {
            this.max = max;
        }

        public Number getMax() {
            return max;
        }

        public Max setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    @JsonTypeName("pattern")
    public static class Pattern extends ValueConstraint {
        protected String regexp;

        @JsonCreator
        public Pattern(@JsonProperty("regexp") String regexp) {
            this.regexp = regexp;
        }

        public String getRegexp() {
            return regexp;
        }

        public Pattern setMessage(String message) {
            this.message = message;
            return this;
        }
    }

    @JsonTypeName("allowedValues")
    public static class AllowedValues extends ValueConstraint {
        String[] allowedValues;

        @JsonCreator
        public AllowedValues(@JsonProperty("allowedValues") String...allowedValues) {
            this.allowedValues = allowedValues;
        }

        public AllowedValues(Class<? extends Enum<?>> enumClass) {
            this.allowedValues = Arrays.stream(enumClass.getEnumConstants()).map(Enum::toString).toArray(String[]::new);
        }

        public String[] getAllowedValues() {
            return allowedValues;
        }
    }

    protected String message;

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }
}
