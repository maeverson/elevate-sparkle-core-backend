package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.CreateJourneyUseCase;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for creating journey definitions
 */
public class CreateJourneyService implements CreateJourneyUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateJourneyService.class);
    
    private final JourneyDefinitionRepositoryPort journeyRepository;
    
    public CreateJourneyService(JourneyDefinitionRepositoryPort journeyRepository) {
        this.journeyRepository = journeyRepository;
    }
    
    @Override
    public JourneyDefinition execute(CreateJourneyCommand command) {
        logger.info("Creating journey: name={}", command.name());
        
        // Check if journey with same name already exists
        if (journeyRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Journey with name '" + command.name() + "' already exists");
        }
        
        // Create domain entity
        JourneyDefinition definition = JourneyDefinition.create(
                command.name(),
                command.description(),
                command.createdBy()
        );
        
        // Persist
        JourneyDefinition saved = journeyRepository.save(definition);
        
        logger.info("Journey created: id={}, name={}", saved.getId(), saved.getName());
        
        return saved;
    }
}
