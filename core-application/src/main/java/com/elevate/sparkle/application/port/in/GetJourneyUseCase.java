package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.JourneyDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case for retrieving journey definitions
 */
public interface GetJourneyUseCase {
    
    /**
     * Get journey by ID
     */
    Optional<JourneyDefinition> findById(UUID id);
    
    /**
     * Get journey by name
     */
    Optional<JourneyDefinition> findByName(String name);
    
    /**
     * List all journeys
     */
    List<JourneyDefinition> findAll();
    
    /**
     * List only active journeys
     */
    List<JourneyDefinition> findAllActive();
}
