package com.elevate.sparkle.application.engine;

import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.command.CompleteStepCommand;
import com.elevate.sparkle.domain.command.StartJourneyCommand;
import com.elevate.sparkle.domain.command.StartStepCommand;
import com.elevate.sparkle.domain.event.*;
import com.elevate.sparkle.domain.port.out.ExecutionEventRepository;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import com.elevate.sparkle.domain.valueobject.StepStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for WorkflowEngine.
 * These tests verify the complete flow:
 * 1. Load events from repository
 * 2. Rebuild aggregate
 * 3. Execute command
 * 4. Persist new events
 */
@ExtendWith(MockitoExtension.class)
class WorkflowEngineTest {
    
    @Mock
    private ExecutionEventRepository eventRepository;
    
    @Captor
    private ArgumentCaptor<List<DomainEvent>> eventsCaptor;
    
    private WorkflowEngine engine;
    private UUID journeyDefinitionId;
    
    @BeforeEach
    void setUp() {
        engine = new WorkflowEngine(eventRepository);
        journeyDefinitionId = UUID.randomUUID();
    }
    
    @Test
    @DisplayName("Should start journey and persist event")
    void shouldStartJourney() {
        // Given
        StartJourneyCommand command = new StartJourneyCommand(
                null,
                journeyDefinitionId,
                "1.0.0",
                Map.of("userId", "user123"),
                "admin",
                Instant.now()
        );
        
        // When
        UUID executionId = engine.startJourney(command);
        
        // Then
        assertNotNull(executionId);
        
        verify(eventRepository, times(1)).saveEvents(eventsCaptor.capture());
        
        List<DomainEvent> savedEvents = eventsCaptor.getValue();
        assertEquals(1, savedEvents.size());
        
        DomainEvent event = savedEvents.get(0);
        assertTrue(event instanceof JourneyStartedEvent);
        assertEquals(executionId, event.aggregateId());
        assertEquals(0L, event.sequenceNumber());
    }
    
    @Test
    @DisplayName("Should start step after loading aggregate from events")
    void shouldStartStep() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.now();
        
        List<DomainEvent> existingEvents = List.of(
                new JourneyStartedEvent(
                        executionId,
                        0L,
                        now,
                        journeyDefinitionId,
                        "1.0.0",
                        Map.of(),
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
                )
        );
        
        when(eventRepository.findByAggregateId(executionId)).thenReturn(existingEvents);
        
        StartStepCommand command = new StartStepCommand(
                executionId,
                "step-1",
                "worker-123",
                now
        );
        
        // When
        engine.startStep(command);
        
        // Then
        verify(eventRepository, times(1)).findByAggregateId(executionId);
        verify(eventRepository, times(1)).saveEvents(eventsCaptor.capture());
        
        List<DomainEvent> savedEvents = eventsCaptor.getValue();
        assertEquals(1, savedEvents.size());
        
        DomainEvent event = savedEvents.get(0);
        assertTrue(event instanceof StepStartedEvent);
        assertEquals(2L, event.sequenceNumber()); // Sequence continues from last event
    }
    
    @Test
    @DisplayName("Should complete step and persist event")
    void shouldCompleteStep() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.now();
        
        List<DomainEvent> existingEvents = List.of(
                new JourneyStartedEvent(
                        executionId,
                        0L,
                        now,
                        journeyDefinitionId,
                        "1.0.0",
                        Map.of(),
                        "admin"
                ),
                new StepScheduledEvent(
                        executionId,
                        1L,
                        now,
                        "step-1",
                        "HTTP",
                        Map.of(),
                        now
                ),
                new StepStartedEvent(
                        executionId,
                        2L,
                        now,
                        "step-1",
                        "worker-1",
                        now
                )
        );
        
        when(eventRepository.findByAggregateId(executionId)).thenReturn(existingEvents);
        
        CompleteStepCommand command = new CompleteStepCommand(
                executionId,
                "step-1",
                Map.of("result", "success"),
                1000L,
                now
        );
        
        // When
        engine.completeStep(command);
        
        // Then
        verify(eventRepository, times(1)).saveEvents(eventsCaptor.capture());
        
        List<DomainEvent> savedEvents = eventsCaptor.getValue();
        assertEquals(1, savedEvents.size());
        
        DomainEvent event = savedEvents.get(0);
        assertTrue(event instanceof StepCompletedEvent);
        assertEquals(3L, event.sequenceNumber());
        
        StepCompletedEvent completedEvent = (StepCompletedEvent) event;
        assertEquals("step-1", completedEvent.stepId());
        assertEquals(Map.of("result", "success"), completedEvent.outputData());
    }
    
    @Test
    @DisplayName("Should get execution by rebuilding from events")
    void shouldGetExecution() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.now();
        
        List<DomainEvent> events = List.of(
                new JourneyStartedEvent(
                        executionId,
                        0L,
                        now,
                        journeyDefinitionId,
                        "1.0.0",
                        Map.of("userId", "user123"),
                        "admin"
                ),
                new StepScheduledEvent(
                        executionId,
                        1L,
                        now,
                        "step-1",
                        "HTTP",
                        Map.of(),
                        now
                )
        );
        
        when(eventRepository.findByAggregateId(executionId)).thenReturn(events);
        
        // When
        JourneyExecutionAggregate aggregate = engine.getExecution(executionId);
        
        // Then
        assertNotNull(aggregate);
        assertEquals(executionId, aggregate.getExecutionId());
        assertEquals(ExecutionStatus.RUNNING, aggregate.getStatus());
        assertEquals(1L, aggregate.getVersion());
        assertTrue(aggregate.getContext().containsKey("userId"));
        
        var step = aggregate.getStep("step-1");
        assertTrue(step.isPresent());
        assertEquals(StepStatus.SCHEDULED, step.get().status());
    }
    
    @Test
    @DisplayName("Should throw exception when execution not found")
    void shouldThrowWhenExecutionNotFound() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(eventRepository.findByAggregateId(executionId)).thenReturn(List.of());
        
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            engine.getExecution(executionId);
        });
    }
    
    @Test
    @DisplayName("Should check if execution exists")
    void shouldCheckExecutionExists() {
        // Given
        UUID executionId = UUID.randomUUID();
        when(eventRepository.exists(executionId)).thenReturn(true);
        
        // When
        boolean exists = engine.executionExists(executionId);
        
        // Then
        assertTrue(exists);
        verify(eventRepository, times(1)).exists(executionId);
    }
    
    @Test
    @DisplayName("Should get execution events")
    void shouldGetExecutionEvents() {
        // Given
        UUID executionId = UUID.randomUUID();
        Instant now = Instant.now();
        
        List<DomainEvent> events = List.of(
                new JourneyStartedEvent(
                        executionId,
                        0L,
                        now,
                        journeyDefinitionId,
                        "1.0.0",
                        Map.of(),
                        "admin"
                )
        );
        
        when(eventRepository.findByAggregateId(executionId)).thenReturn(events);
        
        // When
        List<DomainEvent> result = engine.getExecutionEvents(executionId);
        
        // Then
        assertEquals(events, result);
        verify(eventRepository, times(1)).findByAggregateId(executionId);
    }
}
