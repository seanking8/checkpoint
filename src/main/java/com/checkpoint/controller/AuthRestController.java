package com.checkpoint.controller;

import com.checkpoint.dto.AuthResponseDto;
import com.checkpoint.dto.LoginRequestDto;
import com.checkpoint.dto.RegisterRequestDto;
import com.checkpoint.dto.UserDto;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import com.checkpoint.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

// Handles registration, login, and the /me endpoint
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthRestController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/auth/register

    // Creates a new user account
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDto body) {

        // Returns 409 if the username is already taken
        if (userRepository.existsByUsername(body.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Username already taken");
        }

        User user = new User();
        user.setUsername(body.getUsername());
        user.setPasswordHash(passwordEncoder.encode(body.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Account created successfully");
    }

    // Authenticates credentials and returns a JWT on success. Returns 401 if credentials are wrong
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto body) {

        // Throws 401 AuthenticationException if credentials are wrong
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword())
        );

        User user = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(
                new AuthResponseDto(token, user.getRole().name(), user.getUsername())
        );
    }

    // ── GET /api/auth/me ────────────────────────────────────────────────────

    // Returns the currently authenticated user's public profile
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                new UserDto(user.getId(), user.getUsername(), user.getRole())
        );
    }
}
