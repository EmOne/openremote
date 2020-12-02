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
package org.openremote.test.assets

import org.openremote.model.asset.AssetModelResource
import org.openremote.model.value.ValueType
import org.openremote.test.ManagerContainerTrait
import spock.lang.Shared
import spock.lang.Specification

import static org.openremote.model.Constants.MASTER_REALM

class AssetModelResourceTest extends Specification implements ManagerContainerTrait {

    @Shared
    static AssetModelResource assetModelResource

    def setupSpec() {
        def container = startContainer(defaultConfig(), defaultServices())
        assetModelResource = getClientApiTarget(serverUri(serverPort), MASTER_REALM).proxy(AssetModelResource.class)
    }

    def "Request types"() {

        when: "a request for Asset types is made"
        def assetDescriptors = assetModelResource.getAssetDescriptors(null)

        then: "the default asset types should be present"
        assetDescriptors.size() == AssetType.values().length
        assetDescriptors.any{it.name == BUILDING.name && it.attributeDescriptors.length == 5 && it.attributeDescriptors.find {it.attributeName == AttributeType.SURFACE_AREA.attributeName}.valueType.valueType == NUMBER}
        assetDescriptors.any{it.name == CITY.name}
        assetDescriptors.any{it.name == AREA.name}
        assetDescriptors.any{it.name == FLOOR.name}
        assetDescriptors.any{it.name == RESIDENCE.name}
        assetDescriptors.any{it.name == ROOM.name}
        assetDescriptors.any{it.name == AGENT.name}
        assetDescriptors.any{it.name == CONSOLE.name}
        assetDescriptors.any{it.name == MICROPHONE.name}
        assetDescriptors.any{it.name == SOUND_EVENT.name}
        assetDescriptors.any{it.name == ENVIRONMENT_SENSOR.name}
        assetDescriptors.any{it.name == LIGHT.name}
        assetDescriptors.any{it.name == LIGHT_CONTROLLER.name}
        assetDescriptors.any{it.name == PEOPLE_COUNTER.name}
        assetDescriptors.any{it.name == THING.name}
        assetDescriptors.any{it.name == THING.name}

        when: "a request for Attribute types is made"
        def attributeTypeDescriptors = assetModelResource.getAttributeDescriptors(null)

        then: "the default types should be present"
        attributeTypeDescriptors.size() == AttributeType.values().length
        attributeTypeDescriptors.any {it.attributeName == "consoleName"}
        attributeTypeDescriptors.any {it.attributeName == "consoleVersion"}
        attributeTypeDescriptors.any {it.attributeName == "consolePlatform"}
        attributeTypeDescriptors.any {it.attributeName == "consoleProviders"}
        attributeTypeDescriptors.any {it.attributeName == "email"}
        attributeTypeDescriptors.any {it.attributeName == "city"}
        attributeTypeDescriptors.any {it.attributeName == "country"}
        attributeTypeDescriptors.any {it.attributeName == "postalCode"}
        attributeTypeDescriptors.any {it.attributeName == "street"}
        attributeTypeDescriptors.any {it.attributeName == "location"}
        attributeTypeDescriptors.any {it.attributeName == "surfaceArea"}
        attributeTypeDescriptors.any {it.attributeName == "assetStatus"}
        attributeTypeDescriptors.any {it.attributeName == "assetTags"}

        when: "a request for Attribute value types is made"
        def attributeValueTypeDescriptors = assetModelResource.getAttributeValueDescriptors(null)

        then: "the default value types should be present"
        attributeValueTypeDescriptors.size() == ValueType.values().length

        when: "a request for Attribute value types is made"
        def metaItemDescriptors = assetModelResource.getMetaItemDescriptors(null)

        then: "the default MetaItem types should be present"
        metaItemDescriptors.size() == MetaItemType.values().length
    }
}
