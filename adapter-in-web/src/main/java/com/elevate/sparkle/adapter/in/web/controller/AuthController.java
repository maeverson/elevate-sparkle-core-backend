package com.elevate.sparkle.adapter.in.web.controller;

import com.elevate.sparkle.adapter.in.web.dto.*;
import com.elevate.sparkle.adapter.in.web.mapper.UserMapper;
import com.elevate.sparkle.application.port.in.AuthenticateUserUseCase;
import com.elevate.sparkle.application.port.in.RegisterUserUseCase;
import com.elevate.sparkle.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Authentication
 * Thin adapter - delegates to use cases
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        log.info("REST: Registering new user: {}", request.getUsername());
        
        User user = registerUserUseCase.registerUser(userMapper.toCommand(request));
        UserResponse response = userMapper.toResponse(user);
        
        return ApiResponse.success(response, HttpStatus.CREATED.value());
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ApiResponse<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST: Authenticating user: {}", request.getUsername());
        
        AuthenticateUserUseCase.AuthenticationCommand command = 
                new AuthenticateUserUseCase.AuthenticationCommand(
                        request.getUsername(),
                        request.getPassword()
                );
        
        AuthenticateUserUseCase.AuthenticationResult result = 
                authenticateUserUseCase.authenticate(command);
        
        AuthenticationResponse response = userMapper.toAuthResponse(result);
        
        return ApiResponse.success(response);
    }
}
