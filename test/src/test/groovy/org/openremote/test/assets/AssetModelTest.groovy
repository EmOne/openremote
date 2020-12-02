package org.openremote.test.assets

import org.openremote.model.attribute.Attribute
import spock.lang.Specification

import java.util.stream.Collectors

import static org.openremote.model.attribute.Attribute.getAddedOrModifiedAttributes
import static org.openremote.model.value.ValueType.*

// TODO: Define new asset model tests (setValue - equality checking etc.)
class AssetModelTest extends Specification {

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
