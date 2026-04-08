package com.elevate.sparkle.application.scheduler;

import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.port.out.StepDispatcherPort;
import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.command.StartStepCommand;
import com.elevate.sparkle.domain.port.out.StepExecutor;
import com.elevate.sparkle.domain.valueobject.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Orchestrates the execution of journey steps.
 * This is the heart of the runtime that dispatches steps to workers.
 */
public class StepOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(StepOrchestrator.class);
    
    private final WorkflowEngine workflowEngine;
    private final StepExecutor stepExecutor;
    private final StepDispatcherPort stepDispatcher;
    
    public StepOrchestrator(
            WorkflowEngine workflowEngine, 
            StepExecutor stepExecutor,
            StepDispatcherPort stepDispatcher
    ) {
        this.workflowEngine = workflowEngine;
        this.stepExecutor = stepExecutor;
        this.stepDispatcher = stepDispatcher;
    }
    
    /**
     * Schedule and execute the next step in a journey
     */
    public void scheduleNextStep(UUID executionId) {
        logger.info("Scheduling next step for execution: {}", executionId);
        
        try {
            JourneyExecutionAggregate aggregate = workflowEngine.getExecution(executionId);
            
            // Find next step to execute
            String nextStepId = findNextPendingStep(aggregate);
            
            if (nextStepId != null) {
                logger.info("Dispatching step: executionId={}, stepId={}", executionId, nextStepId);
                
                // Get step details
                JourneyExecutionAggregate.StepExecution step = aggregate.getStep(nextStepId)
                        .orElseThrow(() -> new IllegalStateException("Step not found: " + nextStepId));
                
                // Dispatch to message queue (for distributed workers)
                dispatchStepToQueue(executionId, step);
                
            } else {
                logger.info("No pending steps found for execution: {}", executionId);
                // Check if journey should be completed
                checkAndCompleteJourney(aggregate);
            }
            
        } catch (Exception e) {
            logger.error("Failed to schedule next step: executionId={}", executionId, e);
        }
    }
    
    /**
     * Dispatch step to message queue for distributed workers
     */
    private void dispatchStepToQueue(UUID executionId, JourneyExecutionAggregate.StepExecution step) {
        logger.info("Dispatching step to queue: executionId={}, stepId={}", executionId, step.stepId());
        
        StepDispatcherPort.StepDispatchCommand command = new StepDispatcherPort.StepDispatchCommand(
                executionId,
                step.stepId(),
                step.stepType(),
                step.config(),
                1 // Initial attempt
        );
        
        stepDispatcher.dispatchStep(command);
    }
    
    /**
     * Execute a single step (called by workers)
     */
    public void executeStep(UUID executionId, String stepId) {
        logger.info("Executing step: executionId={}, stepId={}", executionId, stepId);
        
        try {
            // 1. Start the step
            StartStepCommand startCommand = new StartStepCommand(
                    executionId,
                    stepId,
                    "worker-001", // TODO: Get actual worker ID
                    Instant.now()
            );
            workflowEngine.startStep(startCommand);
            
            // 2. Load aggregate to get step config
            JourneyExecutionAggregate aggregate = workflowEngine.getExecution(executionId);
            JourneyExecutionAggregate.StepExecution stepExecution = 
                    aggregate.getStep(stepId)
                            .orElseThrow(() -> new IllegalStateException("Step not found: " + stepId));
            
            // 3. Execute the step using appropriate executor
            StepExecutor.StepExecutionResult result = stepExecutor.execute(
                    stepId,
                    stepExecution.stepType(),
                    stepExecution.config(),
                    aggregate.getContext()
            );
            
            // 4. Handle result
            if (result.success()) {
                workflowEngine.completeStep(new com.elevate.sparkle.domain.command.CompleteStepCommand(
                        executionId,
                        stepId,
                        result.outputData(),
                        0L, // TODO: Calculate actual duration
                        Instant.now()
                ));
                
                // Schedule next step
                scheduleNextStep(executionId);
                
            } else {
                workflowEngine.failStep(new com.elevate.sparkle.domain.command.FailStepCommand(
                        executionId,
                        stepId,
                        result.errorType(),
                        result.errorMessage(),
                        result.errorDetails(),
                        1, // TODO: Get actual attempt number
                        Instant.now()
                ));
            }
            
        } catch (Exception e) {
            logger.error("Step execution error: executionId={}, stepId={}", executionId, stepId, e);
            
            try {
                workflowEngine.failStep(new com.elevate.sparkle.domain.command.FailStepCommand(
                        executionId,
                        stepId,
                        "EXECUTION_ERROR",
                        e.getMessage(),
                        Map.of(),
                        1,
                        Instant.now()
                ));
            } catch (Exception failError) {
                logger.error("Failed to mark step as failed", failError);
            }
        }
    }
    
    /**
     * Find the next pending step to execute
     */
    private String findNextPendingStep(JourneyExecutionAggregate aggregate) {
        // Find first scheduled step
        return aggregate.getSteps().values().stream()
                .filter(s -> s.status() == StepStatus.SCHEDULED)
                .map(JourneyExecutionAggregate.StepExecution::stepId)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if journey should be marked as completed
     */
    private void checkAndCompleteJourney(JourneyExecutionAggregate aggregate) {
        long totalSteps = aggregate.getSteps().size();
        long completedSteps = aggregate.getSteps().values().stream()
                .filter(s -> s.status() == StepStatus.COMPLETED)
                .count();
        long failedSteps = aggregate.getSteps().values().stream()
                .filter(s -> s.status() == StepStatus.FAILED)
                .count();
        
        if (completedSteps == totalSteps) {
            logger.info("All steps completed, marking journey as completed: {}", 
                    aggregate.getExecutionId());
            workflowEngine.completeJourney(
                    aggregate.getExecutionId(),
                    Instant.now(),
                    0L // TODO: Calculate total duration
            );
        } else if (failedSteps > 0 && (completedSteps + failedSteps == totalSteps)) {
            logger.info("Journey has failed steps, marking as failed: {}",
                    aggregate.getExecutionId());
            workflowEngine.failJourney(
                    aggregate.getExecutionId(),
                    "One or more steps failed",
                    null,
                    Instant.now()
            );
        }
    }
}
