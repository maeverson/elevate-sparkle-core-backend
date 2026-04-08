package com.elevate.sparkle.application.port.in;

import java.util.UUID;

/**
 * Use case for publishing a journey version
 */
public interface PublishJourneyVersionUseCase {
    
    /**
     * Publish a specific version of a journey
     */
    void execute(UUID journeyDefinitionId, UUID versionId);
}
