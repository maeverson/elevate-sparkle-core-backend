package com.elevate.sparkle.domain.aggregate;

import com.elevate.sparkle.domain.event.*;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import com.elevate.sparkle.domain.valueobject.StepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for JourneyExecutionAggregate - the core of event sourcing.
 * These tests verify that:
 * 1. Events are applied correctly
 * 2. State is derived from events
 * 3. Business rules are enforced
 * 4. Rehydration from events works correctly
 */
class JourneyExecutionAggregateTest {
    
    private UUID executionId;
    private UUID journeyDefinitionId;
    private Instant now;
    
    @BeforeEach
    void setUp() {
        executionId = UUID.randomUUID();
        journeyDefinitionId = UUID.randomUUID();
        now = Instant.now();
    }
    
    @Test
    @DisplayName("Should start journey and emit event")
    void shouldStartJourney() {
        // Given
        JourneyExecutionAggregate aggregate = new JourneyExecutionAggregate();
        Map<String, Object> initialContext = Map.of("userId", "user123");
        
        // When
        aggregate.startJourney(
                executionId,
                journeyDefinitionId,
                "1.0.0",
                initialContext,
                "admin",
                now
        );
        
        // Then
        assertEquals(ExecutionStatus.RUNNING, aggregate.getStatus());
        assertEquals(executionId, aggregate.getExecutionId());
        assertEquals(journeyDefinitionId, aggregate.getJourneyDefinitionId());
        assertEquals("1.0.0", aggregate.getJourneyVersion());
        assertEquals(initialContext, aggregate.getContext());
        assertEquals(0L, aggregate.getVersion());
        
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof JourneyStartedEvent);
    }
    
    @Test
    @DisplayName("Should schedule step and emit event")
    void shouldScheduleStep() {
        // Given
        JourneyExecutionAggregate aggregate = createStartedAggregate();
        Map<String, Object> stepConfig = Map.of("url", "https://api.example.com");
        
        // When
        aggregate.scheduleStep("step-1", "HTTP", stepConfig, now);
        
        // Then
        assertTrue(aggregate.getStep("step-1").isPresent());
        var step = aggregate.getStep("step-1").get();
        assertEquals("step-1", step.stepId());
        assertEquals("HTTP", step.stepType());
        assertEquals(StepStatus.SCHEDULED, step.status());
        assertEquals("step-1", aggregate.getCurrentStepId());
        
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        assertEquals(1, events.size()); // Only the new event (JourneyStarted was already committed)
        assertTrue(events.get(0) instanceof StepScheduledEvent);
    }
    
    @Test
    @DisplayName("Should start step and update status")
    void shouldStartStep() {
        // Given
        JourneyExecutionAggregate aggregate = createAggregateWithScheduledStep();
        
        // When
        aggregate.startStep("step-1", "worker-123", now);
        
        // Then
        var step = aggregate.getStep("step-1").get();
        assertEquals(StepStatus.RUNNING, step.status());
        assertEquals(1, step.attemptNumber());
        assertNotNull(step.startedAt());
    }
    
    @Test
    @DisplayName("Should complete step and merge output data into context")
    void shouldCompleteStep() {
        // Given
        JourneyExecutionAggregate aggregate = createAggregateWithRunningStep();
        Map<String, Object> outputData = Map.of("result", "success", "value", 42);
        
        // When
        aggregate.completeStep("step-1", outputData, now, 1000L);
        
        // Then
        var step = aggregate.getStep("step-1").get();
        assertEquals(StepStatus.COMPLETED, step.status());
        assertEquals(outputData, step.outputData());
        
        // Output data should be merged into context
        assertTrue(aggregate.getContext().containsKey("result"));
        assertTrue(aggregate.getContext().containsKey("value"));
        assertEquals("success", aggregate.getContext().get("result"));
        assertEquals(42, aggregate.getContext().get("value"));
    }
    
    @Test
    @DisplayName("Should fail step with error details")
    void shouldFailStep() {
        // Given
        JourneyExecutionAggregate aggregate = createAggregateWithRunningStep();
        Map<String, Object> errorDetails = Map.of("httpCode", 500);
        
        // When
        aggregate.failStep(
                "step-1",
                "HTTP_ERROR",
                "Service unavailable",
                errorDetails,
                now,
                1
        );
        
        // Then
        var step = aggregate.getStep("step-1").get();
        assertEquals(StepStatus.FAILED, step.status());
        assertEquals("Service unavailable", step.errorMessage());
        assertEquals(1, step.attemptNumber());
    }
    
    @Test
    @DisplayName("Should complete journey")
    void shouldCompleteJourney() {
        // Given
        JourneyExecutionAggregate aggregate = createStartedAggregate();
        
        // When
        aggregate.completeJourney(now, 5000L);
        
        // Then
        assertEquals(ExecutionStatus.COMPLETED, aggregate.getStatus());
        assertNotNull(aggregate.getCompletedAt());
    }
    
    @Test
    @DisplayName("Should fail journey")
    void shouldFailJourney() {
        // Given
        JourneyExecutionAggregate aggregate = createStartedAggregate();
        
        // When
        aggregate.failJourney(
                "Step execution failed",
                "step-1",
                Map.of("error", "details"),
                now
        );
        
        // Then
        assertEquals(ExecutionStatus.FAILED, aggregate.getStatus());
        assertNotNull(aggregate.getCompletedAt());
    }
    
    @Test
    @DisplayName("Should rebuild aggregate from events (event sourcing)")
    void shouldRebuildFromEvents() {
        // Given
        List<DomainEvent> events = List.of(
                new JourneyStartedEvent(
                        executionId,
                        0L,
                        now,
                        journeyDefinitionId,
                        "1.0.0",
                        Map.of("initial", "data"),
                        "admin"
                ),
                new StepScheduledEvent(
                        executionId,
                        1L,
                        now,
                        "step-1",
                        "HTTP",
                        Map.of("url", "https://example.com"),
                        now
                ),
                new StepStartedEvent(
                        executionId,
                        2L,
                        now,
                        "step-1",
                        "worker-1",
                        now
                ),
                new StepCompletedEvent(
                        executionId,
                        3L,
                        now,
                        "step-1",
                        Map.of("result", "ok"),
                        now,
                        1000L
                )
        );
        
        // When
        JourneyExecutionAggregate aggregate = JourneyExecutionAggregate.fromEvents(events);
        
        // Then
        assertEquals(executionId, aggregate.getExecutionId());
        assertEquals(ExecutionStatus.RUNNING, aggregate.getStatus());
        assertEquals(3L, aggregate.getVersion());
        
        var step = aggregate.getStep("step-1").get();
        assertEquals(StepStatus.COMPLETED, step.status());
        
        // Context should have initial data + step output
        assertTrue(aggregate.getContext().containsKey("initial"));
        assertTrue(aggregate.getContext().containsKey("result"));
        
        // No uncommitted events after rehydration
        assertEquals(0, aggregate.getUncommittedEvents().size());
    }
    
    @Test
    @DisplayName("Should enforce business rules - cannot start already started journey")
    void shouldEnforceBusinessRules() {
        // Given
        JourneyExecutionAggregate aggregate = createStartedAggregate();
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            aggregate.startJourney(
                    UUID.randomUUID(),
                    journeyDefinitionId,
                    "1.0.0",
                    Map.of(),
                    "admin",
                    now
            );
        });
    }
    
    @Test
    @DisplayName("Should enforce business rules - cannot complete non-running step")
    void shouldNotCompleteNonRunningStep() {
        // Given
        JourneyExecutionAggregate aggregate = createAggregateWithScheduledStep();
        
        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            aggregate.completeStep("step-1", Map.of(), now, 1000L);
        });
    }
    
    @Test
    @DisplayName("Should track sequence numbers correctly")
    void shouldTrackSequenceNumbers() {
        // Given
        JourneyExecutionAggregate aggregate = new JourneyExecutionAggregate();
        
        // When
        aggregate.startJourney(executionId, journeyDefinitionId, "1.0.0", Map.of(), "admin", now);
        aggregate.markEventsAsCommitted();
        
        aggregate.scheduleStep("step-1", "HTTP", Map.of(), now);
        aggregate.markEventsAsCommitted();
        
        aggregate.startStep("step-1", "worker-1", now);
        
        // Then
        assertEquals(2L, aggregate.getVersion());
        
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        assertEquals(1, events.size());
        assertEquals(2L, events.get(0).sequenceNumber());
    }
    
    // ========== Helper Methods ==========
    
    private JourneyExecutionAggregate createStartedAggregate() {
        JourneyExecutionAggregate aggregate = new JourneyExecutionAggregate();
        aggregate.startJourney(
                executionId,
                journeyDefinitionId,
                "1.0.0",
                Map.of("initial", "context"),
                "admin",
                now
        );
        aggregate.markEventsAsCommitted();
        return aggregate;
    }
    
    private JourneyExecutionAggregate createAggregateWithScheduledStep() {
        JourneyExecutionAggregate aggregate = createStartedAggregate();
        aggregate.scheduleStep("step-1", "HTTP", Map.of("url", "https://example.com"), now);
        aggregate.markEventsAsCommitted();
        return aggregate;
    }
    
    private JourneyExecutionAggregate createAggregateWithRunningStep() {
        JourneyExecutionAggregate aggregate = createAggregateWithScheduledStep();
        aggregate.startStep("step-1", "worker-1", now);
        aggregate.markEventsAsCommitted();
        return aggregate;
    }
}
