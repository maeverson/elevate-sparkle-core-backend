package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.Map;

/**
 * Request to create or update a journey using the UI Model
 */
public record CreateJourneyUIRequest(
        String name,
        String description,
        JourneyUIModel uiModel
) {
}
