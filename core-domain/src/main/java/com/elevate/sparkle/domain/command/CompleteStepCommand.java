package com.elevate.sparkle.domain.command;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Command to complete a step in a journey execution.
 */
public record CompleteStepCommand(
        UUID executionId,
        String stepId,
        Map<String, Object> outputData,
        Long durationMs,
        Instant timestamp
) {
    public CompleteStepCommand {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("Step ID cannot be null or blank");
        }
        if (outputData == null) {
            outputData = Map.of();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
