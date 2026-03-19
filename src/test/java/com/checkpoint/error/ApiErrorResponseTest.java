package com.checkpoint.error;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiErrorResponseTest {

    @Test
    void testApiErrorResponseFields() {
        ApiErrorResponse response = new ApiErrorResponse(
                400,
                "BAD_REQUEST",
                "Validation failed",
                List.of("username is required", "password is required")
        );

        assertEquals(400, response.getStatus());
        assertEquals("BAD_REQUEST", response.getCode());
        assertEquals("Validation failed", response.getMessage());
        assertEquals(List.of("username is required", "password is required"), response.getErrors());
    }
}

