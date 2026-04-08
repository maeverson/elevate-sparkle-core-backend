package com.elevate.sparkle.adapter.out.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event payload for order cancelled
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private OrderCancellationData order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderCancellationData {
        private String orderId;
        private String userId;
        private String status;
        private BigDecimal totalAmount;
        private String currency;
        private String cancellationReason;
        private Instant cancelledAt;
    }
}
