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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

public abstract class AbstractNameValueDescriptorHolder<T> implements ValueDescriptorHolder<T>, NameHolder {

    @JsonIgnore
    protected String name;
    @JsonIgnore
    protected ValueDescriptor<T> type;

    AbstractNameValueDescriptorHolder() {}

    public AbstractNameValueDescriptorHolder(String name, ValueDescriptor<T> type) {
        this.name = name;
        this.type = type;
    }

    @JsonIgnore
    @Override
    public String getName() {
        return name;
    }

    @JsonIgnore
    @Override
    public ValueDescriptor<T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Descriptor names are unique identifiers so can use this for equality purposes
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AbstractNameValueDescriptorHolder<?> that = (AbstractNameValueDescriptorHolder<?>)obj;
        return Objects.equals(name, that.name);
    }
}
