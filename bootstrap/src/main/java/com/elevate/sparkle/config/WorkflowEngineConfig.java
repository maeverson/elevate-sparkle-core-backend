package com.elevate.sparkle.config;

import com.elevate.sparkle.application.engine.WorkflowEngine;
import com.elevate.sparkle.application.executor.CompositeStepExecutor;
import com.elevate.sparkle.application.executor.HttpStepExecutor;
import com.elevate.sparkle.application.executor.InternalStepExecutor;
import com.elevate.sparkle.application.executor.MessageStepExecutor;
import com.elevate.sparkle.domain.port.out.ExecutionEventRepository;
import com.elevate.sparkle.domain.port.out.StepExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Spring configuration for the Workflow Engine.
 * This wires together the hexagonal architecture components.
 */
@Configuration
public class WorkflowEngineConfig {
    
    /**
     * Create the core WorkflowEngine bean.
     * This is framework-agnostic - the engine doesn't know about Spring.
     */
    @Bean
    public WorkflowEngine workflowEngine(ExecutionEventRepository eventRepository) {
        return new WorkflowEngine(eventRepository);
    }
    
    /**
     * Create HTTP step executor.
     */
    @Bean
    public HttpStepExecutor httpStepExecutor() {
        return new HttpStepExecutor();
    }
    
    /**
     * Create internal step executor.
     */
    @Bean
    public InternalStepExecutor internalStepExecutor() {
        return new InternalStepExecutor();
    }
    
    /**
     * Create message step executor.
     */
    @Bean
    public MessageStepExecutor messageStepExecutor() {
        return new MessageStepExecutor();
    }
    
    /**
     * Create composite step executor that delegates to specific executors.
     */
    @Bean
    public StepExecutor stepExecutor(
            HttpStepExecutor httpExecutor,
            InternalStepExecutor internalExecutor,
            MessageStepExecutor messageExecutor
    ) {
        return new CompositeStepExecutor(List.of(
                httpExecutor,
                internalExecutor,
                messageExecutor
        ));
    }
}
