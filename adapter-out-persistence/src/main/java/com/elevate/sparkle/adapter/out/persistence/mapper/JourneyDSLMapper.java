package com.elevate.sparkle.adapter.out.persistence.mapper;

import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for JourneyDSL to/from JSON Map
 */
@Component
public class JourneyDSLMapper {
    
    private final ObjectMapper objectMapper;
    
    public JourneyDSLMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    /**
     * Convert DSL value object to Map for JSON storage
     */
    public Map<String, Object> toMap(JourneyDSL dsl) {
        return objectMapper.convertValue(dsl, new TypeReference<>() {});
    }
    
    /**
     * Convert Map from JSON to DSL value object
     */
    @SuppressWarnings("unchecked")
    public JourneyDSL fromMap(Map<String, Object> map) {
        String startStepId = (String) map.get("startStepId");
        
        List<Map<String, Object>> stepsData = (List<Map<String, Object>>) map.get("steps");
        List<JourneyDSL.StepDefinition> steps = stepsData.stream()
                .map(this::mapToStepDefinition)
                .collect(Collectors.toList());
        
        Map<String, Object> globalConfig = (Map<String, Object>) map.getOrDefault("globalConfig", Map.of());
        
        return new JourneyDSL(startStepId, steps, globalConfig);
    }
    
    @SuppressWarnings("unchecked")
    private JourneyDSL.StepDefinition mapToStepDefinition(Map<String, Object> stepMap) {
        String id = (String) stepMap.get("id");
        String name = (String) stepMap.get("name");
        String type = (String) stepMap.get("type");
        Map<String, Object> config = (Map<String, Object>) stepMap.getOrDefault("config", Map.of());
        String nextStepId = (String) stepMap.get("nextStepId");
        
        List<JourneyDSL.ConditionalTransition> conditionalTransitions = null;
        if (stepMap.containsKey("conditionalTransitions")) {
            List<Map<String, Object>> transitionsData = 
                    (List<Map<String, Object>>) stepMap.get("conditionalTransitions");
            conditionalTransitions = transitionsData.stream()
                    .map(t -> new JourneyDSL.ConditionalTransition(
                            (String) t.get("condition"),
                            (String) t.get("targetStepId")
                    ))
                    .collect(Collectors.toList());
        }
        
        JourneyDSL.RetryPolicy retryPolicy = null;
        if (stepMap.containsKey("retryPolicy")) {
            Map<String, Object> retryData = (Map<String, Object>) stepMap.get("retryPolicy");
            retryPolicy = new JourneyDSL.RetryPolicy(
                    ((Number) retryData.get("maxAttempts")).intValue(),
                    ((Number) retryData.get("initialDelayMs")).longValue(),
                    ((Number) retryData.get("maxDelayMs")).longValue(),
                    ((Number) retryData.get("backoffMultiplier")).doubleValue()
            );
        }
        
        Long timeoutMs = stepMap.containsKey("timeoutMs") ? 
                ((Number) stepMap.get("timeoutMs")).longValue() : null;
        
        return new JourneyDSL.StepDefinition(
                id,
                name,
                type,
                config,
                nextStepId,
                conditionalTransitions,
                retryPolicy,
                timeoutMs
        );
    }
}
