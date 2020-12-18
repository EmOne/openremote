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

import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.value.AttributeDescriptor;
import org.openremote.model.value.ValueType;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Optional;

@Entity
public class BuildingAsset extends CityAsset {

    public static final AttributeDescriptor<String> STREET = new AttributeDescriptor<>("street", ValueType.STRING);
    public static final AttributeDescriptor<String> POSTAL_CODE = new AttributeDescriptor<>("postalCode", ValueType.STRING);

    public static final AssetDescriptor<BuildingAsset> DESCRIPTOR = new AssetDescriptor<>("office-building", "4b5966", BuildingAsset.class);

    /**
     * For use by hydrators (i.e. JPA/Jackson)
     */
    BuildingAsset() {
        this(null);
    }

    public BuildingAsset(String name) {
        super(name);
    }

    public Optional<String> getStreet() {
        return getAttributes().getValue(STREET);
    }

    public Optional<String> getPostalCode() {
        return getAttributes().getValue(POSTAL_CODE);
    }
}
