package com.checkpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request body for POST /api/platforms and PUT /api/platforms/{id}
public class PlatformRequestDto {

    @NotBlank(message = "Platform name is required")
    @Size(max = 50, message = "Platform name must not exceed 50 characters")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

