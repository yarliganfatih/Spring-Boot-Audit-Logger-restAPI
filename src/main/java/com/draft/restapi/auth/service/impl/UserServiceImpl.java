package com.draft.restapi.auth.service.impl;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.auth.mapper.UserMapper;
import com.draft.restapi.auth.service.UserService;
import com.draft.restapi.common.exception.ResourceNotFoundException;
import com.draft.restapi.common.payload.PageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import com.draft.restapi.auth.entity.User;
import com.draft.restapi.auth.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Override
    public PageDto<UserDto> getAllUsers(UserDto.Filter filter, Pageable pageable) {
        User probe = userMapper.toEntity(filter);
        probe.setDeleted(false);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase()
                .withIgnoreNullValues();
        Example<User> example = Example.of(probe, matcher);
        Page<User> userPage = userRepository.findAll(example, pageable);
        Page<UserDto> userDtoPage = userPage.map(userMapper::toDto);
        return new PageDto<>(userDtoPage);
    }

    @Override
    public UserDto getUserById(Integer userId) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        return userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public UserDto updateUser(Integer userId, UserDto userDto) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        User user = userRepository.findById(userId)
                .filter(u -> !u.getDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        userMapper.updateUserFromDto(userDto, user);
        if (!StringUtils.isEmpty(userDto.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    @Override
    public void deleteUser(Integer userId, boolean purge) {
        if (userId == null)
            throw new IllegalArgumentException("Id cannot be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (purge) {
            userRepository.delete(user);
        } else {
            user.setDeleted(true);
            user.setEnabled(false); // to prevent authenticate
            userRepository.save(user);
        }
    }
}
