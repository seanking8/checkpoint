package com.checkpoint.security;

import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

// Unit tests for JwtUtil
class JwtUtilTest {

    // 64-byte (128 hex chars) hex secret — same format as application.yml
    private static final String TEST_SECRET =
            "4a6f686e446f655365637265744b657931323334353637383930313233343536" +
            "3738393031323334353637383930313233343536373839303132333435363738";

    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET, ONE_HOUR_MS);

        testUser = new User();
        testUser.setUsername("sean");
        testUser.setPasswordHash("irrelevant");
        testUser.setRole(Role.USER);
    }

    @Test
    @DisplayName("generateToken returns a non-blank JWT string")
    void generateToken_returnsNonBlankString() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(token).isNotBlank();
        // JWTs always have exactly 3 dot-separated segments
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername returns the username that was embedded in the token")
    void extractUsername_matchesOriginalUsername() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("sean");
    }

    @Test
    @DisplayName("extractRole returns ROLE_USER for a USER-role account")
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("isTokenValid returns true for a freshly-issued token")
    void isTokenValid_freshToken_returnsTrue() {
        String token = jwtUtil.generateToken(testUser);
        assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false when token belongs to a different user")
    void isTokenValid_wrongUser_returnsFalse() {
        // Token was issued for "sean"
        String token = jwtUtil.generateToken(testUser);

        // But we validate against a different user object
        User otherUser = new User();
        otherUser.setUsername("hacker");
        otherUser.setPasswordHash("irrelevant");
        otherUser.setRole(Role.USER);

        assertThat(jwtUtil.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for an already-expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        // Create a JwtUtil with a -1 ms expiry so the token is expired immediately
        JwtUtil expiredJwtUtil = new JwtUtil(TEST_SECRET, -1L);
        String token = expiredJwtUtil.generateToken(testUser);

        assertThat(jwtUtil.isTokenValid(token, testUser)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for a tampered token")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken(testUser);
        // Flip one character in the signature segment (last part after the final dot)
        int lastDot = token.lastIndexOf('.');
        String tampered = token.substring(0, lastDot + 1) + "X" + token.substring(lastDot + 2);

        assertThat(jwtUtil.isTokenValid(tampered, testUser)).isFalse();
    }
}

