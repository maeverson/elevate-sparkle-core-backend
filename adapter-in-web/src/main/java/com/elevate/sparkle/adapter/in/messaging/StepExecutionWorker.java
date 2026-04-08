package com.elevate.sparkle.adapter.in.messaging;

import com.elevate.sparkle.application.scheduler.StepOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Worker consumer that processes step execution messages from SQS
 */
@Component
public class StepExecutionWorker {
    
    private static final Logger logger = LoggerFactory.getLogger(StepExecutionWorker.class);
    
    private final StepOrchestrator stepOrchestrator;
    private final ObjectMapper objectMapper;
    
    public StepExecutionWorker(StepOrchestrator stepOrchestrator, ObjectMapper objectMapper) {
        this.stepOrchestrator = stepOrchestrator;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Listen for step execution messages from SQS
     */
    @SqsListener("${aws.sqs.step-queue-url:step-execution-queue}")
    public void processStepExecution(String message) {
        logger.info("Received step execution message: {}", message);
        
        try {
            // Parse message
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            
            UUID executionId = UUID.fromString((String) payload.get("executionId"));
            String stepId = (String) payload.get("stepId");
            
            logger.info("Processing step: executionId={}, stepId={}", executionId, stepId);
            
            // Execute the step
            stepOrchestrator.executeStep(executionId, stepId);
            
            logger.info("Step processed successfully: executionId={}, stepId={}", executionId, stepId);
            
        } catch (Exception e) {
            logger.error("Failed to process step execution message", e);
            throw new RuntimeException("Step execution failed", e);
        }
    }
}
