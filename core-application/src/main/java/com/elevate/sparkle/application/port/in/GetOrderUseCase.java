package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;

/**
 * Input port for retrieving order by ID
 */
public interface GetOrderUseCase {

    /**
     * Get order by ID
     * @param orderId the order ID
     * @return the order
     * @throws com.elevate.sparkle.domain.exception.EntityNotFoundException if not found
     */
    Order getOrder(OrderId orderId);
}
