package com.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// Used for GET /api/games and POST/PUT /api/games responses.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameDto {

    private Long id;
    private String title;
    private String coverArtUrl;
    private int releaseYear;

    /** Platforms this game is available on (id + name). */
    private List<PlatformDto> platforms;
}

