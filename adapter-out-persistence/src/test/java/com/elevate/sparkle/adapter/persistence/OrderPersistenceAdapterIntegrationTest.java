package com.elevate.sparkle.adapter.persistence;

import com.elevate.sparkle.adapter.out.persistence.adapter.OrderPersistenceAdapter;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.model.OrderItem;
import com.elevate.sparkle.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for OrderPersistenceAdapter using Testcontainers
 * Tests real database interactions with PostgreSQL container
 * 
 * NOTE: These tests require Docker to be running.
 * Run with: mvn test -Pintegration-tests
 */
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackages = "com.elevate.sparkle.adapter.out.persistence")
@ActiveProfiles("test")
class OrderPersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Test
    void shouldSaveAndFindOrder() {
        // Given
        Order order = createTestOrder();

        // When
        Order savedOrder = orderPersistenceAdapter.save(order);
        Optional<Order> foundOrder = orderPersistenceAdapter.findById(savedOrder.getId());

        // Then
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getId()).isEqualTo(savedOrder.getId());
        assertThat(foundOrder.get().getStatus()).isEqualTo(savedOrder.getStatus());
        assertThat(foundOrder.get().getItems()).hasSize(1);
        assertThat(foundOrder.get().getTotalAmount().getAmount())
                .isEqualByComparingTo(savedOrder.getTotalAmount().getAmount());
    }

    @Test
    void shouldUpdateOrderStatus() {
        // Given
        Order order = createTestOrder();
        Order savedOrder = orderPersistenceAdapter.save(order);

        // When
        savedOrder.submit();
        Order updatedOrder = orderPersistenceAdapter.save(savedOrder);

        // Then
        Optional<Order> foundOrder = orderPersistenceAdapter.findById(updatedOrder.getId());
        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getStatus()).isEqualTo(OrderStatus.SUBMITTED);
    }

    @Test
    void shouldFindOrdersByUserId() {
        // Given
        UserId userId = UserId.generate();
        Order order1 = createTestOrderForUser(userId);
        Order order2 = createTestOrderForUser(userId);

        orderPersistenceAdapter.save(order1);
        orderPersistenceAdapter.save(order2);

        // When
        List<Order> orders = orderPersistenceAdapter.findByUserId(userId);

        // Then
        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getUserId().equals(userId));
    }

    @Test
    void shouldReturnEmptyWhenOrderNotFound() {
        // Given
        OrderId nonExistentId = OrderId.generate();

        // When
        Optional<Order> foundOrder = orderPersistenceAdapter.findById(nonExistentId);

        // Then
        assertThat(foundOrder).isEmpty();
    }

    // Helper methods

    private Order createTestOrder() {
        return createTestOrderForUser(UserId.generate());
    }

    private Order createTestOrderForUser(UserId userId) {
        Address address = Address.of("123 Main St", "New York", "NY", "10001", "USA");
        OrderItem item = OrderItem.create(
                ProductId.generate(),
                "Test Product",
                Quantity.of(1),
                Money.of(BigDecimal.valueOf(100.00), "USD")
        );

        return Order.createNew(userId, address, List.of(item));
    }
}
