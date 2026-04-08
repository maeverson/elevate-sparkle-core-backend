package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Use case for listing and querying executions
 */
public interface ListExecutionsUseCase {
    
    /**
     * List all executions with optional filters
     */
    List<ExecutionSummary> execute(ListExecutionsQuery query);
    
    /**
     * Query parameters for listing executions
     */
    record ListExecutionsQuery(
            UUID journeyDefinitionId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore,
            Integer limit,
            Integer offset
    ) {
        public ListExecutionsQuery {
            if (limit != null && limit <= 0) {
                throw new IllegalArgumentException("limit must be positive");
            }
            if (offset != null && offset < 0) {
                throw new IllegalArgumentException("offset cannot be negative");
            }
        }
        
        public static ListExecutionsQuery all() {
            return new ListExecutionsQuery(null, null, null, null, 100, 0);
        }
        
        public static ListExecutionsQuery byJourney(UUID journeyDefinitionId) {
            return new ListExecutionsQuery(journeyDefinitionId, null, null, null, 100, 0);
        }
        
        public static ListExecutionsQuery byStatus(ExecutionStatus status) {
            return new ListExecutionsQuery(null, status, null, null, 100, 0);
        }
    }
    
    /**
     * Summary of an execution (for list views)
     */
    record ExecutionSummary(
            UUID executionId,
            UUID journeyDefinitionId,
            String journeyVersion,
            ExecutionStatus status,
            Instant startedAt,
            Instant completedAt,
            long durationMs,
            int totalSteps,
            int completedSteps,
            int failedSteps
    ) {}
}
