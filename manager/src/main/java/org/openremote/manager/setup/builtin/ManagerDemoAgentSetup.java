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
package org.openremote.manager.setup.builtin;

import org.openremote.agent.protocol.knx.KNXProtocol;
import org.openremote.agent.protocol.velbus.VelbusSerialProtocol;
import org.openremote.model.Container;
import org.openremote.manager.setup.AbstractManagerSetup;
import org.openremote.model.asset.Asset;
import org.openremote.model.security.Tenant;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.Values;

import java.util.logging.Logger;

import static org.openremote.container.util.MapAccess.getBoolean;
import static org.openremote.container.util.MapAccess.getString;

public class ManagerDemoAgentSetup extends AbstractManagerSetup {

    private static final Logger LOG = Logger.getLogger(ManagerDemoAgentSetup.class.getName());

    public static final String SETUP_IMPORT_DEMO_AGENT_KNX = "SETUP_IMPORT_DEMO_AGENT_KNX";
    public static final String SETUP_IMPORT_DEMO_AGENT_KNX_GATEWAY_IP = "SETUP_IMPORT_DEMO_AGENT_KNX_GATEWAY_IP";
    public static final String SETUP_IMPORT_DEMO_AGENT_KNX_LOCAL_IP = "SETUP_IMPORT_DEMO_AGENT_KNX_LOCAL_IP";

    public static final String SETUP_IMPORT_DEMO_AGENT_VELBUS = "SETUP_IMPORT_DEMO_AGENT_VELBUS";
    public static final String SETUP_IMPORT_DEMO_AGENT_VELBUS_COM_PORT = "SETUP_IMPORT_DEMO_AGENT_VELBUS_COM_PORT";

    public String masterRealm;

    final protected boolean knx;
    final protected String knxGatewayIp;
    final protected String knxLocalIp;

    final protected boolean velbus;
    final protected String velbusComPort;

    public ManagerDemoAgentSetup(Container container) {
        super(container);

        this.knx = getBoolean(container.getConfig(), SETUP_IMPORT_DEMO_AGENT_KNX, false);
        this.knxGatewayIp = getString(container.getConfig(), SETUP_IMPORT_DEMO_AGENT_KNX_GATEWAY_IP, "192.168.0.64");
        this.knxLocalIp = getString(container.getConfig(), SETUP_IMPORT_DEMO_AGENT_KNX_LOCAL_IP, "192.168.0.65");

        this.velbus = getBoolean(container.getConfig(), SETUP_IMPORT_DEMO_AGENT_VELBUS, false);
        this.velbusComPort = getString(container.getConfig(), SETUP_IMPORT_DEMO_AGENT_VELBUS_COM_PORT, "COM3");
    }

    @Override
    public void onStart() throws Exception {

        KeycloakDemoSetup keycloakDemoSetup = setupService.getTaskOfType(KeycloakDemoSetup.class);
        Tenant masterTenant = keycloakDemoSetup.masterTenant;
        masterRealm = masterTenant.getRealm();

        Asset agent = new Asset("Demo Agent", AGENT);
        agent.setRealm(masterRealm);
        agent = assetStorageService.merge(agent);

        if (knx) {
            LOG.info("Enable KNX demo protocol configuration, gateway/local IP: " + knxGatewayIp + "/" + knxLocalIp);
            agent.addAttributes(
                initProtocolConfiguration(new Attribute<>("knxConfig"), KNXProtocol.PROTOCOL_NAME)
                    .addMeta(
                        new MetaItem<>(KNXProtocol.META_KNX_GATEWAY_HOST, knxGatewayIp),
                        new MetaItem<>(KNXProtocol.META_KNX_LOCAL_HOST, knxLocalIp)
                    )
            );
            Asset knxDevices = new Asset("KNX Devices", THING, agent, masterRealm);
            knxDevices = assetStorageService.merge(knxDevices);
        }

        if (velbus) {
            LOG.info("Enable Velbus demo protocol configuration, COM port: " + velbusComPort);
            agent.addAttributes(
                initProtocolConfiguration(new Attribute<>("velbusConfig"), VelbusSerialProtocol.PROTOCOL_NAME)
                    .addMeta(
                        new MetaItem<>(VelbusSerialProtocol.META_VELBUS_SERIAL_PORT, velbusComPort)
                    )
            );
            Asset velbusDevices = new Asset("VELBUS Devices", THING, agent, masterRealm);
            velbusDevices = assetStorageService.merge(velbusDevices);
        }

        agent = assetStorageService.merge(agent);
    }
}
