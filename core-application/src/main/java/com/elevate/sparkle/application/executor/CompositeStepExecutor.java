package com.elevate.sparkle.application.executor;

import com.elevate.sparkle.domain.port.out.StepExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Composite executor that delegates to specific step executors based on step type.
 * This is the main entry point for step execution.
 */
public class CompositeStepExecutor implements StepExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(CompositeStepExecutor.class);
    
    private final List<StepExecutor> executors;
    
    public CompositeStepExecutor(List<StepExecutor> executors) {
        this.executors = executors;
    }
    
    @Override
    public StepExecutionResult execute(
            String stepId,
            String stepType,
            Map<String, Object> stepConfig,
            Map<String, Object> executionContext
    ) throws StepExecutionException {
        
        logger.debug("Finding executor for step type: {}", stepType);
        
        for (StepExecutor executor : executors) {
            if (executor.supports(stepType)) {
                logger.info("Executing step using {}: stepId={}, stepType={}",
                        executor.getClass().getSimpleName(), stepId, stepType);
                return executor.execute(stepId, stepType, stepConfig, executionContext);
            }
        }
        
        throw new StepExecutionException(
                "UNSUPPORTED_STEP_TYPE",
                "No executor found for step type: " + stepType,
                Map.of("stepType", stepType, "stepId", stepId)
        );
    }
    
    @Override
    public boolean supports(String stepType) {
        return executors.stream().anyMatch(e -> e.supports(stepType));
    }
}
