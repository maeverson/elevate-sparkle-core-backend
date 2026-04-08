package com.elevate.sparkle.domain.valueobject;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Value Object for Address
 */
@Getter
@Builder
@EqualsAndHashCode
public class Address {
    
    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;
    private final String country;

    public static Address of(String street, String city, String state, String zipCode, String country) {
        validateInputs(street, city, state, zipCode, country);
        
        return Address.builder()
                .street(street)
                .city(city)
                .state(state)
                .zipCode(zipCode)
                .country(country)
                .build();
    }

    private static void validateInputs(String street, String city, String state, String zipCode, String country) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State is required");
        }
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Zip code is required");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s %s, %s", street, city, state, zipCode, country);
    }
}
