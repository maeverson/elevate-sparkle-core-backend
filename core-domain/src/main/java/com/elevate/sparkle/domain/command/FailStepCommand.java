package com.elevate.sparkle.domain.command;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Command to mark a step as failed.
 */
public record FailStepCommand(
        UUID executionId,
        String stepId,
        String errorType,
        String errorMessage,
        Map<String, Object> errorDetails,
        Integer attemptNumber,
        Instant timestamp
) {
    public FailStepCommand {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("Step ID cannot be null or blank");
        }
        if (errorType == null || errorType.isBlank()) {
            throw new IllegalArgumentException("Error type cannot be null or blank");
        }
        if (errorMessage == null || errorMessage.isBlank()) {
            throw new IllegalArgumentException("Error message cannot be null or blank");
        }
        if (errorDetails == null) {
            errorDetails = Map.of();
        }
        if (attemptNumber == null || attemptNumber < 1) {
            attemptNumber = 1;
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
