package com.elevate.sparkle.adapter.in.web.dto;

import java.time.Instant;

/**
 * Response DTO for individual domain events.
 */
public record EventResponse(
        String eventType,
        Long sequenceNumber,
        Instant occurredAt,
        Object payload
) {}
