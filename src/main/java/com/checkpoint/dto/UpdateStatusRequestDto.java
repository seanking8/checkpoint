package com.checkpoint.dto;

import com.checkpoint.model.GameStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// Request body for PUT
// Updates the play status of a backlog entry
@Getter
@Setter
public class UpdateStatusRequestDto {

    @NotNull(message = "status is required")
    private GameStatus status;

}

