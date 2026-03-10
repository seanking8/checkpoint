package com.checkpoint.dto;

import java.util.List;

// Used for GET /api/games and POST/PUT /api/games responses.
public class GameDto {

    private Long id;
    private String title;
    private String coverArtUrl;
    private int releaseYear;

    /** Names of platforms this game is available on. */
    private List<String> platforms;

    public GameDto() {}

    public GameDto(Long id, String title, String coverArtUrl, int releaseYear, List<String> platforms) {
        this.id = id;
        this.title = title;
        this.coverArtUrl = coverArtUrl;
        this.releaseYear = releaseYear;
        this.platforms = platforms;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverArtUrl() { return coverArtUrl; }
    public void setCoverArtUrl(String coverArtUrl) { this.coverArtUrl = coverArtUrl; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public List<String> getPlatforms() { return platforms; }
    public void setPlatforms(List<String> platforms) { this.platforms = platforms; }
}

