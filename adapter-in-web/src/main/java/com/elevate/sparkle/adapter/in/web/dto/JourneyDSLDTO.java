package com.elevate.sparkle.adapter.in.web.dto;

import java.util.List;

/**
 * DTO for Journey DSL
 */
public record JourneyDSLDTO(
        String initialStep,
        List<StepDefinitionDTO> steps,
        Long globalTimeout
) {
}
