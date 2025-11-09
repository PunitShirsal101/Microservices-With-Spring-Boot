package com.enterprise.ecommerce.user.service;

import com.enterprise.ecommerce.common.exception.InvalidRequestException;
import com.enterprise.ecommerce.common.exception.ResourceNotFoundException;
import com.enterprise.ecommerce.common.kafka.KafkaProducerService;
import com.enterprise.ecommerce.common.events.UserRegisteredEvent;
import com.enterprise.ecommerce.common.security.JwtUtil;
import com.enterprise.ecommerce.user.dto.*;
import com.enterprise.ecommerce.user.entity.User;
import com.enterprise.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for user management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private static final String USER_NOT_FOUND_MESSAGE = "User not found with ID: ";
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final KafkaProducerService kafkaProducerService;
    
    /**
     * Register a new user
     * @param request user registration request
     * @return user response
     */
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Attempting to register user with username: {}", request.getUsername());
        
        // Check if username already exists
        if (Boolean.TRUE.equals(userRepository.existsByUsername(request.getUsername()))) {
            throw new InvalidRequestException("Username is already taken!");
        }
        
        // Check if email already exists
        if (Boolean.TRUE.equals(userRepository.existsByEmail(request.getEmail()))) {
            throw new InvalidRequestException("Email is already in use!");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        if (user == null) {
            throw new InvalidRequestException("Failed to create user");
        }

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        
        // Publish user registered event
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId().toString())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .registeredAt(savedUser.getCreatedAt())
                .build();
        
        kafkaProducerService.sendMessage("user-events", savedUser.getId().toString(), event);
        log.info("Published UserRegisteredEvent for user: {}", savedUser.getId());
        
        return mapToUserResponse(savedUser);
    }
    
    /**
     * Authenticate user and generate JWT token
     * @param request login request
     * @return authentication response with JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        log.info("Attempting to authenticate user: {}", request.getUsernameOrEmail());
        
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        // Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        String jwt = jwtUtil.generateToken(user.getUsername(), user.getRoles().iterator().next().name(), user.getId());
        
        log.info("User authenticated successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
                .token(jwt)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRoles().iterator().next().name())
                .build();
    }
    
    /**
     * Get user by ID
     * @param userId user ID
     * @return user response
     */
    @Cacheable(value = "users", key = "#userId")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        if (userId == null) {
            throw new InvalidRequestException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + userId));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Get user by username
     * @param username username
     * @return user response
     */
    @Cacheable(value = "users", key = "#username")
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        
        return mapToUserResponse(user);
    }
    
    /**
     * Update user profile
     * @param userId user ID
     * @param request user update request
     * @return updated user response
     */
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUser(Long userId, UserRegistrationRequest request) {
        if (userId == null) {
            throw new InvalidRequestException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + userId));
        
        // Update user fields
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
                
        if (user == null) {
            throw new InvalidRequestException("Failed to create user");
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getUsername());
        
        return mapToUserResponse(updatedUser);
    }
    
    /**
     * Delete user by ID
     * @param userId user ID
     */
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long userId) {
        if (userId == null) {
            throw new InvalidRequestException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND_MESSAGE + userId));
        
        if (user != null) {
            userRepository.delete(user);
            log.info("User deleted successfully: {}", user.getUsername());
        }
    }
    
    /**
     * Check if user exists by username
     * @param username username
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    /**
     * Check if user exists by email
     * @param email email
     * @return true if user exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Map User entity to UserResponse DTO
     * @param user user entity
     * @return user response DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.getEnabled())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}