package com.checkpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// Request body for POST
// Separate from GameDto so the client cannot supply an id on creation
@Getter
@Setter
public class GameRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String coverArtUrl;

    private int releaseYear;

    private List<Long> platformIds;

}

