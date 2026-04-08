package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.CreateOrderUseCase;
import com.elevate.sparkle.application.port.out.OrderEventPublisherPort;
import com.elevate.sparkle.application.port.out.OrderRepositoryPort;
import com.elevate.sparkle.domain.exception.BusinessRuleViolationException;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.model.OrderItem;
import com.elevate.sparkle.domain.valueobject.Quantity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case implementation for creating orders
 * Contains application logic (orchestration)
 * Does NOT contain business logic (that's in domain)
 */
@Slf4j
@RequiredArgsConstructor
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final OrderEventPublisherPort eventPublisher;

    @Override
    public Order createOrder(CreateOrderCommand command) {
        log.info("Creating order for user: {}", command.userId());

        // Validate business rules if needed (cross-aggregate validation)
        validateCommand(command);

        // Convert command to domain objects
        List<OrderItem> orderItems = command.items().stream()
                .map(itemCmd -> OrderItem.create(
                        itemCmd.productId(),
                        itemCmd.productName(),
                        Quantity.of(itemCmd.quantity()),
                        itemCmd.unitPrice()
                ))
                .collect(Collectors.toList());

        // Create domain object (domain logic is in Order.createNew)
        Order order = Order.createNew(
                command.userId(),
                command.shippingAddress(),
                orderItems
        );

        // Submit the order (domain operation)
        order.submit();

        // Persist using output port
        Order savedOrder = orderRepository.save(order);

        // Publish event using output port
        try {
            eventPublisher.publishOrderCreated(savedOrder);
        } catch (Exception e) {
            log.error("Failed to publish order created event", e);
            // In production, you might want to use outbox pattern
            // For now, we log and continue (order is already saved)
        }

        log.info("Order created successfully: {}", savedOrder.getId());
        return savedOrder;
    }

    private void validateCommand(CreateOrderCommand command) {
        // Example: validate that total amount doesn't exceed some limit
        // This is application-level validation, not domain validation
        
        if (command.items().size() > 100) {
            throw new BusinessRuleViolationException("Order cannot have more than 100 items");
        }
    }
}
