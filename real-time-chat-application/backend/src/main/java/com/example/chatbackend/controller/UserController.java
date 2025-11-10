package com.example.chatbackend.controller;

import com.example.chatbackend.model.User;
import com.example.chatbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getUsers(Principal principal) {
        List<User> users = userRepository.findAll();
        // Remove current user and sensitive data
        String currentUser = principal.getName();
        users = users.stream()
                .filter(user -> !user.getUsername().equals(currentUser))
                .map(user -> {
                    user.setPassword(null); // Remove password
                    return user;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
