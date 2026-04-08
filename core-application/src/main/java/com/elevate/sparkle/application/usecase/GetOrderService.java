package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.GetOrderUseCase;
import com.elevate.sparkle.application.port.out.OrderRepositoryPort;
import com.elevate.sparkle.domain.exception.EntityNotFoundException;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case implementation for getting order by ID
 */
@Slf4j
@RequiredArgsConstructor
public class GetOrderService implements GetOrderUseCase {

    private final OrderRepositoryPort orderRepository;

    @Override
    public Order getOrder(OrderId orderId) {
        log.debug("Fetching order: {}", orderId);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order", orderId.getValue()));
    }
}
