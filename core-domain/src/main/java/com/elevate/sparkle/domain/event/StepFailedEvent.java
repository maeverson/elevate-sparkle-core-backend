package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a step fails.
 */
public record StepFailedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        String stepId,
        String errorType,
        String errorMessage,
        Map<String, Object> errorDetails,
        Instant failedAt,
        Integer attemptNumber
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "StepFailed";
    }
    
    public StepFailedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("Aggregate ID cannot be null");
        }
        if (sequenceNumber == null || sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number must be non-negative");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null");
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
        if (failedAt == null) {
            failedAt = occurredAt;
        }
        if (attemptNumber == null || attemptNumber < 1) {
            attemptNumber = 1;
        }
    }
}
