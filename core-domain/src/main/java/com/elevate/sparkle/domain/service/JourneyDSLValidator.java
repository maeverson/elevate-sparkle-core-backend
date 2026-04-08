package com.elevate.sparkle.domain.service;

import com.elevate.sparkle.domain.valueobject.JourneyDSL;

import java.util.*;

/**
 * Domain service for validating Journey DSL.
 * Ensures the workflow definition is valid before persistence.
 */
public class JourneyDSLValidator {
    
    /**
     * Validate a Journey DSL
     * @return Validation result with errors if any
     */
    public ValidationResult validate(JourneyDSL dsl) {
        List<String> errors = new ArrayList<>();
        
        if (dsl == null) {
            errors.add("DSL cannot be null");
            return new ValidationResult(false, errors);
        }
        
        // 1. Validate start step exists
        validateStartStep(dsl, errors);
        
        // 2. Validate no orphaned steps
        validateNoOrphanedSteps(dsl, errors);
        
        // 3. Validate no duplicate step IDs
        validateNoDuplicateSteps(dsl, errors);
        
        // 4. Validate all referenced steps exist
        validateReferencedStepsExist(dsl, errors);
        
        // 5. Validate step types
        validateStepTypes(dsl, errors);
        
        // 6. Validate no cycles (optional - can be allowed for loops)
        // Not implemented yet as some workflows may need loops
        
        // 7. Validate timeout values
        validateTimeouts(dsl, errors);
        
        // 8. Validate retry policies
        validateRetryPolicies(dsl, errors);
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    private void validateStartStep(JourneyDSL dsl, List<String> errors) {
        if (dsl.startStepId() == null || dsl.startStepId().isBlank()) {
            errors.add("Start step ID cannot be blank");
            return;
        }
        
        boolean startStepExists = dsl.steps().stream()
                .anyMatch(s -> s.id().equals(dsl.startStepId()));
        
        if (!startStepExists) {
            errors.add("Start step '" + dsl.startStepId() + "' does not exist in step definitions");
        }
    }
    
    private void validateNoOrphanedSteps(JourneyDSL dsl, List<String> errors) {
        Set<String> reachableSteps = new HashSet<>();
        Set<String> visited = new HashSet<>();
        Queue<String> toVisit = new LinkedList<>();
        
        toVisit.add(dsl.startStepId());
        
        while (!toVisit.isEmpty()) {
            String currentId = toVisit.poll();
            if (visited.contains(currentId)) {
                continue;
            }
            visited.add(currentId);
            reachableSteps.add(currentId);
            
            Optional<JourneyDSL.StepDefinition> stepOpt = dsl.findStep(currentId);
            if (stepOpt.isEmpty()) {
                continue;
            }
            
            JourneyDSL.StepDefinition step = stepOpt.get();
            
            // Add next step
            if (step.nextStepId() != null) {
                toVisit.add(step.nextStepId());
            }
            
            // Add conditional transitions
            if (step.conditionalTransitions() != null) {
                for (JourneyDSL.ConditionalTransition transition : step.conditionalTransitions()) {
                    toVisit.add(transition.targetStepId());
                }
            }
        }
        
        // Find orphaned steps
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            if (!reachableSteps.contains(step.id())) {
                errors.add("Step '" + step.id() + "' is not reachable from start step");
            }
        }
    }
    
    private void validateNoDuplicateSteps(JourneyDSL dsl, List<String> errors) {
        Set<String> seenIds = new HashSet<>();
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            if (!seenIds.add(step.id())) {
                errors.add("Duplicate step ID found: '" + step.id() + "'");
            }
        }
    }
    
    private void validateReferencedStepsExist(JourneyDSL dsl, List<String> errors) {
        Set<String> existingStepIds = dsl.getAllStepIds();
        Set<String> referencedStepIds = dsl.getAllReferencedStepIds();
        
        for (String referencedId : referencedStepIds) {
            if (!existingStepIds.contains(referencedId)) {
                errors.add("Referenced step '" + referencedId + "' does not exist");
            }
        }
    }
    
    private void validateStepTypes(JourneyDSL dsl, List<String> errors) {
        Set<String> validTypes = Set.of("HTTP", "INTERNAL", "MESSAGE", "WAIT", "CONDITION");
        
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            if (!validTypes.contains(step.type())) {
                errors.add("Step '" + step.id() + "' has invalid type: '" + step.type() + 
                        "'. Valid types: " + validTypes);
            }
        }
    }
    
    private void validateTimeouts(JourneyDSL dsl, List<String> errors) {
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            if (step.timeoutMs() != null && step.timeoutMs() <= 0) {
                errors.add("Step '" + step.id() + "' has invalid timeout: must be positive");
            }
        }
    }
    
    private void validateRetryPolicies(JourneyDSL dsl, List<String> errors) {
        for (JourneyDSL.StepDefinition step : dsl.steps()) {
            if (step.retryPolicy() != null) {
                try {
                    // Validation is in the record constructor
                    // This just ensures it doesn't throw
                } catch (IllegalArgumentException e) {
                    errors.add("Step '" + step.id() + "' has invalid retry policy: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Validation result
     */
    public record ValidationResult(
            boolean valid,
            List<String> errors
    ) {
        public ValidationResult {
            errors = errors == null ? List.of() : List.copyOf(errors);
        }
        
        public String getErrorMessage() {
            if (valid) {
                return "";
            }
            return String.join("; ", errors);
        }
    }
}
