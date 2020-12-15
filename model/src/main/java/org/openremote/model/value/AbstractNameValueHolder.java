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

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openremote.model.util.TextUtil;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;

@JsonFilter("excludeNameFilter")
public abstract class AbstractNameValueHolder<T> implements NameValueHolder<T> {

    @JsonSerialize(converter = ValueDescriptor.ValueDescriptorStringConverter.class)
    @JsonDeserialize(converter = ValueDescriptor.StringValueDescriptorConverter.class)
    protected ValueDescriptor<T> type;
    @Valid
    protected T value;
    @NotBlank(message = "{Asset.valueHolder.name.NotBlank}")
    protected String name;

    protected AbstractNameValueHolder() {
    }

    public AbstractNameValueHolder(@NotNull String name, @NotNull ValueDescriptor<T> type, T value) {
        if (TextUtil.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public ValueDescriptor<T> getValueType() {
        return type;
    }

    @Override
    public Optional<T> getValue() {
        return Optional.ofNullable(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> Optional<U> getValueAs(Class<U> valueType) {
        if (valueType.isAssignableFrom(getValueType().getType())) {
            return Optional.ofNullable((U)value);
        }
        return Optional.empty();
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractNameValueHolder<?> that = (AbstractNameValueHolder<?>) o;
        return name.equals(that.name)
            && Objects.equals(type, that.type)
            && Objects.equals(Values.convert(value, JsonNode.class), Values.convert(that.value, JsonNode.class));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type, name);
    }
}
