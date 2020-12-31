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

    String UNITS_TEMPERATURE_CELSIUS = "CELSIUS";
    String UNITS_TEMPERATURE_FAHRENHEIT = "FAHRENHEIT";
    String UNITS_TIME_HOUR = "HOUR";
    String UNITS_TIME_MINUTE = "MINUTE";
    String UNITS_TIME_SECOND = "SECOND";
    String UNITS_TIME_MILLISECOND = "MILLISECOND";
    String UNITS_SPEED_KNOT = "SPEED_KNOT";
    String UNITS_SPEED_KILOMETERS_HOUR = "SPEED_KMPH";
    String UNITS_SPEED_MILES_HOUR = "SPEED_MIPH";
    String UNITS_SPEED_METRES_SECOND = "SPEED_MPS";
    String UNITS_SPEED_MILLIMETRES_HOUR = "SPEED_MMPH";
    String UNITS_SPEED_INCHES_HOUR = "SPEED_INPH";
    String UNITS_ACCELERATION_METRE_SECOND = "ACCELERATION_MS";
    String UNITS_SOUND_DECIBELS = "SOUND_DB";
    String UNITS_SOUND_DECIBELS_ATTENUATED = "SOUND_DBA";
    String UNITS_DISTANCE_KILOMETRE = "DISTANCE_KM";
    String UNITS_DISTANCE_MILE = "DISTANCE_MI";
    String UNITS_DISTANCE_METRE = "DISTANCE_M";
    String UNITS_DISTANCE_FOOT = "DISTANCE_FT";
    String UNITS_DISTANCE_CENTIMETRE = "DISTANCE_CM";
    String UNITS_DISTANCE_MILLIMETRE = "DISTANCE_MM";
    String UNITS_FLOW_LITRE_MINUTE = "FLOW_LPM";
    String UNITS_FLOW_LITRE_HOUR = "FLOW_LPH";
    String UNITS_FLOW_CUBIC_M_MINUTE = "FLOW_CMPM";
    String UNITS_FLOW_CUBIC_FOOT_MINUTE = "FLOW_CFPM";
    String UNITS_EUR_PER_KILOWATT_HOUR = "EUR_PER_KWH";
    String UNITS_EUR_PER_MONTH = "EUR_PER_MONTH";
    String UNITS_GBP_PER_KILOWATT_HOUR = "GBP_PER_KWH";
    String UNITS_GBP_PER_MONTH = "GBP_PER_MONTH";
    String UNITS_CURRENCY_EUR = "CURRENCY_EUR";
    String UNITS_CURRENCY_GBP = "CURRENCY_GBP";
    String UNITS_POWER_KILOWATT = "POWER_KW";
    String UNITS_POWER_WATT = "POWER_W";
    String UNITS_POWER_BTU_PER_HOUR = "POWER_BTU_PER_H";
    String UNITS_POWER_KILOWATT_PEAK = "POWER_KWP";
    String UNITS_POWER_WATT_SQUARE_M = "POWER_WATT_PER_M2";
    String UNITS_ENERGY_KILOWATT_HOUR = "ENERGY_KWH";
    String UNITS_ENERGY_JOULE = "ENERGY_J";
    String UNITS_KILOGRAM_CARBON_PER_KILOWATT_HOUR = "KG_CARBON_PER_KWH";
    String UNITS_MASS_KILOGRAM = "MASS_KG";
    String UNITS_MASS_POUND = "MASS_LB";
    String UNITS_ANGLE_DEGREE = "ANGLE_DEGREE";
    String UNITS_ANGLE_RADIAN = "ANGLE_RADIAN";
    String UNITS_PERCENTAGE = "PERCENTAGE";
    String UNITS_DENSITY_KILOGRAM_CUBIC_M = "DENSITY_KG_M3";
    String UNITS_DENSITY_MICROGRAM_CUBIC_M = "DENSITY_UG_M3";
    String UNITS_DENSITY_GRAM_CUBIC_M = "DENSITY_G_M3";
    String UNITS_DENSITY_PARTS_MILLION = "DENSITY_PPM";
    String UNITS_ON_OFF = "ON_OFF";
    String UNITS_PRESSED_RELEASED = "PRESSED_RELEASED";
    String UNITS_COUNT_PER_HOUR = "COUNT_PER_HOUR";
    String UNITS_COUNT_PER_MINUTE = "COUNT_PER_MINUTE";
    String UNITS_COUNT_PER_SECOND = "COUNT_PER_SECOND";
    String UNITS_BRIGHTNESS_LUX = "BRIGHTNESS_LUX";
    String UNITS_PRESSURE_KPA = "PRESSURE_KPA";
    String UNITS_PRESSURE_PA = "PRESSURE_PA";
    String UNITS_PRESSURE_BAR = "PRESSURE_BAR";
    String UNITS_PRESSURE_MILLIBAR = "PRESSURE_MBAR";
    String UNITS_PRESSURE_IN_HG = "PRESSURE_IN_HG";
    String UNITS_VOLTAGE_VOLT = "VOLTAGE_V";
    String UNITS_VOLTAGE_MILLIVOLT = "VOLTAGE_MV";
    String UNITS_RESISTANCE_OHM = "RESISTANCE_OHM";
    String UNITS_CURRENT_AMP = "CURRENT_A";
    String UNITS_CURRENT_MILLIAMP = "CURRENT_MA";
    String UNITS_VOLUME_LITRE = "VOLUME_L";
    String UNITS_VOLUME_CUBIC_METRE = "VOLUME_CM";
    String UNITS_VOLUME_CUBIC_FOOT = "VOLUME_CFT";
    String UNITS_VOLUME_GALLON = "VOLUME_GAL";
    String UNITS_FREQUENCY_HERTZ = "FREQUENCY_HZ";
    String UNITS_FREQUENCY_KILOHERTZ = "FREQUENCY_kHZ";
    String UNITS_FREQUENCY_RPM = "FREQUENCY_RPM";
    String UNITS_COLOR_RGB = "COLOR_RGB";
    String UNITS_COLOR_ARGB = "COLOR_ARGB";
    String UNITS_COLOR_RGBW = "COLOR_RGBW";
}
