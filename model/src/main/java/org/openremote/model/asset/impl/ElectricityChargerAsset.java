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
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.MetaItemType;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class ElectricityChargerAsset extends DeviceAsset {

    public static final AttributeDescriptor<String> STATUS = new AttributeDescriptor<>("status", ValueType.STRING,
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<String> CHARGER_TYPE = new AttributeDescriptor<>("chargerType", ValueType.STRING);
    public static final AttributeDescriptor<Double> POWER_CAPACITY = new AttributeDescriptor<>("powerCapacity", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT)
    );
    public static final AttributeDescriptor<Double> POWER_CONSUMPTION = new AttributeDescriptor<>("powerConsumption", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_POWER_KILOWATT),
        new MetaItem<>(MetaItemType.READ_ONLY)
    );
    public static final AttributeDescriptor<Double> TARIFF_IMPORT = new AttributeDescriptor<>("tariffImport", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_EXPORT = new AttributeDescriptor<>("tariffExport", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_EUR_PER_KILOWATT_HOUR)
    );
    public static final AttributeDescriptor<Double> TARIFF_START = new AttributeDescriptor<>("tariffStart", ValueType.NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );

    public static final AssetDescriptor<ElectricityChargerAsset> DESCRIPTOR = new AssetDescriptor<>("ev-station", "8A293D", ElectricityChargerAsset.class);

    public ElectricityChargerAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<String> getStatus() {
        return getAttributes().getValue(STATUS);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setStatus(String value) {
        getAttributes().getOrCreate(STATUS).setValue(value);
        return (T)this;
    }

    public Optional<String> getChargerType() {
        return getAttributes().getValue(CHARGER_TYPE);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setChargerType(String value) {
        getAttributes().getOrCreate(CHARGER_TYPE).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPowerCapacity() {
        return getAttributes().getValue(POWER_CAPACITY);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setPowerCapacity(Double value) {
        getAttributes().getOrCreate(POWER_CAPACITY).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPowerConsumption() {
        return getAttributes().getValue(POWER_CONSUMPTION);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setPowerConsumption(Double value) {
        getAttributes().getOrCreate(POWER_CONSUMPTION).setValue(value);
        return (T)this;
    }

    public Optional<Double> getTariffImport() {
        return getAttributes().getValue(TARIFF_IMPORT);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setTariffImport(Double value) {
        getAttributes().getOrCreate(TARIFF_IMPORT).setValue(value);
        return (T)this;
    }

    public Optional<Double> getTariffExport() {
        return getAttributes().getValue(TARIFF_EXPORT);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setTariffExport(Double value) {
        getAttributes().getOrCreate(TARIFF_EXPORT).setValue(value);
        return (T)this;
    }

    public Optional<Double> getTariffStart() {
        return getAttributes().getValue(TARIFF_START);
    }

    @SuppressWarnings("unchecked")
    public <T extends ElectricityChargerAsset> T setTariffStart(Double value) {
        getAttributes().getOrCreate(TARIFF_START).setValue(value);
        return (T)this;
    }
}
