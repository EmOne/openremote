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
package org.openremote.agent.protocol.websocket;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.Entity;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Entity
public class WebsocketClientAgent extends IoAgent<WebsocketClientAgent, WebsocketClientProtocol, WebsocketClientAgent.WebsocketClientAgentLink> {

    public static class WebsocketClientAgentLink extends AgentLink<WebsocketClientAgentLink> {

        protected WebsocketSubscription[] websocketSubscriptions;

        // For Hydrators
        protected WebsocketClientAgentLink() {}

        public WebsocketClientAgentLink(String id) {
            super(id);
        }

        @JsonPropertyDescription("Array of WebsocketSubscriptions that should be executed when the linked attribute is linked; the subscriptions are executed in the order specified in the array.")
        public Optional<WebsocketSubscription[]> getWebsocketSubscriptions() {
            return Optional.ofNullable(websocketSubscriptions);
        }

        @SuppressWarnings("unchecked")
        public <T extends WebsocketClientAgentLink> T setWebsocketSubscriptions(WebsocketSubscription[] websocketSubscriptions) {
            this.websocketSubscriptions = websocketSubscriptions;
            return (T)this;
        }
    }


    /*--------------- META ITEMS TO BE USED ON PROTOCOL CONFIGURATIONS ---------------*/

    public static final ValueDescriptor<WebsocketSubscription> WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR = new ValueDescriptor<>("Websocket subscription", WebsocketSubscription.class);

    /**
     * Websocket connect endpoint URI
     */
    public static final AttributeDescriptor<String> CONNECT_URI = new AttributeDescriptor<>("connectUri", ValueType.STRING);

    /**
     * Headers for websocket connect call
     */
    public static final AttributeDescriptor<ValueType.MultivaluedStringMap> CONNECT_HEADERS = new AttributeDescriptor<>("connectHeaders", ValueType.MULTIVALUED_STRING_MAP);

    /**
     * Array of {@link WebsocketSubscription}s that should be executed once the websocket connection is established; the
     * subscriptions are executed in the order specified in the array.
     */
    public static final AttributeDescriptor<WebsocketSubscription[]> CONNECT_SUBSCRIPTIONS = new AttributeDescriptor<>("connectSubscriptions", WEBSOCKET_SUBSCRIPTION_VALUE_DESCRIPTOR.asArray());

    /*--------------- META ITEMS TO BE USED ON LINKED ATTRIBUTES ---------------*/

    public static final AgentDescriptor<WebsocketClientAgent, WebsocketClientProtocol, WebsocketClientAgentLink> DESCRIPTOR = new AgentDescriptor<>(
        WebsocketClientAgent.class, WebsocketClientProtocol.class, WebsocketClientAgentLink.class, null
    );

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    WebsocketClientAgent() {
    }

    public WebsocketClientAgent(String name) {
        super(name);
    }

    public Optional<String> getConnectUri() {
        return getAttributes().getValue(CONNECT_URI);
    }

    @SuppressWarnings("unchecked")
    public <T extends WebsocketClientAgent> T setConnectUri(String value) {
        getAttributes().getOrCreate(CONNECT_URI).setValue(value);
        return (T)this;
    }

    public Optional<ValueType.MultivaluedStringMap> getConnectHeaders() {
        return getAttributes().getValue(CONNECT_HEADERS);
    }

    @SuppressWarnings("unchecked")
    public <T extends WebsocketClientAgent> T setConnectHeaders(ValueType.MultivaluedStringMap value) {
        getAttributes().getOrCreate(CONNECT_HEADERS).setValue(value);
        return (T)this;
    }

    public Optional<WebsocketSubscription[]> getConnectSubscriptions() {
        return getAttributes().getValue(CONNECT_SUBSCRIPTIONS);
    }

    @SuppressWarnings("unchecked")
    public <T extends WebsocketClientAgent> T setConnectSubscriptions(WebsocketSubscription[] value) {
        getAttributes().getOrCreate(CONNECT_SUBSCRIPTIONS).setValue(value);
        return (T)this;
    }

    @Override
    public WebsocketClientProtocol getProtocolInstance() {
        return new WebsocketClientProtocol(this);
    }
}
