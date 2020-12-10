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

import org.openremote.model.attribute.Attribute;

import java.util.Arrays;

/**
 * Adds additional predicate logic to {@link NameValuePredicate}, allowing predicating on
 * {@link Attribute#getMeta} presence/absence and/or values. Can also predicate on the previous value of the
 * {@link Attribute} which is only relevant when applied to {@link org.openremote.model.rules.AssetState}.
 */
public class AttributePredicate extends NameValuePredicate {

    public NameValuePredicate[] meta;
    public ValuePredicate previousValue;

    public AttributePredicate() {
    }

    public AttributePredicate(String name) {
        this(new StringPredicate(name));
    }

    public AttributePredicate(StringPredicate name) {
        this.name = name;
    }

    public AttributePredicate(ValuePredicate value) {
        this.value = value;
    }

    public AttributePredicate(StringPredicate name, ValuePredicate value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public AttributePredicate name(StringPredicate name) {
        this.name = name;
        return this;
    }

    @Override
    public AttributePredicate value(ValuePredicate value) {
        this.value = value;
        return this;
    }

    @Override
    public AttributePredicate mustNotExist() {
        this.mustNotExist = true;
        return this;
    }

    public AttributePredicate previousValue(ValuePredicate previousValue) {
        this.previousValue = previousValue;
        return this;
    }

    public AttributePredicate meta(NameValuePredicate...meta) {
        this.meta = meta;
        return this;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name=" + name +
            ", mustNotExist=" + mustNotExist +
            ", value=" + value +
            ", meta=" + Arrays.toString(meta) +
            ", previousValue=" + previousValue +
            '}';
    }
}
