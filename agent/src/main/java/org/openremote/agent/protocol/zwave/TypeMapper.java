/*
 * Copyright 2018, OpenRemote Inc.
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
package org.openremote.agent.protocol.zwave;

import org.openremote.model.attribute.MetaItem;
import org.openremote.model.value.MetaItemType;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueType;
import org.openremote.protocol.zwave.model.commandclasses.channel.ChannelType;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.openremote.model.Constants.*;

public class TypeMapper {

    private static final Logger LOG = Logger.getLogger(TypeMapper.class.getName());

    static private Map<ChannelType, ValueDescriptor<?>> typeMap = new HashMap<>();

    static {

        // Basic types

        typeMap.put(ChannelType.INTEGER, ValueType.INTEGER);
        typeMap.put(ChannelType.NUMBER, ValueType.NUMBER);
        typeMap.put(ChannelType.STRING, ValueType.STRING);
        typeMap.put(ChannelType.BOOLEAN, ValueType.BOOLEAN);
        typeMap.put(ChannelType.ARRAY, ValueType.OBJECT.asArray());

        // COMMAND_CLASS_SENSOR_MULTILEVEL

        typeMap.put(ChannelType.TEMPERATURE_CELSIUS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)));
        typeMap.put(ChannelType.TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT)));
        typeMap.put(ChannelType.PERCENTAGE, ValueType.PERCENTAGE_INTEGER_0_100);
        typeMap.put(ChannelType.LUMINANCE_PERCENTAGE, ValueType.PERCENTAGE_INTEGER_0_100);
        typeMap.put(ChannelType.LUMINANCE_LUX, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_BRIGHTNESS_LUX)));
        typeMap.put(ChannelType.POWER_WATT, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT)));
        typeMap.put(ChannelType.POWER_BTU_H, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_BTU_PER_HOUR)));
        typeMap.put(ChannelType.HUMIDITY_PERCENTAGE, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE)));
        typeMap.put(ChannelType.HUMIDITY_ABSOLUTE, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DENSITY_GRAM_CUBIC_M)));
        typeMap.put(ChannelType.SPEED_MS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_METRES_SECOND)));
        typeMap.put(ChannelType.SPEED_MPH, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILES_HOUR)));
        typeMap.put(ChannelType.DIRECTION_DECIMAL_DEGREES, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE)));
        typeMap.put(ChannelType.PRESSURE_KPA, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_KPA)));
        typeMap.put(ChannelType.PRESSURE_IN_HG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_IN_HG)));
        typeMap.put(ChannelType.SOLAR_RADIATION_WATT_M2, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT_SQUARE_M)));
        typeMap.put(ChannelType.DEW_POINT_CELSIUS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)));
        typeMap.put(ChannelType.DEW_POINT_FAHRENHEIT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT)));
        typeMap.put(ChannelType.RAINFALL_MMPH, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILLIMETRES_HOUR)));
        typeMap.put(ChannelType.RAINFALL_INPH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SPEED_MILLIMETRES_HOUR)));
        typeMap.put(ChannelType.TIDE_LEVEL_M, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_METRE)));
        typeMap.put(ChannelType.TIDE_LEVEL_FT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_FOOT)));
        typeMap.put(ChannelType.WEIGHT_KG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM)));
        typeMap.put(ChannelType.WEIGHT_LB, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_POUND)));
        typeMap.put(ChannelType.VOLTAGE_V, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_VOLT)));
        typeMap.put(ChannelType.VOLTAGE_MV, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_MILLIVOLT)));
        typeMap.put(ChannelType.CURRENT_A, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_AMP)));
        typeMap.put(ChannelType.CURRENT_MA, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_MILLIAMP)));
        typeMap.put(ChannelType.CO2_PPM, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DENSITY_PARTS_MILLION)));
        typeMap.put(ChannelType.AIR_FLOW_CMPH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_CUBIC_M_MINUTE)));
        typeMap.put(ChannelType.AIR_FLOW_CFTPM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_CUBIC_FOOT_MINUTE)));
        typeMap.put(ChannelType.TANK_CAPACITY_L, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_LITRE)));
        typeMap.put(ChannelType.TANK_CAPACITY_CBM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE)));
        typeMap.put(ChannelType.TANK_CAPACITY_GAL, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_GALLON)));
        typeMap.put(ChannelType.DISTANCE_M, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_METRE)));
        typeMap.put(ChannelType.DISTANCE_CM, ValueType.POSITIVE_NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_CENTIMETRE)));
        typeMap.put(ChannelType.DISTANCE_FT, ValueType.POSITIVE_NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_DISTANCE_FOOT)));
        typeMap.put(ChannelType.ANGLE_POSITION_PERCENT, ValueType.PERCENTAGE_INTEGER_0_100);
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_NORTH_POLE, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE)));
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_SOUTH_POLE, ValueType.INTEGER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ANGLE_DEGREE)));
        typeMap.put(ChannelType.ROTATION_HZ, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_HERTZ)));
        typeMap.put(ChannelType.ROTATION_RPM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_RPM)));
        typeMap.put(ChannelType.WATER_TEMPERATURE_CELSIUS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)));
        typeMap.put(ChannelType.WATER_TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT)));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_CELSIUS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT)));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_MERCALLI, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_EU_MACROSEISMIC, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_LIEDU, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_SHINDO, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_LOCAL, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_MOMENT, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_SURFACE_WAVE, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_BODY_WAVE, ValueType.NUMBER);
        typeMap.put(ChannelType.ULTRAVIOLET_UV_INDEX, ValueType.NUMBER);
        typeMap.put(ChannelType.RESISTIVITY_OHM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_RESISTANCE_OHM)));
        typeMap.put(ChannelType.CONDUCTIVITY_SPM, ValueType.NUMBER);
        typeMap.put(ChannelType.LOUDNESS_DB, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SOUND_DECIBELS)));
        typeMap.put(ChannelType.LOUDNESS_DBA, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_SOUND_DECIBELS_ATTENUATED)));
        typeMap.put(ChannelType.MOISTURE_PERCENTAGE, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE)));
        typeMap.put(ChannelType.MOISTURE_VOLUME_WATER_CONTENT, ValueType.NUMBER);
        typeMap.put(ChannelType.MOISTURE_IMPEDANCE, ValueType.NUMBER);
        typeMap.put(ChannelType.MOISTURE_WATER_ACTIVITY, ValueType.NUMBER);
        typeMap.put(ChannelType.FREQUENCY_HZ, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_HERTZ)));
        typeMap.put(ChannelType.FREQUENCY_KHZ, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FREQUENCY_KILOHERTZ)));
        typeMap.put(ChannelType.TIME_SECONDS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TIME_SECOND)));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_CELSIUS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_CELSIUS)));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_FAHRENHEIT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_TEMPERATURE_FAHRENHEIT)));
        typeMap.put(ChannelType.PARTICULATE_MATTER_2_5_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.PARTICULATE_MATTER_2_5_MCGPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.FORMALDEHYDE_LEVEL_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.RADON_CONCENTRATION_BQPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.RADON_CONCENTRATION_PCIPL, ValueType.NUMBER);
        typeMap.put(ChannelType.METHANE_DENSITY_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.VOLATILE_ORGANIC_COMPOUND_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.VOLATILE_ORGANIC_COMPOUND_PPM, ValueType.NUMBER);
        typeMap.put(ChannelType.CO_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.CO_PPM, ValueType.NUMBER);
        typeMap.put(ChannelType.SOIL_HUMIDITY_PERCENTAGE, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE)));
        typeMap.put(ChannelType.SOIL_REACTIVITY_PH, ValueType.NUMBER);
        typeMap.put(ChannelType.SOIL_SALINITY_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.HEART_RATE_BPM, ValueType.NUMBER);
        typeMap.put(ChannelType.BLOOD_PRESSURE_SYSTOLIC, ValueType.NUMBER);
        typeMap.put(ChannelType.BLOOD_PRESSURE_DIASTOLIC, ValueType.NUMBER);
        typeMap.put(ChannelType.MUSCLE_MASS_KG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM)));
        typeMap.put(ChannelType.FAT_MASS_KG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM)));
        typeMap.put(ChannelType.BONE_MASS_KG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM)));
        typeMap.put(ChannelType.TOTAL_BODY_WATER_KG, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_MASS_KILOGRAM)));
        typeMap.put(ChannelType.BASIC_METABOLIC_RATE_JOULE, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_JOULE)));
        typeMap.put(ChannelType.BODY_MASS_INDEX, ValueType.NUMBER);
        typeMap.put(ChannelType.ACCELERATION_X_MPSS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND)));
        typeMap.put(ChannelType.ACCELERATION_Y_MPSS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND)));
        typeMap.put(ChannelType.ACCELERATION_Z_MPSS, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ACCELERATION_METRE_SECOND)));
        typeMap.put(ChannelType.SMOKE_DENSITY_PERCENTAGE, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE)));
        typeMap.put(ChannelType.WATER_FLOW_LPH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_FLOW_LITRE_HOUR)));
        typeMap.put(ChannelType.WATER_PRESSURE_KPA, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PRESSURE_KPA)));
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_RSSI, ValueType.NUMBER);
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_DBM, ValueType.NUMBER);
        typeMap.put(ChannelType.PARTICULATE_MATTER_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.PARTICULATE_MATTER_MCGPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.RESPIRATORY_RATE_BPM, ValueType.NUMBER);

        // COMMAND_CLASS_METER

        // Electric Meter
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KWH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR)));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVAH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR)));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_W, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_WATT)));
        typeMap.put(ChannelType.ELECTRIC_METER_PULSE_COUNT, ValueType.NUMBER);
        typeMap.put(ChannelType.ELECTRIC_METER_VOLTAGE_V, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLTAGE_VOLT)));
        typeMap.put(ChannelType.ELECTRIC_METER_CURRENT_A, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_CURRENT_AMP)));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_FACTOR, ValueType.NUMBER);
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_KVAR, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_POWER_KILOWATT)));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVARH, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ENERGY_KILOWATT_HOUR)));

        // Gas Meter
        typeMap.put(ChannelType.GAS_METER_VOLUME_CM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE)));
        typeMap.put(ChannelType.GAS_METER_VOLUME_CFT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_FOOT)));
        typeMap.put(ChannelType.GAS_METER_PULSE_COUNT, ValueType.NUMBER);

        // Water Meter
        typeMap.put(ChannelType.WATER_METER_VOLUME_CM, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_METRE)));
        typeMap.put(ChannelType.WATER_METER_VOLUME_CFT, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_CUBIC_FOOT)));
        typeMap.put(ChannelType.WATER_METER_VOLUME_GAL, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_VOLUME_GALLON)));
        typeMap.put(ChannelType.WATER_METER_PULSE_COUNT, ValueType.NUMBER);

        // COMMAND_CLASS_COLOR_CONTROL

        typeMap.put(ChannelType.COLOR_WARM_WHITE, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_COLD_WHITE, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_RED, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_GREEN, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_BLUE, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_AMBER, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_CYAN, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_PURPLE, ValueType.INT_BYTE);
        typeMap.put(ChannelType.COLOR_INDEXED, ValueType.NUMBER);
        typeMap.put(ChannelType.COLOR_RGB, ValueType.COLOUR_RGB.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_COLOR_RGB)));
        typeMap.put(ChannelType.COLOR_ARGB, ValueType.COLOUR_RGB.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_COLOR_ARGB)));

        // COMMAND_CLASS_SENSOR_ALARM

        typeMap.put(ChannelType.GENERAL_PURPOSE_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.SMOKE_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.CO_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.CO2_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.HEAT_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.WATER_LEAK_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));
        typeMap.put(ChannelType.FIRST_SUPPORTED_ALARM, ValueType.BOOLEAN.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_ON_OFF)));

        // COMMAND_CLASS_BATTERY

        typeMap.put(ChannelType.CHARGE_PERCENTAGE, ValueType.NUMBER.addOrReplaceMeta(new MetaItem<>(MetaItemType.UNIT_TYPE, UNITS_PERCENTAGE)));

        // COMMAND_CLASS_CLOCK

        typeMap.put(ChannelType.DATETIME, ValueType.TIMESTAMP_ISO8601);
    }

    public static ValueDescriptor<?> toAttributeType(ChannelType channelType) {

        ValueDescriptor<?> valueType = ValueType.STRING;

        if (typeMap.containsKey(channelType)) {
            valueType = typeMap.get(channelType);
        } else {
            switch(channelType.getValueType()) {
                case INTEGER:
                    valueType = ValueType.INTEGER;
                    break;
                case NUMBER:
                    valueType = ValueType.NUMBER;
                    break;
                case BOOLEAN:
                    valueType = ValueType.BOOLEAN;
                    break;
                case STRING:
                    valueType = ValueType.STRING;
                    break;
                case ARRAY:
                    valueType = ValueType.OBJECT.asArray();
                    break;
            }
        }
        return valueType;
    }
}
