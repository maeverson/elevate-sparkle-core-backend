package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.List;

/**
 * Configuration for parallel execution nodes
 */
public record ParallelNodeConfig(
        List<String> branchIds,
        Integer maxConcurrency
) implements NodeConfig {
}
