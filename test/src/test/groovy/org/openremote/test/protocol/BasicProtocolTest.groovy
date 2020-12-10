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
package org.openremote.test.protocol


import org.openremote.manager.agent.AgentService
import org.openremote.manager.asset.AssetProcessingService
import org.openremote.manager.asset.AssetStorageService
import org.openremote.model.asset.agent.ConnectionStatus
import org.openremote.model.asset.impl.ThingAsset
import org.openremote.model.attribute.*
import org.openremote.model.value.RegexValueFilter
import org.openremote.model.value.SubStringValueFilter
import org.openremote.model.value.ValueFilter
import org.openremote.model.value.Values
import org.openremote.test.ManagerContainerTrait
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import static org.openremote.model.Constants.MASTER_REALM
import static org.openremote.model.value.ValueType.*
import static org.openremote.model.value.MetaItemType.*

/**
 * This tests the basic protocol interface and abstract protocol implementation.
 */
class BasicProtocolTest extends Specification implements ManagerContainerTrait {

    def "Check abstract protocol linking/un-linking"() {

        given: "expected conditions"
        def conditions = new PollingConditions(timeout: 10, delay: 0.2)

        and: "a mock protocol"
        def mockProtocolName = "Mock Protocol"
        Map<String, Integer> protocolExpectedLinkedAttributeCount = [:]
        protocolExpectedLinkedAttributeCount["mockAgent1"] = 5
        protocolExpectedLinkedAttributeCount["mockAgent2"] = 2
        protocolExpectedLinkedAttributeCount["mockAgent3"] = 2
        protocolExpectedLinkedAttributeCount['mockConfig4'] = 2

        and: "the container is started"
        def assetStorageService = container.getService(AssetStorageService.class)
        def agentService = container.getService(AgentService.class)
        def assetProcessingService = container.getService(AssetProcessingService.class)

        when: "several mock agents that uses the mock protocol are created"
        def mockAgent1 = new MockAgent("Mock agent 1")
            .setRealm(MASTER_REALM)
            .setRequired(true)
        mockAgent1 = assetStorageService.merge(mockAgent1)

        def mockAgent2 = new MockAgent("Mock agent 2")
            .setRealm(MASTER_REALM)
            .setRequired(true)
            .setDisabled(true)
        mockAgent2 = assetStorageService.merge(mockAgent2)

        def mockAgent3 = new MockAgent("Mock agent 3")
            .setRealm(MASTER_REALM)
        mockAgent3 = assetStorageService.merge(mockAgent3)

        then: "the protocol instances should have been created and the agent status attributes should be updated"
        conditions.eventually {
            assert agentService.agentMap.size() == 3
            assert agentService.protocolInstanceMap.size() == 2
            assert agentService.getAgent(mockAgent1.id) != null
            assert agentService.getAgent(mockAgent2.id) != null
            assert agentService.getAgent(mockAgent3.id) != null
            assert agentService.getProtocolInstance(mockAgent1.id) != null
            assert agentService.getProtocolInstance(mockAgent2.id) == null
            assert agentService.getProtocolInstance(mockAgent3.id) != null
            assert agentService.getAgent(mockAgent1.id).getAgentStatus().orElse(null) == ConnectionStatus.CONNECTED
            assert agentService.getAgent(mockAgent2.id).getAgentStatus().orElse(null) == ConnectionStatus.DISABLED
            assert agentService.getAgent(mockAgent3.id).getAgentStatus().orElse(null) == ConnectionStatus.ERROR
        }

        when: "a mock thing asset is created that links to the mock agents"
        def mockThing = new ThingAsset("Mock Thing Asset")
            .setRealm(MASTER_REALM)
        
        mockThing.addOrReplaceAttributes(
            new Attribute<>("lightToggle1", BOOLEAN)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("tempTarget1", NUMBER)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("invalidToggle1", BOOLEAN)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                    )
                ),
            new Attribute<>("lightToggle2", BOOLEAN)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent2.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("tempTarget2", NUMBER)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent2.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("lightToggle3", BOOLEAN)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent3.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("tempTarget3", NUMBER)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent3.id)
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("invalidToggle5", BOOLEAN, false)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink("invalid id")
                            .setRequiredValue(true)
                    )
                ),
            new Attribute<>("plainAttribute", STRING, "demo")
                .addOrReplaceMeta(
                    new MetaItem<>(READ_ONLY, true)
                ),
            new Attribute<>("filterRegex", NUMBER)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                            .setRequiredValue(true)
                            .setValueFilters(
                                [
                                    new RegexValueFilter("\\w(\\d+)", 1, 2)
                                ] as ValueFilter[]
                            )
                    )
                ),
            new Attribute<>("filterSubstring", STRING)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                            .setRequiredValue(true)
                        .setValueFilters(
                            [
                                new SubStringValueFilter(10, 12)
                            ] as ValueFilter[]
                        )
                    )
                ),
            new Attribute<>("filterRegexSubstring", NUMBER)
                .addOrReplaceMeta(
                    new MetaItem<>(
                        AGENT_LINK,
                        new MockAgent.MockAgentLink(mockAgent1.id)
                            .setRequiredValue(true)
                            .setValueFilters(
                                [
                                    new SubStringValueFilter(23),
                                    new RegexValueFilter("[a-z|\\s]+(\\d+)%\"}", 1, 0)
                                ] as ValueFilter[]
                            )
                        )
            )
        )

        mockThing = assetStorageService.merge(mockThing)

        then: "the mock thing to be fully deployed"
        conditions.eventually {
            assert agentService.getProtocolInstance(mockAgent1.id).linkedAttributes.size() == protocolExpectedLinkedAttributeCount["mockAgent1"]
            assert agentService.getProtocolInstance(mockAgent2.id).linkedAttributes.size() == protocolExpectedLinkedAttributeCount["mockAgent2"]
            assert agentService.getProtocolInstance(mockAgent1.id).linkedAttributes.size() == protocolExpectedLinkedAttributeCount["mockAgent3"]
        }

        and: "the deployment should have occurred in the correct order"
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent1.id)).protocolMethodCalls.size() == 3
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent1.id)).protocolMethodCalls.get(0) == "START"
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent1.id)).protocolMethodCalls.get(1).startsWith("LINK ATTRIBUTE")
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent1.id)).protocolMethodCalls.get(2).startsWith("LINK ATTRIBUTE")
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent2.id)).protocolMethodCalls.size() == 3
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent2.id)).protocolMethodCalls.get(0) == "START"
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent2.id)).protocolMethodCalls.get(1).startsWith("LINK ATTRIBUTE")
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent2.id)).protocolMethodCalls.get(2).startsWith("LINK ATTRIBUTE")
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent3.id)).protocolMethodCalls.size() == 3
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent3.id)).protocolMethodCalls.get(0) == "START"
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent3.id)).protocolMethodCalls.get(1).startsWith("LINK ATTRIBUTE")
        assert ((MockProtocol)agentService.getProtocolInstance(mockAgent3.id)).protocolMethodCalls.get(2).startsWith("LINK ATTRIBUTE")

        and: "the linked attributes values should have been updated by the protocol"
        conditions.eventually {
            def mockAsset = assetStorageService.find(mockThing.getId(), true)
            // Check all valid linked attributes have the new values
            assert mockAsset.getAttributes().<Boolean>getValue("lightToggle1").orElse(false)
            assert mockAsset.getAttribute("tempTarget1", Double.class).flatMap{it.getValue()}.orElse(0d) == 25.5d
            // Check disabled linked attributes don't have the new values
            assert !mockAsset.getAttribute("lightToggle4", Boolean.class).get().getValue().isPresent()
            assert !mockAsset.getAttribute("tempTarget4", Double.class).get().getValue().isPresent()
            // Check invalid attributes don't have the new values
            assert !mockAsset.getAttribute("lightToggle2", Boolean.class).get().getValue().isPresent()
            assert !mockAsset.getAttribute("tempTarget2", Double.class).get().getValue().isPresent()
            assert !mockAsset.getAttribute("lightToggle3", Boolean.class).get().getValue().isPresent()
            assert !mockAsset.getAttribute("tempTarget3", Double.class).get().getValue().isPresent()
        }

        when: "a linked attribute is removed"
        mockThing = assetStorageService.find(mockThing.getId(), true)
        protocolMethodCalls.clear()
        mockThing.removeAttribute("tempTarget3")
        mockThing = assetStorageService.merge(mockThing)

        then: "the protocol should not be unlinked"
        conditions.eventually {
            assert protocolLinkedAttributes["mockAgent3"].size() == 1
            assert protocolMethodCalls.size() == 1
            assert protocolMethodCalls[0] == "UNLINK_ATTRIBUTE"
        }

        when: "a protocol configuration is removed"
        protocolMethodCalls.clear()
        mockAgent.removeAttribute("mockConfig3")
        mockAgent = assetStorageService.merge(mockAgent)

        then: "the attributes should be unlinked then the protocol configuration"
        conditions.eventually {
            assert protocolLinkedAttributes["mockConfig3"].size() == 0
            assert protocolMethodCalls.size() == 2
            assert protocolMethodCalls[0] == "UNLINK_ATTRIBUTE"
            assert protocolMethodCalls[1] == "UNLINK_PROTOCOL"
        }

        when: "the mock protocol tries to update the plain readonly attribute"
        mockProtocol.updateAttribute(new AttributeState(mockThing.getId(),"plainAttribute", "UPDATE"))

        then: "the plain attributes value should be updated"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("plainAttribute").get().getValue().orElse("") == "UPDATE"
        }

        when: "a target temp linked attribute value is updated it should reach the protocol"
        assetProcessingService.sendAttributeEvent(new AttributeEvent(mockThing.getId(), "tempTarget1", 30d))

        then: "the update should reach the protocol as an attribute write request"
        conditions.eventually {
            assert protocolWriteAttributeEvents.size() == 1
            assert protocolWriteAttributeEvents[0].attributeName == "tempTarget1"
            assert protocolWriteAttributeEvents[0].attributeRef.assetId == mockThing.getId()
            Values.getNumber(protocolWriteAttributeEvents[0].value.orElse(null)).orElse(0d) == 30d
        }

        when: "the protocol has finished processing the attribute write"
        def state = mockProtocol.protocolWriteAttributeEvents.last().getAttributeState()
        mockProtocol.updateReceived(state)

        then: "the target temp attributes value should be updated"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("tempTarget1").get().getValueAsNumber().orElse(0d) == 30d
        }

        when: "a sensor value is received that links to an attribute using a regex filter"
        state = new AttributeState(mockThing.id, "filterRegex", "s100 d56 g1212")
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated with the filtered result"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("filterRegex").get().getValueAsNumber().orElse(0d) == 1212d
        }

        when: "the same attribute receives a sensor value that doesn't match the regex filter (match index invalid)"
        state = new AttributeState(mockThing.id, "filterRegex", "s100")
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated to null"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert !mockThing.getAttribute("filterRegex").get().getValueAsNumber().isPresent()
        }

        when: "the same attribute receives a sensor value that doesn't match the regex filter (no match)"
        def lastUpdate = mockThing.getAttribute("filterRegex").get().timestamp.get()
        state = new AttributeState(mockThing.id, "filterRegex", "no match to be found!")
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated to null"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("filterRegex").get().valueTimestamp.get() > lastUpdate
            assert !mockThing.getAttribute("filterRegex").get().getValueAsNumber().isPresent()
        }

        when: "a sensor value is received that links to an attribute using a substring filter"
        state = new AttributeState(mockThing.id, "filterSubstring", "Substring test value")
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated with the filtered result"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("filterSubstring").get().getValue().orElse(null) == "te"
        }

        when: "the same attribute receives a sensor value that doesn't match the substring filter"
        state = new AttributeState(mockThing.id, "filterSubstring", "Substring")
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated to null"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert !mockThing.getAttribute("filterSubstring").get().getValue().isPresent()
        }

        when: "a sensor value is received that links to an attribute using a regex and substring filter"
        state = new AttributeState(mockThing.id, "filterRegexSubstring", '{"prop1":true,"prop2":"volume is at 90%"}')
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated with the filtered result"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert mockThing.getAttribute("filterRegexSubstring").get().getValueAsNumber().orElse(0d) == 90d
        }

        when: "the same attribute receives a sensor value that doesn't match the substring filter"
        state = new AttributeState(mockThing.id, "filterRegexSubstring", '"volume is at 90%"}')
        mockProtocol.updateReceived(state)

        then: "the linked attributes value should be updated to null"
        conditions.eventually {
            mockThing = assetStorageService.find(mockThing.getId(), true)
            assert !mockThing.getAttribute("filterRegexSubstring").get().getValueAsNumber().isPresent()
        }

        when: "the disabled protocol configuration is enabled"
        protocolMethodCalls.clear()
        mockAgent.getAttribute("mockConfig4").ifPresent({it.meta.removeIf({it.name.get() == DISABLED.urn})})
        mockAgent = assetStorageService.merge(mockAgent)

        then: "the newly enabled protocol configuration should be unlinked and re-linked"
        conditions.eventually {
            assert protocolMethodCalls.size() == 3
            assert protocolMethodCalls[0] == "LINK_PROTOCOL"
            assert protocolMethodCalls[1] == "LINK_ATTRIBUTE"
            assert protocolMethodCalls[2] == "LINK_ATTRIBUTE"
        }
    }
}
