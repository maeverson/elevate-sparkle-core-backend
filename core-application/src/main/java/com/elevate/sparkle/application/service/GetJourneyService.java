package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.GetJourneyUseCase;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyDefinition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for retrieving journey definitions
 */
public class GetJourneyService implements GetJourneyUseCase {
    
    private final JourneyDefinitionRepositoryPort repository;
    
    public GetJourneyService(JourneyDefinitionRepositoryPort repository) {
        this.repository = repository;
    }
    
    @Override
    public Optional<JourneyDefinition> findById(UUID id) {
        return repository.findById(id);
    }
    
    @Override
    public Optional<JourneyDefinition> findByName(String name) {
        return repository.findByName(name);
    }
    
    @Override
    public List<JourneyDefinition> findAll() {
        return repository.findAll();
    }
    
    @Override
    public List<JourneyDefinition> findAllActive() {
        return repository.findAllActive();
    }
}
