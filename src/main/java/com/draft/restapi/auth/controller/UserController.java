package com.draft.restapi.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.auth.entity.dto.UserFilter;
import com.draft.restapi.auth.service.UserService;
import com.draft.restapi.common.payload.ApiResponse;
import com.draft.restapi.common.validation.ValidationGroups;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@SuppressWarnings("null")
@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ApiResponse<UserDto> getAllUsers(
            UserFilter filter,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ApiResponse.success(userService.getAllUsers(filter, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserDto> getUserById(
            @Min(1) @PathVariable("id") Integer userId) {
        return ApiResponse.success(userService.getUserById(userId));
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserDto> createUser(
            @Validated(ValidationGroups.OnCreate.class) @RequestBody UserDto userDto) {
        return ApiResponse.success(userService.createUser(userDto), "User created successfully");
    }

    @PatchMapping("/{id}")
    public ApiResponse<UserDto> updateUser(
            @NotNull @Min(1) @PathVariable("id") Integer userId,
            @Validated(ValidationGroups.OnUpdate.class) @RequestBody UserDto userDetails) {
        return ApiResponse.success(userService.updateUser(userId, userDetails), "User updated successfully");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(
            @NotNull @Min(1) @PathVariable("id") Integer userId,
            @RequestParam(value = "purge", defaultValue = "false") boolean purge) {
        userService.deleteUser(userId, purge);
        String respMessage = purge ? "User deleted permanently" : "User deleted successfully";
        return ApiResponse.success(null, respMessage);
    }
}
