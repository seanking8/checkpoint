package com.checkpoint.dto;

import com.checkpoint.model.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Represents a single entry in the logged-in user's personal backlog
// Used for GET /api/me/backlog responses
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BacklogItemDto {

    private Long id;

    private Long gameId;
    private String gameTitle;
    private String coverArtUrl;

    private Long platformId;
    private String platformName;

    private GameStatus status;
}

