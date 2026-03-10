package com.checkpoint.dto;

import jakarta.validation.constraints.NotNull;

// Request body for POST /api/me/backlog. Adds a game/platform combination to the logged-in user's backlog
public class AddToBacklogRequestDto {

    @NotNull(message = "gameId is required")
    private Long gameId;

    @NotNull(message = "platformId is required")
    private Long platformId;

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public Long getPlatformId() { return platformId; }
    public void setPlatformId(Long platformId) { this.platformId = platformId; }
}

