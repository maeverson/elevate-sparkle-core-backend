package com.elevate.sparkle.adapter.in.web.dto;

/**
 * DTO for Conditional Transition
 */
public record ConditionalTransitionDTO(
        String condition,
        String nextStepId
) {
}
