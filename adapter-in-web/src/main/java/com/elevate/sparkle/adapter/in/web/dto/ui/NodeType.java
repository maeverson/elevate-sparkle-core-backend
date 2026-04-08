package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Node types supported in the visual journey builder
 * Corresponds to frontend NodeType enum
 */
public enum NodeType {
    START,
    END,
    HTTP,
    DECISION,
    MESSAGE,
    WAIT,
    TRANSFORM,
    PARALLEL,
    AGGREGATE
}
