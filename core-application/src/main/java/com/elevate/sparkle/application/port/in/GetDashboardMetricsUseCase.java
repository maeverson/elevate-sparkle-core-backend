package com.elevate.sparkle.application.port.in;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Use case for dashboard metrics and statistics
 */
public interface GetDashboardMetricsUseCase {
    
    /**
     * Get overall journey statistics
     */
    JourneyStatistics getJourneyStatistics(UUID journeyId);
    
    /**
     * Get execution statistics for a time period
     */
    ExecutionMetrics getExecutionMetrics(Instant from, Instant to);
    
    /**
     * Get failed steps summary
     */
    List<FailedStepSummary> getFailedSteps(Integer limit);
    
    /**
     * Journey statistics
     */
    record JourneyStatistics(
            UUID journeyId,
            String journeyName,
            Long totalExecutions,
            Long successfulExecutions,
            Long failedExecutions,
            Long runningExecutions,
            Double averageDurationMs,
            Double successRate
    ) {}
    
    /**
     * Execution metrics for a period
     */
    record ExecutionMetrics(
            Long totalExecutions,
            Long successfulExecutions,
            Long failedExecutions,
            Long runningExecutions,
            Double averageDurationMs,
            Map<String, Long> executionsByStatus,
            Map<String, Long> executionsByJourney
    ) {}
    
    /**
     * Failed step summary
     */
    record FailedStepSummary(
            UUID executionId,
            UUID journeyId,
            String journeyName,
            String stepId,
            String stepType,
            String errorType,
            String errorMessage,
            Instant failedAt,
            Integer attemptNumber
    ) {}
}
