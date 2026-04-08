package com.elevate.sparkle.domain.aggregate;

import com.elevate.sparkle.domain.event.*;
import com.elevate.sparkle.domain.valueobject.ExecutionStatus;
import com.elevate.sparkle.domain.valueobject.StepStatus;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate root for Journey Execution.
 * This is the heart of the event-sourced workflow engine.
 * 
 * State is derived entirely from events - no direct persistence.
 * All mutations happen through event application.
 */
public class JourneyExecutionAggregate {
    
    private UUID executionId;
    private UUID journeyDefinitionId;
    private String journeyVersion;
    private ExecutionStatus status;
    private Map<String, Object> context;
    private Instant startedAt;
    private Instant completedAt;
    private Long version; // Current event sequence number
    
    // Step tracking
    private String currentStepId;
    private final Map<String, StepExecution> steps;
    private final List<DomainEvent> uncommittedEvents;
    
    /**
     * Represents the execution state of a single step
     */
    public record StepExecution(
            String stepId,
            String stepType,
            StepStatus status,
            Map<String, Object> config,
            Instant scheduledAt,
            Instant startedAt,
            Instant completedAt,
            Map<String, Object> outputData,
            String errorMessage,
            Integer attemptNumber
    ) {}
    
    /**
     * Constructor for creating a new aggregate (no history)
     */
    public JourneyExecutionAggregate() {
        this.steps = new HashMap<>();
        this.uncommittedEvents = new ArrayList<>();
        this.context = new HashMap<>();
        this.version = -1L;
        this.status = ExecutionStatus.PENDING;
    }
    
    /**
     * Rebuild aggregate from event history (event sourcing)
     */
    public static JourneyExecutionAggregate fromEvents(List<DomainEvent> events) {
        JourneyExecutionAggregate aggregate = new JourneyExecutionAggregate();
        for (DomainEvent event : events) {
            aggregate.apply(event, false);
        }
        return aggregate;
    }
    
    /**
     * Apply an event to this aggregate.
     * 
     * @param event The event to apply
     * @param isNew If true, this is a new event that should be tracked as uncommitted
     */
    private void apply(DomainEvent event, boolean isNew) {
        if (event instanceof JourneyStartedEvent e) {
            applyJourneyStarted(e);
        } else if (event instanceof StepScheduledEvent e) {
            applyStepScheduled(e);
        } else if (event instanceof StepStartedEvent e) {
            applyStepStarted(e);
        } else if (event instanceof StepCompletedEvent e) {
            applyStepCompleted(e);
        } else if (event instanceof StepFailedEvent e) {
            applyStepFailed(e);
        } else if (event instanceof JourneyCompletedEvent e) {
            applyJourneyCompleted(e);
        } else if (event instanceof JourneyFailedEvent e) {
            applyJourneyFailed(e);
        } else {
            throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
        }
        
        this.version = event.sequenceNumber();
        
        if (isNew) {
            uncommittedEvents.add(event);
        }
    }
    
    // ========== Command Methods (Business Logic) ==========
    
    /**
     * Start a new journey execution
     */
    public void startJourney(
            UUID executionId,
            UUID journeyDefinitionId,
            String journeyVersion,
            Map<String, Object> initialContext,
            String startedBy,
            Instant timestamp
    ) {
        if (this.executionId != null) {
            throw new IllegalStateException("Journey already started");
        }
        
        JourneyStartedEvent event = new JourneyStartedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                journeyDefinitionId,
                journeyVersion,
                initialContext,
                startedBy
        );
        
        apply(event, true);
    }
    
    /**
     * Schedule a step for execution
     */
    public void scheduleStep(
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Instant timestamp
    ) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Cannot schedule step on terminal journey: " + status);
        }
        
        StepScheduledEvent event = new StepScheduledEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                stepId,
                stepType,
                stepConfig,
                timestamp
        );
        
        apply(event, true);
    }
    
    /**
     * Mark a step as started
     */
    public void startStep(String stepId, String workerId, Instant timestamp) {
        StepExecution step = steps.get(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        if (step.status != StepStatus.SCHEDULED) {
            throw new IllegalStateException("Step cannot be started from status: " + step.status);
        }
        
        StepStartedEvent event = new StepStartedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                stepId,
                workerId,
                timestamp
        );
        
        apply(event, true);
    }
    
    /**
     * Mark a step as completed successfully
     */
    public void completeStep(
            String stepId,
            Map<String, Object> outputData,
            Instant timestamp,
            Long durationMs
    ) {
        StepExecution step = steps.get(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        if (step.status != StepStatus.RUNNING) {
            throw new IllegalStateException("Step cannot be completed from status: " + step.status);
        }
        
        StepCompletedEvent event = new StepCompletedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                stepId,
                outputData,
                timestamp,
                durationMs
        );
        
        apply(event, true);
    }
    
    /**
     * Mark a step as failed
     */
    public void failStep(
            String stepId,
            String errorType,
            String errorMessage,
            Map<String, Object> errorDetails,
            Instant timestamp,
            Integer attemptNumber
    ) {
        StepExecution step = steps.get(stepId);
        if (step == null) {
            throw new IllegalArgumentException("Step not found: " + stepId);
        }
        
        StepFailedEvent event = new StepFailedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                stepId,
                errorType,
                errorMessage,
                errorDetails,
                timestamp,
                attemptNumber
        );
        
        apply(event, true);
    }
    
    /**
     * Mark the journey as completed
     */
    public void completeJourney(Instant timestamp, Long totalDurationMs) {
        if (status != ExecutionStatus.RUNNING) {
            throw new IllegalStateException("Journey cannot be completed from status: " + status);
        }
        
        JourneyCompletedEvent event = new JourneyCompletedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                new HashMap<>(context),
                timestamp,
                totalDurationMs
        );
        
        apply(event, true);
    }
    
    /**
     * Mark the journey as failed
     */
    public void failJourney(
            String failureReason,
            String failedStepId,
            Map<String, Object> errorContext,
            Instant timestamp
    ) {
        if (status.isTerminal()) {
            throw new IllegalStateException("Journey cannot be failed from status: " + status);
        }
        
        JourneyFailedEvent event = new JourneyFailedEvent(
                executionId,
                nextSequenceNumber(),
                timestamp,
                failureReason,
                failedStepId,
                errorContext,
                timestamp
        );
        
        apply(event, true);
    }
    
    // ========== Event Application Methods (State Mutations) ==========
    
    private void applyJourneyStarted(JourneyStartedEvent event) {
        this.executionId = event.aggregateId();
        this.journeyDefinitionId = event.journeyDefinitionId();
        this.journeyVersion = event.journeyVersion();
        this.status = ExecutionStatus.RUNNING;
        this.context = new HashMap<>(event.initialContext());
        this.startedAt = event.occurredAt();
    }
    
    private void applyStepScheduled(StepScheduledEvent event) {
        StepExecution stepExecution = new StepExecution(
                event.stepId(),
                event.stepType(),
                StepStatus.SCHEDULED,
                event.stepConfig(),
                event.scheduledFor(),
                null,
                null,
                null,
                null,
                0
        );
        steps.put(event.stepId(), stepExecution);
        this.currentStepId = event.stepId();
    }
    
    private void applyStepStarted(StepStartedEvent event) {
        StepExecution current = steps.get(event.stepId());
        StepExecution updated = new StepExecution(
                current.stepId,
                current.stepType,
                StepStatus.RUNNING,
                current.config,
                current.scheduledAt,
                event.startedAt(),
                null,
                null,
                null,
                current.attemptNumber + 1
        );
        steps.put(event.stepId(), updated);
        this.currentStepId = event.stepId();
    }
    
    private void applyStepCompleted(StepCompletedEvent event) {
        StepExecution current = steps.get(event.stepId());
        StepExecution updated = new StepExecution(
                current.stepId,
                current.stepType,
                StepStatus.COMPLETED,
                current.config,
                current.scheduledAt,
                current.startedAt,
                event.completedAt(),
                event.outputData(),
                null,
                current.attemptNumber
        );
        steps.put(event.stepId(), updated);
        
        // Merge output data into context
        if (event.outputData() != null) {
            context.putAll(event.outputData());
        }
    }
    
    private void applyStepFailed(StepFailedEvent event) {
        StepExecution current = steps.get(event.stepId());
        StepExecution updated = new StepExecution(
                current.stepId,
                current.stepType,
                StepStatus.FAILED,
                current.config,
                current.scheduledAt,
                current.startedAt,
                event.failedAt(),
                null,
                event.errorMessage(),
                event.attemptNumber()
        );
        steps.put(event.stepId(), updated);
    }
    
    private void applyJourneyCompleted(JourneyCompletedEvent event) {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = event.completedAt();
        this.context = new HashMap<>(event.finalContext());
    }
    
    private void applyJourneyFailed(JourneyFailedEvent event) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = event.failedAt();
    }
    
    // ========== Helper Methods ==========
    
    private Long nextSequenceNumber() {
        return version + 1;
    }
    
    /**
     * Get all uncommitted events and clear the list
     */
    public List<DomainEvent> getUncommittedEvents() {
        List<DomainEvent> events = new ArrayList<>(uncommittedEvents);
        uncommittedEvents.clear();
        return events;
    }
    
    /**
     * Mark all uncommitted events as committed
     */
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    // ========== Getters ==========
    
    public UUID getExecutionId() {
        return executionId;
    }
    
    public UUID getJourneyDefinitionId() {
        return journeyDefinitionId;
    }
    
    public String getJourneyVersion() {
        return journeyVersion;
    }
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }
    
    public Instant getStartedAt() {
        return startedAt;
    }
    
    public Instant getCompletedAt() {
        return completedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public String getCurrentStepId() {
        return currentStepId;
    }
    
    public Map<String, StepExecution> getSteps() {
        return Collections.unmodifiableMap(steps);
    }
    
    public Optional<StepExecution> getStep(String stepId) {
        return Optional.ofNullable(steps.get(stepId));
    }
}
