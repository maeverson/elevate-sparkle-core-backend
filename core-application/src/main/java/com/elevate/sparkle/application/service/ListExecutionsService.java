package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.port.in.ListExecutionsUseCase;
import com.elevate.sparkle.application.port.out.ExecutionQueryPort;
import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.valueobject.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for listing and querying executions
 */
public class ListExecutionsService implements ListExecutionsUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ListExecutionsService.class);
    
    private final ExecutionQueryPort queryPort;
    private final WorkflowEngine workflowEngine;
    
    public ListExecutionsService(ExecutionQueryPort queryPort, WorkflowEngine workflowEngine) {
        this.queryPort = queryPort;
        this.workflowEngine = workflowEngine;
    }
    
    @Override
    public List<ExecutionSummary> execute(ListExecutionsQuery query) {
        logger.info("Listing executions: journeyId={}, status={}, limit={}",
                query.journeyDefinitionId(), query.status(), query.limit());
        
        // Query execution IDs
        List<UUID> executionIds = queryPort.findExecutionIds(
                query.journeyDefinitionId(),
                query.status(),
                query.startedAfter(),
                query.startedBefore(),
                query.limit() != null ? query.limit() : 100,
                query.offset() != null ? query.offset() : 0
        );
        
        // Load aggregates and build summaries
        return executionIds.stream()
                .map(this::loadAndBuildSummary)
                .filter(summary -> summary != null)
                .collect(Collectors.toList());
    }
    
    private ExecutionSummary loadAndBuildSummary(UUID executionId) {
        try {
            JourneyExecutionAggregate aggregate = workflowEngine.getExecution(executionId);
            
            int totalSteps = aggregate.getSteps().size();
            int completedSteps = (int) aggregate.getSteps().values().stream()
                    .filter(s -> s.status() == StepStatus.COMPLETED)
                    .count();
            int failedSteps = (int) aggregate.getSteps().values().stream()
                    .filter(s -> s.status() == StepStatus.FAILED)
                    .count();
            
            long durationMs = 0;
            if (aggregate.getCompletedAt() != null) {
                durationMs = Duration.between(aggregate.getStartedAt(), aggregate.getCompletedAt())
                        .toMillis();
            }
            
            return new ExecutionSummary(
                    aggregate.getExecutionId(),
                    aggregate.getJourneyDefinitionId(),
                    aggregate.getJourneyVersion(),
                    aggregate.getStatus(),
                    aggregate.getStartedAt(),
                    aggregate.getCompletedAt(),
                    durationMs,
                    totalSteps,
                    completedSteps,
                    failedSteps
            );
            
        } catch (Exception e) {
            logger.error("Failed to load execution summary: executionId={}", executionId, e);
            return null;
        }
    }
}
