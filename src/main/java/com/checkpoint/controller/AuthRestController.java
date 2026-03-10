package com.checkpoint.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

// Auth/account endpoints (registration, login support)
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/me")
    public ResponseEntity<Object> me() {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }
}

