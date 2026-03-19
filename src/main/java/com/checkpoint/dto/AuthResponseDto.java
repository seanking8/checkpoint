package com.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// Response body for successful login/register.contains the JWT and the user's role
@Getter
@Setter
@AllArgsConstructor
public class AuthResponseDto {

    private String token;
    private String role;
    private String username;
}

