package com.elevate.sparkle.adapter.out.persistence.repository;

import com.elevate.sparkle.adapter.out.persistence.entity.ConnectorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Connector
 */
@Repository
public interface ConnectorJpaRepository extends JpaRepository<ConnectorJpaEntity, String> {

    List<ConnectorJpaEntity> findAllByTenantId(String tenantId);
}
