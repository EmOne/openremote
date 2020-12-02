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
package org.openremote.agent.protocol.http;

import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueType;

import java.util.Optional;

import static org.openremote.agent.protocol.http.HttpClientAgent.VALUE_HTTP_METHOD;

public abstract class AbstractHttpServerAgent<T extends AbstractHttpServerAgent<T, U, V>, U extends AbstractHttpServerProtocol<U, T, V>, V extends AgentLink<?>> extends Agent<T, U, V> {

    public static final AttributeDescriptor<String> DEPLOYMENT_PATH = new AttributeDescriptor<>("deploymentPath", ValueType.STRING);

    public static final AttributeDescriptor<HttpMethod[]> ALLOWED_HTTP_METHODS = new AttributeDescriptor<>("allowedHTTPMethods", VALUE_HTTP_METHOD.asArray());
    public static final AttributeDescriptor<String[]> ALLOWED_ORIGINS = new AttributeDescriptor<>("allowedOrigins", ValueType.STRING.asArray());
    public static final AttributeDescriptor<Boolean> ROLE_BASED_SECURITY = new AttributeDescriptor<>("roleBasedSecurity", ValueType.BOOLEAN);

    protected AbstractHttpServerAgent(String name, AgentDescriptor<T, U, V> descriptor) {
        super(name, descriptor);
    }

    public Optional<String> getDeploymentPath() {
        return getAttributes().getValue(DEPLOYMENT_PATH);
    }

    public Optional<HttpMethod[]> getAllowedHTTPMethods() {
        return getAttributes().getValue(ALLOWED_HTTP_METHODS);
    }

    public Optional<String[]> getAllowedOrigins() {
        return getAttributes().getValue(ALLOWED_ORIGINS);
    }

    public Optional<Boolean> isRoleBasedSecurity() {
        return getAttributes().getValue(ROLE_BASED_SECURITY);
    }
}
