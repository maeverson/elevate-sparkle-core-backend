package com.elevate.sparkle.adapter.out.persistence.mapper;

import com.elevate.sparkle.adapter.out.persistence.entity.JourneyVersionEntity;
import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.valueobject.JourneyDSL;
import com.elevate.sparkle.domain.valueobject.VersionStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mapper between JourneyVersion domain model and JPA entity
 */
@Component
public class JourneyVersionMapper {
    
    private final ObjectMapper objectMapper;
    private final JourneyPersistenceMapper dslMapper;
    
    public JourneyVersionMapper(ObjectMapper objectMapper, JourneyPersistenceMapper dslMapper) {
        this.objectMapper = objectMapper;
        this.dslMapper = dslMapper;
    }
    
    /**
     * Convert domain model to JPA entity
     */
    public JourneyVersionEntity toEntity(JourneyVersion domain) {
        JourneyVersionEntity entity = new JourneyVersionEntity();
        entity.setId(domain.getId());
        entity.setJourneyDefinitionId(domain.getJourneyDefinitionId());
        entity.setVersionNumber(domain.getVersionNumber());
        entity.setDsl(dslMapper.toMap(domain.getDsl()));
        entity.setStatus(domain.getStatus().name());
        entity.setCreatedBy(domain.getCreatedBy());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setPublishedAt(domain.getPublishedAt());
        entity.setChangeNotes(domain.getChangeNotes());
        return entity;
    }
    
    /**
     * Convert JPA entity to domain model
     */
    public JourneyVersion toDomain(JourneyVersionEntity entity) {
        JourneyDSL dsl = dslMapper.fromMap(entity.getDsl());
        
        JourneyVersion domain = new JourneyVersion(
                entity.getId(),
                entity.getJourneyDefinitionId(),
                entity.getVersionNumber(),
                dsl,
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getChangeNotes()
        );
        
        // Note: Status and publishedAt will need to be set via reflection or domain methods
        // For now, we accept this limitation
        
        return domain;
    }
    
    /**
     * Update entity from domain
     */
    public void updateEntity(JourneyVersion domain, JourneyVersionEntity entity) {
        entity.setStatus(domain.getStatus().name());
        entity.setPublishedAt(domain.getPublishedAt());
    }
}
