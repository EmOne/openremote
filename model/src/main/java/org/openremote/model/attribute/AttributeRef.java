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
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

import static org.openremote.model.util.TextUtil.requireNonNullAndNonEmpty;

/**
 * A reference to an entity and an {@link Attribute}.
 * <p>
 * The {@link #assetId} and {@link #attributeName} are required to identify
 * an entity's attribute.
 * <p>
 * Two attribute references are {@link #equals} if they reference the same entity
 * and attribute.
 */
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"assetId", "attributeName"})
public class AttributeRef implements Serializable {

    protected String assetId;
    protected String attributeName;

    @JsonCreator
    public AttributeRef(@JsonProperty("assetId") String assetId, @JsonProperty("attributeName") String attributeName) {
        requireNonNullAndNonEmpty(assetId);
        requireNonNullAndNonEmpty(attributeName);
        this.assetId = assetId;
        this.attributeName = attributeName;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAttributeName() {
        return attributeName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeRef that = (AttributeRef) o;
        return assetId.equals(that.assetId) && attributeName.equals(that.attributeName);
    }

    @Override
    public int hashCode() {
        int result = assetId.hashCode();
        result = 31 * result + attributeName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "assetId='" + assetId + '\'' +
            ", attributeName='" + attributeName + '\'' +
            '}';
    }
}
