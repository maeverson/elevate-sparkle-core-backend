package com.elevate.sparkle.adapter.in.web.mapper;

import com.elevate.sparkle.adapter.in.web.dto.ConnectorResponse;
import com.elevate.sparkle.adapter.in.web.dto.CreateConnectorRequest;
import com.elevate.sparkle.adapter.in.web.dto.UpdateConnectorRequest;
import com.elevate.sparkle.application.port.in.ConnectorUseCase.CreateConnectorCommand;
import com.elevate.sparkle.application.port.in.ConnectorUseCase.UpdateConnectorCommand;
import com.elevate.sparkle.domain.model.Connector;
import com.elevate.sparkle.domain.valueobject.ConnectorStatus;
import com.elevate.sparkle.domain.valueobject.ConnectorType;
import org.springframework.stereotype.Component;

/**
 * Mapper between Connector DTOs and domain/command objects
 */
@Component
public class ConnectorDtoMapper {

    public CreateConnectorCommand toCreateCommand(CreateConnectorRequest request, String tenantId) {
        return new CreateConnectorCommand(
                request.getName(),
                ConnectorType.valueOf(request.getType()),
                request.getConfig(),
                tenantId
        );
    }

    public UpdateConnectorCommand toUpdateCommand(String connectorId, UpdateConnectorRequest request) {
        return new UpdateConnectorCommand(
                connectorId,
                request.getName(),
                request.getType() != null ? ConnectorType.valueOf(request.getType()) : null,
                request.getConfig(),
                request.getStatus() != null ? ConnectorStatus.valueOf(request.getStatus()) : null
        );
    }

    public ConnectorResponse toResponse(Connector connector) {
        return ConnectorResponse.builder()
                .id(connector.getId())
                .name(connector.getName())
                .type(connector.getType().name())
                .config(connector.getConfig())
                .status(connector.getStatus().name())
                .createdAt(connector.getCreatedAt())
                .updatedAt(connector.getUpdatedAt())
                .build();
    }
}
