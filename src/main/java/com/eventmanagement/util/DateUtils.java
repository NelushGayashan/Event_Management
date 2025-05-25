// src/main/java/com/eventmanagement/util/DateUtils.java
package com.eventmanagement.util;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtils {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null) return null;
        return LocalDateTime.parse(dateTimeString, FORMATTER);
    }

    public static boolean isInFuture(LocalDateTime dateTime, Clock clock) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now(clock));
    }

    public static boolean isInPast(LocalDateTime dateTime, Clock clock) {
        if (dateTime == null) return false;
        return dateTime.isBefore(LocalDateTime.now(clock));
    }
}
