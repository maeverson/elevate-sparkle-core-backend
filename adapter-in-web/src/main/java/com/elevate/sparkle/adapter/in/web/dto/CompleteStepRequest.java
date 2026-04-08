package com.elevate.sparkle.adapter.in.web.dto;

import java.util.Map;

/**
 * Request DTO for completing a step.
 */
public record CompleteStepRequest(
        String stepId,
        Map<String, Object> outputData,
        Long durationMs
) {
    public CompleteStepRequest {
        if (stepId == null || stepId.isBlank()) {
            throw new IllegalArgumentException("Step ID is required");
        }
        if (outputData == null) {
            outputData = Map.of();
        }
    }
}
