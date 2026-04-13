package com.elevate.sparkle.adapter.in.web.mapper;

import com.elevate.sparkle.adapter.in.web.dto.ConditionalTransitionDTO;
import com.elevate.sparkle.adapter.in.web.dto.JourneyDSLDTO;
import com.elevate.sparkle.adapter.in.web.dto.RetryPolicyDTO;
import com.elevate.sparkle.adapter.in.web.dto.StepDefinitionDTO;
import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting between JourneyDSL domain object and DTOs
 */
@Component
public class JourneyDtoMapper {
    
    /**
     * Convert DTO to domain object
     */
    public JourneyDSL toDomain(JourneyDSLDTO dto) {
        if (dto == null) {
            return null;
        }
        
        // Convert globalTimeout to globalConfig map
        Map<String, Object> globalConfig = new HashMap<>();
        if (dto.globalTimeout() != null) {
            globalConfig.put("timeout", dto.globalTimeout());
        }
        
        return new JourneyDSL(
                dto.initialStep(),
                dto.steps() == null ? null : dto.steps().stream()
                        .map(this::toStepDefinition)
                        .collect(Collectors.toList()),
                globalConfig
        );
    }
    
    /**
     * Convert domain object to DTO
     */
    public JourneyDSLDTO toDTO(JourneyDSL dsl) {
        if (dsl == null) {
            return null;
        }
        
        // Extract globalTimeout from globalConfig
        Long globalTimeout = null;
        if (dsl.globalConfig() != null && dsl.globalConfig().containsKey("timeout")) {
            Object timeoutValue = dsl.globalConfig().get("timeout");
            if (timeoutValue instanceof Number) {
                globalTimeout = ((Number) timeoutValue).longValue();
            }
        }
        
        return new JourneyDSLDTO(
                dsl.startStepId(),
                dsl.steps() == null ? null : dsl.steps().stream()
                        .map(this::toStepDefinitionDTO)
                        .collect(Collectors.toList()),
                globalTimeout
        );
    }
    
    /**
     * Convert Map to domain object (used by REST endpoints)
     */
    @SuppressWarnings("unchecked")
    public JourneyDSL fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        
        // For now, convert to DTO first (can be optimized later)
        JourneyDSLDTO dto = new JourneyDSLDTO(
                (String) map.get("initialStep"),
                null, // steps handled separately if needed
                map.containsKey("globalTimeout") ? ((Number) map.get("globalTimeout")).longValue() : null
        );
        
        return toDomain(dto);
    }
    
    /**
     * Convert domain object to Map (used by REST responses)
     */
    public Map<String, Object> toMap(JourneyDSL dsl) {
        if (dsl == null) {
            return null;
        }
        
        // For now, convert to DTO first (can be optimized later)
        JourneyDSLDTO dto = toDTO(dsl);
        
        Map<String, Object> map = new HashMap<>();
        map.put("initialStep", dto.initialStep());
        if (dto.globalTimeout() != null) {
            map.put("globalTimeout", dto.globalTimeout());
        }
        // Add other fields as needed
        
        return map;
    }
    
    private JourneyDSL.StepDefinition toStepDefinition(StepDefinitionDTO dto) {
        return new JourneyDSL.StepDefinition(
                dto.id(),
                dto.id(), // Use id as name for now (can be enhanced later)
                dto.type(),
                dto.config(),
                null, // nextStepId - not in DTO
                dto.transitions() == null ? null : dto.transitions().stream()
                        .map(this::toConditionalTransition)
                        .collect(Collectors.toList()),
                dto.retryPolicy() == null ? null : toRetryPolicy(dto.retryPolicy()),
                dto.timeout()
        );
    }
    
    private StepDefinitionDTO toStepDefinitionDTO(JourneyDSL.StepDefinition step) {
        return new StepDefinitionDTO(
                step.id(),
                step.type(),
                step.config(),
                step.timeoutMs(),
                toRetryPolicyDTO(step.retryPolicy()),
                step.conditionalTransitions() == null ? null : step.conditionalTransitions().stream()
                        .map(this::toConditionalTransitionDTO)
                        .collect(Collectors.toList())
        );
    }
    
    private JourneyDSL.ConditionalTransition toConditionalTransition(ConditionalTransitionDTO dto) {
        return new JourneyDSL.ConditionalTransition(
                dto.condition(),
                dto.nextStepId()
        );
    }
    
    private ConditionalTransitionDTO toConditionalTransitionDTO(JourneyDSL.ConditionalTransition transition) {
        return new ConditionalTransitionDTO(
                transition.condition(),
                transition.targetStepId()
        );
    }
    
    private JourneyDSL.RetryPolicy toRetryPolicy(RetryPolicyDTO dto) {
        if (dto == null) {
            return null;
        }
        
        return new JourneyDSL.RetryPolicy(
                dto.maxAttempts() != null ? dto.maxAttempts() : 0,
                dto.initialBackoffMs() != null ? dto.initialBackoffMs() : 1000L,
                dto.initialBackoffMs() != null ? dto.initialBackoffMs() * 30 : 30000L, // maxDelayMs
                dto.backoffMultiplier() != null ? dto.backoffMultiplier() : 2.0
        );
    }
    
    private RetryPolicyDTO toRetryPolicyDTO(JourneyDSL.RetryPolicy policy) {
        if (policy == null) {
            return null;
        }
        
        return new RetryPolicyDTO(
                policy.maxAttempts(),
                policy.backoffMultiplier(),
                policy.initialDelayMs()
        );
    }
}
