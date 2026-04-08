package com.elevate.sparkle.adapter.in.web.dto;

import java.util.Map;

/**
 * Request DTO for creating a new journey version
 */
public record CreateJourneyVersionRequest(
        String versionNumber,
        Map<String, Object> dsl,
        String changeNotes
) {
    public CreateJourneyVersionRequest {
        if (versionNumber == null || versionNumber.isBlank()) {
            throw new IllegalArgumentException("Version number cannot be blank");
        }
        if (dsl == null) {
            throw new IllegalArgumentException("DSL cannot be null");
        }
    }
}
