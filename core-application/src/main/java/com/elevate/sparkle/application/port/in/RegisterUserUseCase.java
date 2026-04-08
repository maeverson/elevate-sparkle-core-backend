package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserRole;

import java.util.Set;

/**
 * Input port for user registration
 */
public interface RegisterUserUseCase {

    /**
     * Register a new user
     * @param command the registration command
     * @return the created user
     */
    User registerUser(RegisterUserCommand command);

    /**
     * Command for user registration
     */
    record RegisterUserCommand(
            String username,
            Email email,
            String password,
            String firstName,
            String lastName,
            Set<UserRole> roles
    ) {
        public RegisterUserCommand {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (email == null) {
                throw new IllegalArgumentException("Email is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
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
}
