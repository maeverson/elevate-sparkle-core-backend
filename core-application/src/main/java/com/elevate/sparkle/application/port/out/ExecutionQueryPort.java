package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Port for querying execution projections/summaries
 * This is a READ model for CQRS pattern
 */
public interface ExecutionQueryPort {
    
    /**
     * List all execution IDs matching criteria
     */
    List<UUID> findExecutionIds(
            UUID journeyDefinitionId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore,
            int limit,
            int offset
    );
    
    /**
     * Count executions matching criteria
     */
    long countExecutions(
            UUID journeyDefinitionId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore
    );
    
    /**
     * Get all execution IDs for a journey
     */
    List<UUID> findByJourneyId(UUID journeyDefinitionId);
    
    /**
     * Get all execution IDs by status
     */
    List<UUID> findByStatus(ExecutionStatus status);
    
    /**
     * Get execution summaries by journey ID
     */
    List<ExecutionSummary> findByJourneyId(
            UUID journeyId,
            ExecutionStatus status,
            Instant startedAfter,
            Instant startedBefore,
            Integer limit
    );
    
    /**
     * Get execution summaries by date range
     */
    List<ExecutionSummary> findByDateRange(Instant from, Instant to);
    
    /**
     * Get execution summaries by status
     */
    List<ExecutionSummary> findByStatus(ExecutionStatus status, int limit);
    
    /**
     * Execution summary for dashboard/reporting
     */
    record ExecutionSummary(
            UUID executionId,
            UUID journeyId,
            ExecutionStatus status,
            Instant startedAt,
            Instant completedAt,
            Long durationMs,
            Integer totalSteps,
            Integer completedSteps,
            Integer failedSteps
    ) {}
}
