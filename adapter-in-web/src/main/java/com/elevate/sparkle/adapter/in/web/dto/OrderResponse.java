package com.elevate.sparkle.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for order response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String id;
    private String userId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private List<OrderItemResponse> items;
    private AddressResponse shippingAddress;
    private Instant createdAt;
    private Instant updatedAt;
    private String cancellationReason;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String id;
        private String productId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
        private String currency;
        private BigDecimal subtotal;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
}
