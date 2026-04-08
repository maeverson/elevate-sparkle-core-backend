package com.elevate.sparkle.domain.valueobject;

/**
 * Status of an individual step within a journey.
 */
public enum StepStatus {
    /**
     * Step is scheduled but not yet started
     */
    SCHEDULED,
    
    /**
     * Step is currently executing
     */
    RUNNING,
    
    /**
     * Step completed successfully
     */
    COMPLETED,
    
    /**
     * Step failed
     */
    FAILED,
    
    /**
     * Step was skipped (e.g., conditional logic)
     */
    SKIPPED;
    
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == SKIPPED;
    }
}
