package com.checkpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

// Request body for POST
@Getter
@Setter
public class PlatformRequestDto {

    @NotBlank(message = "Platform name is required")
    @Size(max = 50, message = "Platform name must not exceed 50 characters")
    private String name;

}

