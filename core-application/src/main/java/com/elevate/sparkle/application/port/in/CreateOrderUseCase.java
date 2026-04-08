package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.*;

import java.util.List;

/**
 * Input port (use case interface) for creating orders
 * This is called by adapters (e.g., REST controllers)
 */
public interface CreateOrderUseCase {

    /**
     * Create a new order
     * @param command the order creation command
     * @return the created order
     */
    Order createOrder(CreateOrderCommand command);

    /**
     * Command for creating an order
     */
    record CreateOrderCommand(
            UserId userId,
            Address shippingAddress,
            List<OrderItemCommand> items
    ) {
        public CreateOrderCommand {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (shippingAddress == null) {
                throw new IllegalArgumentException("Shipping address is required");
            }
            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("At least one item is required");
            }
        }
    }

    /**
     * Command for order item
     */
    record OrderItemCommand(
            ProductId productId,
            String productName,
            int quantity,
            Money unitPrice
    ) {
        public OrderItemCommand {
            if (productId == null) {
                throw new IllegalArgumentException("Product ID is required");
            }
            if (productName == null || productName.trim().isEmpty()) {
                throw new IllegalArgumentException("Product name is required");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (unitPrice == null) {
                throw new IllegalArgumentException("Unit price is required");
            }
        }
    }
}
