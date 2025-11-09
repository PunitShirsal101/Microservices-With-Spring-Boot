package com.enterprise.ecommerce.user.dto;

import com.enterprise.ecommerce.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for user response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean enabled;
    private Set<User.Role> roles;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}