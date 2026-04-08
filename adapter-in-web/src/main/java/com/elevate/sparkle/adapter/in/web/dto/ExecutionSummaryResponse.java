package com.elevate.sparkle.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for execution summary (list view)
 */
public record ExecutionSummaryResponse(
        UUID executionId,
        UUID journeyDefinitionId,
        String journeyVersion,
        String status,
        Instant startedAt,
        Instant completedAt,
        long durationMs,
        int totalSteps,
        int completedSteps,
        int failedSteps
) {}
