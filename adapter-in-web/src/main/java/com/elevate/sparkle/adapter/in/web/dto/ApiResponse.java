package com.elevate.sparkle.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Standard API response wrapper
 * Provides consistent response structure across all endpoints
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Instant timestamp;
    private int status;
    private T data;
    private List<ErrorDetail> errors;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(200)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, int status) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, List<ErrorDetail> errors) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(Instant.now())
                .status(status)
                .errors(List.of(new ErrorDetail("error", message)))
                .build();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorDetail {
        private String field;
        private String message;
    }
}
