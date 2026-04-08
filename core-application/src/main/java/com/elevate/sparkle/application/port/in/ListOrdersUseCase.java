package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderStatus;
import com.elevate.sparkle.domain.valueobject.UserId;

import java.util.List;

/**
 * Input port for listing orders
 */
public interface ListOrdersUseCase {

    /**
     * List all orders for a user
     * @param userId the user ID
     * @return list of orders
     */
    List<Order> listOrdersByUser(UserId userId);

    /**
     * List orders by status
     * @param status the order status
     * @return list of orders
     */
    List<Order> listOrdersByStatus(OrderStatus status);

    /**
     * List all orders with pagination
     * @param page page number (0-based)
     * @param size page size
     * @return list of orders
     */
    List<Order> listAllOrders(int page, int size);
}
