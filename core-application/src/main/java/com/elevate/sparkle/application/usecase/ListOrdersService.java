package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.ListOrdersUseCase;
import com.elevate.sparkle.application.port.out.OrderRepositoryPort;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import com.elevate.sparkle.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Use case implementation for listing orders
 */
@Slf4j
@RequiredArgsConstructor
public class ListOrdersService implements ListOrdersUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public List<Order> listOrdersByUser(UserId userId) {
        log.debug("Listing orders for user: {}", userId);
        return orderRepository.findByUserId(userId);
    }

    @Override
    public List<Order> listOrdersByStatus(OrderStatus status) {
        log.debug("Listing orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }

    @Override
    public List<Order> listAllOrders(int page, int size) {
        log.debug("Listing all orders - page: {}, size: {}", page, size);
        
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        
        return orderRepository.findAll(page, size);
    }
}
