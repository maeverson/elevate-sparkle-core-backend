package com.elevate.sparkle.adapter.in.web.listener;

import com.elevate.sparkle.adapter.in.web.websocket.ExecutionMonitoringWebSocketHandler;
import com.elevate.sparkle.domain.event.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens to domain events and broadcasts them via WebSocket
 * Bridges the domain layer with the WebSocket infrastructure
 */
@Component
public class ExecutionEventWebSocketBroadcaster {
    
    private static final Logger logger = LoggerFactory.getLogger(ExecutionEventWebSocketBroadcaster.class);
    
    private final ExecutionMonitoringWebSocketHandler webSocketHandler;
    
    public ExecutionEventWebSocketBroadcaster(ExecutionMonitoringWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }
    
    @PostConstruct
    public void init() {
        logger.info("ExecutionEventWebSocketBroadcaster initialized");
    }
    
    @EventListener
    public void onJourneyStarted(JourneyStartedEvent event) {
        logger.debug("Broadcasting JourneyStartedEvent: executionId={}", event.aggregateId());
        broadcastEvent(event.aggregateId(), "JOURNEY_STARTED", Map.of(
                "journeyDefinitionId", event.journeyDefinitionId().toString(),
                "version", event.journeyVersion()
        ));
    }
    
    @EventListener
    public void onStepScheduled(StepScheduledEvent event) {
        logger.debug("Broadcasting StepScheduledEvent: executionId={}, stepId={}", 
                event.aggregateId(), event.stepId());
        broadcastEvent(event.aggregateId(), "STEP_SCHEDULED", Map.of(
                "stepId", event.stepId(),
                "stepType", event.stepType()
        ));
    }
    
    @EventListener
    public void onStepStarted(StepStartedEvent event) {
        logger.debug("Broadcasting StepStartedEvent: executionId={}, stepId={}", 
                event.aggregateId(), event.stepId());
        broadcastEvent(event.aggregateId(), "STEP_STARTED", Map.of(
                "stepId", event.stepId(),
                "startedAt", event.startedAt().toString()
        ));
    }
    
    @EventListener
    public void onStepCompleted(StepCompletedEvent event) {
        logger.debug("Broadcasting StepCompletedEvent: executionId={}, stepId={}", 
                event.aggregateId(), event.stepId());
        
        Map<String, Object> data = new HashMap<>();
        data.put("stepId", event.stepId());
        data.put("completedAt", event.completedAt().toString());
        data.put("durationMs", event.durationMs());
        
        broadcastEvent(event.aggregateId(), "STEP_COMPLETED", data);
    }
    
    @EventListener
    public void onStepFailed(StepFailedEvent event) {
        logger.debug("Broadcasting StepFailedEvent: executionId={}, stepId={}", 
                event.aggregateId(), event.stepId());
        broadcastEvent(event.aggregateId(), "STEP_FAILED", Map.of(
                "stepId", event.stepId(),
                "errorType", event.errorType(),
                "errorMessage", event.errorMessage(),
                "failedAt", event.failedAt().toString(),
                "attemptNumber", event.attemptNumber() != null ? event.attemptNumber() : 1
        ));
    }
    
    @EventListener
    public void onJourneyCompleted(JourneyCompletedEvent event) {
        logger.debug("Broadcasting JourneyCompletedEvent: executionId={}", event.aggregateId());
        broadcastEvent(event.aggregateId(), "JOURNEY_COMPLETED", Map.of(
                "completedAt", event.completedAt().toString(),
                "totalDurationMs", event.totalDurationMs() != null ? event.totalDurationMs() : 0L
        ));
    }
    
    @EventListener
    public void onJourneyFailed(JourneyFailedEvent event) {
        logger.debug("Broadcasting JourneyFailedEvent: executionId={}", event.aggregateId());
        broadcastEvent(event.aggregateId(), "JOURNEY_FAILED", Map.of(
                "failedStepId", event.failedStepId() != null ? event.failedStepId() : "unknown",
                "failureReason", event.failureReason(),
                "failedAt", event.failedAt().toString()
        ));
    }
    
    private void broadcastEvent(UUID executionId, String eventType, Map<String, Object> data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "EXECUTION_EVENT");
        message.put("eventType", eventType);
        message.put("executionId", executionId.toString());
        message.put("timestamp", System.currentTimeMillis());
        message.put("data", data);
        
        webSocketHandler.broadcastExecutionUpdate(executionId, message);
    }
}
