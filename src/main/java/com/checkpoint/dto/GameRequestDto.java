package com.checkpoint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

// Request body for POST /api/games and PUT /api/games/{id}
// Separate from GameDto so the client cannot supply an id on creation
public class GameRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String coverArtUrl;

    private int releaseYear;

    private List<Long> platformIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCoverArtUrl() { return coverArtUrl; }
    public void setCoverArtUrl(String coverArtUrl) { this.coverArtUrl = coverArtUrl; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }

    public List<Long> getPlatformIds() { return platformIds; }
    public void setPlatformIds(List<Long> platformIds) { this.platformIds = platformIds; }
}

