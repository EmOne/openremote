/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openremote.model.util;

import org.openremote.model.Constants;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

/**
 * A helper class with utility methods for
 * time related operations.
 */
public class TimeUtil {

    // Simple syntax
    protected static final Pattern SIMPLE = Pattern.compile(Constants.DURATION_REGEXP);

    private static final int SIM_SGN = 1;
    private static final int SIM_DAY = 3;
    private static final int SIM_HOU = 5;
    private static final int SIM_MIN = 7;
    private static final int SIM_SEC = 9;
    private static final int SIM_MS = 11;
    private static final int SIM_WK = 14;
    private static final int SIM_MON = 16;
    private static final int SIM_YR = 18;

    private static final long SEC_MS = 1000;
    private static final long MIN_MS = 60 * SEC_MS;
    private static final long HOU_MS = 60 * MIN_MS;
    private static final long DAY_MS = 24 * HOU_MS;
    private static final long WK_MS = 7 * DAY_MS;

    /**
     * Parses the given time duration String and returns the corresponding number of milliseconds.
     *
     * @throws NullPointerException if time is null
     */
    public static long parseTimeDuration(String time) {
        time = time.trim();
        if (time.length() > 0) {
            String trimmed = time.trim();
            long result = 0;
            if (trimmed.length() > 0) {
                Matcher mat = SIMPLE.matcher(trimmed);
                if (mat.matches()) {
                    int years = (mat.group(SIM_YR) != null) ? Integer.parseInt(mat.group(SIM_YR)) : 0;
                    int months = (mat.group(SIM_MON) != null) ? Integer.parseInt(mat.group(SIM_MON)) : 0;
                    int weeks = (mat.group(SIM_WK) != null) ? Integer.parseInt(mat.group(SIM_WK)) : 0;
                    int days = (mat.group(SIM_DAY) != null) ? Integer.parseInt(mat.group(SIM_DAY)) : 0;
                    int hours = (mat.group(SIM_HOU) != null) ? Integer.parseInt(mat.group(SIM_HOU)) : 0;
                    int min = (mat.group(SIM_MIN) != null) ? Integer.parseInt(mat.group(SIM_MIN)) : 0;
                    int sec = (mat.group(SIM_SEC) != null) ? Integer.parseInt(mat.group(SIM_SEC)) : 0;
                    int ms = (mat.group(SIM_MS) != null) ? Integer.parseInt(mat.group(SIM_MS)) : 0;
                    long r = weeks * WK_MS + days * DAY_MS + hours * HOU_MS + min * MIN_MS + sec * SEC_MS + ms;
                    if (years != 0 || months != 0) {
                        LocalDateTime dateTime = LocalDateTime.now();
                        LocalDateTime dateTime2 = dateTime.plusMonths(months);
                        dateTime2 = dateTime2.plusYears(years);
                        r += (dateTime2.toEpochSecond(ZoneOffset.UTC) - dateTime.toEpochSecond(ZoneOffset.UTC)) * 1000;
                    }
                    if (mat.group(SIM_SGN) != null && mat.group(SIM_SGN).equals("-")) {
                        r = -r;
                    }
                    result = r;
                } else if (isTimeDurationPositiveInfinity(trimmed)) {
                    // positive infinity
                    result = Long.MAX_VALUE;
                } else if (isTimeDurationNegativeInfinity(trimmed)) {
                    // negative infinity
                    result = Long.MIN_VALUE;
                } else {
                    throw new RuntimeException("Error parsing time string: [ " + time + " ]");
                }
            }
            return result;
        }
        throw new RuntimeException("Empty parameters not allowed in: [" + time + "]");
    }

    public static boolean isTimeDuration(String time) {
        time = time != null ? time.trim() : null;
        return time != null && time.length() > 0
                && (SIMPLE.matcher(time).matches()
                    || isTimeDurationPositiveInfinity(time)
                    || isTimeDurationNegativeInfinity(time));
    }

    public static boolean isTimeDurationPositiveInfinity(String time) {
        time = time != null ? time.trim() : null;
        return "*".equals(time) || "+*".equals(time);
    }

    public static boolean isTimeDurationNegativeInfinity(String time) {
        time = time != null ? time.trim() : null;
        return "-*".equals(time);
    }

    /**
     * Parses ISO8601 strings with optional time and/or offset; if no zone is provided then UTC is assumed if no
     * time is provided then 00:00:00 is assumed.
     */
    public static long parseTimeIso8601(String datetime) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .optionalStart()           // time made optional
            .appendLiteral('T')
            .append(ISO_LOCAL_TIME)
            .optionalStart()           // zone and offset made optional
            .appendOffsetId()
            .optionalStart()
            .appendLiteral('[')
            .parseCaseSensitive()
            .appendZoneRegionId()
            .appendLiteral(']')
            .optionalEnd()
            .optionalEnd()
            .optionalEnd()
            .toFormatter();

        TemporalAccessor temporalAccessor = formatter.parseBest(datetime, ZonedDateTime::from, LocalDateTime::from, LocalDate::from);
        ZonedDateTime zonedDateTime;

        if (temporalAccessor instanceof ZonedDateTime) {
            zonedDateTime = (ZonedDateTime)temporalAccessor;
        } else if (temporalAccessor instanceof LocalDateTime) {
            zonedDateTime = ((LocalDateTime)temporalAccessor).atZone(ZoneOffset.UTC);
        } else {
            zonedDateTime = ((LocalDate) temporalAccessor).atStartOfDay(ZoneOffset.UTC);
        }
        return zonedDateTime.toInstant().toEpochMilli();
    }
}
