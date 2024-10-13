package com.draft.restapi.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.auth.service.UserService;
import com.draft.restapi.common.payload.ApiResponse;
import com.draft.restapi.common.validation.ValidationGroups;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<UserDto>> getAllUsers(
            UserDto.Filter filter,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@Min(1) @PathVariable("id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Validated(ValidationGroups.OnCreate.class) @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.createUser(userDto), "User created successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@NotNull @Min(1) @PathVariable("id") Integer userId, @Validated(ValidationGroups.OnUpdate.class) @RequestBody UserDto userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, userDetails), "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@NotNull @Min(1) @PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
