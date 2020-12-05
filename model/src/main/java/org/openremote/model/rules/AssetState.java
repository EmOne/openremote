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
package org.openremote.model.rules;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.MetaList;
import org.openremote.model.value.ValueDescriptor;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * An asset attribute value update, capturing asset state at a point in time.
 * <p>
 * This class layout is convenient for writing rules. Two asset states
 * are equal if they have the same asset ID and attribute name (the same attribute
 * reference).
 */
public class AssetState implements Comparable<AssetState> {

    final protected String attributeName;

    @JsonSerialize(converter = ValueDescriptor.ValueDescriptorStringConverter.class)
    @JsonDeserialize(converter = ValueDescriptor.StringValueDescriptorConverter.class)
    final protected ValueDescriptor<?> attributeValueType;

    final protected Object value;

    final protected long timestamp;

    final protected AttributeEvent.Source source;

    final protected Object oldValue;

    final protected long oldValueTimestamp;

    final protected String id;

    final protected String name;

    final protected String type;

    final protected Date createdOn;

    final protected String[] path;

    final protected String parentId;

    final protected String parentName;

    final protected String parentType;

    final protected String realm;

    final protected MetaList meta;

    public AssetState(Asset<?> asset, Attribute<?> attribute, AttributeEvent.Source source) {
        this.attributeName = attribute.getName();
        this.attributeValueType = attribute.getValueType();
        this.value = attribute.getValue().orElse(null);
        this.timestamp = attribute.getTimestamp().orElse(-1L);
        this.source = source;
        this.oldValue = asset.getAttributes().get(attributeName).flatMap(Attribute::getValue).orElse(null);
        this.oldValueTimestamp = asset.getAttributes().get(attributeName).flatMap(Attribute::getTimestamp).orElse(-1L);
        this.id = asset.getId();
        this.name = asset.getName();
        this.type = asset.getType();
        this.createdOn = asset.getCreatedOn();
        this.path = asset.getPath();
        this.parentId = asset.getParentId();
        this.parentName = asset.getParentName();
        this.parentType = asset.getParentType();
        this.realm = asset.getRealm();
        this.meta = attribute.getMeta();
    }

    public String getAttributeName() {
        return attributeName;
    }

    public ValueDescriptor<?> getAttributeValueType() {
        return attributeValueType;
    }

    public Optional<Object> getValue() {
        return Optional.ofNullable(value);
    }

    public long getTimestamp() {
        return timestamp;
    }

    public AttributeEvent.Source getSource() {
        return source;
    }

    public Optional<Object> getOldValue() {
        return Optional.ofNullable(oldValue);
    }

    public long getOldValueTimestamp() {
        return oldValueTimestamp;
    }

    public String getId() {
        return id;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String[] getPath() {
        return path;
    }

    public String getParentId() {
        return parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public String getParentType() {
        return parentType;
    }

    public String getRealm() {
        return realm;
    }

    public MetaList getMeta() {
        return meta;
    }

    /**
     * Compares entity identifier, attribute name, value, and timestamp.
     */
    public boolean matches(AttributeEvent event) {
        return matches(event, null, false);
    }

    /**
     * Compares entity identifier, attribute name, value, source, and optional timestamp.
     */
    public boolean matches(AttributeEvent event, AttributeEvent.Source source, boolean ignoreTimestamp) {
        return getId().equals(event.getAssetId())
            && getAttributeName().equals(event.getAttributeName())
            && getValue().equals(event.getValue())
            && (ignoreTimestamp || getTimestamp() == event.getTimestamp())
            && (source == null || getSource() == source);
    }

    @Override
    public int compareTo(AssetState that) {
        int result = getId().compareTo(that.getId());
        if (result == 0)
            result = getAttributeName().compareTo(that.getAttributeName());
        if (result == 0)
            result = Long.compare(getTimestamp(), that.getTimestamp());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetState that = (AssetState) o;
        return Objects.equals(attributeName, that.attributeName) &&
            Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributeName, id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "id='" + getId() + '\'' +
            ", name='" + getName() + '\'' +
            ", parentName='" + getParentName() + '\'' +
            ", type='" + getType() + '\'' +
            ", attributeName='" + getAttributeName() + '\'' +
            ", attributeValueDescriptor=" + getAttributeValueType() +
            ", value=" + getValue() +
            ", timestamp=" + getTimestamp() +
            ", oldValue=" + getOldValue() +
            ", oldValueTimestamp=" + getOldValueTimestamp() +
            ", source=" + getSource() +
            '}';
    }
}
