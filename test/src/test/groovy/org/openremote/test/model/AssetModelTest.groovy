package org.openremote.test.model

import com.fasterxml.jackson.databind.node.ObjectNode
import org.openremote.agent.protocol.http.HttpClientAgent
import org.openremote.agent.protocol.simulator.SimulatorAgent
import org.openremote.model.Constants
import org.openremote.model.asset.Asset
import org.openremote.model.asset.agent.AgentLink
import org.openremote.model.asset.impl.LightAsset
import org.openremote.model.asset.impl.ThingAsset
import org.openremote.model.attribute.Attribute
import org.openremote.model.attribute.MetaItem
import org.openremote.model.util.AssetModelUtil
import org.openremote.model.value.MetaItemType
import org.openremote.model.value.SubStringValueFilter
import org.openremote.model.value.ValueFilter
import org.openremote.model.value.Values
import org.openremote.model.value.impl.ColourRGB
import org.openremote.test.protocol.http.HttpServerTestAgent
import spock.lang.Specification

import java.util.stream.Collectors

import static org.openremote.model.attribute.Attribute.getAddedOrModifiedAttributes
import static org.openremote.model.value.ValueType.BIG_NUMBER
import static org.openremote.model.value.ValueType.STRING

// TODO: Define new asset model tests (setValue - equality checking etc.)
class AssetModelTest extends Specification {

    def "Descriptors"() {
        when: "The Asset model is explicitly initialised"
        AssetModelUtil.initialiseOrThrow()

        then: "the asset model should be available"
        def thingAssetInfo = AssetModelUtil.getAssetInfo(ThingAsset.class).orElse(null)
        thingAssetInfo != null
        thingAssetInfo.assetDescriptor != null
        thingAssetInfo.attributeDescriptors != null
        thingAssetInfo.metaItemDescriptors != null
        thingAssetInfo.valueDescriptors != null
        thingAssetInfo.attributeDescriptors.contains(Asset.LOCATION)
        thingAssetInfo.metaItemDescriptors.contains(MetaItemType.AGENT_LINK)
        AssetModelUtil.getAssetDescriptor(ThingAsset.class) != null
        AssetModelUtil.getAgentDescriptor(SimulatorAgent.class) != null

        and: "the test asset model provider should have registered test agents and assets"
        AssetModelUtil.getAgentDescriptor(HttpServerTestAgent.DESCRIPTOR.name).isPresent()
    }

    def "Serialize/Deserialize Asset"() {
        given: "An asset"
        def asset = new LightAsset("Test light")
            .setRealm(Constants.MASTER_REALM)
            .setTemperature(100I)
            .setColourRGB(new ColourRGB(50, 100, 200))
            .addAttributes(
                new Attribute<>("testAttribute", BIG_NUMBER, 100.5, System.currentTimeMillis())
                    .addOrReplaceMeta(
                        new MetaItem<>(MetaItemType.AGENT_LINK, new HttpClientAgent.HttpClientAgentLink("http_agent_id")
                            .setPath("test_path")
                            .setPagingMode(true))
                    )
            )

        asset.getAttribute(LightAsset.COLOUR_RGB).ifPresent({
            it.addOrReplaceMeta(
                new MetaItem<>(MetaItemType.AGENT_LINK, new AgentLink.Default("agent_id")
                    .setValueFilters(
                        [new SubStringValueFilter(0,10)] as ValueFilter[]
                    )
                )
            )
        })

        expect: "the attributes to match the set values"
        asset.getTemperature().orElse(null) == 100I
        asset.getColourRGB().map{it.getRed()}.orElse(null) == 50I
        asset.getColourRGB().map{it.getGreen()}.orElse(null) == 100I
        asset.getColourRGB().map{it.getBlue()}.orElse(null) == 200I

        when: "the asset is serialised using default object mapper"
        def assetStr = Values.asJSON(asset).orElse(null)

        then: "the string should be valid JSON"
        def assetObjectNode = Values.parse(assetStr, ObjectNode.class).get()
        assetObjectNode.get("name").asText() == "Test light"
        assetObjectNode.get("attributes").get("colourRGB").get("timestamp") == null
        assetObjectNode.get("attributes").get("colourRGB").get("meta").get(MetaItemType.AGENT_LINK.name).isObject()
        assetObjectNode.get("attributes").get("colourRGB").get("meta").get(MetaItemType.AGENT_LINK.name).get("id").asText() == "agent_id"
        assetObjectNode.get("attributes").get("colourRGB").get("meta").get(MetaItemType.AGENT_LINK.name).get("type").asText() == AgentLink.Default.class.getSimpleName()
        assetObjectNode.get("attributes").get("testAttribute").get("meta").get(MetaItemType.AGENT_LINK.name).isObject()
        assetObjectNode.get("attributes").get("testAttribute").get("value").decimalValue() == 100.5
        assetObjectNode.get("attributes").get("testAttribute").get("meta").get(MetaItemType.AGENT_LINK.name).get("id").asText() == "http_agent_id"
        assetObjectNode.get("attributes").get("testAttribute").get("meta").get(MetaItemType.AGENT_LINK.name).get("type").asText() == HttpClientAgent.HttpClientAgentLink.class.getSimpleName()

        when: "the asset is deserialized"
        def asset2 = Values.parse(assetStr, LightAsset.class).orElse(null)

        then: "it should match the original"
        asset.getName() == asset2.getName()
        asset2.getType() == asset.getType()
        asset2.getTemperature().orElse(null) == asset.getTemperature().orElse(null)
        asset2.getColourRGB().map{it.getRed()}.orElse(null) == asset.getColourRGB().map{it.getRed()}.orElse(null)
        asset2.getColourRGB().map{it.getGreen()}.orElse(null) == asset.getColourRGB().map{it.getGreen()}.orElse(null)
        asset2.getColourRGB().map{it.getBlue()}.orElse(null) == asset.getColourRGB().map{it.getBlue()}.orElse(null)
        asset2.getAttribute("testAttribute", BIG_NUMBER.type).flatMap{it.getMetaValue(MetaItemType.AGENT_LINK)}.orElse(null) instanceof HttpClientAgent.HttpClientAgentLink
        asset2.getAttribute("testAttribute", BIG_NUMBER.type).flatMap{it.getMetaValue(MetaItemType.AGENT_LINK)}.map{(HttpClientAgent.HttpClientAgentLink)it}.flatMap{it.path}.orElse("") == "test_path"
        asset2.getAttribute("testAttribute", BIG_NUMBER.type).flatMap{it.getMetaValue(MetaItemType.AGENT_LINK)}.map{(HttpClientAgent.HttpClientAgentLink)it}.flatMap{it.pagingMode}.orElse(false)
    }

    def "Comparing asset attributes"() {

        when: "two attributes have different value timestamps"
        def timestamp = System.currentTimeMillis()
        def timestamp2 = timestamp + 1000

        def attributeA = new Attribute<>("a", STRING, "foo", timestamp)
        def attributeB = new Attribute<>("b", STRING, "foo", timestamp2)

        then: "they should be different"
        !attributeA.getObjectValue().equalsIgnoreKeys(attributeB.getObjectValue(), null)

        and: "if we ignore the timestamp they should be equal"
        attributeA.getObjectValue().equalsIgnoreKeys(attributeB.getObjectValue(), { key -> key == VALUE_TIMESTAMP_FIELD_NAME })

        when: "an attribute has no timestamp"
        def attributeC = new Attribute<>("c", STRING, "foo")

        then: "it should be different than attributes with a timestamp"
        !attributeA.getObjectValue().equalsIgnoreKeys(attributeC.getObjectValue(), null)
        !attributeB.getObjectValue().equalsIgnoreKeys(attributeC.getObjectValue(), null)

        and: "if we ignore the timestamp they all should be equal"
        attributeA.getObjectValue().equalsIgnoreKeys(attributeC.getObjectValue(), { key -> key == VALUE_TIMESTAMP_FIELD_NAME })
        attributeB.getObjectValue().equalsIgnoreKeys(attributeC.getObjectValue(), { key -> key == VALUE_TIMESTAMP_FIELD_NAME })
    }

    def "Comparing asset attribute lists"() {

        when: "two lists of asset attributes are compared"
        def timestamp = System.currentTimeMillis()
        def attributesA = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
        ]
        def attributesB = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
                new Attribute<>("a3", STRING, "a333", timestamp),
        ]
        List<Attribute> addedOrModifiedAttributes = getAddedOrModifiedAttributes(attributesA, attributesB).collect(Collectors.toList())

        then: "they should be different"
        addedOrModifiedAttributes.size() == 1
        addedOrModifiedAttributes[0].name.get() == "a3"

        when: "two lists of asset attributes are compared, ignoring some"
        timestamp = System.currentTimeMillis()
        attributesA = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
        ]
        attributesB = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
                new Attribute<>("a3", STRING, "a333", timestamp),
        ]
        addedOrModifiedAttributes = getAddedOrModifiedAttributes(attributesA, attributesB, { name -> name == "a3" }).collect(Collectors.toList())

        then: "they should be the same"
        addedOrModifiedAttributes.size() == 0

        when: "two lists of asset attributes with different value timestamp are compared"
        timestamp = System.currentTimeMillis()
        def timestamp2 = timestamp + 1000

        attributesA = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
        ]
        attributesB = [
                new Attribute<>("a1", STRING, "a111", timestamp2),
                new Attribute<>("a2", STRING, "a222", timestamp2),
        ]
        addedOrModifiedAttributes = getAddedOrModifiedAttributes(attributesA, attributesB).collect(Collectors.toList())

        then: "they should be different"
        addedOrModifiedAttributes.size() == 2
        addedOrModifiedAttributes[0].name.get() == "a1"
        addedOrModifiedAttributes[1].name.get() == "a2"

        when: "two lists of asset attributes with different value timestamp are compared, ignoring timestamps"
        timestamp = System.currentTimeMillis()
        timestamp2 = timestamp + 1000

        attributesA = [
                new Attribute<>("a1", STRING, "a111", timestamp),
                new Attribute<>("a2", STRING, "a222", timestamp),
        ]
        attributesB = [
                new Attribute<>("a1", STRING, "a111", timestamp2),
                new Attribute<>("a2", STRING, "a222", timestamp2),
        ]
        addedOrModifiedAttributes = getAddedOrModifiedAttributes(attributesA, attributesB).collect(Collectors.toList())

        then: "they should be the same"
        addedOrModifiedAttributes.size() == 0
    }
}
