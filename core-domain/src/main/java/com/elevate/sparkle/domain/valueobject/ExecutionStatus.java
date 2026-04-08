package com.elevate.sparkle.domain.valueobject;

/**
 * Status of a journey execution.
 */
public enum ExecutionStatus {
    /**
     * Journey has been created but not yet started
     */
    PENDING,
    
    /**
     * Journey is currently running
     */
    RUNNING,
    
    /**
     * Journey completed successfully
     */
    COMPLETED,
    
    /**
     * Journey failed
     */
    FAILED,
    
    /**
     * Journey was cancelled
     */
    CANCELLED;
    
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
    
    public boolean canTransitionTo(ExecutionStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == RUNNING || newStatus == CANCELLED;
            case RUNNING -> newStatus == COMPLETED || newStatus == FAILED || newStatus == CANCELLED;
            case COMPLETED, FAILED, CANCELLED -> false;
        };
    }
}
