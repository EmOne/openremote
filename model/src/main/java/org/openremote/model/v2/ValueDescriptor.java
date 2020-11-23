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
package org.openremote.model.v2;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.attribute.MetaList;
import org.openremote.model.value.Values;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A simple wrapper around a {@link Class} that describes a value that can be used by {@link Attribute}s and
 * {@link MetaItem}s; it also conveniently stores {@link MetaItem}s that will be added to new {@link Attribute}
 * instances that use the {@link ValueDescriptor} (useful for adding units information etc.).
 */
public class ValueDescriptor<T> implements NameHolder, MetaHolder {

    /**
     * A class that represents an array {@link ValueDescriptor} which avoids the need to explicitly define
     * {@link ValueDescriptor}s for every value type in array form (e.g. string and string[])
     */
    static class ValueArrayDescriptor<T> extends ValueDescriptor<T> {
        public ValueArrayDescriptor(String name, Class<T> type, MetaList meta) {
            super(name, type, meta);
        }
    }

    /**
     * This class handles serialising {@link ValueDescriptor}s as strings with support for array representation
     */
    public static class ValueDescriptorStringConverter extends StdConverter<ValueDescriptor<?>, String> {

        @Override
        public String convert(ValueDescriptor<?> value) {
            return value instanceof ValueArrayDescriptor ? value.getName() + "[]" : value.getName();
        }
    }

    protected String name;
    protected Class<T> type;
    protected MetaList meta;

    public ValueDescriptor(String name, Class<T> type) {
        this(name, type, (MetaList)null);
    }

    public ValueDescriptor(String name, Class<T> type, MetaItem<?>...meta) {
        this(name, type, new MetaList(Arrays.asList(meta)));
    }

    public ValueDescriptor(String name, Class<T> type, Collection<MetaItem<?>> meta) {
        this(name, type, new MetaList(meta));
    }

    public ValueDescriptor(String name, Class<T> type, MetaList meta) {
        this.name = name;
        this.type = type;
        this.meta = meta;
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public Collection<MetaItem<?>> getMeta() {
        return meta;
    }

    public boolean isArray() {
        return this instanceof ValueArrayDescriptor;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Only interested in type equality as this is the critical part of a {@link ValueDescriptor}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ValueDescriptor<?> that = (ValueDescriptor<?>)obj;
        return Objects.equals(type, that.type);
    }

    /**
     * Returns an instance of this {@link ValueDescriptor} where the value type is an array of the current value type
     */
    @SuppressWarnings("unchecked")
    public ValueArrayDescriptor<T[]> asArray() {
        try {
            Class<T[]> arrayClass = (Class<T[]>) Values.getArrayClass(type);
            return new ValueArrayDescriptor<>(name, arrayClass, meta);
        } catch (ClassNotFoundException ignored) {
            // Can't happen as we have the source class already
        }

        return null;
    }

    public ValueDescriptor<T> withMeta(MetaItem<?>...meta) {
        return withMeta(Arrays.asList(meta));
    }

    public ValueDescriptor<T> withMeta(Collection<MetaItem<?>> meta) {
        MetaList metaList = new MetaList(this.meta);
        metaList.addOrReplace(meta);
        return new ValueDescriptor<>(name, type, metaList);
    }

    public ValueDescriptor<T> withMeta(MetaList meta) {
        return withMeta((Collection<MetaItem<?>>)meta);
    }
}
