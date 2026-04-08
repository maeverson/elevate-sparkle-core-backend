package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Configuration for data transformation nodes
 */
public record TransformNodeConfig(
        String script,
        String language
) implements NodeConfig {
    
    public TransformNodeConfig {
        if (language == null) language = "javascript";
    }
}
