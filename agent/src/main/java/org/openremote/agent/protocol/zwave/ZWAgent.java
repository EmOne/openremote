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
package org.openremote.agent.protocol.zwave;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.MetaItemDescriptor;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class ZWAgent extends Agent<ZWAgent, ZWProtocol, ZWAgent.ZWAgentLink> {

    public static class ZWAgentLink extends AgentLink {

        protected Integer deviceNodeId;
        protected Integer deviceEndpoint;
        protected String deviceValue;

        public ZWAgentLink(String id, Integer deviceNodeId, Integer deviceEndpoint, String deviceValue) {
            super(id);
            this.deviceNodeId = deviceNodeId;
            this.deviceEndpoint = deviceEndpoint;
            this.deviceValue = deviceValue;
        }

        public Optional<Integer> getDeviceNodeId() {
            return Optional.ofNullable(deviceNodeId);
        }

        public Optional<Integer> getDeviceEndpoint() {
            return Optional.ofNullable(deviceEndpoint);
        }

        public Optional<String> getDeviceValue() {
            return Optional.ofNullable(deviceValue);
        }
    }

    public static AgentDescriptor<ZWAgent, ZWProtocol, ZWAgentLink> DESCRIPTOR = new AgentDescriptor<>(
        ZWAgent.class, ZWProtocol.class, ZWAgentLink.class, null
    );

    public ZWAgent(String name) {
        super(name, DESCRIPTOR);
    }

    @Override
    public ZWProtocol getProtocolInstance() {
        return new ZWProtocol(this);
    }
}
