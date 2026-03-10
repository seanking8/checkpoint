package com.checkpoint.dto;

// Used for GET /api/platforms responses and admin create/update responses.
public class PlatformDto {

    private Long id;
    private String name;

    public PlatformDto() {}

    public PlatformDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

