package com.elevate.sparkle.adapter.out.persistence.mapper;

import com.elevate.sparkle.adapter.out.persistence.entity.AddressEmbeddable;
import com.elevate.sparkle.adapter.out.persistence.entity.OrderItemJpaEntity;
import com.elevate.sparkle.adapter.out.persistence.entity.OrderJpaEntity;
import com.elevate.sparkle.domain.model.Order;
import com.elevate.sparkle.domain.model.OrderItem;
import com.elevate.sparkle.domain.valueobject.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper between Order domain entity and JPA entity
 * Critical for hexagonal architecture - maintains separation
 */
@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity jpaEntity = OrderJpaEntity.builder()
                .id(order.getId().getValue())
                .userId(order.getUserId().getValue())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount().getAmount())
                .currency(order.getTotalAmount().getCurrency())
                .shippingAddress(toAddressEmbeddable(order.getShippingAddress()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .cancellationReason(order.getCancellationReason())
                .build();

        // Map items
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
                .map(item -> toItemJpaEntity(item, jpaEntity))
                .collect(Collectors.toList());

        jpaEntity.setItems(itemEntities);

        return jpaEntity;
    }

    public Order toDomainEntity(OrderJpaEntity jpaEntity) {
        List<OrderItem> items = jpaEntity.getItems().stream()
                .map(this::toItemDomain)
                .collect(Collectors.toList());

        return Order.reconstruct(
                OrderId.of(jpaEntity.getId()),
                UserId.of(jpaEntity.getUserId()),
                OrderStatus.valueOf(jpaEntity.getStatus()),
                Money.of(jpaEntity.getTotalAmount(), jpaEntity.getCurrency()),
                items,
                toAddressDomain(jpaEntity.getShippingAddress()),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt(),
                jpaEntity.getCancellationReason()
        );
    }

    private OrderItemJpaEntity toItemJpaEntity(OrderItem item, OrderJpaEntity orderEntity) {
        return OrderItemJpaEntity.builder()
                .id(item.getId().getValue())
                .order(orderEntity)
                .productId(item.getProductId().getValue())
                .productName(item.getProductName())
                .quantity(item.getQuantity().getValue())
                .unitPrice(item.getUnitPrice().getAmount())
                .currency(item.getUnitPrice().getCurrency())
                .build();
    }

    private OrderItem toItemDomain(OrderItemJpaEntity jpaEntity) {
        return OrderItem.reconstruct(
                OrderItemId.of(jpaEntity.getId()),
                ProductId.of(jpaEntity.getProductId()),
                jpaEntity.getProductName(),
                Quantity.of(jpaEntity.getQuantity()),
                Money.of(jpaEntity.getUnitPrice(), jpaEntity.getCurrency())
        );
    }

    private AddressEmbeddable toAddressEmbeddable(Address address) {
        return AddressEmbeddable.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }

    private Address toAddressDomain(AddressEmbeddable embeddable) {
        return Address.of(
                embeddable.getStreet(),
                embeddable.getCity(),
                embeddable.getState(),
                embeddable.getZipCode(),
                embeddable.getCountry()
        );
    }
}
