package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Execution state overlay for the visual journey builder
 * Shows real-time status of nodes and edges during execution
 */
public record ExecutionGraphOverlay(
        UUID executionId,
        UUID journeyDefinitionId,
        String status,
        String currentStepId,
        Map<String, NodeStatus> nodeStatuses,
        Map<String, EdgeStatus> edgeStatuses,
        List<ExecutionEvent> events,
        String startedAt,
        String completedAt
) {
    /**
     * Status of a node during execution
     */
    public record NodeStatus(
            String nodeId,
            String status, // SCHEDULED, RUNNING, COMPLETED, FAILED, SKIPPED
            Long durationMs,
            String errorMessage,
            String timestamp
    ) {}
    
    /**
     * Status of an edge (whether it was traversed)
     */
    public record EdgeStatus(
            String edgeId,
            String status, // TRAVERSED, ACTIVE
            String timestamp
    ) {}
    
    /**
     * Individual execution event for timeline
     */
    public record ExecutionEvent(
            String eventId,
            String eventType,
            String stepId,
            String timestamp,
            Map<String, Object> metadata
    ) {}
}
