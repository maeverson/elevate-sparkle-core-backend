package com.elevate.sparkle.domain.valueobject;

import java.util.*;

/**
 * Value object representing the Journey DSL (Domain Specific Language).
 * This defines the workflow structure with steps and transitions.
 */
public record JourneyDSL(
        String startStepId,
        List<StepDefinition> steps,
        Map<String, Object> globalConfig
) {
    
    public JourneyDSL {
        Objects.requireNonNull(startStepId, "startStepId cannot be null");
        Objects.requireNonNull(steps, "steps cannot be null");
        
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Journey must have at least one step");
        }
        
        // Make immutable copies
        steps = List.copyOf(steps);
        globalConfig = globalConfig == null ? Map.of() : Map.copyOf(globalConfig);
    }
    
    /**
     * Represents a single step in the journey
     */
    public record StepDefinition(
            String id,
            String name,
            String type,
            Map<String, Object> config,
            String nextStepId,
            List<ConditionalTransition> conditionalTransitions,
            RetryPolicy retryPolicy,
            Long timeoutMs
    ) {
        public StepDefinition {
            Objects.requireNonNull(id, "step id cannot be null");
            Objects.requireNonNull(name, "step name cannot be null");
            Objects.requireNonNull(type, "step type cannot be null");
            
            config = config == null ? Map.of() : Map.copyOf(config);
            conditionalTransitions = conditionalTransitions == null ? 
                    List.of() : List.copyOf(conditionalTransitions);
        }
        
        public boolean hasConditionalTransitions() {
            return conditionalTransitions != null && !conditionalTransitions.isEmpty();
        }
    }
    
    /**
     * Conditional transition based on step output
     */
    public record ConditionalTransition(
            String condition,
            String targetStepId
    ) {
        public ConditionalTransition {
            Objects.requireNonNull(condition, "condition cannot be null");
            Objects.requireNonNull(targetStepId, "targetStepId cannot be null");
        }
    }
    
    /**
     * Retry policy for a step
     */
    public record RetryPolicy(
            int maxAttempts,
            long initialDelayMs,
            long maxDelayMs,
            double backoffMultiplier
    ) {
        public RetryPolicy {
            if (maxAttempts < 0) {
                throw new IllegalArgumentException("maxAttempts cannot be negative");
            }
            if (initialDelayMs < 0) {
                throw new IllegalArgumentException("initialDelayMs cannot be negative");
            }
            if (maxDelayMs < initialDelayMs) {
                throw new IllegalArgumentException("maxDelayMs cannot be less than initialDelayMs");
            }
            if (backoffMultiplier < 1.0) {
                throw new IllegalArgumentException("backoffMultiplier must be >= 1.0");
            }
        }
        
        public static RetryPolicy defaultPolicy() {
            return new RetryPolicy(3, 1000, 30000, 2.0);
        }
        
        public static RetryPolicy noRetry() {
            return new RetryPolicy(0, 0, 0, 1.0);
        }
    }
    
    /**
     * Get all step IDs in this journey
     */
    public Set<String> getAllStepIds() {
        Set<String> stepIds = new HashSet<>();
        for (StepDefinition step : steps) {
            stepIds.add(step.id());
        }
        return stepIds;
    }
    
    /**
     * Find a step by ID
     */
    public Optional<StepDefinition> findStep(String stepId) {
        return steps.stream()
                .filter(s -> s.id().equals(stepId))
                .findFirst();
    }
    
    /**
     * Get all referenced step IDs (including in transitions)
     */
    public Set<String> getAllReferencedStepIds() {
        Set<String> referenced = new HashSet<>();
        referenced.add(startStepId);
        
        for (StepDefinition step : steps) {
            if (step.nextStepId() != null) {
                referenced.add(step.nextStepId());
            }
            if (step.conditionalTransitions() != null) {
                for (ConditionalTransition transition : step.conditionalTransitions()) {
                    referenced.add(transition.targetStepId());
                }
            }
        }
        
        return referenced;
    }
}
