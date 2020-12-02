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

import org.openremote.agent.protocol.simulator.SimulatorProtocol
import org.openremote.container.Container
import org.openremote.model.attribute.AttributeValidationFailure
import org.openremote.model.attribute.Attribute
import org.openremote.model.attribute.AttributeValidationResult
import org.openremote.model.attribute.MetaItem
import org.openremote.model.value.Values
import spock.lang.Specification

class SerialisationTest extends Specification {

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
