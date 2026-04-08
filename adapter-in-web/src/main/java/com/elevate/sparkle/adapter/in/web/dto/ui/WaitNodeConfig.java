package com.elevate.sparkle.adapter.in.web.dto.ui;

/**
 * Configuration for wait/delay nodes
 */
public record WaitNodeConfig(
        int duration,
        String unit
) implements NodeConfig {
    
    public WaitNodeConfig {
        if (unit == null) unit = "s";
    }
}
