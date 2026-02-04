package com.old.silence.job.server.common.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.old.silence.job.common.constant.SystemConstants.YYYYMMDDHHMMSS;
import static com.old.silence.job.common.constant.SystemConstants.YYYY_MM_DD_HH_MM_SS;


public class DateUtils {

    public static final DateTimeFormatter NORM_DATETIME_PATTERN = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);

    public static final DateTimeFormatter PURE_DATETIME_MS_PATTERN = DateTimeFormatter.ofPattern(YYYYMMDDHHMMSS);

    private static final ZoneOffset zoneOffset = ZoneOffset.of("+8");

    private DateUtils() {
    }

    public static long toEpochMilli(Date date) {
        return toLocalDateTime(date.getTime()).atZone(zoneOffset).toInstant().toEpochMilli();
    }

    public static long toEpochMilli(Instant date) {
        return date.atZone(zoneOffset).toInstant().toEpochMilli();
    }

    public static Instant toLocalDateTime(long milli) {
        return Instant.ofEpochMilli(milli).atZone(zoneOffset).toInstant();
    }

    public static long toNowMilli() {
        return System.currentTimeMillis();
    }

    public static Instant toNowLocalDateTime() {
        return Instant.now();
    }

    public static String format(Instant time, DateTimeFormatter dateFormatter) {
        return time.atZone(zoneOffset).format(dateFormatter);
    }

    public static String toNowFormat(DateTimeFormatter dateFormatter) {
        return format(toNowLocalDateTime(), dateFormatter);
    }

    public static long toEpochMilli(long second) {
        return second * 1000L;
    }

}
