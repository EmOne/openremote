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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public abstract class AbstractNameValueDescriptorHolder<T> implements ValueDescriptorHolder<T>, NameHolder {

    protected String name;
    @JsonSerialize(converter = ValueDescriptor.ValueDescriptorStringConverter.class)
    @JsonDeserialize(converter = ValueDescriptor.StringValueDescriptorConverter.class)
    protected ValueDescriptor<T> valueDescriptor;

    public AbstractNameValueDescriptorHolder(String name, ValueDescriptor<T> valueDescriptor) {
        this.name = name;
        this.valueDescriptor = valueDescriptor;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ValueDescriptor<T> getValueType() {
        return valueDescriptor;
    }
}
