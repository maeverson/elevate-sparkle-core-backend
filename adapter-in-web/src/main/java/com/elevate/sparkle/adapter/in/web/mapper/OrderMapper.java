package com.elevate.sparkle.adapter.in.web.mapper;

import com.elevate.sparkle.adapter.in.web.dto.CreateOrderRequest;
import com.elevate.sparkle.adapter.in.web.dto.OrderResponse;
import com.elevate.sparkle.application.port.in.CreateOrderUseCase.CreateOrderCommand;
import com.elevate.sparkle.application.port.in.CreateOrderUseCase.OrderItemCommand;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.model.OrderItem;
import com.elevate.sparkle.domain.valueobject.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Order DTOs and domain objects
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    default CreateOrderCommand toCommand(CreateOrderRequest request) {
        UserId userId = UserId.of(request.getUserId());
        Address address = toAddress(request.getShippingAddress());
        List<OrderItemCommand> items = request.getItems().stream()
                .map(this::toOrderItemCommand)
                .collect(Collectors.toList());
        
        return new CreateOrderCommand(userId, address, items);
    }

    default Address toAddress(CreateOrderRequest.AddressDto dto) {
        return Address.of(
                dto.getStreet(),
                dto.getCity(),
                dto.getState(),
                dto.getZipCode(),
                dto.getCountry()
        );
    }

    default OrderItemCommand toOrderItemCommand(CreateOrderRequest.OrderItemDto dto) {
        return new OrderItemCommand(
                ProductId.of(dto.getProductId()),
                dto.getProductName(),
                dto.getQuantity(),
                Money.of(dto.getUnitPrice(), dto.getCurrency())
        );
    }

    default OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId().getValue())
                .userId(order.getUserId().getValue())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrency())
                .items(toItemResponses(order.getItems()))
                .shippingAddress(toAddressResponse(order.getShippingAddress()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancellationReason(order.getCancellationReason())
                .build();
    }

    default List<OrderResponse.OrderItemResponse> toItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
    }

    default OrderResponse.OrderItemResponse toItemResponse(OrderItem item) {
        return OrderResponse.OrderItemResponse.builder()
                .id(item.getId().getValue())
                .productId(item.getProductId().getValue())
                .productName(item.getProductName())
                .quantity(item.getQuantity().getValue())
                .unitPrice(item.getUnitPrice().getAmount())
                .currency(item.getUnitPrice().getCurrency())
                .subtotal(item.getSubtotal().getAmount())
                .build();
    }

    default OrderResponse.AddressResponse toAddressResponse(Address address) {
        return OrderResponse.AddressResponse.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }

    default List<OrderResponse> toResponses(List<Order> orders) {
        return orders.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
