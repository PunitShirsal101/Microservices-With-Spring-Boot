package com.enterprise.ecommerce.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event fired when a new user registers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime registeredAt;
}