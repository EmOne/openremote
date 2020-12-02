package org.openremote.test.model

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.openremote.model.value.Values
import spock.lang.Specification

class ModelValueTest extends Specification {

    def "Read and write JSON"() {
        expect:
        ObjectNode sampleObject1 = Values.JSON.createObjectNode()
        sampleObject1.put("object1A", "O1-AAA")
        sampleObject1.put("object1B", 456)

        ArrayNode sampleArray1 = Values.JSON.createArrayNode()
        sampleArray1.add("A0")
        sampleArray1.add("A1")
        sampleArray1.add(true)
        sampleArray1.add(123.45)
        sampleArray1.add(sampleObject1)

        String rawValue = Values.asJSON(sampleArray1)
        ArrayNode parsedValue = (ArrayNode)Values.parse(rawValue).get()
        parsedValue == sampleArray1
    }

    def "Null support"() {
        expect:
        def sampleObject = Values.JSON.createObjectNode().put("prop", null)
        Values.asJSON(sampleObject).orElse(null) == '{"prop":null}'
        sampleObject.set("prop1", Values.JSON.createObjectNode().put("prop2", Values.parse('{"prop3":1234,"prop4":{"prop5":null,"prop6":true}}').get()))
        sampleObject.get("prop1").get("prop2").get("prop4").get("prop5") == null
        sampleObject.get("prop1").get("prop2").get("prop4").get("prop6").asBoolean()
        def sampleArr = Values.JSON.createArrayNode().add(null)
        Values.asJSON(sampleArr) == '[null]'
    }

    def "Compare scalar values"() {
        expect:
        true == true
        true.hashCode() == true.hashCode()

        false != true
        false.hashCode() != true.hashCode()

        true != false
        true.hashCode() != false.hashCode()

        123 != false
        123.hashCode() != false.hashCode()

        0 != false
        0.hashCode() != false.hashCode()

        1 != true
        1.hashCode() != true.hashCode()

        123 == 123
        123.hashCode() == 123.hashCode()

        123 != 456
        123.hashCode() != 456.hashCode()

        456 != 123
        456.hashCode() != 123.hashCode()

        0 != ""
        0.hashCode() != "".hashCode()

        "abc" == "abc"
        "abc".hashCode() == "abc".hashCode()

        "abcd" != "abc"
        "abcd".hashCode() != "abc".hashCode()

        "abc" != "abcd"
        "abc".hashCode() != "abcd".hashCode()

        "abc" != 123
        "abc".hashCode() != 123.hashCode()

        "" != 0
        "".hashCode() != 0.hashCode()
    }

    def "Compare non-scalar Json values"() {

        given:
        ObjectNode sampleObject1 = Values.JSON.createObjectNode()
        sampleObject1.put("object1A", "O1-AAA")
        sampleObject1.put("object1B", 456)

        ObjectNode sampleObject2 = Values.JSON.createObjectNode()
        sampleObject2.put("object2A", "O2-AAA")
        sampleObject2.put("object2B", 789)

        ArrayNode sampleArray1 = Values.JSON.createArrayNode()
        sampleArray1.set(0, "A0")
        sampleArray1.set(1, "A1")
        sampleArray1.set(2, sampleObject1)

        ArrayNode sampleArray2 = Values.JSON.createArrayNode()
        sampleArray1.set(0, sampleObject1)
        sampleArray1.set(1, "A1")
        sampleArray1.set(2, "A0")

        expect: "Array equality comparisons to be correct"
        Values.JSON.createArrayNode() == Values.JSON.createArrayNode()
        Values.JSON.createArrayNode().hashCode() == Values.JSON.createArrayNode().hashCode()

        sampleArray1 == sampleArray1
        sampleArray1.hashCode() == sampleArray1.hashCode()

        sampleArray2 != sampleArray1
        sampleArray2.hashCode() != sampleArray1.hashCode()

        sampleArray1 != sampleArray2
        sampleArray1.hashCode() != sampleArray2.hashCode()

        and: "Objects with no fields to be equal"
        Values.JSON.createObjectNode() == Values.JSON.createObjectNode()
        Values.JSON.createObjectNode().hashCode() == Values.JSON.createObjectNode().hashCode()

        Values.JSON.createArrayNode() != Values.JSON.createObjectNode()
        Values.JSON.createArrayNode().hashCode() != Values.JSON.createObjectNode().hashCode()

        and: "Objects with the same fields to be equal"
        ObjectNode sameFields1 = Values.JSON.createObjectNode()
        sameFields1.put("fieldA", "AAA")
        sameFields1.put("fieldB", 123)
        sameFields1.put("fieldC", true)
        sameFields1.put("fieldD", sampleArray1)
        ObjectNode sameFields2 = Values.JSON.createObjectNode()
        sameFields2.put("fieldD", sampleArray1)
        sameFields2.put("fieldC", true)
        sameFields2.put("fieldB", 123)
        sameFields2.put("fieldA", "AAA")
        sameFields1 == sameFields2
        sameFields1.hashCode() == sameFields2.hashCode()

        and: "Objects with the different fields to be not equal"
        ObjectNode otherFields1 = Values.JSON.createObjectNode()
        otherFields1.put("fieldA", "AAA")
        ObjectNode otherFields2 = Values.JSON.createObjectNode()
        otherFields2.put("fieldB", 123)
        otherFields1 != otherFields2
        otherFields1.hashCode() != otherFields2.hashCode()

        and: "Objects with the different field values to be not equal"
        ObjectNode differentValues1 = Values.JSON.createObjectNode()
        differentValues1.put("fieldA", "AAA")
        ObjectNode differentValues2 = Values.JSON.createObjectNode()
        differentValues2.put("fieldA", "AAAAA")
        differentValues1 != differentValues2
        differentValues1.hashCode() != differentValues2.hashCode()
    }

}
