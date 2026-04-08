package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;

/**
 * Validation result when checking a UI Model
 */
public record ValidationResult(
        boolean isValid,
        List<ValidationError> errors,
        List<ValidationError> warnings
) {
    public record ValidationError(
            String code,
            String message,
            String nodeId,
            String severity
    ) {}
}
