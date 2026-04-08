package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.*;
import lombok.Builder;
import lombok.Getter;

/**
 * Order item entity within Order aggregate
 */
@Getter
@Builder
public class OrderItem {

    private OrderItemId id;
    private ProductId productId;
    private String productName;
    private Quantity quantity;
    private Money unitPrice;

    public static OrderItem create(ProductId productId, String productName, Quantity quantity, Money unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (quantity == null || quantity.getValue() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        return OrderItem.builder()
                .id(OrderItemId.generate())
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    /**
     * Factory method - reconstruct order item from persistence
     * Used by persistence adapters only
     */
    public static OrderItem reconstruct(
            OrderItemId id,
            ProductId productId,
            String productName,
            Quantity quantity,
            Money unitPrice) {
        
        return OrderItem.builder()
                .id(id)
                .productId(productId)
                .productName(productName)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();
    }

    /**
     * Calculate subtotal for this item
     */
    public Money getSubtotal() {
        return unitPrice.multiply(quantity.getValue());
    }
}
