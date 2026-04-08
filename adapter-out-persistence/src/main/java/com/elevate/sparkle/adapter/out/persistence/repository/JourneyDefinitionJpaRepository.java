package com.elevate.sparkle.adapter.out.persistence.repository;

import com.elevate.sparkle.adapter.out.persistence.entity.JourneyDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Journey Definitions
 */
@Repository
public interface JourneyDefinitionJpaRepository extends JpaRepository<JourneyDefinitionEntity, UUID> {
    
    Optional<JourneyDefinitionEntity> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT j FROM JourneyDefinitionEntity j WHERE j.archived = false ORDER BY j.createdAt DESC")
    List<JourneyDefinitionEntity> findAllActive();
}
