/*
 * Copyright 2017, OpenRemote Inc.
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
package org.openremote.test.model

import com.fasterxml.jackson.databind.node.ObjectNode
import org.openremote.agent.protocol.simulator.SimulatorProtocol
import org.openremote.container.Container
import org.openremote.model.Constants
import org.openremote.model.asset.Asset
import org.openremote.model.asset.agent.AgentLink
import org.openremote.model.asset.impl.LightAsset
import org.openremote.model.attribute.AttributeValidationFailure
import org.openremote.model.attribute.Attribute
import org.openremote.model.attribute.AttributeValidationResult
import org.openremote.model.attribute.MetaItem
import org.openremote.model.value.MetaItemType
import org.openremote.model.value.SubStringValueFilter
import org.openremote.model.value.ValueFilter
import org.openremote.model.value.Values
import org.openremote.model.value.impl.ColourRGB
import spock.lang.Specification

class SerialisationTest extends Specification {

    def "Serialize/Deserialize Asset"() {
        given: "An asset"
        def asset = new LightAsset("Test light")
            .setRealm(Constants.MASTER_REALM)
            .setTemperature(100I)
            .setColourRGB(new ColourRGB(50, 100, 200))
        asset.getAttribute(LightAsset.COLOUR_RGB).ifPresent({
            it.addOrReplaceMeta(
                new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink.Default("agent_id")
                    .setValueFilters(
                        [new SubStringValueFilter(0,10)] as ValueFilter[]
                    )
                )
            )
        })

        when: "the asset is serialised using default object mapper"
        def assetStr = Values.asJSON(asset).orElse(null)

        then: "the string should be valid JSON"
        def assetObjectNode = Values.parse(assetStr, ObjectNode.class).get()
        assetObjectNode.get("name").asText() == "Test light"

        when: "the asset is deserialized"
        def asset2 = Values.parse(assetStr, Asset.class).orElse(null)

        then: "it should match the original"
        asset.getName() == asset2.getName()
        asset2.getType() == asset.getType()
    }

    def "Serialize/Deserialize Attribute"() {
        given:
        Attribute attribute = new Attribute<>("testAttribute")
        ProtocolConfiguration.initProtocolConfiguration(attribute, SimulatorProtocol.PROTOCOL_NAME)
        attribute.addMeta(
            new MetaItem<>(READ_ONLY, true),
            new MetaItem<>(LABEL, "Test Attribute")
        )

        String str = Values.asJSON(attribute)
        attribute = Container.JSON.readValue(str, Attribute.class)

        expect:
        attribute.getName().isPresent()
        attribute.getName().get().equals("testAttribute")
        attribute.getValue().isPresent()
        attribute.getValue().get().equals(SimulatorProtocol.PROTOCOL_NAME)
        attribute.getMetaItem(PROTOCOL_CONFIGURATION).isPresent()
        attribute.getMetaItem(PROTOCOL_CONFIGURATION).get().getValueAsBoolean().isPresent()
        attribute.getMetaItem(PROTOCOL_CONFIGURATION).flatMap{it.value}.orElse(false)
        attribute.getMetaItem(READ_ONLY).isPresent()
        attribute.getMetaItem(READ_ONLY).get().getValueAsBoolean().isPresent()
        attribute.getMetaItem(READ_ONLY).flatMap{it.value}.orElse(false)
        attribute.getMetaItem(LABEL).isPresent()
        attribute.getMetaItem(LABEL).get().getValue().isPresent()
        attribute.getMetaItem(LABEL).get().getValue().get().equals("Test Attribute")
    }

    def "Serialize/Deserialize AttributeValidationResult"() {
        given:
        AttributeValidationResult result = new AttributeValidationResult(
            "myAttribute",
            [
                new AttributeValidationFailure(ValueHolder.ValueFailureReason.VALUE_INVALID, "Test")
            ],
            [
                1: [new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_DUPLICATION)],
                2: [new AttributeValidationFailure(MetaItem.MetaItemFailureReason.META_ITEM_MISSING, "my:meta")]
            ]
        )

        String str = Values.asJSON(result)
        result = Container.JSON.readValue(str, AttributeValidationResult.class)

        expect:
        !result.isValid()
        result.getAttributeFailures().size() == 1
        result.getAttributeFailures().get(0).reason.name() == ValueHolder.ValueFailureReason.VALUE_INVALID.name()
        result.getAttributeFailures().get(0).parameter.isPresent()
        result.getAttributeFailures().get(0).parameter.get().equals("Test")
        result.getMetaFailures().size() == 2
        result.getMetaFailures()[1].size() == 1
        result.getMetaFailures()[2].size() == 1
        result.getMetaFailures()[1].get(0).reason.name() == MetaItem.MetaItemFailureReason.META_ITEM_DUPLICATION.name()
        !result.getMetaFailures()[1].get(0).parameter.isPresent()
        result.getMetaFailures()[2].get(0).reason.name() == MetaItem.MetaItemFailureReason.META_ITEM_MISSING.name()
        result.getMetaFailures()[2].get(0).parameter.isPresent()
        result.getMetaFailures()[2].get(0).parameter.get().equals("my:meta")
    }
}
