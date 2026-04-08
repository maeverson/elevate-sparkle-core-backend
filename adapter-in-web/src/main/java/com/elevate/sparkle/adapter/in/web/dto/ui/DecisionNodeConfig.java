package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;

/**
 * Configuration for decision/branching nodes
 */
public record DecisionNodeConfig(
        List<DecisionBranch> branches,
        String defaultBranch
) implements NodeConfig {
    
    public record DecisionBranch(
            String id,
            String label,
            String condition
    ) {}
}
