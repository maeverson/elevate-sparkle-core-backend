package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.RegisterUserUseCase;
import com.elevate.sparkle.application.port.out.PasswordEncoderPort;
import com.elevate.sparkle.application.port.out.UserRepositoryPort;
import com.elevate.sparkle.domain.exception.BusinessRuleViolationException;
import com.elevate.sparkle.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case implementation for user registration
 */
@Slf4j
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public User registerUser(RegisterUserCommand command) {
        log.info("Registering new user: {}", command.username());

        // Check if username already exists
        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessRuleViolationException("Username already exists: " + command.username());
        }

        // Check if email already exists
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email already exists: " + command.email());
        }

        // Encode password
        String passwordHash = passwordEncoder.encode(command.password());

        // Create user (domain logic)
        User user = User.createNew(
                command.username(),
                command.email(),
                passwordHash,
                command.firstName(),
                command.lastName(),
                command.roles()
        );

        // Persist
        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {}", savedUser.getId());
        return savedUser;
    }
}
