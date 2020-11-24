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
package org.openremote.agent.protocol.tcp;

import org.openremote.agent.protocol.io.AbstractIoClientProtocol;
import org.openremote.agent.protocol.io.IoAgent;
import org.openremote.model.asset.agent.AgentDescriptor;

public class TcpClientAgent extends IoAgent<String, TcpIoClient<String>> {

//    public static final AgentDescriptor<TcpClientAgent, TcpClientProtocol> DESCRIPTOR = new AgentDescriptor(
//
//    );


    public TcpClientAgent(String name) {
        this(name, DESCRIPTOR);
    }

    protected <V extends IoAgent<String, TcpIoClient<String>>, W extends AbstractIoClientProtocol<String, TcpIoClient<String>, V>> TcpClientAgent(String name, AgentDescriptor<V, W> descriptor) {
        super(name, descriptor);
    }

    @Override
    public AbstractIoClientProtocol<String, TcpIoClient<String>, TcpClientAgent> getProtocolInstance() {
        return new TcpClientProtocol(this);
    }
}
