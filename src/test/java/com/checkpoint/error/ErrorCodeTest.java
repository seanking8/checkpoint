package com.checkpoint.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorCodeTest {

    @Test
    void testErrorCodeMetadata() {
        assertEquals(HttpStatus.CONFLICT, ErrorCode.USERNAME_TAKEN.getStatus());
        assertEquals("Username already taken", ErrorCode.USERNAME_TAKEN.getDefaultMessage());

        assertEquals(HttpStatus.BAD_REQUEST, ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED.getStatus());
        assertEquals("At least one platform is required", ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED.getDefaultMessage());

        assertEquals(HttpStatus.CONFLICT, ErrorCode.GAME_TITLE_EXISTS.getStatus());
        assertEquals("Game title already exists", ErrorCode.GAME_TITLE_EXISTS.getDefaultMessage());
    }

    @Test
    void testErrorCodeCount() {
        assertEquals(10, ErrorCode.values().length);
    }
}

