package com.elevate.sparkle.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base interface for all domain events in the event-sourced workflow engine.
 * Events are immutable facts that represent something that has happened in the past.
 */
public interface DomainEvent {
    
    /**
     * @return The type of the event (e.g., "JourneyStarted", "StepCompleted")
     */
    String eventType();
    
    /**
     * @return The ID of the aggregate this event belongs to (execution ID)
     */
    UUID aggregateId();
    
    /**
     * @return The sequence number of this event within the aggregate's event stream
     */
    Long sequenceNumber();
    
    /**
     * @return The timestamp when this event occurred
     */
    Instant occurredAt();
    
    /**
     * @return The version of this event type (for schema evolution)
     */
    default Integer eventVersion() {
        return 1;
    }
    
    /**
     * @return The type of the aggregate this event belongs to
     */
    default String aggregateType() {
        return "JOURNEY_EXECUTION";
    }
}
