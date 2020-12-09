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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.openremote.model.util.AssetModelUtil;
import org.openremote.model.value.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Stores a named value with associated {@link MetaItem}s.
 */
@JsonDeserialize(using = Attribute.AttributeDeserializer.class)
public class Attribute<T> extends AbstractNameValueHolder<T> {

    public static class AttributeDeserializer extends StdDeserializer<Attribute<?>> {

        protected AttributeDeserializer() {
            super(Attribute.class);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        @Override
        public Attribute<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {

            // Need to find the type field to know how to deserialise the value
            TokenBuffer tokenBuffer = TokenBuffer.asCopyOfValue(jp);
            JsonParser jp2 = tokenBuffer.asParser();
            JsonParser jp3 = tokenBuffer.asParser();
            String attributeValueType = null;
            int level = 1;
            jp2.nextToken();

            while (level > 0) {
                JsonToken nextToken = jp2.nextToken();

                if (nextToken == JsonToken.START_OBJECT) {
                    level++;
                    continue;
                }

                if (nextToken == JsonToken.END_OBJECT) {
                    level--;
                    continue;
                }

                if (level == 1 && jp2.currentName().equals("type")) {
                    jp2.nextToken();
                    attributeValueType = jp2.getValueAsString();
                    break;
                }
            }

            if (attributeValueType == null) {
                throw new JsonParseException(jp, "Failed to extract attribute type information");
            }

            // Get inner attribute type or fallback to primitive/JSON type
            Optional<ValueDescriptor<?>> valueDescriptor = AssetModelUtil.getValueDescriptor(attributeValueType);
            Attribute attribute = new Attribute<>();

            while (jp3.nextToken() != JsonToken.END_OBJECT) {
                if (jp3.currentToken() == JsonToken.FIELD_NAME) {
                    String propName = jp3.currentName();
                    JsonToken token = jp3.nextToken();
                    if (token == JsonToken.VALUE_NULL) {
                        continue;
                    }
                    switch (propName) {
                        case "meta":
                            attribute.meta = jp3.readValueAs(MetaList.class);
                            break;
                        case "name":
                            attribute.name = jp3.readValueAs(String.class);
                            break;
                        case "timestamp":
                            attribute.timestamp = jp3.readValueAs(Long.class);
                            break;
                        case "value":
                            @SuppressWarnings("unchecked")
                            Class valueType = valueDescriptor.map(ValueDescriptor::getType)
                                .orElseGet(() -> (Class) Object.class);
                            attribute.value = jp3.readValueAs(valueType);
                            break;
                    }
                }
            }

            // Get the value descriptor from the value if it isn't known
            attribute.type = valueDescriptor.orElseGet(() -> {
                if (attribute.value == null) {
                    return ValueType.OBJECT;
                }
                Object value = attribute.value;
                return AssetModelUtil.getValueDescriptorForValue(value);
            });

            return (Attribute<?>) attribute;
        }
    }

    protected MetaList meta;
    @JsonIgnore
    protected long timestamp;

    Attribute() {}

    public Attribute(AttributeDescriptor<T> attributeDescriptor) {
        this(attributeDescriptor, null);
    }

    public Attribute(AttributeDescriptor<T> attributeDescriptor, T value) {
        this(attributeDescriptor.getName(), attributeDescriptor.getValueType(), value);

        // Auto merge meta from attribute descriptor
        if (attributeDescriptor.getMeta() != null) {
            getMeta().addOrReplace(attributeDescriptor.getMeta());
        }
    }

    public Attribute(AttributeDescriptor<T> attributeDescriptor, T value, long timestamp) {
        this(attributeDescriptor, value);
        setTimestamp(timestamp);
    }

    @SuppressWarnings("unchecked")
    public Attribute(String name, ValueDescriptor<?> valueDescriptor) {
        this(name, (ValueDescriptor<T>)valueDescriptor, null);
    }

    public Attribute(String name, ValueDescriptor<T> valueDescriptor, T value) {
        super(name, valueDescriptor, value);

        // Auto add meta from value descriptor
        if (valueDescriptor.getMeta() != null) {
            getMeta().addAll(valueDescriptor.getMeta());
        }
    }

    public Attribute(String name, ValueDescriptor<T> valueDescriptor, T value, long timestamp) {
        this(name, valueDescriptor, value);
        setTimestamp(timestamp);
    }

    // For JPA/Hydrators
    void setNameInternal(String name) {
        this.name = name;
    }

    public MetaList getMeta() {
        if (meta == null) {
            meta = new MetaList();
        }

        return meta;
    }

    public Attribute<T> setMeta(MetaList meta) {
        this.meta = meta;
        return this;
    }

    public Attribute<T> addMeta(@NotNull MetaList meta) {
        getMeta().addAll(meta);
        return this;
    }

    public Attribute<T> addMeta(@NotNull MetaItem<?>...meta) {
        getMeta().addAll(meta);
        return this;
    }

    public Attribute<T> addMeta(@NotNull Collection<MetaItem<?>> meta) {
        getMeta().addAll(meta);
        return this;
    }

    public Attribute<T> addOrReplaceMeta(@NotNull MetaList meta) {
        getMeta().addAll(meta);
        return this;
    }

    public Attribute<T> addOrReplaceMeta(@NotNull MetaItem<?>...meta) {
        return addOrReplaceMeta(Arrays.asList(meta));
    }

    public Attribute<T> addOrReplaceMeta(@NotNull Collection<MetaItem<?>> meta) {
        getMeta().addAll(meta);
        return this;
    }

    public <U> Optional<U> getMetaValue(MetaItemDescriptor<U> metaItemDescriptor) {
        return getMeta().getValue(metaItemDescriptor);
    }

    public <U> U getMetaValueOrDefault(MetaItemDescriptor<U> metaItemDescriptor) {
        return getMeta().getValueOrDefault(metaItemDescriptor);
    }

    public boolean hasMeta(MetaItemDescriptor<?> metaItemDescriptor) {
        return getMeta().has(metaItemDescriptor);
    }

    public boolean hasMeta(String metaItemName) {
        return getMeta().has(metaItemName);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
        // Store system time as negative timestamp to allow simple & fast equality check using timestamp; assuming value
        // is immutable as requested in ValueProvider then when the value is set we change the timestamp which
        // indirectly indicates that the value has changed, we use negative value to indicate that the backend needs
        // to set the time at the point of saving.
        setTimestamp(-1*System.currentTimeMillis());
    }

    public void setValue(T value, long timestamp) {
        super.setValue(value);
        if (timestamp <= this.timestamp) {
            AssetModelUtil.LOG.warning("timestamp cannot be less than or equal to the current value so using system time");
            timestamp = -1*System.currentTimeMillis();
        }
        setTimestamp(timestamp);
    }

    @JsonIgnore
    public Optional<Long> getTimestamp() {
        return hasExplicitTimestamp() ? Optional.of(Math.abs(timestamp)) : Optional.empty();
    }

    @JsonProperty("timestamp")
    protected Long getTimestampInternal() {
        return getTimestamp().orElse(null);
    }


    public boolean hasExplicitTimestamp() {
        return timestamp > 0;
    }

    public Attribute<T> setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    //    public boolean hasLabel() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(LABEL));
//    }
//
//    public Optional<String> getLabel() {
//        return Optional.ofNullable(getMetaStream()
//            .filter(isMetaNameEqualTo(LABEL))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString)
//            .orElseGet(() -> getName().orElse(null)));
//    }
//
//    public Optional<String> getLabelOrName() {
//        return getLabel().map(Optional::of).orElseGet(this::getName);
//    }
//
//    public void setLabel(String label) {
//        if (!isNullOrEmpty(label)) {
//            replaceMetaByName(getMeta(), LABEL, label);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(LABEL));
//        }
//    }
//
//    public boolean isExecutable() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(EXECUTABLE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setExecutable(boolean executable) {
//        if (executable) {
//            replaceMetaByName(getMeta(), EXECUTABLE, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(EXECUTABLE));
//        }
//    }
//
//    public boolean hasAgentLink() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(AGENT_LINK));
//    }
//
//    public boolean isProtocolConfiguration() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(PROTOCOL_CONFIGURATION))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isShowOnDashboard() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(SHOW_ON_DASHBOARD))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setShowOnDashboard(boolean show) {
//        if (show) {
//            replaceMetaByName(getMeta(), SHOW_ON_DASHBOARD, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(SHOW_ON_DASHBOARD));
//        }
//    }
//
//    public boolean hasFormat() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(FORMAT));
//    }
//
//    public Optional<String> getFormat() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(FORMAT))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setFormat(String format) {
//        if (!isNullOrEmpty(format)) {
//            replaceMetaByName(getMeta(), FORMAT, format);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(FORMAT));
//        }
//    }
//
//    public boolean hasDescription() {
//        return getMetaStream().anyMatch(isMetaNameEqualTo(DESCRIPTION));
//    }
//
//    public Optional<String> getDescription() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(DESCRIPTION))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setDescription(String description) {
//        if (!isNullOrEmpty(description)) {
//            replaceMetaByName(getMeta(), DESCRIPTION, description);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(DESCRIPTION));
//        }
//    }
//
//    public boolean isAccessRestrictedRead() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_RESTRICTED_READ))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isAccessRestrictedWrite() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_RESTRICTED_WRITE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isAccessPublicRead() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(MetaItemType.ACCESS_PUBLIC_READ))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public boolean isReadOnly() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(READ_ONLY))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setReadOnly(boolean readOnly) {
//        if (readOnly) {
//            replaceMetaByName(getMeta(), READ_ONLY, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(READ_ONLY));
//        }
//    }
//
//    public boolean isStoreDatapoints() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(STORE_DATA_POINTS))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setStoreDatapoints(boolean storeDatapoints) {
//        if (storeDatapoints) {
//            replaceMetaByName(getMeta(), STORE_DATA_POINTS, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(STORE_DATA_POINTS));
//        }
//    }
//
//    public boolean isRuleState() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_STATE))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setRuleState(boolean ruleState) {
//        if (ruleState) {
//            replaceMetaByName(getMeta(), RULE_STATE, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_STATE));
//        }
//    }
//
//    public boolean isRuleEvent() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_EVENT))
//            .findFirst()
//            .map(metaItem -> metaItem.getValueAsBoolean().orElse(false))
//            .orElse(false);
//    }
//
//    public void setRuleEvent(boolean ruleEvent) {
//        if (ruleEvent) {
//            replaceMetaByName(getMeta(), RULE_EVENT, true);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_EVENT));
//        }
//    }
//
//    public Optional<String> getRuleEventExpires() {
//        return getMetaStream()
//            .filter(isMetaNameEqualTo(RULE_EVENT_EXPIRES))
//            .findFirst()
//            .flatMap(AbstractValueHolder::getValueAsString);
//    }
//
//    public void setRuleEventExpires(String expiry) {
//        if (!isNullOrEmpty(expiry)) {
//            replaceMetaByName(getMeta(), RULE_EVENT_EXPIRES, expiry);
//        } else {
//            getMeta().removeIf(isMetaNameEqualTo(RULE_EVENT_EXPIRES));
//        }
//    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", timestamp='" + getTimestamp() + '\'' +
            ", meta='" + getMeta().stream().map(MetaItem::toString).collect(Collectors.joining(",")) + '\'' +
            "} ";
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(timestamp) + Objects.hash(meta);
    }

    /**
     * Equality check by converting value to {@link JsonNode} as all {@link Attribute} values must be serialisable
     * but this doesn't mean that the value type has an equality override so this is the safest mechanism.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Attribute))
            return false;
        Attribute<?> that = (Attribute<?>) obj;

        return Objects.equals(timestamp, that.timestamp)
            && Objects.equals(meta, that.meta)
            && super.equals(obj);
    }

    /**
     * Basic (fast) equality check of timestamps only
     */
    public boolean equalsBasic(Object obj) {
        return equals(obj, Comparator.comparingLong(o -> o.timestamp));
    }

    public boolean equals(Object obj, Comparator<Attribute<?>> comparator) {
        if (comparator == null) {
            return equals(obj);
        }
        if (obj == null)
            return false;
        if (!(obj instanceof Attribute))
            return false;
        Attribute<?> that = (Attribute<?>) obj;
        return comparator.compare(this, that) == 0;
    }

    /**
     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
     */
    public static Stream<Attribute<?>> getAddedOrModifiedAttributes(List<Attribute<?>> oldAttributes,
                                                                 List<Attribute<?>> newAttributes) {
        return getAddedOrModifiedAttributes(oldAttributes, newAttributes, null);
    }

    /**
     * @return All attributes that exist only in the new list or are different than any attribute in the old list.
     */
    public static Stream<Attribute<?>> getAddedOrModifiedAttributes(List<Attribute<?>> oldAttributes,
                                                                    List<Attribute<?>> newAttributes,
                                                                    Predicate<String> ignoredAttributeNames) {
        return getAddedOrModifiedAttributes(
            oldAttributes,
            newAttributes,
            null,
            ignoredAttributeNames);
    }

    /**
     * @return All attributes that exist only in the new list or are different than any attribute in the old list
     */
    public static Stream<Attribute<?>> getAddedOrModifiedAttributes(List<Attribute<?>> oldAttributes,
                                                                    List<Attribute<?>> newAttributes,
                                                                    Predicate<String> limitToAttributeNames,
                                                                    Predicate<String> ignoredAttributeNames) {
        return newAttributes.stream()
            .filter(newAttribute -> {
                if (limitToAttributeNames != null && !limitToAttributeNames.test(newAttribute.getName())) {
                    return false;
                }

                if (ignoredAttributeNames != null && ignoredAttributeNames.test(newAttribute.getName())) {
                    return false;
                }

                return oldAttributes.stream().filter(attribute ->
                            attribute.getName().equals(newAttribute.getName()))
                    .findFirst()
                    .map(attribute -> {
                        // Attribute may have been modified do basic equality check
                        return !attribute.equals(newAttribute);
                    })
                    .orElse(true); // Attribute is new
            }
        );
    }
}
