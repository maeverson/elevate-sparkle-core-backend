package com.elevate.sparkle.application.usecase;

import com.elevate.sparkle.application.port.in.AdminUserUseCase;
import com.elevate.sparkle.application.port.out.PasswordEncoderPort;
import com.elevate.sparkle.application.port.out.UserRepositoryPort;
import com.elevate.sparkle.domain.exception.BusinessRuleViolationException;
import com.elevate.sparkle.domain.exception.EntityNotFoundException;
import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Use case implementation for admin user management
 */
@Slf4j
@RequiredArgsConstructor
public class AdminUserService implements AdminUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    @Override
    public List<User> listUsers(String tenantId) {
        log.info("Listing users for tenant: {}", tenantId);
        if (tenantId != null && !tenantId.isEmpty()) {
            return userRepository.findAllByTenantId(tenantId);
        }
        return userRepository.findAll();
    }

    @Override
    public User getUser(String userId) {
        log.info("Getting user: {}", userId);
        return userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
    }

    @Override
    public User createUser(CreateUserCommand command) {
        log.info("Admin creating user: {}", command.username());

        if (userRepository.existsByUsername(command.username())) {
            throw new BusinessRuleViolationException("Username already exists: " + command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new BusinessRuleViolationException("Email already exists: " + command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());

        User user = User.createNew(
                command.username(),
                command.email(),
                passwordHash,
                command.firstName(),
                command.lastName(),
                command.roles(),
                command.tenantId()
        );

        User savedUser = userRepository.save(user);
        log.info("User created by admin: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User updateUser(UpdateUserCommand command) {
        log.info("Updating user: {}", command.userId());

        User user = userRepository.findById(UserId.of(command.userId()))
                .orElseThrow(() -> new EntityNotFoundException("User", command.userId()));

        user.updateProfile(command.firstName(), command.lastName(), command.email(), command.roles());

        if (command.active() != null) {
            if (command.active()) {
                user.activate();
            } else {
                user.deactivate();
            }
        }

        User savedUser = userRepository.save(user);
        log.info("User updated: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public void deleteUser(String userId) {
        log.info("Deleting user: {}", userId);
        userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new EntityNotFoundException("User", userId));
        userRepository.deleteById(UserId.of(userId));
        log.info("User deleted: {}", userId);
    }
}
