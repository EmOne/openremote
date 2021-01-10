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

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openremote.model.Constants;
import org.openremote.model.asset.agent.ConnectionStatus;
import org.openremote.model.attribute.AttributeExecuteStatus;
import org.openremote.model.attribute.AttributeLink;
import org.openremote.model.attribute.AttributeRef;
import org.openremote.model.attribute.AttributeState;
import org.openremote.model.auth.OAuthGrant;
import org.openremote.model.auth.UsernamePassword;
import org.openremote.model.calendar.CalendarEvent;
import org.openremote.model.console.ConsoleProviders;
import org.openremote.model.geo.GeoJSONPoint;
import org.openremote.model.util.TsIgnore;
import org.openremote.model.value.impl.ColourRGB;
import org.openremote.model.value.impl.ColourRGBA;
import org.openremote.model.value.impl.ColourRGBAW;
import org.openremote.model.value.impl.ColourRGBW;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@TsIgnore
public final class ValueType {

    /* SOME CUSTOM TYPES TO AVOID GENERIC TYPE SO THESE CAN BE CONSUMED IN VALUE DESCRIPTORS */
    public static class StringMap extends HashMap<String, String> {}
    public static class IntegerMap extends HashMap<String, Integer> {}
    public static class DoubleMap extends HashMap<String, Double> {}
    public static class BooleanMap extends HashMap<String, Double> {}

    public static class MultivaluedStringMap extends HashMap<String, List<String>> {}

    public static final ValueDescriptor<Boolean> BOOLEAN = new ValueDescriptor<>("Boolean", Boolean.class);

    public static final ValueDescriptor<BooleanMap> BOOLEAN_MAP = new ValueDescriptor<>("Boolean map", BooleanMap.class);

    public static final ValueDescriptor<Integer> INTEGER = new ValueDescriptor<>("Integer", Integer.class);

    public static final ValueDescriptor<Long> LONG = new ValueDescriptor<>("Long", Long.class);

    public static final ValueDescriptor<BigInteger> BIG_INTEGER = new ValueDescriptor<>("Big integer", BigInteger.class);

    public static final ValueDescriptor<IntegerMap> INTEGER_MAP = new ValueDescriptor<>("Integer map", IntegerMap.class);

    public static final ValueDescriptor<Double> NUMBER = new ValueDescriptor<>("Number", Double.class);

    public static final ValueDescriptor<DoubleMap> NUMBER_MAP = new ValueDescriptor<>("Number map", DoubleMap.class);

    public static final ValueDescriptor<BigDecimal> BIG_NUMBER = new ValueDescriptor<>("Big number", BigDecimal.class);

    public static final ValueDescriptor<String> STRING = new ValueDescriptor<>("String", String.class);

    public static final ValueDescriptor<StringMap> STRING_MAP = new ValueDescriptor<>("String map", StringMap.class);

    public static final ValueDescriptor<MultivaluedStringMap> MULTIVALUED_STRING_MAP = new ValueDescriptor<>("Multivalued string map", MultivaluedStringMap.class);

    public static final ValueDescriptor<ObjectNode> JSON_OBJECT = new ValueDescriptor<>("JSON Object", ObjectNode.class);

    public static final ValueDescriptor<Date> DATE_AND_TIME = new ValueDescriptor<>("Date and time", Date.class);

    public static final ValueDescriptor<Integer> POSITIVE_INTEGER = new ValueDescriptor<>("Positive integer", Integer.class, 
        new ValueConstraint.Min(0)
    );

    public static final ValueDescriptor<Double> POSITIVE_NUMBER = new ValueDescriptor<>("Positive number", Double.class, 
        new ValueConstraint.Min(0)
    );

    public static final ValueDescriptor<Integer> INT_BYTE = new ValueDescriptor<>("Integer (byte)", Integer.class, 
        new ValueConstraint.Min(0),
        new ValueConstraint.Max(255)
    );

    public static final ValueDescriptor<Byte> BYTE = new ValueDescriptor<>("Byte", Byte.class);

    public static final ValueDescriptor<Long> TIMESTAMP = new ValueDescriptor<>("Timestamp", Long.class);

    public static final ValueDescriptor<String> TIMESTAMP_ISO8601 = new ValueDescriptor<>("Timestamp ISO8601", String.class, 
        new ValueConstraint.Pattern(Constants.TIMESTAMP_ISO8601_REGEXP)
    );

    public static final ValueDescriptor<String> DURATION_STRING = new ValueDescriptor<>("Duration string", String.class, 
        new ValueConstraint.Pattern(Constants.DURATION_REGEXP)
    );

    public static final ValueDescriptor<String> EMAIL = new ValueDescriptor<>("Email", String.class, 
        new ValueConstraint.Pattern(Constants.EMAIL_REGEXP)
    );

    public static final ValueDescriptor<String> UUID = new ValueDescriptor<>("UUID", String.class, 
        new ValueConstraint.Pattern(Constants.UUID_REGEXP)
    );

    public static final ValueDescriptor<String> ASSET_ID = new ValueDescriptor<>("Asset ID", String.class, 
        new ValueConstraint.Pattern(Constants.ASSET_ID_REGEXP)
    );

    public static final ValueDescriptor<Integer> DIRECTION = new ValueDescriptor<>("Direction", Integer.class, 
        new ValueConstraint.Min(0),
        new ValueConstraint.Max(259)
    );

    public static final ValueDescriptor<Integer> PORT = new ValueDescriptor<>("TCP/IP port number", Integer.class, 
        new ValueConstraint.Min(1),
        new ValueConstraint.Max(65536)
    );

    public static final ValueDescriptor<String> HOSTNAME_OR_IP_ADDRESS = new ValueDescriptor<>("Host", String.class, 
        new ValueConstraint.Pattern(Constants.HOSTNAME_OR_IP_REGEXP)
    );

    public static final ValueDescriptor<String> IP_ADDRESS = new ValueDescriptor<>("IP address", String.class, 
        new ValueConstraint.Pattern(Constants.IP_REGEXP)
    );

    public static final ValueDescriptor<AttributeLink> ATTRIBUTE_LINK = new ValueDescriptor<>("Attribute link", AttributeLink.class);

    public static final ValueDescriptor<AttributeRef> ATTRIBUTE_REF = new ValueDescriptor<>("Attribute reference", AttributeRef.class);

    public static final ValueDescriptor<AttributeState> ATTRIBUTE_STATE = new ValueDescriptor<>("Attribute state", AttributeState.class);

    public static final ValueDescriptor<GeoJSONPoint> GEO_JSON_POINT = new ValueDescriptor<>("GeoJSON point", GeoJSONPoint.class);

    public static final ValueDescriptor<CalendarEvent> CALENDAR_EVENT = new ValueDescriptor<>("Calendar event", CalendarEvent.class);

    public static final ValueDescriptor<AttributeExecuteStatus> EXECUTION_STATUS = new ValueDescriptor<>("Execution status", AttributeExecuteStatus.class);

    public static final ValueDescriptor<ConnectionStatus> CONNECTION_STATUS = new ValueDescriptor<>("Connection status", ConnectionStatus.class);

    public static final ValueDescriptor<ConsoleProviders> CONSOLE_PROVIDERS = new ValueDescriptor<>("Console providers", ConsoleProviders.class);

    public static final ValueDescriptor<ColourRGB> COLOUR_RGB = new ValueDescriptor<>("Colour RGB", ColourRGB.class);
    public static final ValueDescriptor<ColourRGBA> COLOUR_RGBA = new ValueDescriptor<>("Colour RGBA", ColourRGBA.class);
    public static final ValueDescriptor<ColourRGBW> COLOUR_RGBW = new ValueDescriptor<>("Colour RGBW", ColourRGBW.class);
    public static final ValueDescriptor<ColourRGBAW> COLOUR_RGBAW = new ValueDescriptor<>("Colour RGBAW", ColourRGBAW.class);

    public static final ValueDescriptor<OAuthGrant> OAUTH_GRANT = new ValueDescriptor<>("OAuth Grant", OAuthGrant.class);

    public static final ValueDescriptor<UsernamePassword> USERNAME_AND_PASSWORD = new ValueDescriptor<>("Username and password", UsernamePassword.class);

    public static final ValueDescriptor<ValueFormat> VALUE_FORMAT = new ValueDescriptor<>("Value Format", ValueFormat.class);

    public static final ValueDescriptor<ValueConstraint> VALUE_CONSTRAINT = new ValueDescriptor<>("Value constraint", ValueConstraint.class);

    protected ValueType() {
    }
}
