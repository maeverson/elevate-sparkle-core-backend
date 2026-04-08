package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.CreateJourneyVersionUseCase;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.application.port.out.JourneyVersionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.service.JourneyDSLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for creating journey versions
 */
public class CreateJourneyVersionService implements CreateJourneyVersionUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(CreateJourneyVersionService.class);
    
    private final JourneyVersionRepositoryPort versionRepository;
    private final JourneyDefinitionRepositoryPort definitionRepository;
    private final JourneyDSLValidator dslValidator;
    
    public CreateJourneyVersionService(
            JourneyVersionRepositoryPort versionRepository,
            JourneyDefinitionRepositoryPort definitionRepository,
            JourneyDSLValidator dslValidator
    ) {
        this.versionRepository = versionRepository;
        this.definitionRepository = definitionRepository;
        this.dslValidator = dslValidator;
    }
    
    @Override
    public JourneyVersion execute(CreateVersionCommand command) {
        logger.info("Creating journey version: journeyId={}, version={}",
                command.journeyDefinitionId(), command.versionNumber());
        
        // 1. Check journey exists
        if (!definitionRepository.existsById(command.journeyDefinitionId())) {
            throw new IllegalArgumentException(
                    "Journey definition not found: " + command.journeyDefinitionId()
            );
        }
        
        // 2. Check version doesn't already exist
        if (versionRepository.existsByJourneyAndVersion(
                command.journeyDefinitionId(),
                command.versionNumber()
        )) {
            throw new IllegalArgumentException(
                    "Version " + command.versionNumber() + " already exists for this journey"
            );
        }
        
        // 3. Validate DSL (CRITICAL)
        JourneyDSLValidator.ValidationResult validation = dslValidator.validate(command.dsl());
        if (!validation.valid()) {
            logger.error("DSL validation failed: {}", validation.getErrorMessage());
            throw new IllegalArgumentException("DSL validation failed: " + validation.getErrorMessage());
        }
        
        // 4. Create version
        JourneyVersion version = JourneyVersion.create(
                command.journeyDefinitionId(),
                command.versionNumber(),
                command.dsl(),
                command.createdBy(),
                command.changeNotes()
        );
        
        // 5. Persist
        JourneyVersion saved = versionRepository.save(version);
        
        logger.info("Journey version created: id={}, version={}",
                saved.getId(), saved.getVersionNumber());
        
        return saved;
    }
}
