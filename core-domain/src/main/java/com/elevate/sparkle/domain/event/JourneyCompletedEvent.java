package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a journey execution completes successfully.
 */
public record JourneyCompletedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        Map<String, Object> finalContext,
        Instant completedAt,
        Long totalDurationMs
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "JourneyCompleted";
    }
    
    public JourneyCompletedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("Aggregate ID cannot be null");
        }
        if (sequenceNumber == null || sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number must be non-negative");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null");
        }
        if (finalContext == null) {
            finalContext = Map.of();
        }
        if (completedAt == null) {
            completedAt = occurredAt;
        }
    }
}
