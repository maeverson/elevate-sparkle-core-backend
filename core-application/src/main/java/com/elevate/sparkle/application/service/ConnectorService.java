package com.elevate.sparkle.application.service;

import com.elevate.sparkle.application.port.in.ConnectorUseCase;
import com.elevate.sparkle.application.port.out.ConnectorRepositoryPort;
import com.elevate.sparkle.domain.exception.EntityNotFoundException;
import com.elevate.sparkle.domain.model.Connector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Service implementation for connector management
 */
@Slf4j
@RequiredArgsConstructor
public class ConnectorService implements ConnectorUseCase {

    private final ConnectorRepositoryPort connectorRepository;

    @Override
    public List<Connector> listConnectors(String tenantId) {
        log.info("Listing connectors for tenant: {}", tenantId);
        if (tenantId != null && !tenantId.isEmpty()) {
            return connectorRepository.findAllByTenantId(tenantId);
        }
        return connectorRepository.findAll();
    }

    @Override
    public Connector getConnector(String connectorId) {
        log.info("Getting connector: {}", connectorId);
        return connectorRepository.findById(connectorId)
                .orElseThrow(() -> new EntityNotFoundException("Connector", connectorId));
    }

    @Override
    public Connector createConnector(CreateConnectorCommand command) {
        log.info("Creating connector: {}", command.name());

        Connector connector = Connector.createNew(
                command.name(),
                command.type(),
                command.config(),
                command.tenantId()
        );

        Connector saved = connectorRepository.save(connector);
        log.info("Connector created: {}", saved.getId());
        return saved;
    }

    @Override
    public Connector updateConnector(UpdateConnectorCommand command) {
        log.info("Updating connector: {}", command.connectorId());

        Connector connector = connectorRepository.findById(command.connectorId())
                .orElseThrow(() -> new EntityNotFoundException("Connector", command.connectorId()));

        connector.update(command.name(), command.type(), command.config(), command.status());

        Connector saved = connectorRepository.save(connector);
        log.info("Connector updated: {}", saved.getId());
        return saved;
    }

    @Override
    public void deleteConnector(String connectorId) {
        log.info("Deleting connector: {}", connectorId);
        connectorRepository.findById(connectorId)
                .orElseThrow(() -> new EntityNotFoundException("Connector", connectorId));
        connectorRepository.deleteById(connectorId);
        log.info("Connector deleted: {}", connectorId);
    }
}
