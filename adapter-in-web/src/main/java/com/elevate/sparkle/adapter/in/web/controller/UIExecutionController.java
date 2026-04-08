package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.ApiResponse;
import com.elevate.sparkle.adapter.in.web.dto.ui.ExecutionGraphOverlay;
import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.event.DomainEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BFF Controller for execution visualization in the journey builder
 */
@RestController
@RequestMapping("/api/ui/executions")
@Tag(name = "Execution Visualization", description = "Real-time execution overlay for journey builder")
public class UIExecutionController {
    
    private static final Logger logger = LoggerFactory.getLogger(UIExecutionController.class);
    
    private final WorkflowEngine workflowEngine;
    
    public UIExecutionController(WorkflowEngine workflowEngine) {
        this.workflowEngine = workflowEngine;
    }
    
    /**
     * GET /api/ui/executions/{id}/graph - Get execution status overlay for visual builder
     */
    @GetMapping("/{executionId}/graph")
    @Operation(summary = "Get execution graph overlay")
    public ResponseEntity<ApiResponse<ExecutionGraphOverlay>> getExecutionGraph(
            @PathVariable UUID executionId
    ) {
        logger.info("Getting execution graph overlay: executionId={}", executionId);
        
        try {
            // Check if execution exists
            JourneyExecutionAggregate aggregate = workflowEngine.getExecution(executionId);
            
            if (aggregate == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(404, "Execution not found: " + executionId));
            }
            
            // Build simplified overlay (detailed event processing would require event repository access)
            ExecutionGraphOverlay overlay = buildSimplifiedGraphOverlay(aggregate);
            
            return ResponseEntity.ok(ApiResponse.success(overlay));
            
        } catch (Exception e) {
            logger.error("Failed to get execution graph", e);
            return ResponseEntity
                    .internalServerError()
                    .body(ApiResponse.error(500, "Failed to get execution graph: " + e.getMessage()));
        }
    }
    
    // ========================================================================
    // HELPER METHODS
    // ========================================================================
    
    private ExecutionGraphOverlay buildSimplifiedGraphOverlay(JourneyExecutionAggregate aggregate) {
        Map<String, ExecutionGraphOverlay.NodeStatus> nodeStatuses = new HashMap<>();
        Map<String, ExecutionGraphOverlay.EdgeStatus> edgeStatuses = new HashMap<>();
        List<ExecutionGraphOverlay.ExecutionEvent> executionEvents = new ArrayList<>();
        
        // Add current step status
        if (aggregate.getCurrentStepId() != null) {
            nodeStatuses.put(aggregate.getCurrentStepId(), new ExecutionGraphOverlay.NodeStatus(
                    aggregate.getCurrentStepId(),
                    "RUNNING",
                    null,
                    null,
                    aggregate.getStartedAt().toString()
            ));
        }
        
        return new ExecutionGraphOverlay(
                aggregate.getExecutionId(),
                aggregate.getJourneyDefinitionId(),
                aggregate.getStatus().toString(),
                aggregate.getCurrentStepId(),
                nodeStatuses,
                edgeStatuses,
                executionEvents,
                aggregate.getStartedAt().toString(),
                aggregate.getCompletedAt() != null ? aggregate.getCompletedAt().toString() : null
        );
    }
}
