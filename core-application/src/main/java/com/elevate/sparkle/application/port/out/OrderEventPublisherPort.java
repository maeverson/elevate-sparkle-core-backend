package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.Order;

/**
 * Output port for publishing order events
 * To be implemented by messaging adapter
 */
public interface OrderEventPublisherPort {

    /**
     * Publish order created event
     * @param order the created order
     */
    void publishOrderCreated(Order order);

    /**
     * Publish order status changed event
     * @param order the order with updated status
     */
    void publishOrderStatusChanged(Order order);

    /**
     * Publish order cancelled event
     * @param order the cancelled order
     */
    void publishOrderCancelled(Order order);
}
