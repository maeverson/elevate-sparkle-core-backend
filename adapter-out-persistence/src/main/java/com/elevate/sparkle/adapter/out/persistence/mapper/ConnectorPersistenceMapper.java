package com.elevate.sparkle.adapter.out.persistence.mapper;

import com.elevate.sparkle.adapter.out.persistence.entity.ConnectorJpaEntity;
import com.elevate.sparkle.domain.model.Connector;
import com.elevate.sparkle.domain.valueobject.ConnectorStatus;
import com.elevate.sparkle.domain.valueobject.ConnectorType;
import org.springframework.stereotype.Component;

/**
 * Mapper between Connector domain entity and JPA entity
 */
@Component
public class ConnectorPersistenceMapper {

    public ConnectorJpaEntity toJpaEntity(Connector connector) {
        return ConnectorJpaEntity.builder()
                .id(connector.getId())
                .name(connector.getName())
                .type(connector.getType().name())
                .config(connector.getConfig())
                .status(connector.getStatus().name())
                .tenantId(connector.getTenantId())
                .createdAt(connector.getCreatedAt())
                .updatedAt(connector.getUpdatedAt())
                .build();
    }

    public Connector toDomainEntity(ConnectorJpaEntity jpaEntity) {
        return Connector.builder()
                .id(jpaEntity.getId())
                .name(jpaEntity.getName())
                .type(ConnectorType.valueOf(jpaEntity.getType()))
                .config(jpaEntity.getConfig())
                .status(ConnectorStatus.valueOf(jpaEntity.getStatus()))
                .tenantId(jpaEntity.getTenantId())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();
    }
}
