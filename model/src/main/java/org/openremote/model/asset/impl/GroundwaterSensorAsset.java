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
package org.openremote.model.asset.impl;

import org.openremote.model.Constants;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.MetaItemType;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class GroundwaterSensorAsset extends Asset<GroundwaterSensorAsset> {

    public static final AttributeDescriptor<Double> TEMPERATURE = new AttributeDescriptor<>("temperature", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_TEMPERATURE_CELSIUS),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Integer> WATER_LEVEL = new AttributeDescriptor<>("waterLevel", ValueType.POSITIVE_INTEGER,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );

    public static final AssetDescriptor<GroundwaterSensorAsset> DESCRIPTOR = new AssetDescriptor<>("water-outline", "95d0df", GroundwaterSensorAsset.class);

    protected GroundwaterSensorAsset(String name, AssetDescriptor<? extends GroundwaterSensorAsset> descriptor) {
        super(name, descriptor);
    }

    public GroundwaterSensorAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Double> getTemperature() {
        return getAttributes().getValue(TEMPERATURE);
    }

    public Optional<Integer> getWaterLevel() {
        return getAttributes().getValue(WATER_LEVEL);
    }
}
