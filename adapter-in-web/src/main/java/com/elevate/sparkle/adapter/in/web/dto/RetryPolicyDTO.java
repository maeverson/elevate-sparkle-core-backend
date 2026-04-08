package com.elevate.sparkle.adapter.in.web.dto;

/**
 * DTO for Retry Policy
 */
public record RetryPolicyDTO(
        Integer maxAttempts,
        Double backoffMultiplier,
        Long initialBackoffMs
) {
}
