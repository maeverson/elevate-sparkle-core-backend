package com.elevate.sparkle.application.engine;

import com.elevate.sparkle.domain.aggregate.JourneyExecutionAggregate;
import com.elevate.sparkle.domain.command.CompleteStepCommand;
import com.elevate.sparkle.domain.command.FailStepCommand;
import com.elevate.sparkle.domain.command.StartJourneyCommand;
import com.elevate.sparkle.domain.command.StartStepCommand;
import com.elevate.sparkle.domain.event.DomainEvent;
import com.elevate.sparkle.domain.port.out.ExecutionEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Core Workflow Engine - the heart of the event-sourced workflow system.
 * 
 * This engine is:
 * - Framework-agnostic (pure Java, no Spring dependencies)
 * - Deterministic (all state derived from events)
 * - Event-sourced (uses Event Store for persistence)
 * 
 * Flow:
 * 1. Load events from repository
 * 2. Rebuild aggregate state
 * 3. Execute command on aggregate
 * 4. Persist new events
 */
public class WorkflowEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(WorkflowEngine.class);
    
    private final ExecutionEventRepository eventRepository;
    
    public WorkflowEngine(ExecutionEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    
    /**
     * Start a new journey execution.
     * Creates a new aggregate and persists the initial event.
     */
    public UUID startJourney(StartJourneyCommand command) {
        logger.info("Starting journey: definitionId={}, version={}, executionId={}",
                command.journeyDefinitionId(), command.journeyVersion(), command.executionId());
        
        // Create new aggregate
        JourneyExecutionAggregate aggregate = new JourneyExecutionAggregate();
        
        // Execute command
        aggregate.startJourney(
                command.executionId(),
                command.journeyDefinitionId(),
                command.journeyVersion(),
                command.initialContext(),
                command.startedBy(),
                command.timestamp()
        );
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Journey started successfully: executionId={}", command.executionId());
        return command.executionId();
    }
    
    /**
     * Start a step execution.
     * Loads the aggregate, executes the command, persists new events.
     */
    public void startStep(StartStepCommand command) {
        logger.info("Starting step: executionId={}, stepId={}, workerId={}",
                command.executionId(), command.stepId(), command.workerId());
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(command.executionId());
        
        // Execute command
        aggregate.startStep(command.stepId(), command.workerId(), command.timestamp());
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Step started successfully: executionId={}, stepId={}",
                command.executionId(), command.stepId());
    }
    
    /**
     * Complete a step execution.
     */
    public void completeStep(CompleteStepCommand command) {
        logger.info("Completing step: executionId={}, stepId={}",
                command.executionId(), command.stepId());
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(command.executionId());
        
        // Execute command
        aggregate.completeStep(
                command.stepId(),
                command.outputData(),
                command.timestamp(),
                command.durationMs()
        );
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Step completed successfully: executionId={}, stepId={}",
                command.executionId(), command.stepId());
    }
    
    /**
     * Fail a step execution.
     */
    public void failStep(FailStepCommand command) {
        logger.info("Failing step: executionId={}, stepId={}, errorType={}",
                command.executionId(), command.stepId(), command.errorType());
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(command.executionId());
        
        // Execute command
        aggregate.failStep(
                command.stepId(),
                command.errorType(),
                command.errorMessage(),
                command.errorDetails(),
                command.timestamp(),
                command.attemptNumber()
        );
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Step failed: executionId={}, stepId={}", command.executionId(), command.stepId());
    }
    
    /**
     * Complete a journey execution
     */
    public void completeJourney(UUID executionId, java.time.Instant timestamp, Long totalDurationMs) {
        logger.info("Completing journey: executionId={}", executionId);
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(executionId);
        
        // Execute command
        aggregate.completeJourney(timestamp, totalDurationMs);
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Journey completed successfully: executionId={}", executionId);
    }
    
    /**
     * Fail a journey execution
     */
    public void failJourney(
            UUID executionId,
            String failureReason,
            String failedStepId,
            java.time.Instant timestamp
    ) {
        logger.info("Failing journey: executionId={}, reason={}", executionId, failureReason);
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(executionId);
        
        // Execute command
        aggregate.failJourney(failureReason, failedStepId, java.util.Map.of(), timestamp);
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Journey failed: executionId={}", executionId);
    }
    
    /**
     * Schedule a step for execution
     */
    public void scheduleStep(
            UUID executionId,
            String stepId,
            String stepType,
            java.util.Map<String, Object> stepConfig,
            java.time.Instant timestamp
    ) {
        logger.info("Scheduling step: executionId={}, stepId={}, stepType={}",
                executionId, stepId, stepType);
        
        // Load and rebuild aggregate
        JourneyExecutionAggregate aggregate = loadAggregate(executionId);
        
        // Execute command
        aggregate.scheduleStep(stepId, stepType, stepConfig, timestamp);
        
        // Persist events
        List<DomainEvent> events = aggregate.getUncommittedEvents();
        eventRepository.saveEvents(events);
        
        logger.info("Step scheduled: executionId={}, stepId={}", executionId, stepId);
    }
    
    /**
     * Get the current state of a journey execution.
     * Returns the aggregate rebuilt from events (read-only).
     */
    public JourneyExecutionAggregate getExecution(UUID executionId) {
        logger.debug("Loading execution: executionId={}", executionId);
        return loadAggregate(executionId);
    }
    
    /**
     * Get all events for an execution.
     */
    public List<DomainEvent> getExecutionEvents(UUID executionId) {
        logger.debug("Loading events for execution: executionId={}", executionId);
        return eventRepository.findByAggregateId(executionId);
    }
    
    /**
     * Check if an execution exists.
     */
    public boolean executionExists(UUID executionId) {
        return eventRepository.exists(executionId);
    }
    
    // ========= Private Helper Methods =========
    
    /**
     * Load aggregate from event store (event sourcing rehydration).
     */
    private JourneyExecutionAggregate loadAggregate(UUID executionId) {
        List<DomainEvent> events = eventRepository.findByAggregateId(executionId);
        
        if (events.isEmpty()) {
            throw new IllegalArgumentException("Execution not found: " + executionId);
        }
        
        // Rebuild state from events
        JourneyExecutionAggregate aggregate = JourneyExecutionAggregate.fromEvents(events);
        
        logger.debug("Loaded aggregate from {} events: executionId={}, version={}",
                events.size(), executionId, aggregate.getVersion());
        
        return aggregate;
    }
}
