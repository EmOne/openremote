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
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.Entity;
import java.util.Optional;

@Entity
public class ElectricityProducerAsset extends Asset<ElectricityProducerAsset> {

    public enum PanelOrientation {
        SOUTH,
        EAST_WEST
    }

    public static final ValueDescriptor<PanelOrientation> PANEL_ORIENTATION_VALUE = new ValueDescriptor<>("Panel orientation", PanelOrientation.class);

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", ValueType.STRING,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_CAPACITY = new AttributeDescriptor<>("powerCapacity", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Integer> EFFICIENCY = new AttributeDescriptor<>("efficiency", ValueType.PERCENTAGE_INTEGER_0_100);
    public static final AttributeDescriptor<Double> POWER_TOTAL = new AttributeDescriptor<>("powerTotal", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> POWER_FORECAST_DEVIATION = new AttributeDescriptor<>("powerForecastDeviation", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> ENERGY_TOTAL = new AttributeDescriptor<>("energyTotal", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_ENERGY_KILOWATT_HOUR),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<PanelOrientation> PANEL_ORIENTATION = new AttributeDescriptor<>("panelOrientation", PANEL_ORIENTATION_VALUE);

    public static final AssetDescriptor<ElectricityProducerAsset> DESCRIPTOR = new AssetDescriptor<>("ev-station", "8A293D", ElectricityProducerAsset.class);

    protected ElectricityProducerAsset(String name, AssetDescriptor<? extends ElectricityProducerAsset> descriptor) {
        super(name, descriptor);
    }

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    ElectricityProducerAsset() {
        this(null);
    }

    public ElectricityProducerAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValue(STATUS);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setStatus(String value) {
        getAttributes().getOrCreate(STATUS).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPowerTotal() {
        return getAttributes().getValue(POWER_TOTAL);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setPowerTotal(Double value) {
        getAttributes().getOrCreate(POWER_TOTAL).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPowerForecastDeviation() {
        return getAttributes().getValue(POWER_FORECAST_DEVIATION);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setPowerForecastDeviation(Double value) {
        getAttributes().getOrCreate(POWER_FORECAST_DEVIATION).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPowerCapacity() {
        return getAttributes().getValue(POWER_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setPowerCapacity(Double value) {
        getAttributes().getOrCreate(POWER_CAPACITY).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getEfficiency() {
        return getAttributes().getValue(EFFICIENCY);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setEfficiency(Integer value) {
        getAttributes().getOrCreate(EFFICIENCY).setValue(value);
        return (T)this;
    }

    public Optional<Double> getEnergyTotal() {
        return getAttributes().getValue(ENERGY_TOTAL);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setEnergyTotal(Double value) {
        getAttributes().getOrCreate(ENERGY_TOTAL).setValue(value);
        return (T)this;
    }

    public Optional<PanelOrientation> getPanelOrientation() {
        return getAttributes().getValue(PANEL_ORIENTATION);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityProducerAsset> T setPanelOrientation(PanelOrientation value) {
        getAttributes().getOrCreate(PANEL_ORIENTATION).setValue(value);
        return (T)this;
    }
}
