package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Order aggregate root - domain entity
 * Contains business logic and invariants
 * NO framework dependencies - pure domain
 */
@Getter
@Builder(access = AccessLevel.PRIVATE)
public class Order {

    private OrderId id;
    private UserId userId;
    private OrderStatus status;
    
    @Setter(AccessLevel.PRIVATE)
    private Money totalAmount;
    
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    private Address shippingAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private String cancellationReason;

    /**
     * Factory method - creates new order
     */
    public static Order createNew(UserId userId, Address shippingAddress, List<OrderItem> items) {
        validateItems(items);
        
        Order order = Order.builder()
                .id(OrderId.generate())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .shippingAddress(shippingAddress)
                .items(new ArrayList<>(items))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        order.calculateTotalAmount();
        return order;
    }

    /**
     * Factory method - reconstruct order from persistence
     * Used by persistence adapters only
     */
    public static Order reconstruct(
            OrderId id,
            UserId userId,
            OrderStatus status,
            Money totalAmount,
            List<OrderItem> items,
            Address shippingAddress,
            Instant createdAt,
            Instant updatedAt,
            String cancellationReason) {
        
        return Order.builder()
                .id(id)
                .userId(userId)
                .status(status)
                .totalAmount(totalAmount)
                .items(new ArrayList<>(items))
                .shippingAddress(shippingAddress)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .cancellationReason(cancellationReason)
                .build();
    }

    /**
     * Domain operation - submit order for processing
     */
    public void submit() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be submitted");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot submit order without items");
        }
        this.status = OrderStatus.SUBMITTED;
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - confirm order
     */
    public void confirm() {
        if (this.status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted orders can be confirmed");
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - ship order
     */
    public void ship() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }
        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - deliver order
     */
    public void deliver() {
        if (this.status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Only shipped orders can be delivered");
        }
        this.status = OrderStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - cancel order
     */
    public void cancel(String reason) {
        if (this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel delivered or already cancelled orders");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Cancellation reason is required");
        }
        this.status = OrderStatus.CANCELLED;
        this.cancellationReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - add item to order
     */
    public void addItem(OrderItem item) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only add items to pending orders");
        }
        this.items.add(item);
        calculateTotalAmount();
        this.updatedAt = Instant.now();
    }

    /**
     * Domain operation - remove item from order
     */
    public void removeItem(OrderItemId itemId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Can only remove items from pending orders");
        }
        boolean removed = this.items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Item not found in order");
        }
        calculateTotalAmount();
        this.updatedAt = Instant.now();
    }

    /**
     * Get immutable view of items
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Calculate total amount from items
     */
    private void calculateTotalAmount() {
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .map(Money::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = Money.of(total, items.isEmpty() ? "USD" : items.get(0).getUnitPrice().getCurrency());
    }

    private static void validateItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
    }

    /**
     * Domain query - check if order can be cancelled
     */
    public boolean canBeCancelled() {
        return this.status != OrderStatus.DELIVERED && this.status != OrderStatus.CANCELLED;
    }

    /**
     * Domain query - check if order is in final state
     */
    public boolean isFinalState() {
        return this.status == OrderStatus.DELIVERED || this.status == OrderStatus.CANCELLED;
    }
}
