package com.checkpoint.dto;

import com.checkpoint.model.GameStatus;

// Represents a single entry in the logged-in user's personal backlog
// Used for GET /api/me/backlog responses
public class BacklogItemDto {

    private Long id;

    private Long gameId;
    private String gameTitle;
    private String coverArtUrl;

    private Long platformId;
    private String platformName;

    private GameStatus status;

    public BacklogItemDto() {}

    public BacklogItemDto(Long id, Long gameId, String gameTitle, String coverArtUrl,
                          Long platformId, String platformName, GameStatus status) {
        this.id = id;
        this.gameId = gameId;
        this.gameTitle = gameTitle;
        this.coverArtUrl = coverArtUrl;
        this.platformId = platformId;
        this.platformName = platformName;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }

    public String getGameTitle() { return gameTitle; }
    public void setGameTitle(String gameTitle) { this.gameTitle = gameTitle; }

    public String getCoverArtUrl() { return coverArtUrl; }
    public void setCoverArtUrl(String coverArtUrl) { this.coverArtUrl = coverArtUrl; }

    public Long getPlatformId() { return platformId; }
    public void setPlatformId(Long platformId) { this.platformId = platformId; }

    public String getPlatformName() { return platformName; }
    public void setPlatformName(String platformName) { this.platformName = platformName; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
}

