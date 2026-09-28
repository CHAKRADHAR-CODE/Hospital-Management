package com.example.hospital;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper to build consistent JSON responses.
 */
public final class ApiResponse {

    private ApiResponse() {
    }

    public static Map<String, Object> success(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", 200);
        m.put("message", message);
        return m;
    }

    public static Map<String, Object> error(int status, String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", status);
        m.put("message", message);
        return m;
    }
}
