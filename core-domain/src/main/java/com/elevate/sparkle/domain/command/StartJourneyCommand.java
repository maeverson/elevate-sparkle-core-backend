package com.elevate.sparkle.domain.command;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Command to start a new journey execution.
 */
public record StartJourneyCommand(
        UUID executionId,
        UUID journeyDefinitionId,
        String journeyVersion,
        Map<String, Object> initialContext,
        String startedBy,
        Instant timestamp
) {
    public StartJourneyCommand {
        if (executionId == null) {
            executionId = UUID.randomUUID();
        }
        if (journeyDefinitionId == null) {
            throw new IllegalArgumentException("Journey definition ID cannot be null");
        }
        if (journeyVersion == null || journeyVersion.isBlank()) {
            throw new IllegalArgumentException("Journey version cannot be null or blank");
        }
        if (initialContext == null) {
            initialContext = Map.of();
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
