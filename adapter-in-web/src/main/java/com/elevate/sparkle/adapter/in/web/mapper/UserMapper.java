package com.elevate.sparkle.adapter.in.web.mapper;

import com.elevate.sparkle.adapter.in.web.dto.AuthenticationResponse;
import com.elevate.sparkle.adapter.in.web.dto.RegisterUserRequest;
import com.elevate.sparkle.adapter.in.web.dto.UserResponse;
import com.elevate.sparkle.application.port.in.AuthenticateUserUseCase.AuthenticationResult;
import com.elevate.sparkle.application.port.in.RegisterUserUseCase.RegisterUserCommand;
import com.elevate.sparkle.domain.model.User;
import com.elevate.sparkle.domain.valueobject.Email;
import com.elevate.sparkle.domain.valueobject.UserRole;
import org.mapstruct.Mapper;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper between User DTOs and domain objects
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    default RegisterUserCommand toCommand(RegisterUserRequest request) {
        return new RegisterUserCommand(
                request.getUsername(),
                Email.of(request.getEmail()),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                toUserRoles(request.getRoles())
        );
    }

    default Set<UserRole> toUserRoles(Set<String> roles) {
        return roles.stream()
                .map(UserRole::valueOf)
                .collect(Collectors.toSet());
    }

    default UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId().getValue())
                .username(user.getUsername())
                .email(user.getEmail().getValue())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(toStringRoles(user.getRoles()))
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    default Set<String> toStringRoles(Set<UserRole> roles) {
        return roles.stream()
                .map(UserRole::name)
                .collect(Collectors.toSet());
    }

    default AuthenticationResponse toAuthResponse(AuthenticationResult result) {
        return AuthenticationResponse.builder()
                .token(result.token())
                .user(toResponse(result.user()))
                .build();
    }
}
