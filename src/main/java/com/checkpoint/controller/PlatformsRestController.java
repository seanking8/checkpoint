package com.checkpoint.controller;

import org.springframework.web.bind.annotation.*;
import com.checkpoint.model.Platform;
import com.checkpoint.repository.PlatformRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Platform endpoints (list for users + admin manage platforms)
@RestController
@RequestMapping("/api/platforms")
@SuppressWarnings("unused") // Invoked by Spring MVC via route mapping
@Tag(name = "Platforms", description = "Browse available gaming platforms")
@SecurityRequirement(name = "bearerAuth")
public class PlatformsRestController {

    private final PlatformRepository platformRepository;

    public PlatformsRestController(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @GetMapping
    @Operation(summary = "List all platforms")
    @ApiResponse(responseCode = "200", description = "Platforms returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.checkpoint.error.ApiErrorResponse.class)))
    public ResponseEntity<Iterable<Platform>> listPlatforms() {
        List<Platform> platforms = new ArrayList<>();
        platformRepository.findAll().forEach(platforms::add);
        platforms.sort(Comparator.comparing(Platform::getName, String.CASE_INSENSITIVE_ORDER));
        return ResponseEntity.ok(platforms);
    }

    @GetMapping("/{platformId}")
    @Operation(summary = "Get one platform by id")
    @ApiResponse(responseCode = "200", description = "Platform found")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.checkpoint.error.ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Platform not found",
            content = @Content(schema = @Schema(implementation = com.checkpoint.error.ApiErrorResponse.class)))
    public ResponseEntity<Platform> getPlatform(@Parameter(description = "Platform id") @PathVariable Long platformId) {
        return platformRepository.findById(platformId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}