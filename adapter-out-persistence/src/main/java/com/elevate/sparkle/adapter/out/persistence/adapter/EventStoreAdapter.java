package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.entity.EventStoreEntity;
import com.elevate.sparkle.adapter.out.persistence.repository.EventStoreJpaRepository;
import com.elevate.sparkle.adapter.out.persistence.serializer.EventSerializer;
import com.elevate.sparkle.domain.event.DomainEvent;
import com.elevate.sparkle.domain.exception.ConcurrencyException;
import com.elevate.sparkle.domain.port.out.ExecutionEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter implementing the ExecutionEventRepository port using JPA.
 * This is the bridge between the domain (hexagonal core) and persistence infrastructure.
 */
@Component
public class EventStoreAdapter implements ExecutionEventRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(EventStoreAdapter.class);
    
    private final EventStoreJpaRepository jpaRepository;
    private final EventSerializer eventSerializer;
    
    public EventStoreAdapter(EventStoreJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
        this.eventSerializer = new EventSerializer();
    }
    
    @Override
    @Transactional
    public void saveEvents(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        // Validate all events are for the same aggregate
        UUID aggregateId = events.get(0).aggregateId();
        boolean allSameAggregate = events.stream()
                .allMatch(e -> e.aggregateId().equals(aggregateId));
        
        if (!allSameAggregate) {
            throw new IllegalArgumentException("All events must belong to the same aggregate");
        }
        
        // Check for concurrency conflicts (optimistic locking)
        if (!events.isEmpty()) {
            Long currentVersion = getCurrentVersion(aggregateId);
            Long firstEventSequence = events.get(0).sequenceNumber();
            
            if (currentVersion >= 0 && firstEventSequence <= currentVersion) {
                throw new ConcurrencyException(aggregateId, firstEventSequence - 1, currentVersion);
            }
        }
        
        // Convert and save all events
        List<EventStoreEntity> entities = events.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        
        try {
            jpaRepository.saveAll(entities);
            logger.info("Saved {} events for aggregate {}", events.size(), aggregateId);
        } catch (DataIntegrityViolationException e) {
            // This catches unique constraint violations (duplicate sequence numbers)
            logger.error("Concurrency violation when saving events for aggregate {}", aggregateId, e);
            throw new ConcurrencyException(aggregateId, null, null);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByAggregateId(UUID aggregateId) {
        List<EventStoreEntity> entities = jpaRepository.findByAggregateIdOrderBySequenceNumberAsc(aggregateId);
        
        List<DomainEvent> events = entities.stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
        
        logger.debug("Loaded {} events for aggregate {}", events.size(), aggregateId);
        return events;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DomainEvent> findByAggregateIdFromSequence(UUID aggregateId, Long fromSequence) {
        List<EventStoreEntity> entities = jpaRepository.findByAggregateIdFromSequence(aggregateId, fromSequence);
        
        return entities.stream()
                .map(this::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean exists(UUID aggregateId) {
        return jpaRepository.existsByAggregateId(aggregateId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Long getCurrentVersion(UUID aggregateId) {
        Long maxSequence = jpaRepository.findMaxSequenceNumberByAggregateId(aggregateId);
        return maxSequence != null ? maxSequence : -1L;
    }
    
    // ========= Private Mapping Methods =========
    
    private EventStoreEntity toEntity(DomainEvent event) {
        EventStoreEntity entity = new EventStoreEntity();
        entity.setAggregateId(event.aggregateId());
        entity.setAggregateType(event.aggregateType());
        entity.setEventType(event.eventType());
        entity.setEventVersion(event.eventVersion());
        entity.setSequenceNumber(event.sequenceNumber());
        entity.setPayload(eventSerializer.toPayload(event));
        entity.setMetadata(createMetadata(event));
        entity.setCreatedAt(event.occurredAt());
        
        return entity;
    }
    
    private DomainEvent fromEntity(EventStoreEntity entity) {
        return eventSerializer.fromPayload(
                entity.getEventType(),
                entity.getEventVersion(),
                entity.getPayload(),
                entity.getAggregateId(),
                entity.getSequenceNumber(),
                entity.getCreatedAt()
        );
    }
    
    private Map<String, Object> createMetadata(DomainEvent event) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("eventType", event.eventType());
        metadata.put("eventVersion", event.eventVersion());
        metadata.put("aggregateType", event.aggregateType());
        // Add correlation ID, causation ID, user context here if available
        return metadata;
    }
}
