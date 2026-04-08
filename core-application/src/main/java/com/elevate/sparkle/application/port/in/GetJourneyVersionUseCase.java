package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.JourneyVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Use case for retrieving journey versions
 */
public interface GetJourneyVersionUseCase {
    
    /**
     * Get version by ID
     */
    Optional<JourneyVersion> findById(UUID versionId);
    
    /**
     * Get all versions of a journey
     */
    List<JourneyVersion> findAllByJourney(UUID journeyDefinitionId);
    
    /**
     * Get specific version
     */
    Optional<JourneyVersion> findByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber);
    
    /**
     * Get the current published version
     */
    Optional<JourneyVersion> findPublishedVersion(UUID journeyDefinitionId);
}
