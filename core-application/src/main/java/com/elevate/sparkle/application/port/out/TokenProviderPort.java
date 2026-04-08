package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.User;

/**
 * Output port for JWT token generation
 * To be implemented by security adapter
 */
public interface TokenProviderPort {

    /**
     * Generate JWT token for user
     * @param user the user
     * @return the JWT token
     */
    String generateToken(User user);

    /**
     * Validate a JWT token
     * @param token the token
     * @return true if valid
     */
    boolean validateToken(String token);

    /**
     * Extract username from token
     * @param token the token
     * @return the username
     */
    String getUsernameFromToken(String token);
}
