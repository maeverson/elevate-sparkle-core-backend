package com.elevate.sparkle.application.port.in;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case for retrying a failed execution from a specific step
 */
public interface RetryExecutionUseCase {
    
    /**
     * Retry execution from a failed step
     */
    void execute(RetryExecutionCommand command);
    
    /**
     * Command for retrying an execution
     */
    record RetryExecutionCommand(
            UUID executionId,
            String fromStepId,
            UUID requestedBy,
            Instant timestamp
    ) {
        public RetryExecutionCommand {
            if (executionId == null) {
                throw new IllegalArgumentException("executionId cannot be null");
            }
            if (requestedBy == null) {
                throw new IllegalArgumentException("requestedBy cannot be null");
            }
            if (timestamp == null) {
                throw new IllegalArgumentException("timestamp cannot be null");
            }
        }
    }
}
