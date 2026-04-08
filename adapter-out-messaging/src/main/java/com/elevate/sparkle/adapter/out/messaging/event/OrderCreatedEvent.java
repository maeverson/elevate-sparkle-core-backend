package com.elevate.sparkle.adapter.out.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Event payload for order created
 * Separate from domain model - used for messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private String eventId;
    private String eventType;
    private Instant timestamp;
    private OrderData order;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderData {
        private String orderId;
        private String userId;
        private String status;
        private BigDecimal totalAmount;
        private String currency;
        private List<OrderItemData> items;
        private AddressData shippingAddress;
        private Instant createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressData {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
