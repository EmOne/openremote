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

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueType;

import java.util.Optional;

public class CityAsset extends Asset {

    public static final AttributeDescriptor<String> CITY = new AttributeDescriptor<>("city", ValueType.STRING);
    public static final AttributeDescriptor<String> COUNTRY = new AttributeDescriptor<>("country", ValueType.STRING);

    public static final AssetDescriptor<CityAsset> DESCRIPTOR = new AssetDescriptor<>("city", null, CityAsset.class);

    public CityAsset(String name) {
        super(name, DESCRIPTOR);
    }

    protected <T extends AssetDescriptor<U>, U extends CityAsset> CityAsset(String name, T descriptor) {
        super(name, descriptor);
    }

    public Optional<String> getCity() {
        return getAttributes().getValue(CITY);
    }

    public Optional<String> getCountry() {
        return getAttributes().getValue(COUNTRY);
    }
}
