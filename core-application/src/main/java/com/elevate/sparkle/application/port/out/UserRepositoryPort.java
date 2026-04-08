package com.elevate.sparkle.application.port.out;

import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserId;

import java.util.Optional;

/**
 * Output port for user persistence
 * To be implemented by persistence adapter
 */
public interface UserRepositoryPort {

    /**
     * Save a user
     * @param user the user to save
     * @return the saved user
     */
    User save(User user);

    /**
     * Find user by ID
     * @param userId the user ID
     * @return optional containing the user if found
     */
    Optional<User> findById(UserId userId);

    /**
     * Find user by username
     * @param username the username
     * @return optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email
     * @param email the email
     * @return optional containing the user if found
     */
    Optional<User> findByEmail(Email email);

    /**
     * Check if username exists
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists
     * @param email the email
     * @return true if exists
     */
    boolean existsByEmail(Email email);

    /**
     * Delete a user
     * @param userId the user ID
     */
    void deleteById(UserId userId);
}
