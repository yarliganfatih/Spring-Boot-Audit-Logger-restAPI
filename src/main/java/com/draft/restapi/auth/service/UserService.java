package com.draft.restapi.auth.service;

import com.draft.restapi.auth.entity.dto.UserDto;

public interface UserService {
    Iterable<UserDto> getAllUsers();

    UserDto getUserById(Integer userId);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Integer userId, UserDto userDetails);

    void deleteUser(Integer userId);
}
