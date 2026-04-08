package com.elevate.sparkle.application.port.out;

import java.util.Map;
import java.util.UUID;

/**
 * Port for dispatching steps to workers (e.g., via message queue)
 */
public interface StepDispatcherPort {
    
    /**
     * Dispatch a step for execution by a worker
     */
    void dispatchStep(StepDispatchCommand command);
    
    /**
     * Command to dispatch a step
     */
    record StepDispatchCommand(
            UUID executionId,
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Integer attemptNumber
    ) {
        public StepDispatchCommand {
            if (executionId == null) {
                throw new IllegalArgumentException("executionId cannot be null");
            }
            if (stepId == null || stepId.isBlank()) {
                throw new IllegalArgumentException("stepId cannot be null or blank");
            }
            if (stepType == null || stepType.isBlank()) {
                throw new IllegalArgumentException("stepType cannot be null or blank");
            }
            if (attemptNumber == null || attemptNumber < 1) {
                throw new IllegalArgumentException("attemptNumber must be >= 1");
            }
        }
    }
}
