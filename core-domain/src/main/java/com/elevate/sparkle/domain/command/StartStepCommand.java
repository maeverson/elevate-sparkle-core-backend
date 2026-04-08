package com.elevate.sparkle.domain.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Command to start executing a step.
 */
public record StartStepCommand(
        UUID executionId,
        String stepId,
        String workerId,
        Instant timestamp
) {
    public StartStepCommand {
        if (executionId == null) {
            throw new IllegalArgumentException("Execution ID cannot be null");
        }
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("Step ID cannot be null or blank");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
