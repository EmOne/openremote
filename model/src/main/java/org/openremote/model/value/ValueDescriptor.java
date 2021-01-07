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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.openremote.model.asset.Asset;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.attribute.MetaMap;
import org.openremote.model.util.AssetModelUtil;
import org.openremote.model.util.TsIgnore;

import java.io.IOException;
import java.io.Serializable;
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
@JsonDeserialize(using = ValueDescriptor.ValueDescriptorDeserialiser.class)
public class ValueDescriptor<T> implements NameHolder, MetaHolder, Serializable {

    /**
     * A class that represents an array {@link ValueDescriptor} which avoids the need to explicitly define
     * {@link ValueDescriptor}s for every value type in array form (e.g. string and string[])
     */
    @TsIgnore
    static class ValueArrayDescriptor<T> extends ValueDescriptor<T> {
        public ValueArrayDescriptor(String name, Class<T> type, MetaMap meta) {
            super(name, type, meta);
            isArray = true;
        }
    }

    /**
     * This class handles serialising {@link ValueDescriptor}s as strings
     */
    public static class ValueDescriptorStringConverter extends StdConverter<ValueDescriptor<?>, String> {

        @Override
        public String convert(ValueDescriptor<?> value) {
            return value.getName();
        }
    }

    /**
     * This class handles deserialising value descriptor names to {@link ValueDescriptor}s
     */
    public static class StringValueDescriptorConverter extends StdConverter<String, ValueDescriptor<?>> {

        @Override
        public ValueDescriptor<?> convert(String value) {
            return AssetModelUtil.getValueDescriptor(value).orElse(ValueDescriptor.UNKNOWN);
        }
    }

    /**
     * This class handles serialising {@link ValueDescriptor#getType} classes as strings that represent the
     * JSON type of the value class
     */
    public static class ValueTypeStringConverter extends StdConverter<Class<?>, String> {

        @Override
        public String convert(Class<?> value) {
            if (Values.isBoolean(value)) {
                return "boolean";
            }
            if (Values.isNumber(value)) {
                return "number";
            }
            if (Values.isString(value)) {
                return "string";
            }
            if (Values.isArray(value)) {
                return "array";
            }
            if (value == Object.class) {
                return "unknown";
            }
            return "object";
        }
    }

    public static class ValueDescriptorDeserialiser extends StdDeserializer<ValueDescriptor<?>> {

        public ValueDescriptorDeserialiser() {
            super(ValueDescriptor.class);
        }

        @Override
        public ValueDescriptor<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            String name;
            String type;
            MetaMap meta = null;
            JsonNode node = p.getCodec().readTree(p);

            if (!node.isObject()) {
                throw MismatchedInputException.from(p, ValueDescriptor.class, "Expected object but got: " + node.getNodeType());
            }

            name = node.get("name").asText();
            type = node.get("type").asText();
            if (node.has("meta")) {
                JsonParser metaParser = node.get("meta").traverse(p.getCodec());
                metaParser.nextToken();
                meta = ctxt.readValue(metaParser, MetaMap.class);
            }

            // Look for an existing value descriptor with this name
            MetaMap finalMeta = meta;
            return AssetModelUtil.getValueDescriptor(name).orElseGet(() -> {
                Class<?> typeClass = ValueType.JSON_OBJECT.type;
                boolean isArray = name.endsWith("[]");

                switch (type) {
                    case "boolean":
                        typeClass = ValueType.BOOLEAN.type;
                        break;
                    case "number":
                        typeClass = ValueType.NUMBER.type;
                        break;
                    case "string":
                        typeClass = ValueType.STRING.type;
                        break;
                    case "array":
                        typeClass = Object[].class;
                        break;
                }

                ValueDescriptor<?> valueDescriptor = new ValueDescriptor<>(name, typeClass, finalMeta);
                return isArray ? valueDescriptor.asArray() : valueDescriptor;
            });
        }
    }

    public static final ValueDescriptor<Object> UNKNOWN = new ValueDescriptor<>("Unknown", Object.class);
    protected String name;
    @JsonSerialize(converter = ValueDescriptor.ValueTypeStringConverter.class)
    protected Class<T> type;
    protected MetaMap meta;
    protected Boolean isArray;

    public ValueDescriptor(String name, Class<T> type) {
        this(name, type, (MetaMap)null);
    }

    public ValueDescriptor(String name, Class<T> type, MetaItem<?>...meta) {
        this(name, type, new MetaMap(Arrays.asList(meta)));
    }

    public ValueDescriptor(String name, Class<T> type, Collection<MetaItem<?>> meta) {
        this(name, type, new MetaMap(meta));
    }

    public ValueDescriptor(String name, Class<T> type, MetaMap meta) {
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
    public MetaMap getMeta() {
        return meta;
    }

    public boolean isArray() {
        return this instanceof ValueArrayDescriptor;
    }

    public ValueDescriptor<?> asNonArray() {
        return isArray() ? new ValueDescriptor<>(name.substring(0, name.length()-2), type.getComponentType(), meta) : this;
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
        MetaMap metaMap = new MetaMap(this.meta);
        metaMap.addOrReplace(meta);
        return new ValueDescriptor<>(name, type, metaMap);
    }

    public ValueDescriptor<T> addOrReplaceMeta(MetaMap meta) {
        return addOrReplaceMeta(meta.values());
    }

    @Override
    public String toString() {
        return ValueDescriptor.class.getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", type=" + type +
            '}';
    }
}
