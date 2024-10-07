package com.draft.restapi.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.draft.restapi.auth.entity.dto.UserDto;
import com.draft.restapi.auth.service.UserService;
import com.draft.restapi.common.payload.ApiResponse;

@RestController
@Validated
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Iterable<UserDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@Min(1) @PathVariable("id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<UserDto>> createUser(@Valid @RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.createUser(userDto), "User created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(@NotNull @Min(1) @PathVariable("id") Integer userId, @Valid @RequestBody UserDto userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, userDetails), "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@NotNull @Min(1) @PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
