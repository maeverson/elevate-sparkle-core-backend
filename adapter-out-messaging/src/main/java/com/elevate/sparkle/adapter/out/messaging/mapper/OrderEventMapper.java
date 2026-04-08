package com.elevate.sparkle.adapter.out.messaging.mapper;

import com.elevate.sparkle.adapter.out.messaging.event.OrderCancelledEvent;
import com.elevate.sparkle.adapter.out.messaging.event.OrderCreatedEvent;
import com.elevate.sparkle.adapter.out.messaging.event.OrderStatusChangedEvent;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.model.OrderItem;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for converting domain entities to event payloads
 */
@Component
public class OrderEventMapper {

    public OrderCreatedEvent toOrderCreatedEvent(Order order) {
        return OrderCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .timestamp(Instant.now())
                .order(toOrderData(order))
                .build();
    }

    public OrderStatusChangedEvent toOrderStatusChangedEvent(Order order, String oldStatus) {
        return OrderStatusChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_STATUS_CHANGED")
                .timestamp(Instant.now())
                .order(OrderStatusChangedEvent.OrderStatusData.builder()
                        .orderId(order.getId().getValue())
                        .userId(order.getUserId().getValue())
                        .oldStatus(oldStatus)
                        .newStatus(order.getStatus().name())
                        .totalAmount(order.getTotalAmount().getAmount())
                        .currency(order.getTotalAmount().getCurrency())
                        .updatedAt(order.getUpdatedAt())
                        .build())
                .build();
    }

    public OrderCancelledEvent toOrderCancelledEvent(Order order) {
        return OrderCancelledEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CANCELLED")
                .timestamp(Instant.now())
                .order(OrderCancelledEvent.OrderCancellationData.builder()
                        .orderId(order.getId().getValue())
                        .userId(order.getUserId().getValue())
                        .status(order.getStatus().name())
                        .totalAmount(order.getTotalAmount().getAmount())
                        .currency(order.getTotalAmount().getCurrency())
                        .cancellationReason(order.getCancellationReason())
                        .cancelledAt(order.getUpdatedAt())
                        .build())
                .build();
    }

    private OrderCreatedEvent.OrderData toOrderData(Order order) {
        return OrderCreatedEvent.OrderData.builder()
                .orderId(order.getId().getValue())
                .userId(order.getUserId().getValue())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrency())
                .items(toOrderItemDataList(order.getItems()))
                .shippingAddress(toAddressData(order))
                .createdAt(order.getCreatedAt())
                .build();
    }

    private List<OrderCreatedEvent.OrderItemData> toOrderItemDataList(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemData)
                .collect(Collectors.toList());
    }

    private OrderCreatedEvent.OrderItemData toOrderItemData(OrderItem item) {
        return OrderCreatedEvent.OrderItemData.builder()
                .productId(item.getProductId().getValue())
                .productName(item.getProductName())
                .quantity(item.getQuantity().getValue())
                .unitPrice(item.getUnitPrice().getAmount())
                .currency(item.getUnitPrice().getCurrency())
                .build();
    }

    private OrderCreatedEvent.AddressData toAddressData(Order order) {
        var address = order.getShippingAddress();
        return OrderCreatedEvent.AddressData.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }
}
