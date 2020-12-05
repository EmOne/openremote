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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.openremote.model.value.AbstractNameValueDescriptorHolder;
import org.openremote.model.value.AbstractNameValueHolder;
import org.openremote.model.value.NameHolder;
import org.openremote.model.value.ValueHolder;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;

/**
 * Special list for {@link NameHolder} items where items with duplicate names are not allowed; this is serialised as
 * a JSON object where the name becomes the key.
 */
@JsonSerialize(using = NamedList.NamedListSerializer.class)
@JsonDeserialize(using = NamedList.NamedListDeserializer.class)
public class NamedList<T extends AbstractNameValueHolder<?>> extends ArrayList<T> {

    public static class NamedListSerializer extends StdSerializer<NamedList<?>> {

        @SuppressWarnings("unchecked")
        public NamedListSerializer() {
            super((Class<NamedList<?>>)(Class)NamedList.class);
        }

        @Override
        public void serialize(NamedList<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();

            for (NameHolder model : value) {
                gen.writeObjectField(model.getName(), model);
            }

            gen.writeEndObject();
        }
    }

    /**
     * Expects an {@link ObjectNode}; for each key-value entry the value is checked to make sure it is also an
     * {@link ObjectNode} if it isn't then an {@link ObjectNode} is constructed and the value entry is assigned to the
     * value field. The key of the entry is then assigned to the name field and then this {@link ObjectNode} is
     * deserialized as an instance of the inner type which is determined using the {@link ContextualDeserializer}.
     */
    public static class NamedListDeserializer extends StdDeserializer<NamedList<?>> implements ContextualDeserializer {

        public NamedListDeserializer() {
            super(NamedList.class);
        }

        public NamedListDeserializer(JavaType valueType) {
            super(valueType);
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
            JavaType wrapperType = property.getType();
            return new NamedListDeserializer(wrapperType);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public NamedList<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JsonNode node = jp.getCodec().readTree(jp);

            if (node.getNodeType() != JsonNodeType.OBJECT) {
                throw new InvalidFormatException(jp, "Expected an object but got type: " + node.getNodeType(), node, NamedList.class);
            }

            JavaType innerType = getValueType().findSuperType(NamedList.class).containedType(0);
            NamedList list = new NamedList<>();
            ObjectNode namedListObj = (ObjectNode) node;

            for (Iterator<Map.Entry<String, JsonNode>> it = namedListObj.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> item = it.next();

                String name = item.getKey();
                JsonNode itemNode = item.getValue();

                if (itemNode.getNodeType() != JsonNodeType.OBJECT) {
                    ObjectNode newItemNode = namedListObj.objectNode();
                    newItemNode.set("value", itemNode);
                    item.setValue(newItemNode);
                    itemNode = newItemNode;
                }

                ((ObjectNode)itemNode).put("name", name);
                AbstractNameValueHolder itemObj = ctxt.readValue(itemNode.traverse(), innerType);
                list.add(itemObj);
            }
            return list;
        }
    }

    public NamedList() {
    }

    public NamedList(Collection<? extends T> c) {
        super();
        // Use addAll to do duplicate checking
        addAll(c);
    }

    protected static void throwIfHas(NamedList<?> list, String name) {
        if (list.has(name)) {
            throw new IllegalStateException("List already contains an item with this name: " + name);
        }
    }

    protected static <T extends NameHolder> void throwIfDuplicates(Collection<T> c) {
        c.stream().map(NameHolder::getName).filter(name ->
            Collections.frequency(c, name) > 1
        ).findAny().ifPresent(duplicateName -> {
            throw new IllegalStateException("Detected multiple items with the same name: " + duplicateName);
        });
    }

    @Override
    public T set(int index, T element) {
        throw new IllegalStateException("set by index not supported for named lists");
    }

    @Override
    public boolean add(T t) {
        throwIfHas(this, t.getName());
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        throwIfHas(this, element.getName());
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c == null) {
            return false;
        }
        throwIfDuplicates(c);
        c.forEach(item -> throwIfHas(this, item.getName()));
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (c == null) {
            return false;
        }
        throwIfDuplicates(c);
        c.forEach(item -> throwIfHas(this, item.getName()));
        return super.addAll(index, c);
    }


    protected boolean addAllSilent(Collection<? extends T> c) {
        return super.addAll(c);
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {
        super.replaceAll(operator);
        throwIfDuplicates(this);
    }

    public <U extends AbstractNameValueDescriptorHolder<T>> void remove(U descriptor) {
        removeIf(item -> item.getName().equals(descriptor.getName()));
    }

    public Optional<T> get(String name) {
        return this.stream().filter(item -> item.getName().equals(name)).findFirst();
    }

    @SuppressWarnings("unchecked")
    public <V, W extends AbstractNameValueHolder<V>> Optional<W> get(AbstractNameValueDescriptorHolder<V> nameValueDescriptorHolder) {
        Optional<T> valueProvider = this.stream().filter(item -> item.getName().equals(nameValueDescriptorHolder.getName())).findFirst();
        return valueProvider.map(item -> {
            Class<?> itemType = item.getValueType().getType();
            Class<V> expectedType = nameValueDescriptorHolder.getValueType().getType();
            if (itemType == expectedType) {
                return (W)item;
            }
            return null;
        });
    }

    @SuppressWarnings("unchecked")
    public <S> Optional<S> getValue(String name) {
        return get(name).flatMap(ValueHolder::getValue).map(v -> (S)v);
    }

    @SuppressWarnings("unchecked")
    public <S> S getValueOrDefault(String name) {
        return (S) get(name).flatMap(ValueHolder::getValue).orElse(null);
    }

    public <S> Optional<S> getValue(AbstractNameValueDescriptorHolder<S> nameValueDescriptorProvider) {
        return get(nameValueDescriptorProvider).flatMap(ValueHolder::getValue);
    }

    public <S> S getValueOrDefault(AbstractNameValueDescriptorHolder<S> nameValueDescriptorProvider) {
        return get(nameValueDescriptorProvider).flatMap(ValueHolder::getValue).orElse(null);
    }

    @SafeVarargs
    public final void addAll(T... items) {
        this.addAll(Arrays.asList(items));
    }

    /**
     * Add or replace the specified items by name
     */
    @SafeVarargs
    public final void addOrReplace(T...items) {
        addOrReplace(Arrays.asList(items));
    }
    public final void addOrReplace(Collection<T> items) {
        items.forEach(item -> this.removeIf(i -> i.getName().equals(item.getName())));
        addAll(items);
    }

    public boolean has(NameHolder nameHolder) {
        return has(nameHolder.getName());
    }

    public boolean has(String name) {
        return this.stream().anyMatch(item -> item.getName().equals(name));
    }
}
