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
package org.openremote.model.attribute;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.openremote.model.util.AssetModelUtil;
import org.openremote.model.value.MetaItemDescriptor;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(using = MetaList.MetaObjectDeserializer.class)
public class MetaList extends NamedList<MetaItem<?>> {
    /**
     * Deserialise a {@link MetaList} that is represented as a JSON object where each key is the name of a
     * {@link MetaItemDescriptor}
     */
    public static class MetaObjectDeserializer extends StdDeserializer<MetaList> {

        public MetaObjectDeserializer() {
            super(MetaList.class);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public MetaList deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (!jp.isExpectedStartObjectToken()) {
                throw new InvalidFormatException(jp, "Expected an object but got something else", jp.nextValue(), MetaList.class);
            }

            List<MetaItem<?>> list = new ArrayList<>();

            while(jp.nextToken() != JsonToken.END_OBJECT) {
                if(jp.currentToken() == JsonToken.FIELD_NAME) {
                    String metaItemName = jp.getCurrentName();
                    jp.nextToken();

                    MetaItem metaItem = new MetaItem<>();
                    metaItem.setNameInternal(metaItemName);

                    // Find the meta descriptor for this meta item as this will give us value type also; fallback to
                    // OBJECT type meta item to allow deserialization of meta that doesn't exist in the current asset model
                    Optional<ValueDescriptor<?>> valueDescriptor = AssetModelUtil.getMetaItemDescriptor(metaItemName)
                        .map(MetaItemDescriptor::getValueType);

                    Class valueType = valueDescriptor.map(ValueDescriptor::getType).orElseGet(() -> (Class) Object.class);
                    metaItem.setValue(jp.readValueAs(valueType));

                    // Get the value descriptor from the value if it isn't known
                    metaItem.setTypeInternal(valueDescriptor.orElseGet(() -> {
                        if (!metaItem.getValue().isPresent()) {
                            return ValueType.OBJECT;
                        }
                        Object value = metaItem.getValue().orElse(null);
                        return AssetModelUtil.getValueDescriptorForValue(value);
                    }));

                    list.add(metaItem);
                }
            }

            MetaList metaList = new MetaList();
            metaList.addAllSilent(list);
            return metaList;
        }
    }

    public MetaList() {
    }

    public MetaList(Collection<MetaItem<?>> meta) {
        super(meta);
    }

    // This works around the crappy type system and avoids the need for a type witness
    public <S> Optional<MetaItem<S>> get(MetaItemDescriptor<S> metaDescriptor) {
        return super.get(metaDescriptor);
    }

    public <S> MetaItem<S> getOrCreate(MetaItemDescriptor<S> metaDescriptor) {
        MetaItem<S> metaItem = get(metaDescriptor).orElse(new MetaItem<>(metaDescriptor));
        addOrReplace(metaItem);
        return metaItem;
    }

    public <T> void set(MetaItemDescriptor<T> descriptor, T value) {
        MetaItem<T> metaItem = get(descriptor).orElse(new MetaItem<>(descriptor, null));
        metaItem.setValue(value);
    }
}
