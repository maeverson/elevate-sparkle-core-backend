package com.elevate.sparkle.adapter.in.web.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for Step Definition
 */
public record StepDefinitionDTO(
        String id,
        String type,
        Map<String, Object> config,
        Long timeout,
        RetryPolicyDTO retryPolicy,
        List<ConditionalTransitionDTO> transitions
) {
}
