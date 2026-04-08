package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Configuration for aggregating results from parallel branches
 */
public record AggregateNodeConfig(
        String strategy
) implements NodeConfig {
    
    public AggregateNodeConfig {
        if (strategy == null) strategy = "merge";
    }
}
