package com.elevate.sparkle.adapter.in.web.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for Journey Version
 */
public record JourneyVersionResponse(
        UUID id,
        UUID journeyDefinitionId,
        String versionNumber,
        Map<String, Object> dsl,
        String status,
        UUID createdBy,
        Instant createdAt,
        Instant publishedAt,
        String changeNotes
) {}
