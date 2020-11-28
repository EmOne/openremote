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

public class ParkingAsset extends Asset<ParkingAsset> {

    public static final AttributeDescriptor<Integer> SPACES_TOTAL = new AttributeDescriptor<>("spacesTotal", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_OCCUPIED = new AttributeDescriptor<>("spacesOccupied", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_OPEN = new AttributeDescriptor<>("spacesOpen", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> SPACES_BUFFER = new AttributeDescriptor<>("spacesBuffer", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Double> PRICE_HOURLY = new AttributeDescriptor<>("priceHourly", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );
    public static final AttributeDescriptor<Double> PRICE_DAILY = new AttributeDescriptor<>("priceDaily", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_CURRENCY_EUR)
    );

    public static final AssetDescriptor<ParkingAsset> DESCRIPTOR = new AssetDescriptor<>("parking", "0260ae", ParkingAsset.class);

    protected ParkingAsset(String name, AssetDescriptor<? extends ParkingAsset> descriptor) {
        super(name, descriptor);
    }

    public ParkingAsset(String name) {
        super(name, DESCRIPTOR);
    }

    public Optional<Integer> getSpacesTotal() {
        return getAttributes().getValue(SPACES_TOTAL);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setSpacesTotal(Integer value) {
        getAttributes().getOrCreate(SPACES_TOTAL).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getSpacesOccupied() {
        return getAttributes().getValue(SPACES_OCCUPIED);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setSpacesOccupied(Integer value) {
        getAttributes().getOrCreate(SPACES_OCCUPIED).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getSpacesOpen() {
        return getAttributes().getValue(SPACES_OPEN);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setSpacesOpen(Integer value) {
        getAttributes().getOrCreate(SPACES_OPEN).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getSpacesBuffer() {
        return getAttributes().getValue(SPACES_BUFFER);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setSpacesBuffer(Integer value) {
        getAttributes().getOrCreate(SPACES_BUFFER).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPriceHourly() {
        return getAttributes().getValue(PRICE_HOURLY);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setPriceHourly(Double value) {
        getAttributes().getOrCreate(PRICE_HOURLY).setValue(value);
        return (T)this;
    }

    public Optional<Double> getPriceDaily() {
        return getAttributes().getValue(PRICE_DAILY);
    }

    @SuppressWarnings("unchecked")
    public <T extends ParkingAsset> T setPriceDaily(Double value) {
        getAttributes().getOrCreate(PRICE_DAILY).setValue(value);
        return (T)this;
    }
}
