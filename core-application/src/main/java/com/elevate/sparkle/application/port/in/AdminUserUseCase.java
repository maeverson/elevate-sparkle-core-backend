package com.elevate.sparkle.application.port.in;

import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserRole;

import java.util.List;
import java.util.Set;

/**
 * Input port for admin user management
 */
public interface AdminUserUseCase {

    List<User> listUsers(String tenantId);

    User getUser(String userId);

    User createUser(CreateUserCommand command);

    User updateUser(UpdateUserCommand command);

    void deleteUser(String userId);

    record CreateUserCommand(
            String username,
            Email email,
            String password,
            String firstName,
            String lastName,
            Set<UserRole> roles,
            String tenantId
    ) {}

    record UpdateUserCommand(
            String userId,
            String firstName,
            String lastName,
            Email email,
            Set<UserRole> roles,
            Boolean active
    ) {}
}
