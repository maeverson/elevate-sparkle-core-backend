package com.elevate.sparkle.adapter.out.persistence.repository;

import com.elevate.sparkle.adapter.out.persistence.entity.JourneyVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Journey Versions
 */
@Repository
public interface JourneyVersionJpaRepository extends JpaRepository<JourneyVersionEntity, UUID> {
    
    List<JourneyVersionEntity> findByJourneyDefinitionIdOrderByCreatedAtDesc(UUID journeyDefinitionId);
    
    Optional<JourneyVersionEntity> findByJourneyDefinitionIdAndVersionNumber(
            UUID journeyDefinitionId,
            String versionNumber
    );
    
    boolean existsByJourneyDefinitionIdAndVersionNumber(
            UUID journeyDefinitionId,
            String versionNumber
    );
    
    List<JourneyVersionEntity> findByStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT v FROM JourneyVersionEntity v WHERE v.journeyDefinitionId = :journeyId AND v.status = 'PUBLISHED'")
    Optional<JourneyVersionEntity> findPublishedVersion(@Param("journeyId") UUID journeyDefinitionId);
    
    @Query("SELECT v FROM JourneyVersionEntity v WHERE v.journeyDefinitionId = :journeyId ORDER BY v.createdAt DESC LIMIT 1")
    Optional<JourneyVersionEntity> findLatestVersion(@Param("journeyId") UUID journeyDefinitionId);
    
    long countByJourneyDefinitionId(UUID journeyDefinitionId);
}
