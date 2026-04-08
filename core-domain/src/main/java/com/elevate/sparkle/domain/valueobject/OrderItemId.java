package com.elevate.sparkle.domain.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * Value Object for Order Item ID
 */
@Getter
@EqualsAndHashCode
public class OrderItemId {
    
    private final String value;

    private OrderItemId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order Item ID cannot be empty");
        }
        this.value = value;
    }

    public static OrderItemId of(String value) {
        return new OrderItemId(value);
    }

    public static OrderItemId generate() {
        return new OrderItemId(UUID.randomUUID().toString());
    }

    @Override
    public String toString() {
        return value;
    }
}
