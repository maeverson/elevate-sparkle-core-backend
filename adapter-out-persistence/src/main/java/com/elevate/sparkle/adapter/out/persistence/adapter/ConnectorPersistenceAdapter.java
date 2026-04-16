package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.mapper.ConnectorPersistenceMapper;
import com.elevate.sparkle.adapter.out.persistence.repository.ConnectorJpaRepository;
import com.elevate.sparkle.application.port.out.ConnectorRepositoryPort;
import com.elevate.sparkle.domain.model.Connector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing ConnectorRepositoryPort
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConnectorPersistenceAdapter implements ConnectorRepositoryPort {

    private final ConnectorJpaRepository connectorJpaRepository;
    private final ConnectorPersistenceMapper mapper;

    @Override
    @Transactional
    public Connector save(Connector connector) {
        log.debug("Saving connector: {}", connector.getId());
        var jpaEntity = mapper.toJpaEntity(connector);
        var saved = connectorJpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Connector> findById(String id) {
        log.debug("Finding connector by ID: {}", id);
        return connectorJpaRepository.findById(id)
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connector> findAll() {
        log.debug("Finding all connectors");
        return connectorJpaRepository.findAll().stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Connector> findAllByTenantId(String tenantId) {
        log.debug("Finding connectors by tenant: {}", tenantId);
        return connectorJpaRepository.findAllByTenantId(tenantId).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        log.debug("Deleting connector: {}", id);
        connectorJpaRepository.deleteById(id);
    }
}
