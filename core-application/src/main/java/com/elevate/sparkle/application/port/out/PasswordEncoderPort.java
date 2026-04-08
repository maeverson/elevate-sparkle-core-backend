package com.elevate.sparkle.application.port.out;

/**
 * Output port for password encoding
 * To be implemented by security adapter
 */
public interface PasswordEncoderPort {

    /**
     * Encode a raw password
     * @param rawPassword the raw password
     * @return the encoded password
     */
    String encode(String rawPassword);

    /**
     * Check if raw password matches encoded password
     * @param rawPassword the raw password
     * @param encodedPassword the encoded password
     * @return true if matches
     */
    boolean matches(String rawPassword, String encodedPassword);
}
