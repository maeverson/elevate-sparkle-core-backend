package com.elevate.sparkle.adapter.in.web.dto;

import java.util.Map;
import java.util.UUID;

/**
 * Request DTO for starting a new journey execution.
 */
public record StartJourneyRequest(
        UUID journeyDefinitionId,
        String journeyVersion,
        Map<String, Object> initialContext,
        String startedBy
) {
    public StartJourneyRequest {
        if (journeyDefinitionId == null) {
            throw new IllegalArgumentException("Journey definition ID is required");
        }
        if (journeyVersion == null || journeyVersion.isBlank()) {
            throw new IllegalArgumentException("Journey version is required");
        }
        if (initialContext == null) {
            initialContext = Map.of();
        }
    }
}
