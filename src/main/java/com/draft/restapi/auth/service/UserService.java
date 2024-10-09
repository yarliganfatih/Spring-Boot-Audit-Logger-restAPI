package com.draft.restapi.auth.service;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.common.payload.PageDto;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PageDto<UserDto> getAllUsers(Pageable pageable);

    UserDto getUserById(Integer userId);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Integer userId, UserDto userDetails);

    void deleteUser(Integer userId);
}
