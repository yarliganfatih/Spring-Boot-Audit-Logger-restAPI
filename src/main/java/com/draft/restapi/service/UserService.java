package com.draft.restapi.service;

import com.draft.restapi.model.dto.UserDto;

public interface UserService {
    Iterable<UserDto> getAllUsers();

    UserDto getUserById(Integer userId);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Integer userId, UserDto userDetails);

    void deleteUser(Integer userId);
}
