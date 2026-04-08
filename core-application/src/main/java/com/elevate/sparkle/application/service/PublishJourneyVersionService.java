package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.PublishJourneyVersionUseCase;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.application.port.out.JourneyVersionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyDefinition;
import com.elevate.sparkle.domain.model.JourneyVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Service for publishing journey versions
 */
public class PublishJourneyVersionService implements PublishJourneyVersionUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(PublishJourneyVersionService.class);
    
    private final JourneyVersionRepositoryPort versionRepository;
    private final JourneyDefinitionRepositoryPort definitionRepository;
    
    public PublishJourneyVersionService(
            JourneyVersionRepositoryPort versionRepository,
            JourneyDefinitionRepositoryPort definitionRepository
    ) {
        this.versionRepository = versionRepository;
        this.definitionRepository = definitionRepository;
    }
    
    @Override
    public void execute(UUID journeyDefinitionId, UUID versionId) {
        logger.info("Publishing journey version: journeyId={}, versionId={}",
                journeyDefinitionId, versionId);
        
        // 1. Load journey definition
        JourneyDefinition definition = definitionRepository.findById(journeyDefinitionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Journey definition not found: " + journeyDefinitionId
                ));
        
        // 2. Load version
        JourneyVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Journey version not found: " + versionId
                ));
        
        // 3. Verify version belongs to journey
        if (!version.getJourneyDefinitionId().equals(journeyDefinitionId)) {
            throw new IllegalArgumentException(
                    "Version does not belong to journey"
            );
        }
        
        // 4. Publish version (domain logic)
        version.publish();
        
        // 5. Update journey definition to point to this published version
        definition.publishVersion(versionId);
        
        // 6. Persist changes
        versionRepository.save(version);
        definitionRepository.save(definition);
        
        logger.info("Journey version published: journeyId={}, versionId={}, version={}",
                journeyDefinitionId, versionId, version.getVersionNumber());
    }
}
