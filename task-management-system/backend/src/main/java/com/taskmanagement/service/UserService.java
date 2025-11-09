package com.taskmanagement.service;

import com.taskmanagement.exception.UserException;
import com.taskmanagement.model.User;
import java.util.List;

public interface UserService {

    public List<User> getAllUser()  throws UserException;

    public User findUserProfileByJwt(String jwt) throws UserException;

    public User findUserByEmail(String email) throws UserException;

    public User findUserById(String userId) throws UserException;

    public List<User> findAllUsers();
}
