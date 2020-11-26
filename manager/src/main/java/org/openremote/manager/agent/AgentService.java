/*
 * Copyright 2016, OpenRemote Inc.
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
package org.openremote.manager.agent;

import org.apache.camel.builder.RouteBuilder;
import org.openremote.agent.protocol.ProtocolAssetService;
import org.openremote.container.message.MessageBrokerService;
import org.openremote.container.persistence.PersistenceEvent;
import org.openremote.container.timer.TimerService;
import org.openremote.manager.asset.AssetProcessingException;
import org.openremote.manager.asset.AssetProcessingService;
import org.openremote.manager.asset.AssetStorageService;
import org.openremote.manager.asset.AssetUpdateProcessor;
import org.openremote.manager.concurrent.ManagerExecutorService;
import org.openremote.manager.event.ClientEventService;
import org.openremote.manager.gateway.GatewayService;
import org.openremote.manager.security.ManagerIdentityService;
import org.openremote.manager.web.ManagerWebService;
import org.openremote.model.Container;
import org.openremote.model.ContainerService;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetTreeNode;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.asset.agent.Protocol;
import org.openremote.model.attribute.Attribute;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.AttributeEvent.Source;
import org.openremote.model.attribute.AttributeList;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.protocol.ProtocolAssetDiscovery;
import org.openremote.model.protocol.ProtocolAssetImport;
import org.openremote.model.protocol.ProtocolInstanceDiscovery;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.LogicGroup;
import org.openremote.model.query.filter.*;
import org.openremote.model.util.Pair;
import org.openremote.model.util.TextUtil;
import org.openremote.model.value.MetaItemType;

import javax.persistence.EntityManager;
import javax.ws.rs.NotSupportedException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.openremote.container.concurrent.GlobalLock.withLock;
import static org.openremote.container.concurrent.GlobalLock.withLockReturning;
import static org.openremote.container.persistence.PersistenceEvent.*;
import static org.openremote.manager.asset.AssetProcessingService.ASSET_QUEUE;
import static org.openremote.model.asset.agent.Protocol.ACTUATOR_TOPIC;
import static org.openremote.model.asset.agent.Protocol.SENSOR_QUEUE;
import static org.openremote.model.attribute.AttributeEvent.HEADER_SOURCE;
import static org.openremote.model.attribute.AttributeEvent.Source.GATEWAY;
import static org.openremote.model.attribute.AttributeEvent.Source.SENSOR;

/**
 * Handles life cycle and communication with {@link Protocol}s.
 * <p>
 * Finds all {@link Agent} assets and manages their {@link Protocol} instances.
 */
public class AgentService extends RouteBuilder implements ContainerService, AssetUpdateProcessor, ProtocolAssetService {

    private static final Logger LOG = Logger.getLogger(AgentService.class.getName());
    public static final int PRIORITY = DEFAULT_PRIORITY + 100; // Start quite late to ensure asset model etc. are initialised
    protected TimerService timerService;
    protected ManagerIdentityService identityService;
    protected AssetProcessingService assetProcessingService;
    protected AssetStorageService assetStorageService;
    protected MessageBrokerService messageBrokerService;
    protected ClientEventService clientEventService;
    protected GatewayService gatewayService;
    protected ManagerExecutorService executorService;
    protected Map<String, Agent<?, ?, ?>> agentMap = new HashMap<>();
    protected final Map<String, Future<Void>> agentDiscoveryImportFutureMap = new HashMap<>();
    protected final Map<String, Protocol<?>> protocolInstanceMap = new HashMap<>();
    protected final Map<String, List<Consumer<PersistenceEvent<Asset>>>> childAssetSubscriptions = new HashMap<>();
    protected boolean initDone;
    protected Container container;

    @Override
    public int getPriority() {
        return PRIORITY;
    }

    @Override
    public void init(Container container) throws Exception {
        this.container = container;
        timerService = container.getService(TimerService.class);
        identityService = container.getService(ManagerIdentityService.class);
        assetProcessingService = container.getService(AssetProcessingService.class);
        assetStorageService = container.getService(AssetStorageService.class);
        messageBrokerService = container.getService(MessageBrokerService.class);
        clientEventService = container.getService(ClientEventService.class);
        gatewayService = container.getService(GatewayService.class);
        executorService = container.getService(ManagerExecutorService.class);

        if (initDone) {
            return;
        }

        container.getService(ManagerWebService.class).getApiSingletons().add(
            new AgentResourceImpl(
                container.getService(TimerService.class),
                container.getService(ManagerIdentityService.class),
                assetStorageService,
                this)
        );

        initDone = true;
    }

    @Override
    public void start(Container container) throws Exception {
        container.getService(MessageBrokerService.class).getContext().addRoutes(this);

        // Load all enabled agents and instantiate a protocol instance for each
        LOG.fine("Loading agents...");
        Collection<Agent<?, ?, ?>> agents = getAgents().values();
        LOG.fine("Found enabled agent count = " + agents.size());

        agents.forEach(this::startAgent);
    }

    @Override
    public void stop(Container container) throws Exception {
        agentMap.values().forEach(this::stopAgent);
        agentMap.clear();
        protocolInstanceMap.clear();
    }

    @Override
    public void configure() throws Exception {
        from(PERSISTENCE_TOPIC)
            .routeId("AgentPersistenceChanges")
            .filter(isPersistenceEventForEntityType(Asset.class))
            .process(exchange -> {
                @SuppressWarnings("unchecked")
                PersistenceEvent<Asset> persistenceEvent = (PersistenceEvent<Asset>)exchange.getIn().getBody(PersistenceEvent.class);
                Asset asset = persistenceEvent.getEntity();
                if (isPersistenceEventForAssetType(Agent.class).matches(exchange)) {
                    processAgentChange((Agent<?, ?, ?>)asset, persistenceEvent);
                } else {
                    processAssetChange(asset, persistenceEvent);
                }
            });

        // A protocol wants to write a new sensor value
        from(SENSOR_QUEUE)
            .routeId("FromSensorUpdates")
            .filter(body().isInstanceOf(AttributeEvent.class))
            .setHeader(HEADER_SOURCE, () -> SENSOR)
            .to(ASSET_QUEUE);
    }

    @Override
    public Asset mergeAsset(Asset asset) {
        Objects.requireNonNull(asset.getId());
        Objects.requireNonNull(asset.getParentId());

        // Do basic check that parent is at least an agent...doesn't confirm its' the correct agent so
        // that's up to the protocol to guarantee
        if (!getAgents().containsKey(asset.getParentId())) {
            String msg = "Cannot merge protocol-provided asset as the parent ID is not a valid agent ID: " + asset;
            LOG.warning(msg);
            throw new IllegalArgumentException(msg);
        }

        // TODO: Define access permissions for merged asset (user asset links inherit from parent agent?)
        LOG.fine("Merging asset with protocol-provided: " + asset);
        return assetStorageService.merge(asset, true);
    }

    @Override
    public boolean deleteAssets(String...assetIds) {
        LOG.fine("Deleting protocol-provided: " + Arrays.toString(assetIds));
        return assetStorageService.delete(Arrays.asList(assetIds), false);
    }

    @Override
    public Asset findAsset(String assetId) {
        LOG.fine("Getting protocol-provided: " + assetId);
        return assetStorageService.find(assetId);
    }

    @Override
    public <T extends Asset> T findAsset(String assetId, Class<T> assetType) {
        LOG.fine("Getting protocol-provided: " + assetId);
        return assetStorageService.find(assetId, assetType);
    }

    @Override
    public List<Asset> findAssets(String assetId, AssetQuery assetQuery) {
        if (TextUtil.isNullOrEmpty(assetId)) {
            return Collections.emptyList();
        }

        if (assetQuery == null) {
            assetQuery = new AssetQuery();
        }

        // Ensure agent ID is injected into each path predicate
        if (assetQuery.paths != null) {
            for (PathPredicate pathPredicate : assetQuery.paths) {
                int len = pathPredicate.path.length;
                pathPredicate.path = Arrays.copyOf(pathPredicate.path, len+1);
                pathPredicate.path[len] = assetId;
            }
        } else {
            assetQuery.paths(new PathPredicate(assetId));
        }

        return assetStorageService.findAll(assetQuery);
    }

    @Override
    public void sendAttributeEvent(AttributeEvent attributeEvent) {
        assetProcessingService.sendAttributeEvent(attributeEvent);
    }

    protected void processAgentChange(Agent<?, ?, ?> agent, PersistenceEvent<?> persistenceEvent) {

        LOG.finest("Processing agent persistence event: " + persistenceEvent.getCause());
        Agent<?, ?, ?> oldAgent = (Agent<?, ?, ?>)persistenceEvent.getEntity();

        switch (persistenceEvent.getCause()) {
            case CREATE:
                if (!addReplaceAgent(agent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }

                startAgent(agent);

                break;
            case UPDATE:
                if (!removeAgent(oldAgent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }

                stopAgent(oldAgent);

                if (agent.isDisabled().orElse(false)) {
                    LOG.info("Agent is marked as disabled so not starting: " + agent);
                } else {
                    addReplaceAgent(agent);
                    startAgent(agent);
                }
                break;
            case DELETE:
                if (!removeAgent(agent)) {
                    LOG.finest("Agent is a gateway asset so ignoring");
                    return;
                }
                // Unlink any attributes that have an agent link to this agent
                stopAgent(agent);
                break;
        }
    }

    /**
     * Looks for new, modified and obsolete AGENT_LINK attributes and links / unlinks them
     * with the protocol
     */
    protected void processAssetChange(Asset asset, PersistenceEvent<Asset> persistenceEvent) {
        LOG.finest("Processing asset persistence event: " + persistenceEvent.getCause());

        switch (persistenceEvent.getCause()) {
            case CREATE:

                // Check if asset parent is a gateway or a gateway descendant, if so ignore it
                // Need to look at parent as this asset may not have been acknowledged by the gateway service yet
                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), asset.getParentId()) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                // Link any AGENT_LINK attributes to their referenced agent asset
                getGroupedAgentLinkAttributes(
                    asset.getAttributes().stream(),
                    attribute -> true
                ).forEach((agent, attributes) -> this.linkAttributes(agent, asset.getId(), attributes));

                break;
            case UPDATE:

                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                List<String> propertyNames = Arrays.asList(persistenceEvent.getPropertyNames());

                // Check if attributes of the asset have been modified
                int attributesIndex = propertyNames.indexOf("attributes");
                if (attributesIndex < 0) {
                    return;
                }

                // Unlink old agent linked attributes and relink new ones
                Map<Agent, List<Attribute<?>>> oldAgentLinkedAttributes = getGroupedAgentLinkAttributes(
                    ((AttributeList)persistenceEvent.getPreviousState()[attributesIndex]).stream(),
                    attribute -> true
                );

                Map<Agent, List<Attribute<?>>> newAgentLinkedAttributes = getGroupedAgentLinkAttributes(
                    ((AttributeList)persistenceEvent.getCurrentState()[attributesIndex]).stream(),
                    attribute -> true
                );

                oldAgentLinkedAttributes.forEach((agent, attributes) -> unlinkAttributes(agent, asset.getId(), attributes));
                newAgentLinkedAttributes.forEach((agent, attributes) -> linkAttributes(agent, asset.getId(), attributes));

                break;
            case DELETE: {

                if (gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) != null) {
                    LOG.finest("This is a gateway descendant asset so ignoring: " + asset.getId());
                    return;
                }

                // Unlink any AGENT_LINK attributes from the referenced protocol
                getGroupedAgentLinkAttributes(asset.getAttributes().stream(), attribute -> true)
                    .forEach((agent, attributes) -> unlinkAttributes(agent, asset.getId(), attributes));
                break;
            }
        }

        String parentAgentId = getAgentAncestorId(asset);
        if (parentAgentId != null) {
            notifyChildAssetChange(parentAgentId, persistenceEvent);
        }
    }

    protected String getAgentAncestorId(Asset asset) {
        if (asset.getPath() == null) {
            // Fully load
            Asset fullyLoaded = assetStorageService.find(asset.getId());
            if (fullyLoaded != null) {
                asset = fullyLoaded;
            } else if (!TextUtil.isNullOrEmpty(asset.getParentId())) {
                fullyLoaded = assetStorageService.find(asset.getParentId());
                List<String> path = new ArrayList<>(Arrays.asList(fullyLoaded.getPath()));
                path.add(0, asset.getId());
                asset.setPath(path.toArray(new String[0]));
            }
        }

        if (asset.getPath() == null) {
            return null;
        }

        return Arrays.stream(asset.getPath())
                .filter(assetId -> getAgents().containsKey(assetId))
                .findFirst()
                .orElse(null);
    }

    protected void startAgent(Agent agent) {
        try {
            Protocol protocol = agent.getProtocolInstance();
            protocolInstanceMap.put(agent.getId(), protocol);

            LOG.fine("Starting protocol instance: " + protocol);
            protocol.start(container);
            LOG.fine("Started protocol instance:" + protocol);

            LOG.fine("Linking attributes to protocol instance: " + protocol);

            // Get all assets that have attributes with agent link meta to this agent
            List<Asset> assets = assetStorageService.findAll(
                new AssetQuery()
                    .attributes(
                        new AttributePredicate().meta(
                            new NameValuePredicate(new StringPredicate(MetaItemType.AGENT_LINK.getName()), new StringPredicate(agent.getId()))
                        )
                    )
            );

            LOG.fine("Found '" + assets.size() + "' asset(s) with attributes linked to this protocol instance: " + protocol);

            assets.forEach(
                asset ->
                    getGroupedAgentLinkAttributes(
                        asset.getAttributes().stream(),
                        assetAttribute -> assetAttribute.getMetaValue(MetaItemType.AGENT_LINK)
                            .map(agentId -> agentId.equals(agent.getId()))
                            .orElse(false)
                    ).forEach((agentId, attributes) -> linkAttributes(agent, asset.getId(), attributes))
            );


        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to start protocol instance for agent: " + agent);
        }
    }

    protected void stopAgent(Agent agent) {
        withLock(getClass().getSimpleName() + "::stopAgent", () -> {
            Protocol<?> protocol = protocolInstanceMap.remove(agent.getId());

            if (protocol == null) {
                LOG.severe("Cannot stop protocol as protocol instance not found for agent: " + agent);
                return;
            }

            Map<String, List<Attribute<?>>> groupedAttributes = protocol.getLinkedAttributes().entrySet().stream().collect(
                Collectors.groupingBy(entry -> entry.getKey().getAssetId(), mapping(Map.Entry::getValue, toList()))
            );

            groupedAttributes.forEach((assetId, linkedAttributes) -> unlinkAttributes(agent, assetId, linkedAttributes));

            // Stop the protocol instance
            try {
                protocol.stop(container);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Protocol instance threw an exception whilst being stopped", e);
            }

            // Remove child asset subscriptions for this agent
            childAssetSubscriptions.remove(agent.getId());
        });
    }

    protected void linkAttributes(Agent agent, String assetId, Collection<Attribute<?>> attributes) {
        withLock(getClass().getSimpleName() + "::linkAttributes", () -> {
            Protocol<?> protocol = getProtocolInstance(agent.getId());

            if (protocol == null) {
                LOG.severe("Cannot link protocol attributes as protocol instance not found for agent: " + agent);
                return;
            }

            LOG.fine("Linking asset attributes linked to protocol: " + protocol);

            attributes.forEach(attribute -> {
                try {
                    LOG.fine("Linking attribute '" + attribute + "' to protocol: " + protocol);
                    protocol.linkAttribute(assetId, attribute);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Failed to link attribute '" + attribute + "' to protocol: " + protocol, ex);
                }
            });
        });
    }

    protected void unlinkAttributes(Agent agent, String assetId, List<Attribute<?>> attributes) {
        withLock(getClass().getSimpleName() + "::unlinkAttributes", () -> {
            Protocol<?> protocol = getProtocolInstance(agent.getId());

            if (protocol == null) {
                LOG.severe("Cannot unlink protocol attributes as protocol instance not found for agent: " + agent);
                return;
            }

            LOG.fine("Unlinking asset attributes linked to protocol: " + protocol);

            attributes.forEach(attribute -> {
                try {
                    LOG.fine("Unlinking attribute '" + attribute + "' to protocol: " + protocol);
                    protocol.unlinkAttribute(assetId, attribute);
                } catch (Exception ex) {
                    LOG.log(Level.SEVERE, "Ignoring error on unlinking attribute '" + attribute + "' from protocol: " + protocol, ex);
                }
            });
        });
    }

    /**
     * If this is an update from a sensor, or if the changed attribute is not linked to an agent, it's ignored.
     * <p>
     * Otherwise push the update to the attributes' linked protocol to handle and prevent any further
     * processing of this event by the processing chain. The protocol should raise sensor updates as
     * required (i.e. the protocol is responsible for synchronising state with the database).
     */
    @Override
    public boolean processAssetUpdate(EntityManager entityManager,
                                      Asset asset,
                                      Attribute<?> attribute,
                                      Source source) throws AssetProcessingException {

        if (source == SENSOR || source == GATEWAY) {
            return false;
        }

        Boolean result = withLockReturning(getClass().getSimpleName() + "::processAssetUpdate", () ->
            attribute.getMetaValue(MetaItemType.AGENT_LINK)
                .map(agentLink -> {
                    LOG.fine("Attribute write for agent linked attribute: agent=" + agentLink.getId() + ", asset=" + asset.getId() + ", attribute=" + attribute.getName());
                    AttributeEvent attributeEvent = new AttributeEvent(new AttributeState(asset.getId(), attribute));
                    messageBrokerService.getProducerTemplate().sendBodyAndHeader(
                        ACTUATOR_TOPIC,
                        attributeEvent,
                        Protocol.ACTUATOR_TOPIC_TARGET_PROTOCOL,
                        getProtocolInstance(agentLink.getId())
                    );
                    return true; // Processing complete, skip other processors
                }).orElse(false) // This is a regular attribute so allow the processing to continue
        );
        return result != null ? result : false;
    }

    /**
     * Gets all agent link attributes and their linked protocol configuration and groups them by Protocol Configuration
     */
    protected Map<Agent, List<Attribute<?>>> getGroupedAgentLinkAttributes(Stream<Attribute<?>> attributes,
                                                                                      Predicate<Attribute<?>> filter) {
        return attributes
            .filter(attribute ->
                // Exclude attributes without agent link or with agent link to not recognised agents (could be gateway agents)
                attribute.getMetaValue(MetaItemType.AGENT_LINK)
                    .map(agentLink -> {
                        if (!getAgents().containsKey(agentLink.getId())) {
                            LOG.fine("Agent linked attribute, agent not found or this is a gateway asset: " + attribute);
                            return false;
                        }
                        return true;
                    })
                    .orElse(false))
            .filter(filter)
            .map(attribute -> new Pair<String, Attribute<?>>(attribute.getMetaValue(MetaItemType.AGENT_LINK).map(AgentLink::getId).orElse(null), attribute))
            .collect(Collectors.groupingBy(
                agentIdAttribute -> getAgents().get(agentIdAttribute.key),
                mapping(agentIdAttribute -> agentIdAttribute.value, toList())
            ));
    }

    public String toString() {
        return getClass().getSimpleName() + "{" + "}";
    }

    protected boolean addReplaceAgent(Agent<?, ?, ?> agent) {
        // Fully load agent asset
        final Agent<?, ?, ?> loadedAgent = assetStorageService.find(agent.getId(), true, Agent.class);
        if (gatewayService.getLocallyRegisteredGatewayId(agent.getId(), agent.getParentId()) != null) {
            return false;
        }
        withLock(getClass().getSimpleName() + "::addReplaceAgent", () -> getAgents().put(loadedAgent.getId(), loadedAgent));
        return true;
    }

    @SuppressWarnings("ConstantConditions")
    protected boolean removeAgent(Agent<?, ?, ?> agent) {
        return withLockReturning(getClass().getSimpleName() + "::removeAgent", () -> getAgents().remove(agent.getId()) != null);
    }

    public Map<String, Agent<?, ?, ?>> getAgents() {
        return withLockReturning(getClass().getSimpleName() + "::getAgents", () -> {
            if (agentMap == null) {
                agentMap = assetStorageService.findAll(
                        new AssetQuery().types(Agent.class).attributes(
                            new LogicGroup<>(
                                LogicGroup.Operator.OR,
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName())).mustNotExist(),
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName()), new ValueEmptyPredicate()),
                                new AttributePredicate(new StringPredicate(Agent.STATUS.getName()), new BooleanPredicate(false))
                            )
                        )
                    )
                    .stream()
                    .filter(asset -> gatewayService.getLocallyRegisteredGatewayId(asset.getId(), null) == null)
                    .collect(Collectors.toMap(Asset::getId, agent -> (Agent<?, ?, ?>)agent));
            }
            return agentMap;
        });
    }

    public Protocol<?> getProtocolInstance(Agent<?, ?, ?> agent) {
        return getProtocolInstance(agent.getId());
    }

    public Protocol<?> getProtocolInstance(String agentId) {
        return protocolInstanceMap.get(agentId);
    }

    @Override
    public void subscribeChildAssetChange(String agentId, Consumer<PersistenceEvent<Asset>> assetChangeConsumer) {
        if (!getAgents().containsKey(agentId)) {
            LOG.info("Attempt to subscribe to child asset changes with an invalid agent ID: " +agentId);
            return;
        }

        withLock(getClass().getSimpleName() + "::subscribeChildAssetChange", () -> {
            List<Consumer<PersistenceEvent<Asset>>> consumerList = childAssetSubscriptions
                .computeIfAbsent(agentId, (id) -> new ArrayList<>());
            if (!consumerList.contains(assetChangeConsumer)) {
                consumerList.add(assetChangeConsumer);
            }
        });
    }

    @Override
    public void unsubscribeChildAssetChange(String agentId, Consumer<PersistenceEvent<Asset>> assetChangeConsumer) {
        withLock(getClass().getSimpleName() + "::unsubscribeChildAssetChange", () ->
            childAssetSubscriptions.computeIfPresent(agentId, (id, consumerList) -> {
                consumerList.remove(assetChangeConsumer);
                return consumerList.isEmpty() ? null : consumerList;
            }));
    }

    protected void notifyChildAssetChange(String agentId, PersistenceEvent<Asset> assetPersistenceEvent) {
        withLock(getClass().getSimpleName() + "::notifyChildAssetChange", () ->
            childAssetSubscriptions.computeIfPresent(agentId, (id, consumerList) -> {
                LOG.fine("Notifying child asset change consumers of change to agent child asset: Agent ID=" + id + ", Asset ID=" + assetPersistenceEvent.getEntity().getId());
                try {
                    consumerList.forEach(consumer -> consumer.accept(assetPersistenceEvent));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Child asset change consumer threw an exception: Agent ID=" + id + ", Asset ID=" + assetPersistenceEvent.getEntity().getId(), e);
                }
                return consumerList;
            }));
    }

    public boolean isProtocolAssetDiscoveryOrImportRunning(String agentId) {
        return agentDiscoveryImportFutureMap.containsKey(agentId);
    }

    public Future<Void> doProtocolInstanceDiscovery(String parentId, Class<? extends ProtocolInstanceDiscovery> instanceDiscoveryProviderClass, Consumer<Agent<?,?,?>[]> onDiscovered) {

        LOG.fine("Initiating protocol instance discovery: Provider = " + instanceDiscoveryProviderClass);

        Runnable task = () -> {
            if (parentId != null && gatewayService.getLocallyRegisteredGatewayId(parentId, null) != null) {
                // TODO: Implement gateway instance discovery using client event bus
                return;
            }

            try {
                ProtocolInstanceDiscovery instanceDiscovery = instanceDiscoveryProviderClass.newInstance();
                Future<Void> discoveryFuture = instanceDiscovery.startInstanceDiscovery(onDiscovered);
                discoveryFuture.get();
            } catch (InterruptedException e) {
                LOG.info("Protocol instance discovery was cancelled");
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Failed to do protocol instance discovery: Provider = " + instanceDiscoveryProviderClass, e);
            } finally {
                LOG.fine("Finished protocol instance discovery: Provider = " + instanceDiscoveryProviderClass);
            }
        };

        return executorService.submit(task, null);
    }

    public Future<Void> doProtocolAssetDiscovery(Agent<?, ?, ?> agent, Consumer<AssetTreeNode[]> onDiscovered) throws RuntimeException {

        if (!(agent instanceof ProtocolAssetDiscovery)) {
            throw new NotSupportedException("Agent protocol doesn't support asset discovery");
        }

        LOG.fine("Initiating protocol asset discovery: Agent = " + agent);

        synchronized (agentDiscoveryImportFutureMap) {
            okToContinueWithImportOrDiscovery(agent.getId());

            Runnable task = () -> {
                try {
                    if (gatewayService.getLocallyRegisteredGatewayId(agent.getId(), null) != null) {
                        // TODO: Implement gateway instance discovery using client event bus
                        return;
                    }

                    ProtocolAssetDiscovery assetDiscovery = (ProtocolAssetDiscovery) agent;
                    Future<Void> discoveryFuture = assetDiscovery.startAssetDiscovery(onDiscovered);
                    discoveryFuture.get();
                } catch (InterruptedException e) {
                    LOG.info("Protocol asset discovery was cancelled");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to do protocol asset discovery: Agent = " + agent, e);
                } finally {
                    LOG.fine("Finished protocol asset discovery: Agent = " + agent);
                    agentDiscoveryImportFutureMap.remove(agent.getId());
                }
            };

            Future<Void> future = executorService.submit(task, null);
            agentDiscoveryImportFutureMap.put(agent.getId(), future);
            return future;
        }
    }

    public Future<Void> doProtocolAssetImport(Agent<?, ?, ?> agent, byte[] fileData, Consumer<AssetTreeNode[]> onDiscovered) throws RuntimeException {

        if (!(agent instanceof ProtocolAssetImport)) {
            throw new NotSupportedException("Agent protocol doesn't support asset import");
        }

        LOG.fine("Initiating protocol asset import: Agent = " + agent);
        synchronized (agentDiscoveryImportFutureMap) {
            okToContinueWithImportOrDiscovery(agent.getId());

            Runnable task = () -> {
                try {
                    if (gatewayService.getLocallyRegisteredGatewayId(agent.getId(), null) != null) {
                        // TODO: Implement gateway instance discovery using client event bus
                        return;
                    }

                    ProtocolAssetImport assetImport = (ProtocolAssetImport) agent;
                    Future<Void> discoveryFuture = assetImport.startAssetImport(fileData, onDiscovered);
                    discoveryFuture.get();
                } catch (InterruptedException e) {
                    LOG.info("Protocol asset import was cancelled");
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to do protocol asset import: Agent = " + agent, e);
                } finally {
                    LOG.fine("Finished protocol asset import: Agent = " + agent);
                    agentDiscoveryImportFutureMap.remove(agent.getId());
                }
            };

            Future<Void> future = executorService.submit(task, null);
            agentDiscoveryImportFutureMap.put(agent.getId(), future);
            return future;
        }
    }

    protected void okToContinueWithImportOrDiscovery(String agentId) {
        if (agentDiscoveryImportFutureMap.containsKey(agentId)) {
            String msg = "Protocol asset discovery or import already running for requested agent: " + agentId;
            LOG.info(msg);
            throw new IllegalStateException(msg);
        }
    }
}
