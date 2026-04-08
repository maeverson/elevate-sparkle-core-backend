package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a journey execution fails.
 */
public record JourneyFailedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        String failureReason,
        String failedStepId,
        Map<String, Object> errorContext,
        Instant failedAt
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "JourneyFailed";
    }
    
    public JourneyFailedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("Aggregate ID cannot be null");
        }
        if (sequenceNumber == null || sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number must be non-negative");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null");
        }
        if (failureReason == null || failureReason.isBlank()) {
            throw new IllegalArgumentException("Failure reason cannot be null or blank");
        }
        if (errorContext == null) {
            errorContext = Map.of();
        }
        if (failedAt == null) {
            failedAt = occurredAt;
        }
    }
}
