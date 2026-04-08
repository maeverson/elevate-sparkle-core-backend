package com.elevate.sparkle.adapter.out.persistence.adapter;

import com.elevate.sparkle.adapter.out.persistence.mapper.OrderPersistenceMapper;
import com.elevate.sparkle.adapter.out.persistence.repository.OrderJpaRepository;
import com.elevate.sparkle.application.port.out.OrderRepositoryPort;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import com.elevate.sparkle.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter implementing OrderRepositoryPort
 * Implements the output port using Spring Data JPA
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderPersistenceMapper mapper;

    @Override
    @Transactional
    public Order save(Order order) {
        log.debug("Saving order: {}", order.getId());
        var jpaEntity = mapper.toJpaEntity(order);
        var saved = orderJpaRepository.save(jpaEntity);
        return mapper.toDomainEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId orderId) {
        log.debug("Finding order by ID: {}", orderId);
        return orderJpaRepository.findById(orderId.getValue())
                .map(mapper::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUserId(UserId userId) {
        log.debug("Finding orders by user ID: {}", userId);
        return orderJpaRepository.findByUserId(userId.getValue()).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        log.debug("Finding orders by status: {}", status);
        return orderJpaRepository.findByStatus(status.name()).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll(int page, int size) {
        log.debug("Finding all orders - page: {}, size: {}", page, size);
        PageRequest pageRequest = PageRequest.of(page, size);
        return orderJpaRepository.findAllBy(pageRequest).stream()
                .map(mapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(OrderId orderId) {
        log.debug("Deleting order: {}", orderId);
        orderJpaRepository.deleteById(orderId.getValue());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(OrderId orderId) {
        return orderJpaRepository.existsById(orderId.getValue());
    }
}
