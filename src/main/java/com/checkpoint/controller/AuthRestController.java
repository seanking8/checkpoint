package com.checkpoint.controller;

import com.checkpoint.dto.AuthResponseDto;
import com.checkpoint.dto.LoginRequestDto;
import com.checkpoint.dto.RegisterRequestDto;
import com.checkpoint.dto.UserDto;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import com.checkpoint.security.JwtUtil;
import com.checkpoint.validation.AuthDomainValidator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Handles registration, login, and the /me endpoint
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthDomainValidator authDomainValidator;

    public AuthRestController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            AuthDomainValidator authDomainValidator
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authDomainValidator = authDomainValidator;
    }

    // POST /api/auth/register

    // Creates a new user account
    @PostMapping("/register")
    @SuppressWarnings("unused") // Invoked by Spring MVC via route mapping
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto body) {
        authDomainValidator.assertUsernameAvailable(body.getUsername());

        User user = new User();
        user.setUsername(body.getUsername());
        user.setPasswordHash(passwordEncoder.encode(body.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(201).body("Account created successfully");
    }

    // Authenticates credentials and returns a JWT on success. Returns 401 if credentials are wrong
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto body) {

        // Throws 401 AuthenticationException if credentials are wrong
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword())
        );

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(
                new AuthResponseDto(token, user.getRole().name(), user.getUsername())
        );
    }

    // ── GET /api/auth/me ────────────────────────────────────────────────────

    // Returns the currently authenticated user's public profile
    @GetMapping("/me")
    @SuppressWarnings("unused") // Invoked by Spring MVC via route mapping
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(
                new UserDto(user.getId(), user.getUsername(), user.getRole())
        );
    }
}
