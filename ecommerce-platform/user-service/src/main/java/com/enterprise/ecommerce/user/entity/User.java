package com.enterprise.ecommerce.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * User entity representing a user in the e-commerce platform
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank
    @Size(max = 100)
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 100)
    @Column(nullable = false)
    private String password;
    
    @Size(max = 100)
    private String firstName;
    
    @Size(max = 100)
    private String lastName;
    
    @Size(max = 15)
    private String phoneNumber;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean accountNonExpired = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean accountNonLocked = true;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean credentialsNonExpired = true;
    
    @Enumerated(EnumType.STRING)
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = Set.of(Role.USER);
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    /**
     * User roles enum
     */
    public enum Role {
        USER, ADMIN, MODERATOR
    }
}