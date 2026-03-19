package com.checkpoint.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Used for GET /api/platforms responses and admin create/update responses.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlatformDto {

    private Long id;
    private String name;
}

