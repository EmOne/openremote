/*
 * Copyright 2016, OpenRemote Inc.
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
package org.openremote.model.value.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

@JsonPropertyOrder({"red", "green", "blue", "amber", "white"})
public class ColourRGBAW extends ColourRGB {

    protected int amber;
    protected int white;

    @JsonCreator
    public ColourRGBAW(@JsonProperty("red") int red, @JsonProperty("green") int green, @JsonProperty("blue") int blue, @JsonProperty("amber") int amber, @JsonProperty("white") int white) {
        super(red, green, blue);
        this.amber = amber;
        this.white = white;
    }

    public int getAmber() {
        return amber;
    }

    public int getWhite() {
        return white;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColourRGBAW other = (ColourRGBAW) o;

        return super.equals(other) && amber == other.amber && white == other.white;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + Objects.hash(amber, white);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
            "red=" + red +
            ", green=" + green +
            ", blue=" + blue +
            ", amber=" + amber +
            ", white=" + white +
            '}';
    }
}
