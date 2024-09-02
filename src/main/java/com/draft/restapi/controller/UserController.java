package com.draft.restapi.controller;

import com.draft.restapi.model.User;
import com.draft.restapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.draft.restapi.common.payload.ApiResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<Iterable<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(userService.createUser(user), "User created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable("id") Integer userId, @Valid @RequestBody User userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateUser(userId, userDetails), "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
}
