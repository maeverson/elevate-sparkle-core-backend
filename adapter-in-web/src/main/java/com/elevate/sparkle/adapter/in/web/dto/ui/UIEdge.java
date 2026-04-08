package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Edge (connection) between nodes in the visual builder
 */
public record UIEdge(
        String id,
        String source,
        String target,
        String sourceHandle,
        String targetHandle,
        UIEdgeData data
) {
    public record UIEdgeData(
            String label,
            String condition
    ) {}
}
