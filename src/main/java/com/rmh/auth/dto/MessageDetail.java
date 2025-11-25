package com.rmh.auth.dto;

/**
 * Encapsulates user-facing and developer-facing message details.
 */
public record MessageDetail(String code, String message) {

    public static MessageDetail of(String code, String message) {
        return new MessageDetail(code, message);
    }
}

