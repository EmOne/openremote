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
package org.openremote.model.protocol;

import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeExecuteStatus;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.query.filter.ValuePredicate;
import org.openremote.model.util.Pair;
import org.openremote.model.util.TextUtil;
import org.openremote.model.value.ValueFilter;
import org.openremote.model.value.ValueType;
import org.openremote.model.value.Values;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

import static org.openremote.model.asset.agent.Protocol.DYNAMIC_VALUE_PLACEHOLDER;
import static org.openremote.model.value.Values.NULL_LITERAL;
import static org.openremote.model.value.Values.applyValueFilters;

public final class ProtocolUtil {

    protected ProtocolUtil() {
    }

    public static String bytesToHexString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    public static byte[] bytesFromHexString(String hex) {
        try {
            return Hex.decodeHex(hex.toCharArray());
        } catch (Exception e) {
            Protocol.LOG.log(Level.WARNING, "Failed to convert hex string to bytes", e);
            return new byte[0];
        }
    }

    public static String bytesToBinaryString(byte[] bytes) {
        return BinaryCodec.toAsciiString(bytes);
    }

    public static byte[] bytesFromBinaryString(String binary) {
        try {
            return BinaryCodec.fromAscii(binary.toCharArray());
        } catch (Exception e) {
            Protocol.LOG.log(Level.WARNING, "Failed to convert hex string to bytes", e);
            return new byte[0];
        }
    }

    /**
     * Will perform standard value processing for outbound values (Linked Attribute -> Protocol); the
     * containsDynamicPlaceholder flag is required so that the entire write value string is not
     * searched on every single write request (for performance reasons), instead this should be recorded when the
     * attribute is first linked.
     */
    public static Pair<Boolean, Object> doOutboundValueProcessing(String assetId, Attribute<?> attribute, AgentLink<?> agentLink, Object value, boolean containsDynamicPlaceholder) {

        String writeValue = agentLink.getWriteValue().orElse(null);

        Pair<Boolean, Object> ignoreAndConvertedValue;
        final AtomicReference<Object> valRef = new AtomicReference<>(value);

        // Check if attribute type is executable
        if (attribute.getValueType().equals(ValueType.EXECUTION_STATUS)) {
            AttributeExecuteStatus status = Values.getValueCoerced(value, AttributeExecuteStatus.class).orElse(null);

            if (status == AttributeExecuteStatus.REQUEST_START && writeValue != null) {
                value = Values.parse(writeValue).orElse(null);
                return new Pair<>(false, value);
            }
        }

        // value conversion
        ignoreAndConvertedValue = agentLink.getWriteValueConverter().map(converter -> {
            Protocol.LOG.fine("Applying attribute write value converter to attribute: assetId=" + assetId + ", attribute=" + attribute.getName());
            return applyValueConverter(valRef.get(), converter);
        }).orElse(new Pair<>(false, valRef.get()));

        if (ignoreAndConvertedValue.key) {
            return ignoreAndConvertedValue;
        }

        if (valRef.get() == null) {
            return new Pair<>(false, null);
        }

        // dynamic value insertion

        boolean hasWriteValue = !TextUtil.isNullOrEmpty(writeValue);

        if (hasWriteValue) {
            if (containsDynamicPlaceholder) {
                String valueStr = value == null ? NULL_LITERAL : value.toString();
                writeValue = writeValue.replaceAll(Protocol.DYNAMIC_VALUE_PLACEHOLDER_REGEXP, valueStr);
            }

            try {
                value = Values.parse(writeValue).orElse(null);
            } catch (Exception e) {
                Protocol.LOG.log(Level.INFO, "Failed to pass attribute write payload generated by META_ATTRIBUTE_WRITE_VALUE", e);
            }
        }

        return new Pair<>(false, value);
    }

    public static boolean hasDynamicWriteValue(AgentLink<?> agentLink) {
        return agentLink.getWriteValue().map(str -> str.contains(DYNAMIC_VALUE_PLACEHOLDER)).orElse(false);
    }

    /**
     * Will perform standard value processing for inbound values (Protocol -> Linked Attribute); returning the processed
     * value and a flag indicating whether the inbound value should be ignored (i.e. drop the inbound message).
     */
    public static Pair<Boolean, Object> doInboundValueProcessing(String assetId, Attribute<?> attribute, AgentLink<?> agentLink, Object value) {

        Pair<Boolean, Object> ignoreAndConvertedValue;
        final AtomicReference<Object> valRef = new AtomicReference<>(value);

        // value filtering
        agentLink.getValueFilters().ifPresent(valueFilters -> {
            Protocol.LOG.fine("Applying attribute value filters to attribute: assetId=" + assetId + ", attribute=" + attribute.getName());
            Object o = Values.applyValueFilters(value, valueFilters);
            if (o == null) {
                Protocol.LOG.info("Value filters generated a null value for attribute: assetId=" + assetId + ", attribute=" + attribute.getName());
            }
            valRef.set(o);
        });

        // value conversion
        ignoreAndConvertedValue = agentLink.getValueConverter().map(converter -> {
            Protocol.LOG.fine("Applying attribute value converter to attribute: assetId=" + assetId + ", attribute=" + attribute.getName());
            return applyValueConverter(valRef.get(), converter);
        }).orElse(new Pair<>(false, valRef.get()));

        if (ignoreAndConvertedValue.key) {
            return ignoreAndConvertedValue;
        }

        if (valRef.get() == null) {
            return new Pair<>(false, null);
        }

        // built in value conversion
        Class<?> toType = attribute.getValueType().getType();
        Class<?> fromType = valRef.get().getClass();

        if (toType != fromType) {
            Protocol.LOG.fine("Applying built in attribute value conversion: " + fromType + " -> " + toType);
            valRef.set(Values.getValueCoerced(valRef.get(), toType).orElse(null));

            if (valRef.get() == null) {
                Protocol.LOG.warning("Failed to convert value: " + fromType + " -> " + toType);
                Protocol.LOG.warning("Cannot send linked attribute update");
                return new Pair<>(true, null);
            }
        }

        return new Pair<>(false, valRef.get());
    }

    public static Pair<Boolean, Object> applyValueConverter(Object value, ObjectNode converter) {

        if (converter == null) {
            return new Pair<>(false, value);
        }

        String converterKey = Values.getValueCoerced(value, String.class).map(str -> str.toUpperCase(Locale.ROOT)).orElse(NULL_LITERAL.toUpperCase());

        return Optional.ofNullable(converter.get(converterKey))
            .map(node -> {
                if (node.getNodeType() == JsonNodeType.STRING) {
                    if ("@IGNORE".equalsIgnoreCase(node.textValue())) {
                        return new Pair<>(true, null);
                    }

                    if ("@NULL".equalsIgnoreCase(node.textValue())) {
                        return new Pair<>(false, null);
                    }
                }

                return new Pair<Boolean, Object>(false, node);
            })
            .orElse(new Pair<>(true, value));
    }

    public static Consumer<String> createGenericAttributeMessageConsumer(String assetId, Attribute<?> attribute, AgentLink<?> agentLink, Supplier<Long> currentMillisSupplier, Consumer<AttributeState> stateConsumer) {

        ValueFilter[] matchFilters =  agentLink.getMessageMatchFilters().orElse(null);
        ValuePredicate matchPredicate = agentLink.getMessageMatchPredicate().orElse(null);

        if (matchPredicate == null) {
            return null;
        }

        return message -> {
            if (!TextUtil.isNullOrEmpty(message)) {
                Object messageFiltered = applyValueFilters(message, matchFilters);
                if (messageFiltered != null) {
                    if (matchPredicate.asPredicate(currentMillisSupplier).test(messageFiltered)) {
                        Protocol.LOG.finest("Inbound message meets attribute matching meta so writing state to state consumer for attribute: asssetId=" + assetId + ", attribute=" + attribute.getName());
                        stateConsumer.accept(new AttributeState(assetId, attribute.getName(), message));
                    }
                }
            }
        };
    }
}
