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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.openremote.model.value.MetaItemDescriptor;

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
    public static class MetaObjectDeserializer extends StdDeserializer<MetaList> implements ContextualDeserializer {

        public MetaObjectDeserializer() {
            super(AttributeList.class);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            return new MetaObjectDeserializer();
        }

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
                    MetaItem<?> metaItem = jp.readValueAs(MetaItem.class);
//                    ObjectNode attributeNode = jp.readValueAsTree();
//                    attributeNode.put("name", attributeName);
//                    Attribute<?> attribute = Values.JSON.convertValue(attributeNode, Attribute.class);
//                    String valueType = attributeNode.get("type").asText();
//                    // Get inner attribute type or fallback to primitive/JSON type
//                    Class<?> innerType = AssetModelUtil.getValueDescriptor(valueType).orElseGet(() -> {
//                        // Look at the value itself otherwise fallback to JsonNode
//                        JsonNode valueNode = attributeNode.get("value");
//                        return AssetModelUtil.getValueDescriptorByNode(valueNode);
//                    }).getType();
//                    Attribute<?> attribute = Values.JSON.convertValue(attributeNode, ctxt.getTypeFactory().constructParametricType(Attribute.class, innerType));
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
