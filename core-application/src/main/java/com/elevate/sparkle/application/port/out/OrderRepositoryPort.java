package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import com.elevate.sparkle.domain.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * Output port for order persistence
 * To be implemented by persistence adapter
 */
public interface OrderRepositoryPort {

    /**
     * Save an order
     * @param order the order to save
     * @return the saved order
     */
    Order save(Order order);

    /**
     * Find order by ID
     * @param orderId the order ID
     * @return optional containing the order if found
     */
    Optional<Order> findById(OrderId orderId);

    /**
     * Find orders by user ID
     * @param userId the user ID
     * @return list of orders
     */
    List<Order> findByUserId(UserId userId);

    /**
     * Find orders by status
     * @param status the order status
     * @return list of orders
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find all orders with pagination
     * @param page page number (0-based)
     * @param size page size
     * @return list of orders
     */
    List<Order> findAll(int page, int size);

    /**
     * Delete an order
     * @param orderId the order ID
     */
    void deleteById(OrderId orderId);

    /**
     * Check if order exists
     * @param orderId the order ID
     * @return true if exists
     */
    boolean existsById(OrderId orderId);
}
