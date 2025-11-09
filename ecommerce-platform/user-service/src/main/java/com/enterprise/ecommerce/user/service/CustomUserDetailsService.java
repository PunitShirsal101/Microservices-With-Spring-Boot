package com.enterprise.ecommerce.user.service;

import com.enterprise.ecommerce.user.entity.User;
import com.enterprise.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom UserDetailsService implementation
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        return UserPrincipal.create(user);
    }
    
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        if (id == null) {
            throw new UsernameNotFoundException("User ID cannot be null");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        
        return UserPrincipal.create(user);
    }
    
    /**
     * Custom UserDetails implementation
     */
    public static class UserPrincipal implements UserDetails {
        private Long id;
        private String username;
        private String email;
        private String password;
        private List<GrantedAuthority> authorities;
        private boolean enabled;
        private boolean accountNonExpired;
        private boolean accountNonLocked;
        private boolean credentialsNonExpired;
        
        private UserPrincipal(Builder builder) {
            this.id = builder.id;
            this.username = builder.username;
            this.email = builder.email;
            this.password = builder.password;
            this.authorities = builder.authorities;
            this.enabled = builder.enabled;
            this.accountNonExpired = builder.accountNonExpired;
            this.accountNonLocked = builder.accountNonLocked;
            this.credentialsNonExpired = builder.credentialsNonExpired;
        }
        
        public static UserPrincipal create(User user) {
            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .collect(Collectors.toList());
            
            return new Builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .enabled(user.getEnabled())
                    .accountNonExpired(user.getAccountNonExpired())
                    .accountNonLocked(user.getAccountNonLocked())
                    .credentialsNonExpired(user.getCredentialsNonExpired())
                    .build();
        }
        
        public Long getId() {
            return id;
        }
        
        public String getEmail() {
            return email;
        }
        
        @Override
        public String getUsername() {
            return username;
        }
        
        @Override
        public String getPassword() {
            return password;
        }
        
        @Override
        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return accountNonExpired;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return accountNonLocked;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return credentialsNonExpired;
        }
        
        @Override
        public boolean isEnabled() {
            return enabled;
        }
        
        /**
         * Builder pattern for UserPrincipal to avoid constructor with too many parameters
         */
        public static class Builder {
            private Long id;
            private String username;
            private String email;
            private String password;
            private List<GrantedAuthority> authorities;
            private boolean enabled = true;
            private boolean accountNonExpired = true;
            private boolean accountNonLocked = true;
            private boolean credentialsNonExpired = true;
            
            public Builder id(Long id) {
                this.id = id;
                return this;
            }
            
            public Builder username(String username) {
                this.username = username;
                return this;
            }
            
            public Builder email(String email) {
                this.email = email;
                return this;
            }
            
            public Builder password(String password) {
                this.password = password;
                return this;
            }
            
            public Builder authorities(List<GrantedAuthority> authorities) {
                this.authorities = authorities;
                return this;
            }
            
            public Builder enabled(boolean enabled) {
                this.enabled = enabled;
                return this;
            }
            
            public Builder accountNonExpired(boolean accountNonExpired) {
                this.accountNonExpired = accountNonExpired;
                return this;
            }
            
            public Builder accountNonLocked(boolean accountNonLocked) {
                this.accountNonLocked = accountNonLocked;
                return this;
            }
            
            public Builder credentialsNonExpired(boolean credentialsNonExpired) {
                this.credentialsNonExpired = credentialsNonExpired;
                return this;
            }
            
            public UserPrincipal build() {
                return new UserPrincipal(this);
            }
        }
    }
}