package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;

/**
 * Node data structure for the visual journey builder
 */
public record UINode(
        String id,
        NodeType type,
        Position position,
        UINodeData data
) {
    public record UINodeData(
            String label,
            String description,
            NodeConfig config,
            RetryPolicy retryPolicy,
            List<String> validationErrors
    ) {}
}
