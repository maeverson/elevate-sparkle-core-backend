package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.UpdateOrderStatusUseCase;
import com.elevate.sparkle.application.port.out.OrderEventPublisherPort;
import com.elevate.sparkle.application.port.out.OrderRepositoryPort;
import com.elevate.sparkle.domain.exception.EntityNotFoundException;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case implementation for updating order status
 */
@Slf4j
@RequiredArgsConstructor
public class UpdateOrderStatusService implements UpdateOrderStatusUseCase {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventPublisherPort eventPublisher;

    @Override
    public Order updateStatus(UpdateStatusCommand command) {
        log.info("Updating order {} status to {}", command.orderId(), command.newStatus());

        // Fetch order
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Order", command.orderId().getValue()));

        // Apply status change based on target status
        // Domain logic ensures valid state transitions
        switch (command.newStatus()) {
            case CONFIRMED -> order.confirm();
            case SHIPPED -> order.ship();
            case DELIVERED -> order.deliver();
            case CANCELLED -> order.cancel(command.reason());
            default -> throw new IllegalArgumentException("Cannot manually set status to " + command.newStatus());
        }

        // Save updated order
        Order updatedOrder = orderRepository.save(order);

        // Publish event
        try {
            if (command.newStatus() == OrderStatus.CANCELLED) {
                eventPublisher.publishOrderCancelled(updatedOrder);
            } else {
                eventPublisher.publishOrderStatusChanged(updatedOrder);
            }
        } catch (Exception e) {
            log.error("Failed to publish order status changed event", e);
        }

        log.info("Order status updated successfully: {}", updatedOrder.getId());
        return updatedOrder;
    }
}
