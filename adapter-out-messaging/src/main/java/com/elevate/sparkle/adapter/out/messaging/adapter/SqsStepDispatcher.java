package com.elevate.sparkle.adapter.out.messaging.adapter;

import com.elevate.sparkle.application.port.out.StepDispatcherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * SQS adapter for dispatching steps to workers
 */
@Component
public class SqsStepDispatcher implements StepDispatcherPort {
    
    private static final Logger logger = LoggerFactory.getLogger(SqsStepDispatcher.class);
    
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;
    private final String queueUrl;
    
    public SqsStepDispatcher(
            SqsTemplate sqsTemplate,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.step-queue-url:step-execution-queue}") String queueUrl
    ) {
        this.sqsTemplate = sqsTemplate;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }
    
    @Override
    public void dispatchStep(StepDispatchCommand command) {
        logger.info("Dispatching step to SQS: executionId={}, stepId={}, type={}", 
                command.executionId(), command.stepId(), command.stepType());
        
        try {
            // Create message payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("executionId", command.executionId().toString());
            payload.put("stepId", command.stepId());
            payload.put("stepType", command.stepType());
            payload.put("stepConfig", command.stepConfig());
            payload.put("attemptNumber", command.attemptNumber());
            
            // Send to SQS
            String messageBody = objectMapper.writeValueAsString(payload);
            sqsTemplate.send(to -> to
                    .queue(queueUrl)
                    .payload(messageBody)
            );
            
            logger.info("Step dispatched successfully: executionId={}, stepId={}", 
                    command.executionId(), command.stepId());
            
        } catch (Exception e) {
            logger.error("Failed to dispatch step to SQS: executionId={}, stepId={}", 
                    command.executionId(), command.stepId(), e);
            throw new RuntimeException("Failed to dispatch step", e);
        }
    }
}
