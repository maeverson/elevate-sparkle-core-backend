package com.elevate.sparkle.domain.model;

import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserId;
import com.elevate.sparkle.domain.valueobject.UserRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

/**
 * User aggregate root
 */
@Getter
@Builder
public class User {

    private UserId id;
    private String username;
    private Email email;
    
    @Setter
    private String passwordHash;
    
    private String firstName;
    private String lastName;
    private Set<UserRole> roles;
    private boolean active;
    private String tenantId;
    private Instant createdAt;
    private Instant updatedAt;

    public static User createNew(String username, Email email, String passwordHash, 
                                  String firstName, String lastName, Set<UserRole> roles,
                                  String tenantId) {
        validateInputs(username, email, passwordHash, firstName, lastName, roles);

        return User.builder()
                .id(UserId.generate())
                .username(username)
                .email(email)
                .passwordHash(passwordHash)
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .active(true)
                .tenantId(tenantId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static User createNew(String username, Email email, String passwordHash,
                                  String firstName, String lastName, Set<UserRole> roles) {
        return createNew(username, email, passwordHash, firstName, lastName, roles, null);
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.active = true;
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String firstName, String lastName, Email email, Set<UserRole> roles) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            this.firstName = firstName;
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            this.lastName = lastName;
        }
        if (email != null) {
            this.email = email;
        }
        if (roles != null && !roles.isEmpty()) {
            this.roles = roles;
        }
        this.updatedAt = Instant.now();
    }

    public boolean hasRole(UserRole role) {
        return this.roles != null && this.roles.contains(role);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    private static void validateInputs(String username, Email email, String passwordHash,
                                       String firstName, String lastName, Set<UserRole> roles) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email is required");
        }
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Password hash is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("At least one role is required");
        }
    }
}
