package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Retry configuration for nodes
 */
public record RetryPolicy(
        int maxAttempts,
        int delayMs,
        String backoffStrategy
) {
    public RetryPolicy {
        if (maxAttempts <= 0) maxAttempts = 3;
        if (delayMs <= 0) delayMs = 1000;
        if (backoffStrategy == null) backoffStrategy = "exponential";
    }
}
