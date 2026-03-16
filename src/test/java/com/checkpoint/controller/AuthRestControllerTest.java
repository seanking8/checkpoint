package com.checkpoint.controller;

import com.checkpoint.dto.LoginRequestDto;
import com.checkpoint.dto.RegisterRequestDto;
import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import com.checkpoint.security.JwtUtil;
import com.checkpoint.validation.AuthDomainValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Unit tests for AuthRestController
@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthDomainValidator authDomainValidator;

    @InjectMocks
    private AuthRestController authRestController;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Standalone setup. no Spring Security filter chain, just the controller
        mockMvc = MockMvcBuilders
                .standaloneSetup(authRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Registration

    @Test
    @DisplayName("POST /api/auth/register → 201 when username is available")
    void register_success() throws Exception {
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$hashed");

        RegisterRequestDto body = new RegisterRequestDto();
        body.setUsername("sean");
        body.setPassword("secret123");
        body.setConfirmPassword("secret123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Account created successfully"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User saved = captor.getValue();
        assertNotNull(saved);
        assertEquals("sean", saved.getUsername());
        assertEquals("$2a$hashed", saved.getPasswordHash());
        assertEquals(Role.USER, saved.getRole());
        assertNotEquals("secret123", saved.getPasswordHash());
    }

    @Test
    @DisplayName("POST /api/auth/register → 409 when username is already taken")
    void register_duplicateUsername_returns409() throws Exception {
        doThrow(new com.checkpoint.error.DomainException(com.checkpoint.error.ErrorCode.USERNAME_TAKEN))
                .when(authDomainValidator).assertUsernameAvailable("sean");

        RegisterRequestDto body = new RegisterRequestDto();
        body.setUsername("sean");
        body.setPassword("secret123");
        body.setConfirmPassword("secret123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isConflict());

        verify(authDomainValidator).assertUsernameAvailable("sean");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when username is blank")
    void register_blankUsername_returns400() throws Exception {
        RegisterRequestDto body = new RegisterRequestDto();
        body.setUsername("  ");   // blank
        body.setPassword("secret123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when password is blank")
    void register_blankPassword_returns400() throws Exception {
        RegisterRequestDto body = new RegisterRequestDto();
        body.setUsername("sean");
        body.setPassword("");     // blank

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register → 400 when passwords do not match")
    void register_passwordMismatch_returns400() throws Exception {
        RegisterRequestDto body = new RegisterRequestDto();
        body.setUsername("sean");
        body.setPassword("secret123");
        body.setConfirmPassword("different");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // Login

    @Test
    @DisplayName("POST /api/auth/login → 200 with token on valid credentials")
    void login_success_returnsToken() throws Exception {
        // Build a real User object to act as the authenticated principal
        User user = new User();
        user.setUsername("sean");
        user.setPasswordHash("$2a$hashed");
        user.setRole(Role.USER);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtUtil.generateToken(user)).thenReturn("mock.jwt.token");

        LoginRequestDto body = new LoginRequestDto();
        body.setUsername("sean");
        body.setPassword("secret123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.username").value("sean"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login → 401 on bad credentials")
    void login_badCredentials_returns401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequestDto body = new LoginRequestDto();
        body.setUsername("sean");
        body.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/login → 400 when username is missing")
    void login_missingUsername_returns400() throws Exception {
        LoginRequestDto body = new LoginRequestDto();
        body.setUsername("");
        body.setPassword("secret123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}


