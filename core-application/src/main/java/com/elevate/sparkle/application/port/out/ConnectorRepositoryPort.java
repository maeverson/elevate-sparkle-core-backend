package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.Connector;

import java.util.List;
import java.util.Optional;

/**
 * Output port for connector persistence
 */
public interface ConnectorRepositoryPort {

    Connector save(Connector connector);

    Optional<Connector> findById(String id);

    List<Connector> findAll();

    List<Connector> findAllByTenantId(String tenantId);

    void deleteById(String id);
}
