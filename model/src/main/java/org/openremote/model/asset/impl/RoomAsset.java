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

public class RoomAsset extends Asset<RoomAsset> {

    public static final AssetDescriptor<RoomAsset> DESCRIPTOR = new AssetDescriptor<>("door", "2eaaa2", RoomAsset.class);

    protected RoomAsset(String name, AssetDescriptor<? extends RoomAsset> descriptor) {
        super(name, descriptor);
    }

    public RoomAsset(String name) {
        super(name, DESCRIPTOR);
    }
}
