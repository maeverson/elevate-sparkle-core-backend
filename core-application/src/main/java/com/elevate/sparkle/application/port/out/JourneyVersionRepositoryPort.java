package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.valueobject.VersionStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for Journey Version persistence
 */
public interface JourneyVersionRepositoryPort {
    
    /**
     * Save a journey version
     */
    JourneyVersion save(JourneyVersion version);
    
    /**
     * Find a version by ID
     */
    Optional<JourneyVersion> findById(UUID id);
    
    /**
     * Find a specific version of a journey
     */
    Optional<JourneyVersion> findByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber);
    
    /**
     * List all versions of a journey
     */
    List<JourneyVersion> findAllByJourneyId(UUID journeyDefinitionId);
    
    /**
     * List all versions with a specific status
     */
    List<JourneyVersion> findAllByStatus(VersionStatus status);
    
    /**
     * Find the current published version of a journey
     */
    Optional<JourneyVersion> findPublishedVersion(UUID journeyDefinitionId);
    
    /**
     * Find the latest version (by creation time)
     */
    Optional<JourneyVersion> findLatestVersion(UUID journeyDefinitionId);
    
    /**
     * Check if a version exists
     */
    boolean existsByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber);
    
    /**
     * Count versions for a journey
     */
    long countByJourneyId(UUID journeyDefinitionId);
}
