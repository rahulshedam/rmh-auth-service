package com.rmh.auth.dto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Describes error details that can be surfaced to API consumers.
 */
public record ErrorDetail(String code, String message, Map<String, Object> details) {

    public ErrorDetail {
        Map<String, Object> safeDetails = details == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(details));
        details = safeDetails;
    }

    public static ErrorDetail of(String code, String message) {
        return new ErrorDetail(code, message, Collections.emptyMap());
    }

    public static ErrorDetail of(String code, String message, Map<String, Object> details) {
        return new ErrorDetail(code, message, details);
    }
}

