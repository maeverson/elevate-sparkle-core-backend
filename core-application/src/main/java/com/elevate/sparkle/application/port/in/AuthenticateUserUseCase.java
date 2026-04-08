package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.User;

/**
 * Input port for user authentication
 */
public interface AuthenticateUserUseCase {

    /**
     * Authenticate user
     * @param command the authentication command
     * @return the authenticated user
     */
    AuthenticationResult authenticate(AuthenticationCommand command);

    /**
     * Command for authentication
     */
    record AuthenticationCommand(
            String username,
            String password
    ) {
        public AuthenticationCommand {
            if (username == null || username.trim().isEmpty()) {
                throw new IllegalArgumentException("Username is required");
            }
            if (password == null || password.trim().isEmpty()) {
                throw new IllegalArgumentException("Password is required");
            }
        }
    }

    /**
     * Authentication result
     */
    record AuthenticationResult(
            User user,
            String token
    ) {
        public AuthenticationResult {
            if (user == null) {
                throw new IllegalArgumentException("User is required");
            }
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Token is required");
            }
        }
    }
}
