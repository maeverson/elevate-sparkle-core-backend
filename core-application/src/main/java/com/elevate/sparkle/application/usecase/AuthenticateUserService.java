package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.AuthenticateUserUseCase;
import com.elevate.sparkle.application.port.out.PasswordEncoderPort;
import com.elevate.sparkle.application.port.out.TokenProviderPort;
import com.elevate.sparkle.application.port.out.UserRepositoryPort;
import com.elevate.sparkle.domain.exception.BusinessRuleViolationException;
import com.elevate.sparkle.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Use case implementation for user authentication
 */
@Slf4j
@RequiredArgsConstructor
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProviderPort tokenProvider;

    @Override
    public AuthenticationResult authenticate(AuthenticationCommand command) {
        log.info("Authenticating user: {}", command.username());

        // Find user
        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new BusinessRuleViolationException("Invalid username or password"));

        // Check if user is active
        if (!user.isActive()) {
            throw new BusinessRuleViolationException("User account is inactive");
        }

        // Verify password
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BusinessRuleViolationException("Invalid username or password");
        }

        // Generate token
        String token = tokenProvider.generateToken(user);

        log.info("User authenticated successfully: {}", user.getId());
        return new AuthenticationResult(user, token);
    }
}
