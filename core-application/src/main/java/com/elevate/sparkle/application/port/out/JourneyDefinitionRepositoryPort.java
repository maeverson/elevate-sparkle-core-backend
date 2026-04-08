package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.JourneyDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for Journey Definition persistence
 */
public interface JourneyDefinitionRepositoryPort {
    
    /**
     * Save a journey definition
     */
    JourneyDefinition save(JourneyDefinition definition);
    
    /**
     * Find a journey definition by ID
     */
    Optional<JourneyDefinition> findById(UUID id);
    
    /**
     * Find a journey definition by name
     */
    Optional<JourneyDefinition> findByName(String name);
    
    /**
     * List all journey definitions
     */
    List<JourneyDefinition> findAll();
    
    /**
     * List journey definitions with pagination
     */
    List<JourneyDefinition> findAll(int page, int size);
    
    /**
     * List only active (non-archived) journeys
     */
    List<JourneyDefinition> findAllActive();
    
    /**
     * Check if a journey exists by ID
     */
    boolean existsById(UUID id);
    
    /**
     * Check if a journey exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Delete a journey definition (soft delete by archiving is preferred)
     */
    void deleteById(UUID id);
}
