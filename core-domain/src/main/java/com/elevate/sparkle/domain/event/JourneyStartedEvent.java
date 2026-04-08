package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Event emitted when a journey execution is started.
 */
public record JourneyStartedEvent(
        UUID aggregateId,
        Long sequenceNumber,
        Instant occurredAt,
        UUID journeyDefinitionId,
        String journeyVersion,
        Map<String, Object> initialContext,
        String startedBy
) implements DomainEvent {
    
    @Override
    public String eventType() {
        return "JourneyStarted";
    }
    
    public JourneyStartedEvent {
        if (aggregateId == null) {
            throw new IllegalArgumentException("Aggregate ID cannot be null");
        }
        if (sequenceNumber == null || sequenceNumber < 0) {
            throw new IllegalArgumentException("Sequence number must be non-negative");
        }
        if (occurredAt == null) {
            throw new IllegalArgumentException("Occurred at cannot be null");
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
    }
}
