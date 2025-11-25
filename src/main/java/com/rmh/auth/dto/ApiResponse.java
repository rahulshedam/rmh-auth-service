package com.rmh.auth.dto;

import java.time.Instant;
import org.springframework.http.HttpStatus;

/**
 * Generic response envelope to keep API payloads consistent across the service.
 */
public record ApiResponse<T>(
        ApiStatus status,
        int httpStatus,
        MessageDetail message,
        String path,
        Instant timestamp,
        T data,
        ErrorDetail error) {

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, T data, String path) {
        return success(httpStatus, data, path,
                MessageDetail.of(httpStatus.name(), httpStatus.getReasonPhrase()));
    }

    public static <T> ApiResponse<T> success(HttpStatus httpStatus, T data, String path, MessageDetail message) {
        return new ApiResponse<>(ApiStatus.SUCCESS, httpStatus.value(), message, path, Instant.now(), data, null);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String path, ErrorDetail error) {
        MessageDetail defaultMessage = MessageDetail.of(httpStatus.name(), httpStatus.getReasonPhrase());
        return error(httpStatus, path, error, defaultMessage);
    }

    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String path, ErrorDetail error, MessageDetail message) {
        return new ApiResponse<>(ApiStatus.FAILURE, httpStatus.value(), message, path, Instant.now(), null, error);
    }
}

