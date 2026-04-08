package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;
import java.util.UUID;

/**
 * Complete UI Model for the journey builder
 * This is the graph-based representation optimized for visual editing
 */
public record JourneyUIModel(
        UUID journeyId,
        String journeyName,
        String description,
        String version,
        List<UINode> nodes,
        List<UIEdge> edges,
        JourneyMetadata metadata
) {
    public record JourneyMetadata(
            String createdBy,
            String lastModifiedBy,
            String createdAt,
            String lastModifiedAt,
            String status
    ) {}
}
