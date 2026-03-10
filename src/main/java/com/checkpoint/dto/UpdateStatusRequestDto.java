package com.checkpoint.dto;

import com.checkpoint.model.GameStatus;
import jakarta.validation.constraints.NotNull;

// Request body for PUT /api/me/backlog/{id}
// Updates the play status of a backlog entry
public class UpdateStatusRequestDto {

    @NotNull(message = "status is required")
    private GameStatus status;

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
}

