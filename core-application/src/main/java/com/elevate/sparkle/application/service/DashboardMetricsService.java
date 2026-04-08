package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.port.in.GetDashboardMetricsUseCase;
import com.elevate.sparkle.application.port.out.ExecutionQueryPort;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.model.JourneyDefinition;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import com.elevate.sparkle.domain.valueobject.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard metrics and statistics
 */
public class DashboardMetricsService implements GetDashboardMetricsUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardMetricsService.class);
    
    private final ExecutionQueryPort queryPort;
    private final WorkflowEngine workflowEngine;
    private final JourneyDefinitionRepositoryPort journeyRepository;
    
    public DashboardMetricsService(
            ExecutionQueryPort queryPort,
            WorkflowEngine workflowEngine,
            JourneyDefinitionRepositoryPort journeyRepository
    ) {
        this.queryPort = queryPort;
        this.workflowEngine = workflowEngine;
        this.journeyRepository = journeyRepository;
    }
    
    @Override
    public JourneyStatistics getJourneyStatistics(UUID journeyId) {
        logger.info("Getting statistics for journey: {}", journeyId);
        
        // Get journey definition
        JourneyDefinition journey = journeyRepository.findById(journeyId)
                .orElseThrow(() -> new IllegalArgumentException("Journey not found: " + journeyId));
        
        // Get all executions for this journey
        List<ExecutionQueryPort.ExecutionSummary> executions = 
                queryPort.findByJourneyId(journeyId, null, null, null, null);
        
        long total = executions.size();
        long successful = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.COMPLETED)
                .count();
        long failed = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.FAILED)
                .count();
        long running = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.RUNNING)
                .count();
        
        // Calculate average duration (only for completed executions)
        OptionalDouble avgDuration = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.COMPLETED && e.completedAt() != null)
                .mapToLong(e -> Duration.between(e.startedAt(), e.completedAt()).toMillis())
                .average();
        
        double successRate = total > 0 ? (double) successful / total * 100 : 0.0;
        
        return new JourneyStatistics(
                journeyId,
                journey.getName(),
                total,
                successful,
                failed,
                running,
                avgDuration.orElse(0.0),
                successRate
        );
    }
    
    @Override
    public ExecutionMetrics getExecutionMetrics(Instant from, Instant to) {
        logger.info("Getting execution metrics from {} to {}", from, to);
        
        // Get all executions in period
        List<ExecutionQueryPort.ExecutionSummary> executions = 
                queryPort.findByDateRange(from, to);
        
        long total = executions.size();
        long successful = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.COMPLETED)
                .count();
        long failed = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.FAILED)
                .count();
        long running = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.RUNNING)
                .count();
        
        // Calculate average duration
        OptionalDouble avgDuration = executions.stream()
                .filter(e -> e.status() == ExecutionStatus.COMPLETED && e.completedAt() != null)
                .mapToLong(e -> Duration.between(e.startedAt(), e.completedAt()).toMillis())
                .average();
        
        // Group by status
        Map<String, Long> byStatus = executions.stream()
                .collect(Collectors.groupingBy(
                        e -> e.status().name(),
                        Collectors.counting()
                ));
        
        // Group by journey
        Map<String, Long> byJourney = executions.stream()
                .filter(e -> e.journeyId() != null)
                .collect(Collectors.groupingBy(
                        e -> e.journeyId().toString(),
                        Collectors.counting()
                ));
        
        return new ExecutionMetrics(
                total,
                successful,
                failed,
                running,
                avgDuration.orElse(0.0),
                byStatus,
                byJourney
        );
    }
    
    @Override
    public List<FailedStepSummary> getFailedSteps(Integer limit) {
        logger.info("Getting failed steps, limit: {}", limit);
        
        // Get failed executions
        List<ExecutionQueryPort.ExecutionSummary> failedExecutions = 
                queryPort.findByStatus(ExecutionStatus.FAILED, limit != null ? limit : 50);
        
        List<FailedStepSummary> failedSteps = new ArrayList<>();
        
        for (ExecutionQueryPort.ExecutionSummary execution : failedExecutions) {
            try {
                // Load aggregate to get failed step details
                JourneyExecutionAggregate aggregate = workflowEngine.getExecution(execution.executionId());
                
                // Find failed steps
                aggregate.getSteps().values().stream()
                        .filter(step -> step.status() == StepStatus.FAILED)
                        .forEach(step -> {
                            // Get journey name
                            String journeyName = execution.journeyId() != null ? 
                                    journeyRepository.findById(execution.journeyId())
                                            .map(JourneyDefinition::getName)
                                            .orElse("Unknown") : 
                                    "Unknown";
                            
                            failedSteps.add(new FailedStepSummary(
                                    execution.executionId(),
                                    execution.journeyId(),
                                    journeyName,
                                    step.stepId(),
                                    step.stepType(),
                                    "STEP_FAILED", // Generic error type since domain doesn't track specific error types
                                    step.errorMessage() != null ? step.errorMessage() : "No error message",
                                    step.completedAt(),
                                    step.attemptNumber()
                            ));
                        });
                
            } catch (Exception e) {
                logger.error("Failed to load execution details: {}", execution.executionId(), e);
            }
        }
        
        return failedSteps.stream()
                .limit(limit != null ? limit : 50)
                .collect(Collectors.toList());
    }
}
