package com.elevate.sparkle.application.executor;

import com.elevate.sparkle.domain.port.out.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Executor for message-based steps.
 * Publishes messages to queues or topics.
 */
public class MessageStepExecutor implements StepExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageStepExecutor.class);
    private static final String STEP_TYPE = "MESSAGE";
    
    @Override
    public StepExecutionResult execute(
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Map<String, Object> executionContext
    ) throws StepExecutionException {
        
        logger.info("Executing message step: stepId={}", stepId);
        
        try {
            String destination = (String) stepConfig.get("destination");
            String messageType = (String) stepConfig.get("messageType");
            Object payload = stepConfig.get("payload");
            
            if (destination == null || destination.isBlank()) {
                throw new StepExecutionException(
                        "CONFIGURATION_ERROR",
                        "Destination is required for message step",
                        Map.of("stepId", stepId)
                );
            }
            
            logger.debug("Publishing message: destination={}, type={}", destination, messageType);
            
            // TODO: Implement actual message publishing (SQS, SNS, Kafka, etc.)
            // For now, return mock success
            Map<String, Object> output = new HashMap<>();
            output.put("published", true);
            output.put("destination", destination);
            output.put("messageType", messageType);
            output.put("messageId", java.util.UUID.randomUUID().toString());
            
            return StepExecutionResult.success(output);
            
        } catch (StepExecutionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Message step execution failed: stepId={}", stepId, e);
            throw new StepExecutionException("EXECUTION_ERROR", "Message step failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String stepType) {
        return STEP_TYPE.equalsIgnoreCase(stepType);
    }
}
