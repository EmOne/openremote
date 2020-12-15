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
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(using = AttributeList.AttributeObjectDeserializer.class)
public class AttributeList extends NamedList<Attribute<?>> {

    /**
     * Deserialise an {@link AttributeList} that is represented as a JSON object where each key is the {@link Attribute}
     * name
     */
    public static class AttributeObjectDeserializer extends StdDeserializer<AttributeList> {

        public AttributeObjectDeserializer() {
            super(AttributeList.class);
        }

        @Override
        public AttributeList deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            if (!jp.isExpectedStartObjectToken()) {
                throw new InvalidFormatException(jp, "Expected an object but got something else", jp.nextValue(), AttributeList.class);
            }

            List<Attribute<?>> list = new ArrayList<>();

            while(jp.nextToken() != JsonToken.END_OBJECT) {
                if(jp.currentToken() == JsonToken.FIELD_NAME) {
                    String attributeName = jp.getCurrentName();
                    jp.nextToken();
                    Attribute<?> attribute = jp.readValueAs(Attribute.class);
                    attribute.setNameInternal(attributeName);
                    list.add(attribute);
                }
            }

            AttributeList attributeList = new AttributeList();
            attributeList.addAllSilent(list);
            return attributeList;
        }
    }

    public AttributeList() {
    }

    public AttributeList(Collection<? extends Attribute<?>> c) {
        super(c);
    }

    // This works around the crappy type system and avoids the need for a type witness
    public <S> Optional<Attribute<S>> get(AttributeDescriptor<S> attributeDescriptor) {
        return super.get(attributeDescriptor);
    }

    public <S> Attribute<S> getOrCreate(AttributeDescriptor<S> attributeDescriptor) {
        return get(attributeDescriptor).orElseGet(() -> {
            Attribute<S> attr = new Attribute<>(attributeDescriptor);
            addSilent(attr);
            return attr;
        });
    }

    @SuppressWarnings("unchecked")
    public <S> Attribute<S> getOrCreate(String attributeName, ValueDescriptor<S> valueDescriptor) {
        return (Attribute<S>) get(attributeName).orElseGet(() -> {
            Attribute<S> attr = new Attribute<>(attributeName, valueDescriptor);
            addSilent(attr);
            return attr;
        });
    }

    public <T> void setValue(AttributeDescriptor<T> descriptor, T value) {
        getOrCreate(descriptor).setValue(value);
    }
}
