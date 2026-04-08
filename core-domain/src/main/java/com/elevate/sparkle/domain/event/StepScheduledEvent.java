package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a step is scheduled for execution.
 */
public record StepScheduledEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        String stepId,
        String stepType,
        Map<String, Object> stepConfig,
        Instant scheduledFor
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "StepScheduled";
    }
    
    public StepScheduledEvent {
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
        if (stepType == null || stepType.isBlank()) {
            throw new IllegalArgumentException("Step type cannot be null or blank");
        }
        if (stepConfig == null) {
            stepConfig = Map.of();
        }
        if (scheduledFor == null) {
            scheduledFor = occurredAt;
        }
    }
}
