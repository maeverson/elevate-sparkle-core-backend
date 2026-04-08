package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.*;
import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.port.in.ListExecutionsUseCase;
import com.elevate.sparkle.application.port.in.RetryExecutionUseCase;
import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.command.CompleteStepCommand;
import com.elevate.sparkle.domain.command.StartJourneyCommand;
import com.elevate.sparkle.domain.event.DomainEvent;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST API for Journey Executions.
 * This is the adapter-in layer for the workflow engine.
 */
@RestController
@RequestMapping("/api/executions")
@Tag(name = "Journey Executions", description = "Workflow engine execution management")
public class ExecutionController {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionController.class);
    
    private final WorkflowEngine workflowEngine;
    private final ListExecutionsUseCase listExecutionsUseCase;
    private final RetryExecutionUseCase retryExecutionUseCase;
    
    public ExecutionController(
            WorkflowEngine workflowEngine,
            ListExecutionsUseCase listExecutionsUseCase,
            RetryExecutionUseCase retryExecutionUseCase
    ) {
        this.workflowEngine = workflowEngine;
        this.listExecutionsUseCase = listExecutionsUseCase;
        this.retryExecutionUseCase = retryExecutionUseCase;
    }
    
    /**
     * Start a new journey execution.
     */
    @PostMapping("/start")
    @Operation(summary = "Start a new journey execution")
    public ResponseEntity<ApiResponse<Map<String, UUID>>> startJourney(
            @RequestBody StartJourneyRequest request
    ) {
        logger.info("Starting journey: definitionId={}, version={}",
                request.journeyDefinitionId(), request.journeyVersion());
        
        try {
            StartJourneyCommand command = new StartJourneyCommand(
                    null, // Will be generated
                    request.journeyDefinitionId(),
                    request.journeyVersion(),
                    request.initialContext(),
                    request.startedBy(),
                    Instant.now()
            );
            
            UUID executionId = workflowEngine.startJourney(command);
            
            Map<String, UUID> response = Map.of("executionId", executionId);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, 201));
                    
        } catch (Exception e) {
            logger.error("Failed to start journey", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to start journey: " + e.getMessage()));
        }
    }
    
    /**
     * Get execution details by ID.
     */
    @GetMapping("/{executionId}")
    @Operation(summary = "Get execution details")
    public ResponseEntity<ApiResponse<ExecutionResponse>> getExecution(
            @PathVariable UUID executionId
    ) {
        logger.info("Getting execution: executionId={}", executionId);
        
        try {
            if (!workflowEngine.executionExists(executionId)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Execution not found"));
            }
            
            JourneyExecutionAggregate aggregate = workflowEngine.getExecution(executionId);
            ExecutionResponse response = toExecutionResponse(aggregate);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Failed to get execution: executionId={}", executionId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get execution: " + e.getMessage()));
        }
    }
    
    /**
     * Get all events for an execution.
     */
    @GetMapping("/{executionId}/events")
    @Operation(summary = "Get all events for an execution")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getExecutionEvents(
            @PathVariable UUID executionId
    ) {
        logger.info("Getting events: executionId={}", executionId);
        
        try {
            if (!workflowEngine.executionExists(executionId)) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Execution not found"));
            }
            
            List<DomainEvent> events = workflowEngine.getExecutionEvents(executionId);
            List<EventResponse> response = events.stream()
                    .map(this::toEventResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Failed to get events: executionId={}", executionId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to get events: " + e.getMessage()));
        }
    }
    
    /**
     * Complete a step in an execution.
     */
    @PostMapping("/{executionId}/steps/complete")
    @Operation(summary = "Complete a step")
    public ResponseEntity<ApiResponse<String>> completeStep(
            @PathVariable UUID executionId,
            @RequestBody CompleteStepRequest request
    ) {
        logger.info("Completing step: executionId={}, stepId={}",
                executionId, request.stepId());
        
        try {
            CompleteStepCommand command = new CompleteStepCommand(
                    executionId,
                    request.stepId(),
                    request.outputData(),
                    request.durationMs(),
                    Instant.now()
            );
            
            workflowEngine.completeStep(command);
            
            return ResponseEntity.ok(ApiResponse.success("Step completed successfully"));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid step completion request", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to complete step", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Failed to complete step: " + e.getMessage()));
        }
    }
    
    /**
     * List executions with filters
     */
    @GetMapping
    @Operation(summary = "List executions with filters")
    public ResponseEntity<ApiResponse<List<ExecutionSummaryResponse>>> listExecutions(
            @RequestParam(required = false) UUID journeyDefinitionId,
            @RequestParam(required = false) ExecutionStatus status,
            @RequestParam(required = false, defaultValue = "100") Integer  limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        logger.info("Listing executions: journeyId={}, status={}, limit={}",
                journeyDefinitionId, status, limit);
        
        try {
            ListExecutionsUseCase.ListExecutionsQuery query = 
                    new ListExecutionsUseCase.ListExecutionsQuery(
                            journeyDefinitionId,
                            status,
                            null,
                            null,
                            limit,
                            offset
                    );
            
            List<ListExecutionsUseCase.ExecutionSummary> summaries = 
                    listExecutionsUseCase.execute(query);
            
            List<ExecutionSummaryResponse> response = summaries.stream()
                    .map(this::toSummaryResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            logger.error("Failed to list executions", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to list executions: " + e.getMessage()));
        }
    }
    
    /**
     * Retry a failed execution
     */
    @PostMapping("/{executionId}/retry")
    @Operation(summary = "Retry a failed execution")
    public ResponseEntity<ApiResponse<String>> retryExecution(
            @PathVariable UUID executionId,
            @RequestParam(required = false) String fromStepId
    ) {
        logger.info("Retrying execution: executionId={}, fromStepId={}",
                executionId, fromStepId);
        
        try {
            RetryExecutionUseCase.RetryExecutionCommand command = 
                    new RetryExecutionUseCase.RetryExecutionCommand(
                            executionId,
                            fromStepId,
                            UUID.randomUUID(), // TODO: Get from auth
                            Instant.now()
                    );
            
            retryExecutionUseCase.execute(command);
            
            return ResponseEntity.ok(ApiResponse.success("Execution retry initiated"));
            
        } catch (UnsupportedOperationException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .body(ApiResponse.error(501, e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error(400, e.getMessage()));
        } catch (Exception e) {
            logger.error("Failed to retry execution", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to retry execution: " + e.getMessage()));
        }
    }
    
    // ========== Mapping Methods ==========
    
    private ExecutionSummaryResponse toSummaryResponse(
            ListExecutionsUseCase.ExecutionSummary summary
    ) {
        return new ExecutionSummaryResponse(
                summary.executionId(),
                summary.journeyDefinitionId(),
                summary.journeyVersion(),
                summary.status().name(),
                summary.startedAt(),
                summary.completedAt(),
                summary.durationMs(),
                summary.totalSteps(),
                summary.completedSteps(),
                summary.failedSteps()
        );
    }
    
    private ExecutionResponse toExecutionResponse(JourneyExecutionAggregate aggregate) {
        Map<String, ExecutionResponse.StepExecutionDetail> stepDetails = aggregate.getSteps().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            var step = e.getValue();
                            return new ExecutionResponse.StepExecutionDetail(
                                    step.stepId(),
                                    step.stepType(),
                                    step.status().name(),
                                    step.scheduledAt(),
                                    step.startedAt(),
                                    step.completedAt(),
                                    step.outputData(),
                                    step.errorMessage(),
                                    step.attemptNumber()
                            );
                        }
                ));
        
        return new ExecutionResponse(
                aggregate.getExecutionId(),
                aggregate.getJourneyDefinitionId(),
                aggregate.getJourneyVersion(),
                aggregate.getStatus().name(),
                aggregate.getContext(),
                aggregate.getCurrentStepId(),
                aggregate.getStartedAt(),
                aggregate.getCompletedAt(),
                aggregate.getVersion(),
                stepDetails
        );
    }
    
    private EventResponse toEventResponse(DomainEvent event) {
        return new EventResponse(
                event.eventType(),
                event.sequenceNumber(),
                event.occurredAt(),
                event // Use the event itself as payload for now
        );
    }
}
