package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.valueobject.OrderId;
import com.elevate.sparkle.domain.valueobject.OrderStatus;

/**
 * Input port for updating order status
 */
public interface UpdateOrderStatusUseCase {

    /**
     * Update order status
     * @param command the command
     * @return the updated order
     */
    Order updateStatus(UpdateStatusCommand command);

    /**
     * Command for updating order status
     */
    record UpdateStatusCommand(
            OrderId orderId,
            OrderStatus newStatus,
            String reason  // Optional, required for CANCELLED
    ) {
        public UpdateStatusCommand {
            if (orderId == null) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (newStatus == null) {
                throw new IllegalArgumentException("New status is required");
            }
            if (newStatus == OrderStatus.CANCELLED && (reason == null || reason.trim().isEmpty())) {
                throw new IllegalArgumentException("Cancellation reason is required");
            }
        }
    }
}
