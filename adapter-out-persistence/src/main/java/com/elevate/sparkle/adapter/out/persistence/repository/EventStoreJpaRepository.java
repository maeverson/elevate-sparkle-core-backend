package com.elevate.sparkle.adapter.out.persistence.repository;

import com.elevate.sparkle.adapter.out.persistence.entity.EventStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for EventStoreEntity.
 */
public interface EventStoreJpaRepository extends JpaRepository<EventStoreEntity, UUID> {
    
    /**
     * Find all events for an aggregate, ordered by sequence number
     */
    List<EventStoreEntity> findByAggregateIdOrderBySequenceNumberAsc(UUID aggregateId);
    
    /**
     * Find events starting from a specific sequence number
     */
    @Query("SELECT e FROM EventStoreEntity e WHERE e.aggregateId = :aggregateId " +
           "AND e.sequenceNumber >= :fromSequence ORDER BY e.sequenceNumber ASC")
    List<EventStoreEntity> findByAggregateIdFromSequence(
            @Param("aggregateId") UUID aggregateId,
            @Param("fromSequence") Long fromSequence
    );
    
    /**
     * Check if events exist for an aggregate
     */
    boolean existsByAggregateId(UUID aggregateId);
    
    /**
     * Get the maximum sequence number for an aggregate
     */
    @Query("SELECT MAX(e.sequenceNumber) FROM EventStoreEntity e WHERE e.aggregateId = :aggregateId")
    Long findMaxSequenceNumberByAggregateId(@Param("aggregateId") UUID aggregateId);
}
