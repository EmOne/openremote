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

import org.openremote.model.value.ValueConstraint;
import org.openremote.model.value.ValueDescriptor;
import org.openremote.model.value.ValueFormat;
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
        typeMap.put(ChannelType.ARRAY, ValueType.JSON_OBJECT.asArray());

        // COMMAND_CLASS_SENSOR_MULTILEVEL

        typeMap.put(ChannelType.TEMPERATURE_CELSIUS, ValueType.NUMBER.withUnits(UNITS_CELSIUS).withFormat(ValueFormat.EXACTLY_1_DECIMAL_PLACES));
        typeMap.put(ChannelType.TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.withUnits(UNITS_FAHRENHEIT).withFormat(ValueFormat.EXACTLY_1_DECIMAL_PLACES));
        typeMap.put(ChannelType.PERCENTAGE, ValueType.NUMBER.withUnits(UNITS_PERCENTAGE));
        typeMap.put(ChannelType.LUMINANCE_PERCENTAGE, ValueType.INTEGER.withUnits(UNITS_PERCENTAGE).withConstraints(new ValueConstraint.Min(0), new ValueConstraint.Max(100)));
        typeMap.put(ChannelType.LUMINANCE_LUX, ValueType.INTEGER.withUnits(UNITS_LUX));
        typeMap.put(ChannelType.POWER_WATT, ValueType.INTEGER.withUnits(UNITS_WATT));
        typeMap.put(ChannelType.POWER_BTU_H, ValueType.NUMBER.withUnits(UNITS_BTU, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.HUMIDITY_PERCENTAGE, ValueType.INTEGER.withUnits(UNITS_PERCENTAGE));
        typeMap.put(ChannelType.HUMIDITY_ABSOLUTE, ValueType.INTEGER.withUnits(UNITS_GRAM, UNITS_PER, UNITS_METRE, UNITS_CUBED));
        typeMap.put(ChannelType.SPEED_MS, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_PER, UNITS_SECOND));
        typeMap.put(ChannelType.SPEED_MPH, ValueType.INTEGER.withUnits(UNITS_MILE, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.DIRECTION_DECIMAL_DEGREES, ValueType.INTEGER.withUnits(UNITS_DEGREE));
        typeMap.put(ChannelType.PRESSURE_KPA, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_PASCAL));
        typeMap.put(ChannelType.PRESSURE_IN_HG, ValueType.NUMBER.withUnits(UNITS_IN_HG));
        typeMap.put(ChannelType.SOLAR_RADIATION_WATT_M2, ValueType.NUMBER.withUnits(UNITS_WATT, UNITS_PER, UNITS_METRE, UNITS_SQUARED));
        typeMap.put(ChannelType.DEW_POINT_CELSIUS, ValueType.NUMBER.withUnits(UNITS_CELSIUS));
        typeMap.put(ChannelType.DEW_POINT_FAHRENHEIT, ValueType.NUMBER.withUnits(UNITS_FAHRENHEIT));
        typeMap.put(ChannelType.RAINFALL_MMPH, ValueType.INTEGER.withUnits(UNITS_MILLI, UNITS_METRE, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.RAINFALL_INPH, ValueType.NUMBER.withUnits(UNITS_INCH, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.TIDE_LEVEL_M, ValueType.NUMBER.withUnits(UNITS_METRE));
        typeMap.put(ChannelType.TIDE_LEVEL_FT, ValueType.NUMBER.withUnits(UNITS_FOOT));
        typeMap.put(ChannelType.WEIGHT_KG, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_GRAM));
        typeMap.put(ChannelType.WEIGHT_LB, ValueType.NUMBER.withUnits(UNITS_MASS_POUND));
        typeMap.put(ChannelType.VOLTAGE_V, ValueType.NUMBER.withUnits(UNITS_VOLT));
        typeMap.put(ChannelType.VOLTAGE_MV, ValueType.NUMBER.withUnits(UNITS_MILLI, UNITS_VOLT));
        typeMap.put(ChannelType.CURRENT_A, ValueType.NUMBER.withUnits(UNITS_AMP));
        typeMap.put(ChannelType.CURRENT_MA, ValueType.NUMBER.withUnits(UNITS_MILLI, UNITS_AMP));
        typeMap.put(ChannelType.CO2_PPM, ValueType.INTEGER.withUnits(UNITS_PART_PER_MILLION));
        typeMap.put(ChannelType.AIR_FLOW_CMPH, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_CUBED, UNITS_PER, UNITS_MINUTE));
        typeMap.put(ChannelType.AIR_FLOW_CFTPM, ValueType.NUMBER.withUnits(UNITS_FOOT, UNITS_CUBED, UNITS_PER, UNITS_MINUTE));
        typeMap.put(ChannelType.TANK_CAPACITY_L, ValueType.NUMBER.withUnits(UNITS_LITRE));
        typeMap.put(ChannelType.TANK_CAPACITY_CBM, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_CUBED));
        typeMap.put(ChannelType.TANK_CAPACITY_GAL, ValueType.NUMBER.withUnits(UNITS_GALLON));
        typeMap.put(ChannelType.DISTANCE_M, ValueType.NUMBER.withUnits(UNITS_METRE));
        typeMap.put(ChannelType.DISTANCE_CM, ValueType.POSITIVE_NUMBER.withUnits(UNITS_CENTI, UNITS_METRE));
        typeMap.put(ChannelType.DISTANCE_FT, ValueType.POSITIVE_NUMBER.withUnits(UNITS_FOOT));
        typeMap.put(ChannelType.ANGLE_POSITION_PERCENT, ValueType.INTEGER.withUnits(UNITS_PERCENTAGE).withConstraints(new ValueConstraint.Min(0), new ValueConstraint.Max(100)));
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_NORTH_POLE, ValueType.INTEGER.withUnits(UNITS_DEGREE));
        typeMap.put(ChannelType.ANGLE_POSITION_DEGREE_SOUTH_POLE, ValueType.INTEGER.withUnits(UNITS_DEGREE));
        typeMap.put(ChannelType.ROTATION_HZ, ValueType.NUMBER.withUnits(UNITS_HERTZ));
        typeMap.put(ChannelType.ROTATION_RPM, ValueType.NUMBER.withUnits(UNITS_RPM));
        typeMap.put(ChannelType.WATER_TEMPERATURE_CELSIUS, ValueType.NUMBER.withUnits(UNITS_CELSIUS));
        typeMap.put(ChannelType.WATER_TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.withUnits(UNITS_FAHRENHEIT));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_CELSIUS, ValueType.NUMBER.withUnits(UNITS_CELSIUS));
        typeMap.put(ChannelType.SOIL_TEMPERATURE_FAHRENHEIT, ValueType.NUMBER.withUnits(UNITS_FAHRENHEIT));
        typeMap.put(ChannelType.SEISMIC_INTENSITY_MERCALLI, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_EU_MACROSEISMIC, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_LIEDU, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_INTENSITY_SHINDO, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_LOCAL, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_MOMENT, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_SURFACE_WAVE, ValueType.NUMBER);
        typeMap.put(ChannelType.SEISMIC_MAGNITUDE_BODY_WAVE, ValueType.NUMBER);
        typeMap.put(ChannelType.ULTRAVIOLET_UV_INDEX, ValueType.NUMBER);
        typeMap.put(ChannelType.RESISTIVITY_OHM, ValueType.NUMBER.withUnits(UNITS_OHM));
        typeMap.put(ChannelType.CONDUCTIVITY_SPM, ValueType.NUMBER);
        typeMap.put(ChannelType.LOUDNESS_DB, ValueType.NUMBER.withUnits(UNITS_DECIBEL));
        typeMap.put(ChannelType.LOUDNESS_DBA, ValueType.NUMBER.withUnits(UNITS_DECIBEL_ATTENUATED));
        typeMap.put(ChannelType.MOISTURE_PERCENTAGE, ValueType.NUMBER.withUnits(UNITS_PERCENTAGE));
        typeMap.put(ChannelType.MOISTURE_VOLUME_WATER_CONTENT, ValueType.NUMBER);
        typeMap.put(ChannelType.MOISTURE_IMPEDANCE, ValueType.NUMBER);
        typeMap.put(ChannelType.MOISTURE_WATER_ACTIVITY, ValueType.NUMBER);
        typeMap.put(ChannelType.FREQUENCY_HZ, ValueType.NUMBER.withUnits(UNITS_HERTZ));
        typeMap.put(ChannelType.FREQUENCY_KHZ, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_HERTZ));
        typeMap.put(ChannelType.TIME_SECONDS, ValueType.NUMBER.withUnits(UNITS_SECOND));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_CELSIUS, ValueType.NUMBER.withUnits(UNITS_CELSIUS));
        typeMap.put(ChannelType.TARGET_TEMPERATUE_FAHRENHEIT, ValueType.NUMBER.withUnits(UNITS_FAHRENHEIT));
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
        typeMap.put(ChannelType.SOIL_HUMIDITY_PERCENTAGE, ValueType.NUMBER.withUnits(UNITS_PERCENTAGE));
        typeMap.put(ChannelType.SOIL_REACTIVITY_PH, ValueType.NUMBER);
        typeMap.put(ChannelType.SOIL_SALINITY_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.HEART_RATE_BPM, ValueType.NUMBER);
        typeMap.put(ChannelType.BLOOD_PRESSURE_SYSTOLIC, ValueType.NUMBER);
        typeMap.put(ChannelType.BLOOD_PRESSURE_DIASTOLIC, ValueType.NUMBER);
        typeMap.put(ChannelType.MUSCLE_MASS_KG, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_GRAM));
        typeMap.put(ChannelType.FAT_MASS_KG, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_GRAM));
        typeMap.put(ChannelType.BONE_MASS_KG, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_GRAM));
        typeMap.put(ChannelType.TOTAL_BODY_WATER_KG, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_GRAM));
        typeMap.put(ChannelType.BASIC_METABOLIC_RATE_JOULE, ValueType.NUMBER.withUnits(UNITS_JOULE));
        typeMap.put(ChannelType.BODY_MASS_INDEX, ValueType.NUMBER);
        typeMap.put(ChannelType.ACCELERATION_X_MPSS, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_PER, UNITS_SECOND, UNITS_SQUARED));
        typeMap.put(ChannelType.ACCELERATION_Y_MPSS, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_PER, UNITS_SECOND, UNITS_SQUARED));
        typeMap.put(ChannelType.ACCELERATION_Z_MPSS, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_PER, UNITS_SECOND, UNITS_SQUARED));
        typeMap.put(ChannelType.SMOKE_DENSITY_PERCENTAGE, ValueType.NUMBER.withUnits(UNITS_PERCENTAGE));
        typeMap.put(ChannelType.WATER_FLOW_LPH, ValueType.NUMBER.withUnits(UNITS_LITRE, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.WATER_PRESSURE_KPA, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_PASCAL));
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_RSSI, ValueType.NUMBER);
        typeMap.put(ChannelType.RF_SIGNAL_STRENGTH_DBM, ValueType.NUMBER);
        typeMap.put(ChannelType.PARTICULATE_MATTER_MOLPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.PARTICULATE_MATTER_MCGPCM, ValueType.NUMBER);
        typeMap.put(ChannelType.RESPIRATORY_RATE_BPM, ValueType.NUMBER);

        // COMMAND_CLASS_METER

        // Electric Meter
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KWH, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_WATT, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVAH, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_WATT, UNITS_PER, UNITS_HOUR));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_W, ValueType.NUMBER.withUnits(UNITS_WATT));
        typeMap.put(ChannelType.ELECTRIC_METER_PULSE_COUNT, ValueType.NUMBER);
        typeMap.put(ChannelType.ELECTRIC_METER_VOLTAGE_V, ValueType.NUMBER.withUnits(UNITS_VOLT));
        typeMap.put(ChannelType.ELECTRIC_METER_CURRENT_A, ValueType.NUMBER.withUnits(UNITS_AMP));
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_FACTOR, ValueType.NUMBER);
        typeMap.put(ChannelType.ELECTRIC_METER_POWER_KVAR, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_WATT));
        typeMap.put(ChannelType.ELECTRIC_METER_ENERGY_KVARH, ValueType.NUMBER.withUnits(UNITS_KILO, UNITS_WATT, UNITS_PER, UNITS_HOUR));

        // Gas Meter
        typeMap.put(ChannelType.GAS_METER_VOLUME_CM, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_CUBED));
        typeMap.put(ChannelType.GAS_METER_VOLUME_CFT, ValueType.NUMBER.withUnits(UNITS_FOOT, UNITS_CUBED));
        typeMap.put(ChannelType.GAS_METER_PULSE_COUNT, ValueType.NUMBER);

        // Water Meter
        typeMap.put(ChannelType.WATER_METER_VOLUME_CM, ValueType.NUMBER.withUnits(UNITS_METRE, UNITS_CUBED));
        typeMap.put(ChannelType.WATER_METER_VOLUME_CFT, ValueType.NUMBER.withUnits(UNITS_FOOT, UNITS_CUBED));
        typeMap.put(ChannelType.WATER_METER_VOLUME_GAL, ValueType.NUMBER.withUnits(UNITS_GALLON));
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
        typeMap.put(ChannelType.COLOR_RGB, ValueType.COLOUR_RGB);
        typeMap.put(ChannelType.COLOR_ARGB, ValueType.COLOUR_RGBA);

        // COMMAND_CLASS_SENSOR_ALARM

        typeMap.put(ChannelType.GENERAL_PURPOSE_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.SMOKE_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.CO_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.CO2_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.HEAT_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.WATER_LEAK_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));
        typeMap.put(ChannelType.FIRST_SUPPORTED_ALARM, ValueType.BOOLEAN.withUnits(UNITS_BINARY_ON_OFF));

        // COMMAND_CLASS_BATTERY

        typeMap.put(ChannelType.CHARGE_PERCENTAGE, ValueType.NUMBER.withUnits(UNITS_PERCENTAGE));

        // COMMAND_CLASS_CLOCK

        typeMap.put(ChannelType.DATETIME, ValueType.TIMESTAMP_ISO8601);
    }

    public static ValueDescriptor<?> toValueType(ChannelType channelType) {

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
                    valueType = ValueDescriptor.UNKNOWN.asArray();
                    break;
            }
        }
        return valueType;
    }
}
