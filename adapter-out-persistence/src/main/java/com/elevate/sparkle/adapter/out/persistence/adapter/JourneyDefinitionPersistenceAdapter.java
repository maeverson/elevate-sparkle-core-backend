package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.entity.JourneyDefinitionEntity;
import com.elevate.sparkle.adapter.out.persistence.mapper.JourneyDefinitionMapper;
import com.elevate.sparkle.adapter.out.persistence.repository.JourneyDefinitionJpaRepository;
import com.elevate.sparkle.application.port.out.JourneyDefinitionRepositoryPort;
import com.elevate.sparkle.domain.model.JourneyDefinition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Persistence adapter for Journey Definitions
 */
@Component
public class JourneyDefinitionPersistenceAdapter implements JourneyDefinitionRepositoryPort {
    
    private final JourneyDefinitionJpaRepository jpaRepository;
    private final JourneyDefinitionMapper mapper;
    
    public JourneyDefinitionPersistenceAdapter(
            JourneyDefinitionJpaRepository jpaRepository,
            JourneyDefinitionMapper mapper
    ) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public JourneyDefinition save(JourneyDefinition definition) {
        // Check if exists
        Optional<JourneyDefinitionEntity> existing = jpaRepository.findById(definition.getId());
        
        JourneyDefinitionEntity entity;
        if (existing.isPresent()) {
            entity = existing.get();
            mapper.updateEntity(definition, entity);
        } else {
            entity = mapper.toEntity(definition);
        }
        
        JourneyDefinitionEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<JourneyDefinition> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<JourneyDefinition> findByName(String name) {
        return jpaRepository.findByName(name)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<JourneyDefinition> findAll() {
        return jpaRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<JourneyDefinition> findAll(int page, int size) {
        return jpaRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<JourneyDefinition> findAllActive() {
        return jpaRepository.findAllActive().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
