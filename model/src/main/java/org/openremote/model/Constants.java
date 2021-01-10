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
package org.openremote.model;

import org.openremote.model.util.TextUtil;
import org.openremote.model.util.TsIgnore;

@TsIgnore
public interface Constants {

    String KEYCLOAK_CLIENT_ID = "openremote";
    String MASTER_REALM = "master";
    String MASTER_REALM_ADMIN_USER = "admin";
    String REALM_ADMIN_ROLE = "admin";
    String READ_LOGS_ROLE = "read:logs";
    String READ_USERS_ROLE = "read:users";
    String READ_ADMIN_ROLE = "read:admin";
    String READ_MAP_ROLE = "read:map";
    String READ_ASSETS_ROLE = "read:assets";
    String READ_RULES_ROLE = "read:rules";
    String READ_APPS_ROLE = "read:apps";
    String WRITE_USER_ROLE = "write:user";
    String WRITE_ADMIN_ROLE = "write:admin";
    String WRITE_LOGS_ROLE = "write:logs";
    String WRITE_ASSETS_ROLE = "write:assets";
    String WRITE_ATTRIBUTES_ROLE = "write:attributes";
    String WRITE_RULES_ROLE = "write:rules";
    String AUTH_CONTEXT = "AUTH_CONTEXT";
    int ACCESS_TOKEN_LIFESPAN_SECONDS = 60; // 1 minute
    String PERSISTENCE_SEQUENCE_ID_GENERATOR = "SEQUENCE_ID_GENERATOR";
    String PERSISTENCE_UNIQUE_ID_GENERATOR = "UNIQUE_ID_GENERATOR";
    String PERSISTENCE_JSON_VALUE_TYPE = "jsonb";
    String PERSISTENCE_STRING_ARRAY_TYPE = "string-array";
    String NAMESPACE = "urn:openremote";
    String ASSET_NAMESPACE = NAMESPACE + ":asset";
    String AGENT_NAMESPACE = NAMESPACE + ":agent";
    String ASSET_META_NAMESPACE = ASSET_NAMESPACE + ":meta";
    String DEFAULT_DATETIME_FORMAT ="dd. MMM yyyy HH:mm:ss zzz";
    String DEFAULT_DATETIME_FORMAT_MILLIS ="dd. MMM yyyy HH:mm:ss:SSS zzz";
    String DEFAULT_DATE_FORMAT ="dd. MMM yyyy";
    String DEFAULT_TIME_FORMAT ="HH:mm:ss";

    String SETUP_EMAIL_USER = "SETUP_EMAIL_USER";
    String SETUP_EMAIL_HOST = "SETUP_EMAIL_HOST";
    String SETUP_EMAIL_PASSWORD = "SETUP_EMAIL_PASSWORD";
    String SETUP_EMAIL_PORT = "SETUP_EMAIL_PORT";
    int SETUP_EMAIL_PORT_DEFAULT = 25;
    String SETUP_EMAIL_TLS = "SETUP_EMAIL_TLS";
    boolean SETUP_EMAIL_TLS_DEFAULT = true;
    String SETUP_EMAIL_FROM = "SETUP_EMAIL_FROM";
    String SETUP_EMAIL_FROM_DEFAULT = "no-reply@openremote.io";
    String REQUEST_HEADER_REALM = "Auth-Realm";


    static String UnitsWithCategory(String category, String...units) {
        return TextUtil.isNullOrEmpty(category) ? Units(units) : category + ":" + Units(units);
    }

    static String Units(String...units) {
        return String.join("-", units);
    }

    String UNITS_FORMAT_CURRENCY = "currency";
    String UNITS_FORMAT_LENGTH = "length";
    String UNITS_FORMAT_MASS = "mass";
    String UNITS_FORMAT_TEMPERATURE = "temperature";
    String UNITS_FORMAT_DURATION = "duration";
    String UNITS_FORMAT_SPEED = "speed";

    String UNITS_BINARY_ON_OFF = "binary_on_off";
    String UNITS_BINARY_OPEN_CLOSED = "binary_open_closed";
    String UNITS_BINARY_0_1 = "binary_0_1";
    String UNITS_BINARY_RELEASED_PRESSED = "binary_released_pressed";

    String UNITS_DATE_DAY_MONTH_YEAR = "date_dmy";
    String UNITS_DATE_WEEK_NUMBER = "date_week";
    String UNITS_DATE_DAY_MONTH_YEAR_TIME_WITHOUT_SECONDS = "date_dmy_time";
    String UNITS_DATE_DAY_MONTH_YEAR_TIME_WITH_SECONDS = "date_dmy_time_s";
    String UNITS_DATE_TIME_WITHOUT_SECONDS = "date_time";
    String UNITS_DATE_TIME_WITH_SECONDS = "date_time_s";
    String UNITS_DATE_ISO8601 = "date_iso";

    String UNITS_PER = "per";
    String UNITS_SQUARED = "squared";
    String UNITS_CUBED = "cubed";
    String UNITS_PEAK = "peak";
    String UNITS_MEGA = "mega";
    String UNITS_KILO = "kilo";
    String UNITS_CENTI = "centi";
    String UNITS_MICRO = "micro";
    String UNITS_MILLI = "milli";

    String UNITS_PERCENTAGE = "percentage";
    String UNITS_ACRE = "acre";
    String UNITS_HECTARE = "hectare";
    String UNITS_DECIBEL = "decibel";
    String UNITS_DECIBEL_ATTENUATED = "decibel_attenuated";
    String UNITS_CELSIUS = "celsius";
    String UNITS_KELVIN = "kelvin";
    String UNITS_FAHRENHEIT = "fahrenheit";
    String UNITS_YEAR = "year";
    String UNITS_MONTH = "month";
    String UNITS_WEEK = "week";
    String UNITS_DAY = "day";
    String UNITS_HOUR = "hour";
    String UNITS_MINUTE = "minute";
    String UNITS_SECOND = "second";
    String UNITS_METRE = "metre";
    String UNITS_INCH = "inch";
    String UNITS_FOOT = "foot";
    String UNITS_YARD = "yard";
    String UNITS_MILE = "mile";
    String UNITS_MILE_SCANDINAVIAN = "mile_scandinavian";
    String UNITS_GRAM = "gram";
    String UNITS_OUNCE = "ounce";
    String UNITS_MASS_POUND = "pound";
    String UNITS_STONE = "stone";
    String UNITS_DEGREE = "degree";
    String UNITS_RADIAN = "radian";
    String UNITS_LITRE = "litre";
    String UNITS_GALLON = "gallon";
    String UNITS_FLUID_OUNCE = "fluid_ounce";
    String UNITS_JOULE = "joule";
    String UNITS_BTU = "btu";
    String UNITS_WATT = "watt";
    String UNITS_LUX = "lux";
    String UNITS_LUMEN = "lumen";
    String UNITS_PA = "pascal";
    String UNITS_BAR = "bar";
    String UNITS_IN_HG = "inch_mercury";
    String UNITS_VOLT = "volt";
    String UNITS_OHM = "ohm";
    String UNITS_AMP = "amp";
    String UNITS_HERTZ = "hertz";
    String UNITS_RPM = "rpm";
}
