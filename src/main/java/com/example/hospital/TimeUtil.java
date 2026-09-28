package com.example.hospital;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Parsing helpers for time strings, since the frontend sends times in mixed
 * 24h ("14:30") and AM/PM ("2:30 PM") formats.
 */
public final class TimeUtil {

    private static final DateTimeFormatter TIME24 = DateTimeFormatter.ofPattern("H:mm");

    private TimeUtil() {
    }

    /**
     * Converts a time string to minutes past midnight, or -1 if unparseable.
     */
    public static int toMinutes(String time) {
        if (time == null) return -1;
        String t = time.trim();
        if (t.isEmpty()) return -1;
        int hour;
        int minute;
        try {
            String upper = t.toUpperCase();
            boolean pm = upper.contains("PM");
            boolean am = upper.endsWith("AM");
            String cleaned = upper.replace("PM", "").replace("AM", "").trim();
            String[] parts = cleaned.split(":");
            hour = Integer.parseInt(parts[0].trim());
            minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;
            if (am && hour == 12) hour = 0;
            if (pm && hour != 12) hour += 12;
            return hour * 60 + minute;
        } catch (Exception e) {
            try {
                LocalTime lt = LocalTime.parse(t, TIME24);
                return lt.getHour() * 60 + lt.getMinute();
            } catch (Exception e2) {
                return -1;
            }
        }
    }

    public static int toMinutes(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Normalizes a day name for comparison ("monday" == "MONDAY" == "mon").
     */
    public static String normalizeDay(String day) {
        if (day == null) return "";
        String d = day.trim().toLowerCase();
        if (d.startsWith("mon")) return "monday";
        if (d.startsWith("tue")) return "tuesday";
        if (d.startsWith("wed")) return "wednesday";
        if (d.startsWith("thu")) return "thursday";
        if (d.startsWith("fri")) return "friday";
        if (d.startsWith("sat")) return "saturday";
        if (d.startsWith("sun")) return "sunday";
        return d;
    }

    public static String dayName(LocalDate date) {
        return normalizeDay(date.getDayOfWeek().name());
    }
}
