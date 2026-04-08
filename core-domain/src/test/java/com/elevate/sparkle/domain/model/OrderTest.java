package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Order domain entity
 * Tests domain logic in isolation
 */
class OrderTest {

    @Test
    void shouldCreateNewOrder() {
        // Given
        UserId userId = UserId.generate();
        Address address = createTestAddress();
        List<OrderItem> items = List.of(createTestOrderItem());

        // When
        Order order = Order.createNew(userId, address, items);

        // Then
        assertThat(order).isNotNull();
        assertThat(order.getId()).isNotNull();
        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    void shouldSubmitOrder() {
        // Given
        Order order = createTestOrder();

        // When
        order.submit();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
    }

    @Test
    void shouldConfirmSubmittedOrder() {
        // Given
        Order order = createTestOrder();
        order.submit();

        // When
        order.confirm();

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldThrowExceptionWhenConfirmingNonSubmittedOrder() {
        // Given
        Order order = createTestOrder();

        // When & Then
        assertThatThrownBy(order::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only submitted orders can be confirmed");
    }

    @Test
    void shouldCancelOrder() {
        // Given
        Order order = createTestOrder();
        order.submit();
        String reason = "Customer requested cancellation";

        // When
        order.cancel(reason);

        // Then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getCancellationReason()).isEqualTo(reason);
    }

    @Test
    void shouldThrowExceptionWhenCancellingWithoutReason() {
        // Given
        Order order = createTestOrder();

        // When & Then
        assertThatThrownBy(() -> order.cancel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cancellation reason is required");
    }

    @Test
    void shouldAddItemToPendingOrder() {
        // Given
        Order order = createTestOrder();
        OrderItem newItem = createTestOrderItem();

        // When
        order.addItem(newItem);

        // Then
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void shouldThrowExceptionWhenAddingItemToNonPendingOrder() {
        // Given
        Order order = createTestOrder();
        order.submit();
        OrderItem newItem = createTestOrderItem();

        // When & Then
        assertThatThrownBy(() -> order.addItem(newItem))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only add items to pending orders");
    }

    @Test
    void shouldCalculateTotalAmount() {
        // Given
        UserId userId = UserId.generate();
        Address address = createTestAddress();
        OrderItem item1 = createTestOrderItemWithPrice(50.00);
        OrderItem item2 = createTestOrderItemWithPrice(75.00);
        List<OrderItem> items = List.of(item1, item2);

        // When
        Order order = Order.createNew(userId, address, items);

        // Then
        assertThat(order.getTotalAmount().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(125.00));
    }

    // Helper methods

    private Order createTestOrder() {
        UserId userId = UserId.generate();
        Address address = createTestAddress();
        List<OrderItem> items = List.of(createTestOrderItem());
        return Order.createNew(userId, address, items);
    }

    private Address createTestAddress() {
        return Address.of("123 Main St", "New York", "NY", "10001", "USA");
    }

    private OrderItem createTestOrderItem() {
        return createTestOrderItemWithPrice(100.00);
    }

    private OrderItem createTestOrderItemWithPrice(double price) {
        return OrderItem.create(
                ProductId.generate(),
                "Test Product",
                Quantity.of(1),
                Money.of(price, "USD")
        );
    }
}
