package com.example.goplant.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class Epoch {
    public static String toDate(String epoch, String format) {
        long epochTimeMillis = Long.parseLong(epoch) * 1000L; // Assuming epoch is in seconds
        Instant instant = Instant.ofEpochMilli(epochTimeMillis);
        LocalDate localDate = instant.atOffset(ZoneOffset.UTC).toLocalDate(); // Using UTC offset

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localDate.format(formatter);
    }

    public static String toTime(String epoch, String format) {
        long epochTimeMillis = Long.parseLong(epoch) * 1000L; // Assuming epoch is in seconds
        Instant instant = Instant.ofEpochMilli(epochTimeMillis);
        LocalTime localTime = instant.atOffset(ZoneOffset.UTC).toLocalTime(); // Using UTC offset

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return localTime.format(formatter);
    }
}
