/*
 * Copyright 2021, OpenRemote Inc.
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

import java.io.Serializable;

/**
 * Represents formatting rules to apply to number values when converting to {@link String} representation; based on
 * HTML Intl API, see: <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl/NumberFormat/NumberFormat">here</a>
 */
public class ValueFormat implements Serializable {

    public static final ValueFormat NO_DECIMAL_PLACES = new ValueFormat().setMaximumFractionDigits(0);
    public static final ValueFormat EXACTLY_1_DECIMAL_PLACES = new ValueFormat().setMinimumFractionDigits(1).setMaximumFractionDigits(1);
    public static final ValueFormat EXACTLY_2_DECIMAL_PLACES = new ValueFormat().setMinimumFractionDigits(2).setMaximumFractionDigits(2);
    public static final ValueFormat EXACTLY_3_DECIMAL_PLACES = new ValueFormat().setMinimumFractionDigits(3).setMaximumFractionDigits(3);
    public static final ValueFormat MAX_1_DECIMAL_PLACES = new ValueFormat().setMaximumFractionDigits(2);
    public static final ValueFormat MAX_2_DECIMAL_PLACES = new ValueFormat().setMaximumFractionDigits(2);
    public static final ValueFormat MAX_3_DECIMAL_PLACES = new ValueFormat().setMaximumFractionDigits(2);

    protected Boolean useGrouping;
    protected Integer minimumIntegerDigits;
    protected Integer minimumFractionDigits;
    protected Integer maximumFractionDigits;
    protected Integer minimumSignificantDigits;
    protected Integer maximumSignificantDigits;

    public Boolean getUseGrouping() {
        return useGrouping;
    }

    public ValueFormat setUseGrouping(Boolean useGrouping) {
        this.useGrouping = useGrouping;
        return this;
    }

    public Integer getMinimumIntegerDigits() {
        return minimumIntegerDigits;
    }

    public ValueFormat setMinimumIntegerDigits(Integer minimumIntegerDigits) {
        this.minimumIntegerDigits = minimumIntegerDigits;
        return this;
    }

    public Integer getMinimumFractionDigits() {
        return minimumFractionDigits;
    }

    public ValueFormat setMinimumFractionDigits(Integer minimumFractionDigits) {
        this.minimumFractionDigits = minimumFractionDigits;
        return this;
    }

    public Integer getMaximumFractionDigits() {
        return maximumFractionDigits;
    }

    public ValueFormat setMaximumFractionDigits(Integer maximumFractionDigits) {
        this.maximumFractionDigits = maximumFractionDigits;
        return this;
    }

    public Integer getMinimumSignificantDigits() {
        return minimumSignificantDigits;
    }

    public ValueFormat setMinimumSignificantDigits(Integer minimumSignificantDigits) {
        this.minimumSignificantDigits = minimumSignificantDigits;
        return this;
    }

    public Integer getMaximumSignificantDigits() {
        return maximumSignificantDigits;
    }

    public ValueFormat setMaximumSignificantDigits(Integer maximumSignificantDigits) {
        this.maximumSignificantDigits = maximumSignificantDigits;
        return this;
    }
}
