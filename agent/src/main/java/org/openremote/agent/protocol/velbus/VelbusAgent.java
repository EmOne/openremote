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
package org.openremote.agent.protocol.velbus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.Entity;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;

public abstract class VelbusAgent<T extends VelbusAgent<T, U>, U extends AbstractVelbusProtocol<U, T>> extends Agent<T, U, VelbusAgent.VelbusAgentLink> {

    public static class VelbusAgentLink extends AgentLink<VelbusAgentLink> {

        @NotBlank
        protected String deviceValueLink;

        @Min(1)
        @Max(255)
        @NotNull
        protected Integer deviceAddress;

        // For Hydrators
        protected VelbusAgentLink() {}

        @JsonCreator
        public VelbusAgentLink(@JsonProperty("id") String id, @JsonProperty("deviceAddress") Integer deviceAddress, @JsonProperty("deviceValueLink") String deviceValueLink) {
            super(id);
            this.deviceValueLink = deviceValueLink;
            this.deviceAddress = deviceAddress;
        }

        public Optional<String> getDeviceValueLink() {
            return Optional.ofNullable(deviceValueLink);
        }

        public VelbusAgentLink setDeviceValueLink(String deviceValueLink) {
            this.deviceValueLink = deviceValueLink;
            return this;
        }

        public Optional<Integer> getDeviceAddress() {
            return Optional.ofNullable(deviceAddress);
        }

        public VelbusAgentLink setDeviceAddress(Integer deviceAddress) {
            this.deviceAddress = deviceAddress;
            return this;
        }
    }

    public static final AttributeDescriptor<Integer> TIME_INJECTION_INTERVAL_SECONDS = new AttributeDescriptor<>("timeInjectionInterval", ValueType.POSITIVE_INTEGER);

    protected VelbusAgent(String name, AgentDescriptor<T, U, VelbusAgentLink> descriptor) {
        super(name, descriptor);
    }

    public Optional<Integer> getTimeInjectionInterval() {
        return getAttributes().getValue(TIME_INJECTION_INTERVAL_SECONDS);
    }
}
