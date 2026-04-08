package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Event emitted when a step starts executing.
 */
public record StepStartedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        String stepId,
        String workerId,
        Instant startedAt
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "StepStarted";
    }
    
    public StepStartedEvent {
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
        if (startedAt == null) {
            startedAt = occurredAt;
        }
    }
}
