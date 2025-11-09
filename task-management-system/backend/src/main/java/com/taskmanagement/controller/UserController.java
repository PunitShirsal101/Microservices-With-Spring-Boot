package com.taskmanagement.controller;

import com.taskmanagement.exception.UserException;
import com.taskmanagement.service.UserService;
import com.taskmanagement.model.User;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile(@RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.findUserProfileByJwt(jwt);
        user.setPassword(null);
        logger.info("Retrieved user profile: {}", user);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/api/users/{userId}")
    public ResponseEntity<User> findUserById(
            @PathVariable String userId,
            @RequestHeader("Authorization") String jwt) throws UserException {

        User user = userService.findUserById(userId);
        user.setPassword(null);

        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @GetMapping("/api/users")
    public ResponseEntity<List<User>> findAllUsers(

            @RequestHeader("Authorization") String jwt)  {

        List<User> users = userService.findAllUsers();

        return new ResponseEntity<>(users, HttpStatus.ACCEPTED);
    }

    @GetMapping()
    public ResponseEntity<Object> getUsers(@RequestHeader("Authorization") String jwt) {
        try {
            List<User> users = userService.getAllUser();
            logger.info("Retrieved all users: {}", users);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception or handle it based on your application's requirements
            return new ResponseEntity<>("Error retrieving users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
