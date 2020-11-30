package org.openremote.test.model

import org.openremote.model.value.ArrayValue
import org.openremote.model.value.ObjectValue
import org.openremote.model.value.Value
import org.openremote.model.value.Values
import spock.lang.Specification

class ModelValueTest extends Specification {

    def "Read and write JSON"() {
        expect:
        ObjectValue sampleObject1 = Values.JSON.createObjectNode()
        sampleObject1.put("object1A", "O1-AAA")
        sampleObject1.put("object1B", 456)

        ArrayValue sampleArray1 = Values.createArray()
        sampleArray1.set(0, "A0")
        sampleArray1.set(1, "A1")
        sampleArray1.set(2, true)
        sampleArray1.set(3, 123.45)
        sampleArray1.set(4, sampleObject1)

        String rawValue = sampleArray1.toJson()
        println rawValue
        ArrayValue parsedValue = Values.<ArrayValue>parse(rawValue).get()
        parsedValue == sampleArray1
    }

    def "Null support"() {
        expect:
        def sampleObject = Values.JSON.createObjectNode().put("prop", (Value)null)
        sampleObject.toJson() == '{"prop":null}'
        sampleObject.put("prop1", Values.JSON.createObjectNode().put("prop2", Values.<ObjectValue>parse('{"prop3":1234,"prop4":{"prop5":null,"prop6":true}}').get()))
        sampleObject.getObject("prop1").flatMap({it.getObject("prop2").flatMap({it.getObject("prop4").map({it.keyContainsNull("prop5")})})}).get() == true
        sampleObject.getObject("prop1").flatMap({it.getObject("prop2").flatMap({it.getObject("prop4").map({it.keyContainsNull("prop6")})})}).get() == false
        sampleObject.toJson() == '{"prop":null,"prop1":{"prop2":{"prop3":1234,"prop4":{"prop5":null,"prop6":true}}}}'
        def sampleArr = Values.createArray().add(null);
        sampleArr.toJson() == '[null]'
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
        ObjectValue sampleObject1 = Values.JSON.createObjectNode()
        sampleObject1.put("object1A", "O1-AAA")
        sampleObject1.put("object1B", 456)

        ObjectValue sampleObject2 = Values.JSON.createObjectNode()
        sampleObject2.put("object2A", "O2-AAA")
        sampleObject2.put("object2B", 789)

        ArrayValue sampleArray1 = Values.createArray()
        sampleArray1.set(0, "A0")
        sampleArray1.set(1, "A1")
        sampleArray1.set(2, sampleObject1)

        ArrayValue sampleArray2 = Values.createArray()
        sampleArray1.set(0, sampleObject1)
        sampleArray1.set(1, "A1")
        sampleArray1.set(2, "A0")

        expect: "Array equality comparisons to be correct"
        Values.createArray() == Values.createArray()
        Values.createArray().hashCode() == Values.createArray().hashCode()

        sampleArray1 == sampleArray1
        sampleArray1.hashCode() == sampleArray1.hashCode()

        sampleArray2 != sampleArray1
        sampleArray2.hashCode() != sampleArray1.hashCode()

        sampleArray1 != sampleArray2
        sampleArray1.hashCode() != sampleArray2.hashCode()

        and: "Objects with no fields to be equal"
        Values.JSON.createObjectNode() == Values.JSON.createObjectNode()
        Values.JSON.createObjectNode().hashCode() == Values.JSON.createObjectNode().hashCode()

        Values.createArray() != Values.JSON.createObjectNode()
        Values.createArray().hashCode() != Values.JSON.createObjectNode().hashCode()

        and: "Objects with the same fields to be equal"
        ObjectValue sameFields1 = Values.JSON.createObjectNode()
        sameFields1.put("fieldA", "AAA")
        sameFields1.put("fieldB", 123)
        sameFields1.put("fieldC", true)
        sameFields1.put("fieldD", sampleArray1)
        ObjectValue sameFields2 = Values.JSON.createObjectNode()
        sameFields2.put("fieldD", sampleArray1)
        sameFields2.put("fieldC", true)
        sameFields2.put("fieldB", 123)
        sameFields2.put("fieldA", "AAA")
        sameFields1 == sameFields2
        sameFields1.hashCode() == sameFields2.hashCode()

        and: "Objects with the different fields to be not equal"
        ObjectValue otherFields1 = Values.JSON.createObjectNode()
        otherFields1.put("fieldA", "AAA")
        ObjectValue otherFields2 = Values.JSON.createObjectNode()
        otherFields2.put("fieldB", 123)
        otherFields1 != otherFields2
        otherFields1.hashCode() != otherFields2.hashCode()

        and: "Objects with the different field values to be not equal"
        ObjectValue differentValues1 = Values.JSON.createObjectNode()
        differentValues1.put("fieldA", "AAA")
        ObjectValue differentValues2 = Values.JSON.createObjectNode()
        differentValues2.put("fieldA", "AAAAA")
        differentValues1 != differentValues2
        differentValues1.hashCode() != differentValues2.hashCode()
    }

}
