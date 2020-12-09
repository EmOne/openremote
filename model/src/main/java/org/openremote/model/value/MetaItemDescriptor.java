/*
 * Copyright 2020, OpenRemote Inc.
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

import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;

import java.util.Objects;

/**
 * Describes a {@link MetaItem} that can be added to an {@link Attribute}; the {@link #getName()} must match the {@link
 * MetaItem#getName()}, it also indicates what the {@link ValueDescriptor} is for the {@link MetaItem}. The {@link
 * MetaItemDescriptor} applies to the {@link Asset} type it is associated with and all subtypes of this type (i.e. a
 * {@link MetaItemDescriptor} associated with the base {@link Asset} type will be available to all {@link Asset} types
 * (e.g. {@link MetaItemType#READ_ONLY} can be applied to any {@link Asset}'s {@link Attribute}).
 * <p>
 * {@link MetaItemDescriptor#getName} must be globally unique within the context of the manager it is registered with.
 */
public class MetaItemDescriptor<T> extends AbstractNameValueDescriptorHolder<T> {

    public MetaItemDescriptor(String name, ValueDescriptor<T> valueDescriptor) {
        super(name, valueDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Meta item descriptor names are unique identifiers so can use this for equality purposes
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MetaItemDescriptor<?> that = (MetaItemDescriptor<?>)obj;
        return Objects.equals(name, that.name);
    }

    @Override
    public String toString() {
        return MetaItemDescriptor.class.getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", valueDescriptor=" + valueDescriptor +
            '}';
    }
}
