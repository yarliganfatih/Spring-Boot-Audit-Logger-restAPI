package com.draft.restapi.controller;

import com.draft.restapi.model.User;
import com.draft.restapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Integer userId, @Valid @RequestBody User userDetails) {
        return ResponseEntity.ok(userService.updateUser(userId, userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}
