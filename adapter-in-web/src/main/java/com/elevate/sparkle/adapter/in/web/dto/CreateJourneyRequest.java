package com.elevate.sparkle.adapter.in.web.dto;

import java.util.UUID;

/**
 * Request DTO for creating a new journey definition
 */
public record CreateJourneyRequest(
        String name,
        String description
) {
    public CreateJourneyRequest {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Journey name cannot be blank");
        }
    }
}
