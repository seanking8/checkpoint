package com.checkpoint.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Request body for POST /api/me/backlog. Adds a game/platform combination to the logged-in user's backlog
@Getter
@Setter
public class AddToBacklogRequestDto {

    @NotNull(message = "gameId is required")
    private Long gameId;

    @NotNull(message = "platformId is required")
    private Long platformId;
}

