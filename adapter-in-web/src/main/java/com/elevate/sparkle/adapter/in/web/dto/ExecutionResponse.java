package com.elevate.sparkle.adapter.in.web.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for journey execution details.
 */
public record ExecutionResponse(
        UUID executionId,
        UUID journeyDefinitionId,
        String journeyVersion,
        String status,
        Map<String, Object> context,
        String currentStepId,
        Instant startedAt,
        Instant completedAt,
        Long version,
        Map<String, StepExecutionDetail> steps
) {
    
    public record StepExecutionDetail(
            String stepId,
            String stepType,
            String status,
            Instant scheduledAt,
            Instant startedAt,
            Instant completedAt,
            Map<String, Object> outputData,
            String errorMessage,
            Integer attemptNumber
    ) {}
}
