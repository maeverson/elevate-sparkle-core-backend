package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.entity.JourneyVersionEntity;
import com.elevate.sparkle.adapter.out.persistence.mapper.JourneyVersionMapper;
import com.elevate.sparkle.adapter.out.persistence.repository.JourneyVersionJpaRepository;
import com.elevate.sparkle.application.port.out.JourneyVersionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyVersion;
import com.elevate.sparkle.domain.valueobject.VersionStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter for Journey Versions
 */
@Component
public class JourneyVersionPersistenceAdapter implements JourneyVersionRepositoryPort {
    
    private final JourneyVersionJpaRepository jpaRepository;
    private final JourneyVersionMapper mapper;
    
    public JourneyVersionPersistenceAdapter(
            JourneyVersionJpaRepository jpaRepository,
            JourneyVersionMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public JourneyVersion save(JourneyVersion version) {
        Optional<JourneyVersionEntity> existing = jpaRepository.findById(version.getId());
        
        JourneyVersionEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            mapper.updateEntity(version, entity);
        } else {
            entity = mapper.toEntity(version);
        }
        
        JourneyVersionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<JourneyVersion> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<JourneyVersion> findByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber) {
        return jpaRepository.findByJourneyDefinitionIdAndVersionNumber(journeyDefinitionId, versionNumber)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<JourneyVersion> findAllByJourneyId(UUID journeyDefinitionId) {
        return jpaRepository.findByJourneyDefinitionIdOrderByCreatedAtDesc(journeyDefinitionId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<JourneyVersion> findAllByStatus(VersionStatus status) {
        return jpaRepository.findByStatusOrderByCreatedAtDesc(status.name()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<JourneyVersion> findPublishedVersion(UUID journeyDefinitionId) {
        return jpaRepository.findPublishedVersion(journeyDefinitionId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<JourneyVersion> findLatestVersion(UUID journeyDefinitionId) {
        return jpaRepository.findLatestVersion(journeyDefinitionId)
                .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByJourneyAndVersion(UUID journeyDefinitionId, String versionNumber) {
        return jpaRepository.existsByJourneyDefinitionIdAndVersionNumber(journeyDefinitionId, versionNumber);
    }
    
    @Override
    public long countByJourneyId(UUID journeyDefinitionId) {
        return jpaRepository.countByJourneyDefinitionId(journeyDefinitionId);
    }
}
