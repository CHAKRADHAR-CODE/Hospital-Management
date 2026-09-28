package com.example.hospital;

/**
 * Helper to parse frontend-supplied String identifiers into Long.
 */
public final class Ids {

    private Ids() {
    }

    public static Long parse(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidOperationException(message + " is required");
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Invalid " + message.toLowerCase());
        }
    }

    public static Long parseNullable(String value, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidOperationException("Invalid " + message.toLowerCase());
        }
    }
}
