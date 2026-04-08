package com.elevate.sparkle.adapter.out.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event payload for order status changed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusChangedEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private OrderStatusData order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatusData {
        private String orderId;
        private String userId;
        private String oldStatus;
        private String newStatus;
        private BigDecimal totalAmount;
        private String currency;
        private Instant updatedAt;
    }
}
