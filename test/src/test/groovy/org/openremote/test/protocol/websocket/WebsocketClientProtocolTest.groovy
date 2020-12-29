/*
 * Copyright 2019, OpenRemote Inc.
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
package org.openremote.test.protocol.websocket

import org.openremote.agent.protocol.simulator.SimulatorProtocol
import org.openremote.agent.protocol.websocket.WebsocketClientAgent
import org.openremote.agent.protocol.websocket.WebsocketClientProtocol
import org.openremote.agent.protocol.websocket.WebsocketHttpSubscription
import org.openremote.agent.protocol.websocket.WebsocketSubscription
import org.openremote.manager.agent.AgentService
import org.openremote.manager.asset.AssetProcessingService
import org.openremote.manager.asset.AssetStorageService
import org.openremote.manager.setup.SetupService
import org.openremote.manager.setup.builtin.ManagerTestSetup
import org.openremote.model.Constants
import org.openremote.model.asset.AssetFilter
import org.openremote.model.asset.ReadAttributeEvent
import org.openremote.model.asset.agent.Agent
import org.openremote.model.asset.agent.ConnectionStatus
import org.openremote.model.asset.agent.Protocol
import org.openremote.model.asset.impl.ThingAsset
import org.openremote.model.attribute.Attribute
import org.openremote.model.attribute.AttributeEvent
import org.openremote.model.attribute.MetaItem
import org.openremote.model.auth.OAuthPasswordGrant
import org.openremote.model.event.TriggeredEventSubscription
import org.openremote.model.event.shared.EventSubscription
import org.openremote.model.event.shared.SharedEvent
import org.openremote.model.query.AssetQuery
import org.openremote.model.query.filter.StringPredicate
import org.openremote.model.value.JsonPathFilter
import org.openremote.model.value.SubStringValueFilter
import org.openremote.model.value.ValueFilter
import org.openremote.model.value.Values
import org.openremote.test.ManagerContainerTrait
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.ws.rs.HttpMethod
import javax.ws.rs.client.ClientRequestContext
import javax.ws.rs.client.ClientRequestFilter
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import static org.openremote.container.util.MapAccess.getString
import static org.openremote.manager.security.ManagerIdentityProvider.SETUP_ADMIN_PASSWORD
import static org.openremote.manager.security.ManagerIdentityProvider.SETUP_ADMIN_PASSWORD_DEFAULT
import static org.openremote.model.Constants.KEYCLOAK_CLIENT_ID
import static org.openremote.model.Constants.MASTER_REALM_ADMIN_USER
import static org.openremote.model.value.MetaItemType.AGENT_LINK
import static org.openremote.model.value.ValueType.NUMBER

class WebsocketClientProtocolTest extends Specification implements ManagerContainerTrait {

    @Shared
    def mockServer = new ClientRequestFilter() {

        private boolean subscriptionDone = false

        @Override
        void filter(ClientRequestContext requestContext) throws IOException {
            def requestUri = requestContext.uri
            def requestPath = requestUri.scheme + "://" + requestUri.host + requestUri.path

            // Check auth header is present
            def authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION)
            if (authHeader == null || authHeader.length() < 8) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build())
                return
            }

            switch (requestPath) {

                case "https://mockapi/assets":
                    if (requestContext.method == HttpMethod.POST
                        && requestContext.getHeaderString("header1") == "header1Value1"
                        && requestContext.getHeaderString("header2") == "header2Value1,header2Value2"
                        && requestContext.getHeaderString("Content-type") == MediaType.APPLICATION_JSON) {

                        String bodyStr = (String)requestContext.getEntity()
                        AssetQuery assetQuery = Values.JSON.readValue(bodyStr, AssetQuery.class)
                        if (assetQuery != null && assetQuery.ids != null && assetQuery.ids.size() == 1) {
                            subscriptionDone = true
                            requestContext.abortWith(Response.ok().build())
                            return
                        }
                    }
                    break
            }

            requestContext.abortWith(Response.serverError().build())
        }
    }

    @SuppressWarnings("GroovyAccessibility")
    def "Check websocket client protocol configuration and linked attribute deployment"() {

        given: "expected conditions"
        def conditions = new PollingConditions(timeout: 10, delay: 0.2)

        and: "the container starts"
        def container = startContainer(defaultConfig(), defaultServices())
        def assetStorageService = container.getService(AssetStorageService.class)
        def assetProcessingService = container.getService(AssetProcessingService.class)
        def agentService = container.getService(AgentService.class)
        def managerTestSetup = container.getService(SetupService.class).getTaskOfType(ManagerTestSetup.class)

        when: "the web target builder is configured to use the mock HTTP server (to test subscriptions)"
        if (!WebsocketClientProtocol.resteasyClient.configuration.isRegistered(mockServer)) {
            WebsocketClientProtocol.resteasyClient.register(mockServer, Integer.MAX_VALUE)
        }

        and: "an agent with a websocket client protocol configuration is created to connect to this tests manager"
        def agent = new WebsocketClientAgent("Test agent")
            .setRealm(Constants.MASTER_REALM)
            .setConnectUri("ws://127.0.0.1:$serverPort/websocket/events?Auth-Realm=master")
            .setOAuthGrant(new OAuthPasswordGrant("http://127.0.0.1:$serverPort/auth/realms/master/protocol/openid-connect/token",
                KEYCLOAK_CLIENT_ID,
                null,
                null,
                MASTER_REALM_ADMIN_USER,
                getString(container.getConfig(), SETUP_ADMIN_PASSWORD, SETUP_ADMIN_PASSWORD_DEFAULT)))
            .setConnectSubscriptions([
                new WebsocketSubscription().body(EventSubscription.SUBSCRIBE_MESSAGE_PREFIX + Values.asJSON(
                    new EventSubscription(
                        AttributeEvent.class,
                        new AssetFilter<AttributeEvent>().setAssetIds(managerTestSetup.apartment1LivingroomId),
                        "1",
                        null)).orElse(null)),
                new WebsocketHttpSubscription()
                    .contentType(MediaType.APPLICATION_JSON)
                    .method(WebsocketHttpSubscription.Method.POST)
                    .headers(new HashMap<String, List<String>>([
                        "header1" : ["header1Value1"],
                        "header2" : ["header2Value1", "header2Value2"]
                    ]))
                    .uri("https://mockapi/assets")
                    .body(
                        Values.asJSON(new AssetQuery().ids(managerTestSetup.apartment1LivingroomId)).orElse(null)
                    )
                ] as WebsocketSubscription[]
            )

        and: "the agent is added to the asset service"
        agent = assetStorageService.merge(agent)

        then: "the protocol should authenticate and the agent status should become CONNECTED"
        conditions.eventually {
            agent = assetStorageService.find(agent.id, Agent.class)
            assert agent.getAgentStatus().orElse(ConnectionStatus.DISCONNECTED) == ConnectionStatus.CONNECTED
        }

        and: "the subscriptions should have been executed"
        conditions.eventually {
            assert mockServer.subscriptionDone
        }

        when: "an asset is created with attributes linked to the protocol configuration"
        def asset = new ThingAsset("Test Asset")
            .setParent(agent)
            .addOrReplaceAttributes(
                // write attribute value
                new Attribute<>("readWriteTargetTemp", NUMBER)
                    .addMeta(
                        new MetaItem<>(AGENT_LINK, new WebsocketClientAgent.WebsocketClientAgentLink(agent.id)
                            .setWriteValue(("\"" + SharedEvent.MESSAGE_PREFIX +
                                Values.asJSON(new AttributeEvent(
                                    managerTestSetup.apartment1LivingroomId,
                                    "targetTemperature",
                                    0.12345))
                                    .orElse(Values.NULL_LITERAL) + "\"")
                                        .replace("0.12345", Protocol.DYNAMIC_VALUE_PLACEHOLDER)
                                        .replace("\r\n", ""))
                        .setMessageMatchFilters(
                            [
                                new SubStringValueFilter(TriggeredEventSubscription.MESSAGE_PREFIX.length()),
                                new JsonPathFilter("\$..attributeState.attributeRef.attributeName", false, false)
                            ] as ValueFilter[]
                        )
                        .setMessageMatchPredicate(
                            new StringPredicate(AssetQuery.Match.CONTAINS, true, "targetTemperature")
                        )
                        .setValueFilters(
                            [
                                new SubStringValueFilter(TriggeredEventSubscription.MESSAGE_PREFIX.length()),
                                new JsonPathFilter("\$..events[?(@.attributeState.attributeRef.attributeName == \"targetTemperature\")].attributeState.value", true, false)
                            ] as ValueFilter[]
                        )
                        .setWebsocketSubscriptions(
                            [
                                new WebsocketSubscription().body(SharedEvent.MESSAGE_PREFIX + Values.asJSON(
                                    new ReadAttributeEvent(managerTestSetup.apartment1LivingroomId, "targetTemperature")
                                ).orElse(null))
                            ] as WebsocketSubscription[]
                        ))
                    ),
                new Attribute<>("readCo2Level", NUMBER)
                    .addMeta(
                        new MetaItem<>(AGENT_LINK, new WebsocketClientAgent.WebsocketClientAgentLink(agent.id)
                            .setMessageMatchFilters(
                                [
                                    new SubStringValueFilter(TriggeredEventSubscription.MESSAGE_PREFIX.length()),
                                    new JsonPathFilter("\$..attributeState.attributeRef.attributeName", false, false)
                                ] as ValueFilter[]
                            )
                            .setMessageMatchPredicate(
                                new StringPredicate(AssetQuery.Match.CONTAINS, "co2Level")
                            )
                            .setValueFilters(
                                [
                                    new SubStringValueFilter(TriggeredEventSubscription.MESSAGE_PREFIX.length()),
                                    new JsonPathFilter("\$..events[?(@.attributeState.attributeRef.attributeName == \"co2Level\")].attributeState.value", true, false),
                                ] as ValueFilter[]
                            )
                            .setWebsocketSubscriptions(
                                [
                                    new WebsocketSubscription().body(SharedEvent.MESSAGE_PREFIX + Values.asJSON(
                                        new ReadAttributeEvent(managerTestSetup.apartment1LivingroomId, "co2Level")
                                    ).orElse(null))
                                ] as WebsocketSubscription[]
                            ))
                    )
        )

        and: "the asset is merged into the asset service"
        asset = assetStorageService.merge(asset)

        then: "the linked attributes should have no initial values"
        conditions.eventually {
            asset = assetStorageService.find(asset.getId(), true)
            assert !asset.getAttribute("readCo2Level").get().value.isPresent()
            assert !asset.getAttribute("readWriteTargetTemp").get().value.isPresent()
        }

        when: "a linked attribute value is updated"
        def attributeEvent = new AttributeEvent(asset.id,
            "readWriteTargetTemp",
            19.5)
        assetProcessingService.sendAttributeEvent(attributeEvent)

        then: "the linked targetTemperature attribute should contain this written value (it should have been written to the target temp attribute and then read back again)"
        conditions.eventually {
            asset = assetStorageService.find(asset.getId(), true)
            assert asset.getAttribute("readWriteTargetTemp", Double.class).flatMap{it.getValue()}.orElse(null) == 19.5d
        }

        when: "the co2level changes"
        def co2LevelIncrement = new AttributeEvent(
            managerTestSetup.apartment1LivingroomId, "co2Level", 600
        )
        ((SimulatorProtocol)agentService.getProtocolInstance(managerTestSetup.agentId)).updateSensor(co2LevelIncrement)

        then: "the linked co2Level attribute should get the new value"
        conditions.eventually {
            asset = assetStorageService.find(asset.getId(), true)
            assert asset.getAttribute("readCo2Level", Integer.class).flatMap{it.getValue()}.orElse(null) == 600d
        }
    }
}
