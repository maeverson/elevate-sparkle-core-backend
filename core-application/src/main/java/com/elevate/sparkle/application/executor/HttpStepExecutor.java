package com.elevate.sparkle.application.executor;

import com.elevate.sparkle.domain.port.out.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for HTTP-based steps.
 * Makes HTTP calls to external services.
 */
public class HttpStepExecutor implements StepExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(HttpStepExecutor.class);
    private static final String STEP_TYPE = "HTTP";
    
    @Override
    public StepExecutionResult execute(
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Map<String, Object> executionContext
    ) throws StepExecutionException {
        
        logger.info("Executing HTTP step: stepId={}", stepId);
        
        try {
            // Extract HTTP configuration
            String url = (String) stepConfig.get("url");
            String method = (String) stepConfig.getOrDefault("method", "GET");
            Map<String, String> headers = (Map<String, String>) stepConfig.getOrDefault("headers", Map.of());
            Object body = stepConfig.get("body");
            
            if (url == null || url.isBlank()) {
                throw new StepExecutionException(
                        "CONFIGURATION_ERROR",
                        "URL is required for HTTP step",
                        Map.of("stepId", stepId)
                );
            }
            
            logger.debug("HTTP request: method={}, url={}", method, url);
            
            // TODO: Implement actual HTTP call using RestTemplate or WebClient
            // For now, return mock success
            Map<String, Object> output = new HashMap<>();
            output.put("status", 200);
            output.put("response", "Mock HTTP response");
            output.put("url", url);
            output.put("method", method);
            
            return StepExecutionResult.success(output);
            
        } catch (StepExecutionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("HTTP step execution failed: stepId={}", stepId, e);
            throw new StepExecutionException("EXECUTION_ERROR", "HTTP step failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String stepType) {
        return STEP_TYPE.equalsIgnoreCase(stepType);
    }
}
