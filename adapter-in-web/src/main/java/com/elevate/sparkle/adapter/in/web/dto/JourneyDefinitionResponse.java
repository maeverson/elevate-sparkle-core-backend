package com.elevate.sparkle.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for Journey Definition
 */
public record JourneyDefinitionResponse(
        UUID id,
        String name,
        String description,
        UUID currentPublishedVersionId,
        UUID createdBy,
        Instant createdAt,
        Instant updatedAt,
        boolean archived
) {}
