package com.elevate.sparkle.adapter.out.persistence.serializer;

import com.elevate.sparkle.domain.event.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Serializer/Deserializer for domain events to/from JSON.
 * Handles event versioning and schema evolution.
 */
public class EventSerializer {
    
    private final ObjectMapper objectMapper;
    
    public EventSerializer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }
    
    /**
     * Serialize event to JSON payload
     */
    public Map<String, Object> toPayload(DomainEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);
            return payload;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize event: " + event.eventType(), e);
        }
    }
    
    /**
     * Deserialize event from payload and metadata
     */
    public DomainEvent fromPayload(
            String eventType,
            Integer eventVersion,
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        // Convert payload to event-specific type based on event type
        return switch (eventType) {
            case "JourneyStarted" -> deserializeJourneyStarted(payload, aggregateId, sequenceNumber, occurredAt);
            case "StepScheduled" -> deserializeStepScheduled(payload, aggregateId, sequenceNumber, occurredAt);
            case "StepStarted" -> deserializeStepStarted(payload, aggregateId, sequenceNumber, occurredAt);
            case "StepCompleted" -> deserializeStepCompleted(payload, aggregateId, sequenceNumber, occurredAt);
            case "StepFailed" -> deserializeStepFailed(payload, aggregateId, sequenceNumber, occurredAt);
            case "JourneyCompleted" -> deserializeJourneyCompleted(payload, aggregateId, sequenceNumber, occurredAt);
            case "JourneyFailed" -> deserializeJourneyFailed(payload, aggregateId, sequenceNumber, occurredAt);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
    
    @SuppressWarnings("unchecked")
    private JourneyStartedEvent deserializeJourneyStarted(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new JourneyStartedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                UUID.fromString((String) payload.get("journeyDefinitionId")),
                (String) payload.get("journeyVersion"),
                (Map<String, Object>) payload.getOrDefault("initialContext", Map.of()),
                (String) payload.get("startedBy")
        );
    }
    
    @SuppressWarnings("unchecked")
    private StepScheduledEvent deserializeStepScheduled(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new StepScheduledEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (String) payload.get("stepId"),
                (String) payload.get("stepType"),
                (Map<String, Object>) payload.getOrDefault("stepConfig", Map.of()),
                parseInstant(payload.get("scheduledFor"))
        );
    }
    
    private StepStartedEvent deserializeStepStarted(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new StepStartedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (String) payload.get("stepId"),
                (String) payload.get("workerId"),
                parseInstant(payload.get("startedAt"))
        );
    }
    
    @SuppressWarnings("unchecked")
    private StepCompletedEvent deserializeStepCompleted(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new StepCompletedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (String) payload.get("stepId"),
                (Map<String, Object>) payload.getOrDefault("outputData", Map.of()),
                parseInstant(payload.get("completedAt")),
                parseLong(payload.get("durationMs"))
        );
    }
    
    @SuppressWarnings("unchecked")
    private StepFailedEvent deserializeStepFailed(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new StepFailedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (String) payload.get("stepId"),
                (String) payload.get("errorType"),
                (String) payload.get("errorMessage"),
                (Map<String, Object>) payload.getOrDefault("errorDetails", Map.of()),
                parseInstant(payload.get("failedAt")),
                parseInteger(payload.get("attemptNumber"))
        );
    }
    
    @SuppressWarnings("unchecked")
    private JourneyCompletedEvent deserializeJourneyCompleted(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new JourneyCompletedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (Map<String, Object>) payload.getOrDefault("finalContext", Map.of()),
                parseInstant(payload.get("completedAt")),
                parseLong(payload.get("totalDurationMs"))
        );
    }
    
    @SuppressWarnings("unchecked")
    private JourneyFailedEvent deserializeJourneyFailed(
            Map<String, Object> payload,
            UUID aggregateId,
            Long sequenceNumber,
            Instant occurredAt
    ) {
        return new JourneyFailedEvent(
                aggregateId,
                sequenceNumber,
                occurredAt,
                (String) payload.get("failureReason"),
                (String) payload.get("failedStepId"),
                (Map<String, Object>) payload.getOrDefault("errorContext", Map.of()),
                parseInstant(payload.get("failedAt"))
        );
    }
    
    private Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return Instant.parse((String) value);
        }
        if (value instanceof Long) {
            return Instant.ofEpochMilli((Long) value);
        }
        if (value instanceof Number) {
            return Instant.ofEpochMilli(((Number) value).longValue());
        }
        return null;
    }
    
    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
    
    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }
}
