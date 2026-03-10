package com.checkpoint.controller;

import org.springframework.web.bind.annotation.*;
import com.checkpoint.model.User;
import org.springframework.http.ResponseEntity;

// Admin user management endpoints (list/add/edit/remove users)
@RestController
@RequestMapping("/api/admin/users")
public class AdminUsersRestController {

    @GetMapping
    public ResponseEntity<Iterable<User>> listUsers() {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }
}

