package com.checkpoint.dto;

// Response body for successful login/register.contains the JWT and the user's role
public class AuthResponseDto {

    private String token;
    private String role;
    private String username;

    public AuthResponseDto(String token, String role, String username) {
        this.token = token;
        this.role = role;
        this.username = username;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}

