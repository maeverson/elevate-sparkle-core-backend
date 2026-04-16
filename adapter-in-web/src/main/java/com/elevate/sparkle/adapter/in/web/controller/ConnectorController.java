package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.*;
import com.elevate.sparkle.adapter.in.web.mapper.ConnectorDtoMapper;
import com.elevate.sparkle.application.port.in.ConnectorUseCase;
import com.elevate.sparkle.domain.model.Connector;
import com.elevate.sparkle.application.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Connector Management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/connectors")
@RequiredArgsConstructor
@Tag(name = "Connectors", description = "Connector management endpoints")
public class ConnectorController {

    private final ConnectorUseCase connectorUseCase;
    private final ConnectorDtoMapper connectorMapper;

    @GetMapping
    @Operation(summary = "List all connectors")
    public ApiResponse<List<ConnectorResponse>> listConnectors() {
        String tenantId = TenantContext.getCurrentTenantId();
        log.info("REST: Listing connectors for tenant: {}", tenantId);

        List<Connector> connectors = connectorUseCase.listConnectors(tenantId);
        List<ConnectorResponse> responses = connectors.stream()
                .map(connectorMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get connector by ID")
    public ApiResponse<ConnectorResponse> getConnector(@PathVariable String id) {
        log.info("REST: Getting connector: {}", id);
        Connector connector = connectorUseCase.getConnector(id);
        return ApiResponse.success(connectorMapper.toResponse(connector));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new connector")
    public ApiResponse<ConnectorResponse> createConnector(@Valid @RequestBody CreateConnectorRequest request) {
        String tenantId = TenantContext.getCurrentTenantId();
        log.info("REST: Creating connector: {}", request.getName());

        Connector connector = connectorUseCase.createConnector(
                connectorMapper.toCreateCommand(request, tenantId)
        );

        return ApiResponse.success(connectorMapper.toResponse(connector), HttpStatus.CREATED.value());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update connector")
    public ApiResponse<ConnectorResponse> updateConnector(@PathVariable String id,
                                                           @Valid @RequestBody UpdateConnectorRequest request) {
        log.info("REST: Updating connector: {}", id);

        Connector connector = connectorUseCase.updateConnector(
                connectorMapper.toUpdateCommand(id, request)
        );

        return ApiResponse.success(connectorMapper.toResponse(connector));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete connector")
    public void deleteConnector(@PathVariable String id) {
        log.info("REST: Deleting connector: {}", id);
        connectorUseCase.deleteConnector(id);
    }
}
