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

import com.fasterxml.jackson.databind.util.StdConverter;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.attribute.MetaList;
import org.openremote.model.util.AssetModelUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * A simple wrapper around a {@link Class} that describes a value that can be used by {@link Attribute}s and
 * {@link MetaItem}s; it also conveniently stores {@link MetaItem}s that should be added to new {@link Attribute}
 * instances that use the {@link ValueDescriptor} (useful for adding default units information etc.).
 * <p>
 * The {@link ValueDescriptor} applies to the {@link Asset} type it is associated with and all subtypes of this type (i.e. a
 * {@link ValueDescriptor} associated with the base {@link Asset} type will be available to all {@link  Asset} types (e.g.
 * {@link ValueType#NUMBER} can be applied to any {@link org.openremote.model.asset.Asset}'s {@link Attribute} and/or
 * {@link MetaItemDescriptor}).
 * <p>
 * {@link ValueDescriptor}s for arrays don't need to be explicitly defined but can be obtained at the point of comsumpution
 * by simply calling {@link ValueDescriptor#asArray}.
 * <p>
 * {@link ValueDescriptor#getName} must be globally unique within the context of the manager it is registered with.
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

    /**
     * This class handles deserialising value type names to {@link ValueDescriptor}s
     */
    public static class StringValueDescriptorConverter extends StdConverter<String, ValueDescriptor<?>> {

        @Override
        public ValueDescriptor<?> convert(String value) {
            return AssetModelUtil.getValueDescriptor(value).orElse(ValueType.OBJECT);
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
        return Objects.hashCode(name);
    }

    /**
     * Value descriptor names are unique identifiers so can use this for equality purposes
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || !(ValueDescriptor.class.isAssignableFrom(obj.getClass()))) return false;
        ValueDescriptor<?> that = (ValueDescriptor<?>)obj;
        return Objects.equals(name, that.name);
    }

    /**
     * Returns an instance of this {@link ValueDescriptor} where the value type is an array of the current value type
     */
    @SuppressWarnings("unchecked")
    public ValueArrayDescriptor<T[]> asArray() {
        try {
            Class<T[]> arrayClass = (Class<T[]>) Values.getArrayClass(type);
            return new ValueArrayDescriptor<>(name + "[]", arrayClass, meta);
        } catch (ClassNotFoundException ignored) {
            // Can't happen as we have the source class already
        }

        return null;
    }

    public ValueDescriptor<T> addOrReplaceMeta(MetaItem<?>...meta) {
        return addOrReplaceMeta(Arrays.asList(meta));
    }

    public ValueDescriptor<T> addOrReplaceMeta(Collection<MetaItem<?>> meta) {
        MetaList metaList = new MetaList(this.meta);
        metaList.addOrReplace(meta);
        return new ValueDescriptor<>(name, type, metaList);
    }

    public ValueDescriptor<T> addOrReplaceMeta(MetaList meta) {
        return addOrReplaceMeta((Collection<MetaItem<?>>)meta);
    }
}
