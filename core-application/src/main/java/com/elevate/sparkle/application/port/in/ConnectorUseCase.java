package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.Connector;
import com.elevate.sparkle.domain.valueobject.ConnectorStatus;
import com.elevate.sparkle.domain.valueobject.ConnectorType;

import java.util.List;

/**
 * Input port for connector management
 */
public interface ConnectorUseCase {

    List<Connector> listConnectors(String tenantId);

    Connector getConnector(String connectorId);

    Connector createConnector(CreateConnectorCommand command);

    Connector updateConnector(UpdateConnectorCommand command);

    void deleteConnector(String connectorId);

    record CreateConnectorCommand(
            String name,
            ConnectorType type,
            String config,
            String tenantId
    ) {}

    record UpdateConnectorCommand(
            String connectorId,
            String name,
            ConnectorType type,
            String config,
            ConnectorStatus status
    ) {}
}
