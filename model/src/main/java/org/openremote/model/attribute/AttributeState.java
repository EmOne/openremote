/*
 * Copyright 2016, OpenRemote Inc.
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
package org.openremote.model.attribute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * The desired or current or past state of an {@link AttributeRef}.
 * <p>
 * <code>null</code> is a valid {@link #value}.
 * </p>
 */
public class AttributeState implements Serializable {

    @JsonProperty("ref")
    protected AttributeRef attributeRef;
    protected Object value;
    protected boolean deleted;

    public AttributeState(String assetId, Attribute<?> attribute) {
        this(assetId, attribute.getName(), attribute.getValue().orElse(null));
    }

    public AttributeState(String assetId, String attributeName, Object value) {
        this(new AttributeRef(assetId, attributeName), value);
    }

    @JsonCreator
    public AttributeState(AttributeRef attributeRef, Object value) {
        this.attributeRef = Objects.requireNonNull(attributeRef);
        this.value = value;
    }

    /**
     * Sets the {@link #value} to <code>null</code>.
     */
    public AttributeState(AttributeRef attributeRef) {
        this(attributeRef, null);
    }

    public AttributeRef getAttributeRef() {
        return attributeRef;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getValue() {
        return Optional.ofNullable(value).map(v -> (T)v);
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "attributeRef=" + attributeRef +
            ", value=" + value +
            ", deleted=" + deleted +
            '}';
    }
}
