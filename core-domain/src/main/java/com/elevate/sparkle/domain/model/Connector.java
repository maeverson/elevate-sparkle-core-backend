package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.ConnectorStatus;
import com.elevate.sparkle.domain.valueobject.ConnectorType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Connector aggregate root
 */
@Getter
@Builder
public class Connector {

    private String id;
    private String name;
    private ConnectorType type;
    private String config;
    private ConnectorStatus status;
    private String tenantId;
    private Instant createdAt;

    @Setter
    private Instant updatedAt;

    public static Connector createNew(String name, ConnectorType type, String config, String tenantId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Connector name is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Connector type is required");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID is required");
        }

        return Connector.builder()
                .id(java.util.UUID.randomUUID().toString())
                .name(name)
                .type(type)
                .config(config)
                .status(ConnectorStatus.ACTIVE)
                .tenantId(tenantId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public void update(String name, ConnectorType type, String config, ConnectorStatus status) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (type != null) {
            this.type = type;
        }
        if (config != null) {
            this.config = config;
        }
        if (status != null) {
            this.status = status;
        }
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.status = ConnectorStatus.INACTIVE;
        this.updatedAt = Instant.now();
    }
}
