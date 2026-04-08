package com.elevate.sparkle.adapter.in.web.dto.ui;

import java.util.Map;

/**
 * Configuration for HTTP API call nodes
 */
public record HttpNodeConfig(
        String url,
        String method,
        Map<String, String> headers,
        String body,
        Integer timeoutMs,
        RetryPolicy retryPolicy
) implements NodeConfig {
    
    public HttpNodeConfig {
        // Default values
        if (method == null) method = "GET";
        if (timeoutMs == null) timeoutMs = 30000;
    }
}
