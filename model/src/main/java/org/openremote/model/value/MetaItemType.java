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
package org.openremote.model.value;

import org.openremote.model.asset.UserAsset;
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.attribute.AttributeEvent;
import org.openremote.model.attribute.AttributeLink;
import org.openremote.model.rules.AssetState;
import org.openremote.model.rules.TemporaryFact;

import javax.validation.constraints.Pattern;

public final class MetaItemType {

    /* PROTOCOL / SERVICE META */

    public static final ValueDescriptor<AgentLink> VALUE_AGENT_LINK = new ValueDescriptor<>("Agent link", AgentLink.class);

    /**
     * Links the attribute to an agent, connecting it to a sensor and/or actuator with required configuration properties
     * encapsulated in the concrete protocol specific {@link org.openremote.model.asset.agent.AgentLink}.
     */
    public static final MetaItemDescriptor<AgentLink> AGENT_LINK = new MetaItemDescriptor<>("agentLink", VALUE_AGENT_LINK);

    /**
     * Links the attribute to another attribute, so an attribute event on the attribute triggers the same attribute
     * event on the linked attribute.
     */
    public static final MetaItemDescriptor<AttributeLink> ATTRIBUTE_LINK = new MetaItemDescriptor<>("attributeLink", ValueType.ATTRIBUTE_LINK);


    /* ACCESS PERMISSION META */

    /**
     * Marks the attribute as readable by unauthenticated public clients.
     */
    public static final MetaItemDescriptor<Boolean> ACCESS_PUBLIC_READ = new MetaItemDescriptor<>("accessPublicRead", ValueType.BOOLEAN);

    /**
     * Marks the attribute as writable by unauthenticated public clients.
     */
    public static final MetaItemDescriptor<Boolean> ACCESS_PUBLIC_WRITE = new MetaItemDescriptor<>("accessPublicWrite", ValueType.BOOLEAN);

    /**
     * Marks the attribute as readable by restricted clients and therefore users who are linked to the asset, see {@link
     * UserAsset}.
     */
    public static final MetaItemDescriptor<Boolean> ACCESS_RESTRICTED_READ = new MetaItemDescriptor<>("accessRestrictedRead", ValueType.BOOLEAN);

    /**
     * Marks the attribute as writable by restricted clients and therefore users who are linked to the asset, see {@link
     * UserAsset}.
     */
    public static final MetaItemDescriptor<Boolean> ACCESS_RESTRICTED_WRITE = new MetaItemDescriptor<>("accessRestrictedWrite", ValueType.BOOLEAN);

    /**
     * Marks the attribute as read-only for non-superuser clients. South-bound {@link AttributeEvent}s by regular or
     * restricted users are ignored. North-bound {@link AttributeEvent}s made by protocols and rules engine are
     * possible.
     */
    public static final MetaItemDescriptor<Boolean> READ_ONLY = new MetaItemDescriptor<>("readOnly", ValueType.BOOLEAN);


    /* DATA POINT META */

    /**
     * Should attribute values be stored in time series database
     */
    public static final MetaItemDescriptor<Boolean> STORE_DATA_POINTS = new MetaItemDescriptor<>("storeDataPoints", ValueType.BOOLEAN);

    /**
     * How long to store attribute values in time series database (data older than this will be automatically purged)
     */
    public static final MetaItemDescriptor<Integer> DATA_POINTS_MAX_AGE_DAYS = new MetaItemDescriptor<>("dataPointsMaxAgeDays", ValueType.POSITIVE_INTEGER);

    /**
     * Could possibly have predicted data points
     */
    // TODO: Re-evaluate this can this info be retrieved automatically using prediction service
    public static final MetaItemDescriptor<Boolean> HAS_PREDICTED_DATA_POINTS = new MetaItemDescriptor<>("hasPredictedDataPoints", ValueType.BOOLEAN);



    /* RULE META */

    /**
     * Set maximum lifetime of {@link AssetState} temporary facts in rules, for example "1h30m5s". The rules engine will
     * remove temporary {@link AssetState} facts if they are older than this value (using event source/value timestamp,
     * not event processing time).
     * <p>
     * The default expiration for asset events can be configured with environment variable
     * <code>RULE_EVENT_EXPIRES</code>.
     * <p>
     * Also see {@link TemporaryFact#GUARANTEED_MIN_EXPIRATION_MILLIS}.
     */
    @Pattern(regexp = "^([+-])?((\\d+)[Dd])?\\s*((\\d+)[Hh])?\\s*((\\d+)[Mm])?\\s*((\\d+)[Ss])?\\s*((\\d+)([Mm][Ss])?)?$")
    public static final MetaItemDescriptor<String> RULE_EVENT_EXPIRES = new MetaItemDescriptor<>("ruleEventExpires", ValueType.STRING);

    /**
     * Should attribute writes be processed by the rules engines as temporary facts. When an attribute is updated, the
     * change will be inserted as a new {@link AssetState} temporary fact in rules engines. These facts expire
     * automatically after a defined time, see {@link #RULE_EVENT_EXPIRES}. If you want to match (multiple) {@link
     * AssetState}s for the same attribute over time, to evaluate the change history of an attribute, add this meta
     * item.
     */
    public static final MetaItemDescriptor<Boolean> RULE_EVENT = new MetaItemDescriptor<>("ruleEvent", ValueType.BOOLEAN);

    /**
     * Should attribute writes be processed by the rules engines as {@link AssetState} facts, with a lifecycle that
     * reflects the state of the asset attribute. Each attribute will have one fact at all times in rules memory. These
     * state facts are kept in sync with asset changes: When the attribute is updated, the fact will be updated
     * (replaced). If you want evaluate the change history of an attribute, you typically need to combine this with
     * {@link #RULE_EVENT}.
     */
    public static final MetaItemDescriptor<Boolean> RULE_STATE = new MetaItemDescriptor<>("ruleState", ValueType.BOOLEAN);



    /* FORMATTING / DISPLAY META */

    /**
     * A human-friendly string that can be displayed in UI instead of the raw attribute name.
     */
    public static final MetaItemDescriptor<String> LABEL = new MetaItemDescriptor<>("label", ValueType.STRING);

    /**
     * Format string that can be used to render the attribute value, see https://github.com/alexei/sprintf.js.
     */
    public static final MetaItemDescriptor<String> FORMAT = new MetaItemDescriptor<>("format", ValueType.STRING);

    /**
     * Indicates the unit type that this value represents.
     * For e.g. if the value represents currency and it's in euro's then the unit type would be EUR.
     * For e.g. if the value represents distance and it's in kilometers then the unit type would be KM.
     */
    public static final MetaItemDescriptor<String> UNIT_TYPE = new MetaItemDescriptor<>("unitType", ValueType.STRING);

    /**
     * Marks the value as secret and indicates that clients should display this in a concealed manner (e.g. password
     * input with optional show)
     */
    public static final MetaItemDescriptor<Boolean> SECRET = new MetaItemDescriptor<>("secret", ValueType.BOOLEAN);

    /**
     * Indicates that any input should support multiline text entry
     */
    public static final MetaItemDescriptor<Boolean> MULTILINE = new MetaItemDescriptor<>("multiline", ValueType.BOOLEAN);

    /**
     * If there is a dashboard, some kind of attribute overview, should this attribute be shown.
     */
    public static final MetaItemDescriptor<Boolean> SHOW_ON_DASHBOARD = new MetaItemDescriptor<>("showOnDashboard", ValueType.BOOLEAN);

    protected MetaItemType() {
    }
}
