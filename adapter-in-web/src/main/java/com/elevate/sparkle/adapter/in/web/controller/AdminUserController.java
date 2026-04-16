package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.*;
import com.elevate.sparkle.adapter.in.web.mapper.UserMapper;
import com.elevate.sparkle.application.port.in.AdminUserUseCase;
import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserRole;
import com.elevate.sparkle.application.context.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for Admin User Management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Admin user management endpoints")
public class AdminUserController {

    private final AdminUserUseCase adminUserUseCase;
    private final UserMapper userMapper;

    @GetMapping
    @Operation(summary = "List all users")
    public ApiResponse<List<UserResponse>> listUsers() {
        String tenantId = TenantContext.getCurrentTenantId();
        log.info("REST: Listing users for tenant: {}", tenantId);

        List<User> users = adminUserUseCase.listUsers(tenantId);
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ApiResponse<UserResponse> getUser(@PathVariable String id) {
        log.info("REST: Getting user: {}", id);
        User user = adminUserUseCase.getUser(id);
        return ApiResponse.success(userMapper.toResponse(user));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody RegisterUserRequest request) {
        String tenantId = TenantContext.getCurrentTenantId();
        log.info("REST: Admin creating user: {}", request.getUsername());

        AdminUserUseCase.CreateUserCommand command = new AdminUserUseCase.CreateUserCommand(
                request.getUsername(),
                Email.of(request.getEmail()),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getRoles().stream()
                        .map(UserRole::valueOf)
                        .collect(Collectors.toSet()),
                tenantId
        );

        User user = adminUserUseCase.createUser(command);
        return ApiResponse.success(userMapper.toResponse(user), HttpStatus.CREATED.value());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ApiResponse<UserResponse> updateUser(@PathVariable String id,
                                                 @Valid @RequestBody UpdateUserRequest request) {
        log.info("REST: Updating user: {}", id);

        Set<UserRole> roles = null;
        if (request.getRoles() != null) {
            roles = request.getRoles().stream()
                    .map(UserRole::valueOf)
                    .collect(Collectors.toSet());
        }

        AdminUserUseCase.UpdateUserCommand command = new AdminUserUseCase.UpdateUserCommand(
                id,
                request.getFirstName(),
                request.getLastName(),
                request.getEmail() != null ? Email.of(request.getEmail()) : null,
                roles,
                request.getActive()
        );

        User user = adminUserUseCase.updateUser(command);
        return ApiResponse.success(userMapper.toResponse(user));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user")
    public void deleteUser(@PathVariable String id) {
        log.info("REST: Deleting user: {}", id);
        adminUserUseCase.deleteUser(id);
    }
}
