package com.elevate.sparkle.domain.exception;

/**
 * Exception thrown when concurrent modifications conflict.
 * This implements optimistic locking for event sourcing.
 */
public class ConcurrencyException extends RuntimeException {
    
    private final Object aggregateId;
    private final Long expectedVersion;
    private final Long actualVersion;
    
    public ConcurrencyException(Object aggregateId, Long expectedVersion, Long actualVersion) {
        super(String.format(
                "Concurrency conflict for aggregate %s: expected version %d, but actual version is %d",
                aggregateId, expectedVersion, actualVersion
        ));
        this.aggregateId = aggregateId;
        this.expectedVersion = expectedVersion;
        this.actualVersion = actualVersion;
    }
    
    public Object getAggregateId() {
        return aggregateId;
    }
    
    public Long getExpectedVersion() {
        return expectedVersion;
    }
    
    public Long getActualVersion() {
        return actualVersion;
    }
}
