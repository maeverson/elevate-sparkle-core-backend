package com.elevate.sparkle.domain.port.out;

import java.util.Map;

/**
 * Port for executing individual steps within a journey.
 * 
 * This is a strategy pattern - different implementations handle different step types:
 * - HTTP calls
 * - Internal business logic
 * - Message publishing
 * - etc.
 */
public interface StepExecutor {
    
    /**
     * Execute a step with the given configuration and context.
     * 
     * @param stepId Unique identifier for the step
     * @param stepType Type of the step (e.g., "HTTP", "INTERNAL", "MESSAGE")
     * @param stepConfig Configuration specific to this step
     * @param executionContext Current execution context
     * @return Output data from the step execution
     * @throws StepExecutionException if the step fails
     */
    StepExecutionResult execute(
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Map<String, Object> executionContext
    ) throws StepExecutionException;
    
    /**
     * Check if this executor supports the given step type.
     */
    boolean supports(String stepType);
    
    /**
     * Result of a step execution.
     */
    record StepExecutionResult(
            boolean success,
            Map<String, Object> outputData,
            String errorType,
            String errorMessage,
            Map<String, Object> errorDetails
    ) {
        public static StepExecutionResult success(Map<String, Object> outputData) {
            return new StepExecutionResult(true, outputData, null, null, null);
        }
        
        public static StepExecutionResult failure(
                String errorType,
                String errorMessage,
                Map<String, Object> errorDetails
        ) {
            return new StepExecutionResult(false, null, errorType, errorMessage, errorDetails);
        }
    }
    
    /**
     * Exception thrown when step execution fails.
     */
    class StepExecutionException extends Exception {
        private final String errorType;
        private final Map<String, Object> errorDetails;
        
        public StepExecutionException(String errorType, String message, Map<String, Object> errorDetails) {
            super(message);
            this.errorType = errorType;
            this.errorDetails = errorDetails;
        }
        
        public StepExecutionException(String errorType, String message, Throwable cause) {
            super(message, cause);
            this.errorType = errorType;
            this.errorDetails = Map.of();
        }
        
        public String getErrorType() {
            return errorType;
        }
        
        public Map<String, Object> getErrorDetails() {
            return errorDetails;
        }
    }
}
