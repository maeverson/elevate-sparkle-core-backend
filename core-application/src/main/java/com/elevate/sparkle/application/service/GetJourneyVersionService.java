package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.GetJourneyVersionUseCase;
import com.elevate.sparkle.application.port.out.JourneyVersionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyVersion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for retrieving journey versions
 */
public class GetJourneyVersionService implements GetJourneyVersionUseCase {
    
    private final JourneyVersionRepositoryPort repository;
    
    public GetJourneyVersionService(JourneyVersionRepositoryPort repository) {
        this.repository = repository;
    }
    
    @Override
    public Optional<JourneyVersion> findById(UUID versionId) {
        return repository.findById(versionId);
    }
    
    @Override
    public List<JourneyVersion> findAllByJourney(UUID journeyDefinitionId) {
        return repository.findAllByJourneyId(journeyDefinitionId);
    }
    
    @Override
    public Optional<JourneyVersion> findByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber) {
        return repository.findByJourneyAndVersion(journeyDefinitionId, versionNumber);
    }
    
    @Override
    public Optional<JourneyVersion> findPublishedVersion(UUID journeyDefinitionId) {
        return repository.findPublishedVersion(journeyDefinitionId);
    }
}
