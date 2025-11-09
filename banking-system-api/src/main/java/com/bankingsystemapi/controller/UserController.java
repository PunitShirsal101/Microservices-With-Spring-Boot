package com.bankingsystemapi.controller;

import com.bankingsystemapi.entity.User;
import com.bankingsystemapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile() {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@RequestBody User updatedUser) {
        String username = getCurrentUsername();
        User user = userService.findByUsername(username);
        user.setEmail(updatedUser.getEmail());
        // Note: password update separate
        userService.registerUser(user); // This will encode password if changed, but since not, ok
        return ResponseEntity.ok(user);
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
