package com.draft.restapi.service.impl;

import com.draft.restapi.common.exception.ResourceNotFoundException;
import com.draft.restapi.model.User;
import com.draft.restapi.repository.UserRepository;
import com.draft.restapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Integer userId) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Integer userId, User userDetails) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setEmail(userDetails.getEmail());
        user.setUsername(userDetails.getUsername());
        user.setPassword(passwordEncoder.encode(userDetails.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Integer userId) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userRepository.delete(user);
    }
}
