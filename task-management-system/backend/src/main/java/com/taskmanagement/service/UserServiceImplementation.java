package com.taskmanagement.service;

import com.taskmanagement.exception.UserException;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.config.JwtProvider;
import com.taskmanagement.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImplementation implements UserService {

    private final UserRepository userRepository;

    public UserServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findUserProfileByJwt(String jwt) throws UserException {
        String email= JwtProvider.getEmailFromJwtToken(jwt);

        User user = userRepository.findByEmail(email);

        if(user==null) {
            throw new UserException("user not exist with email "+email);
        }
        return user;
    }

    public List<User> getAllUser()  throws UserException{
        return userRepository.findAll();

    }

    @Override
    public User findUserByEmail(String email) throws UserException {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findUserById(String userId) throws UserException {
        java.util.Optional<User> opt = userRepository.findById(userId);

        if(opt.isEmpty()) {
            throw new UserException("user not found with id "+userId);
        }
        return opt.get();
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
