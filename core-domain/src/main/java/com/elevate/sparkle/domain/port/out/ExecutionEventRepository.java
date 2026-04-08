package com.elevate.sparkle.domain.port.out;

import com.elevate.sparkle.domain.event.DomainEvent;

import java.util.List;
import java.util.UUID;

/**
 * Port (interface) for Event Store persistence.
 * 
 * This is a hexagonal architecture port - the core domain defines what it needs,
 * but doesn't know how it's implemented.
 */
public interface ExecutionEventRepository {
    
    /**
     * Save a list of events atomically.
     * Events must be saved in order, maintaining sequence numbers.
     * 
     * @param events Events to save (must be from same aggregate)
     * @throws IllegalArgumentException if events are from different aggregates
     * @throws ConcurrencyException if sequence numbers conflict (optimistic locking)
     */
    void saveEvents(List<DomainEvent> events);
    
    /**
     * Load all events for a specific aggregate (execution).
     * Events are returned in sequence number order.
     * 
     * @param aggregateId The execution ID
     * @return List of events, ordered by sequence number
     */
    List<DomainEvent> findByAggregateId(UUID aggregateId);
    
    /**
     * Load events for an aggregate starting from a specific sequence number.
     * Useful for snapshot-based rehydration.
     * 
     * @param aggregateId The execution ID
     * @param fromSequence Starting sequence number (inclusive)
     * @return List of events, ordered by sequence number
     */
    List<DomainEvent> findByAggregateIdFromSequence(UUID aggregateId, Long fromSequence);
    
    /**
     * Check if events exist for an aggregate
     * 
     * @param aggregateId The execution ID
     * @return true if events exist
     */
    boolean exists(UUID aggregateId);
    
    /**
     * Get the current version (highest sequence number) for an aggregate
     * 
     * @param aggregateId The execution ID
     * @return Current version, or -1 if no events exist
     */
    Long getCurrentVersion(UUID aggregateId);
}
