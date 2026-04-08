package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a step completes successfully.
 */
public record StepCompletedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        String stepId,
        Map<String, Object> outputData,
        Instant completedAt,
        Long durationMs
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "StepCompleted";
    }
    
    public StepCompletedEvent {
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
        if (outputData == null) {
            outputData = Map.of();
        }
        if (completedAt == null) {
            completedAt = occurredAt;
        }
    }
}
