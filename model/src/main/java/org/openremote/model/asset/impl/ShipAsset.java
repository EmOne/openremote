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

public class ShipAsset extends Asset<ShipAsset> {

    public static final AttributeDescriptor<Integer> MSSI_NUMBER = new AttributeDescriptor<>("mSSINumber", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> IMO_NUMBER = new AttributeDescriptor<>("iMONumber", ValueType.POSITIVE_INTEGER);
    public static final AttributeDescriptor<Integer> DIRECTION = new AttributeDescriptor<>("direction", ValueType.DIRECTION);
    public static final AttributeDescriptor<Integer> LENGTH = new AttributeDescriptor<>("length", ValueType.POSITIVE_INTEGER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_DISTANCE_METRE)
    );
    public static final AttributeDescriptor<Double> SPEED = new AttributeDescriptor<>("speed", ValueType.POSITIVE_NUMBER,
        new MetaItem<>(MetaItemType.UNIT_TYPE, Constants.UNITS_SPEED_KNOT)
    );
    public static final AttributeDescriptor<String> SHIP_TYPE = new AttributeDescriptor<>("shipType", ValueType.STRING);

    public static final AssetDescriptor<ShipAsset> DESCRIPTOR = new AssetDescriptor<>("ferry", "000080", ShipAsset.class);

    protected ShipAsset(String name, AssetDescriptor<? extends ShipAsset> descriptor) {
        super(name, descriptor);
    }

    public ShipAsset(String name) {
        this(name, DESCRIPTOR);
    }

    public Optional<Integer> getMSSINumber() {
        return getAttributes().getValue(MSSI_NUMBER);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setMSSINumber(Integer value) {
        getAttributes().getOrCreate(MSSI_NUMBER).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getIMONumber() {
        return getAttributes().getValue(IMO_NUMBER);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setIMONumber(Integer value) {
        getAttributes().getOrCreate(IMO_NUMBER).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getDirection() {
        return getAttributes().getValue(DIRECTION);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setDirection(Integer value) {
        getAttributes().getOrCreate(DIRECTION).setValue(value);
        return (T)this;
    }

    public Optional<Integer> getLength() {
        return getAttributes().getValue(LENGTH);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setLength(Integer value) {
        getAttributes().getOrCreate(LENGTH).setValue(value);
        return (T)this;
    }

    public Optional<Double> getSpeed() {
        return getAttributes().getValue(SPEED);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setSpeed(Double value) {
        getAttributes().getOrCreate(SPEED).setValue(value);
        return (T)this;
    }

    public Optional<String> getShipType() {
        return getAttributes().getValue(SHIP_TYPE);
    }

    @SuppressWarnings("unchecked")
    public <T extends ShipAsset> T setShipType(String value) {
        getAttributes().getOrCreate(SHIP_TYPE).setValue(value);
        return (T)this;
    }
}
