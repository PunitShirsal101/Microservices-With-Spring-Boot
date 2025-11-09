package com.enterprise.ecommerce.user.controller;

import com.enterprise.ecommerce.common.dto.ApiResponse;
import com.enterprise.ecommerce.user.dto.*;
import com.enterprise.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user management operations
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user registration, authentication and management")
public class UserController {
    
    private final UserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {
        
        log.info("Registration request received for username: {}", request.getUsername());
        
        try {
            UserResponse userResponse = userService.registerUser(request);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User registered successfully")
                    .data(userResponse)
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Registration failed for username: {}", request.getUsername(), e);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Authenticate user and get JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> loginUser(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login request received for: {}", request.getUsernameOrEmail());
        
        try {
            AuthResponse authResponse = userService.authenticateUser(request);
            
            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("User authenticated successfully")
                    .data(authResponse)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Authentication failed for: {}", request.getUsernameOrEmail(), e);
            
            ApiResponse<AuthResponse> response = ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message("Invalid username/email or password")
                    .build();
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user profile by user ID")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        try {
            UserResponse userResponse = userService.getUserById(userId);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(userResponse)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve user with ID: {}", userId, e);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Get user profile by username
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Retrieve user profile by username")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        
        try {
            UserResponse userResponse = userService.getUserByUsername(username);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User retrieved successfully")
                    .data(userResponse)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to retrieve user with username: {}", username, e);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * Update user profile
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update user profile", description = "Update user profile information")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody UserRegistrationRequest request) {
        
        try {
            UserResponse userResponse = userService.updateUser(userId, request);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(true)
                    .message("User updated successfully")
                    .data(userResponse)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to update user with ID: {}", userId, e);
            
            ApiResponse<UserResponse> response = ApiResponse.<UserResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete user
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Delete user account")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        
        try {
            userService.deleteUser(userId);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(true)
                    .message("User deleted successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to delete user with ID: {}", userId, e);
            
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check if username exists
     */
    @GetMapping("/check/username/{username}")
    @Operation(summary = "Check username availability", description = "Check if username is available")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
            @Parameter(description = "Username to check") @PathVariable String username) {
        
        boolean exists = userService.existsByUsername(username);
        
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "Username is taken" : "Username is available")
                .data(!exists) // Return availability (opposite of exists)
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if email exists
     */
    @GetMapping("/check/email/{email}")
    @Operation(summary = "Check email availability", description = "Check if email is available")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @Parameter(description = "Email to check") @PathVariable String email) {
        
        boolean exists = userService.existsByEmail(email);
        
        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "Email is taken" : "Email is available")
                .data(!exists) // Return availability (opposite of exists)
                .build();
        
        return ResponseEntity.ok(response);
    }
}