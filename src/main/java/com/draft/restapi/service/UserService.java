package com.draft.restapi.service;

import com.draft.restapi.model.User;

public interface UserService {
    Iterable<User> getAllUsers();

    User getUserById(Integer userId);

    User createUser(User user);

    User updateUser(Integer userId, User userDetails);

    void deleteUser(Integer userId);
}
