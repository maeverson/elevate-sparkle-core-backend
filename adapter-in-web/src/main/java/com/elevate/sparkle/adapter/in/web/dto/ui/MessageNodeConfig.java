package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Configuration for message queue publishing nodes
 */
public record MessageNodeConfig(
        String queue,
        String message
) implements NodeConfig {
}
