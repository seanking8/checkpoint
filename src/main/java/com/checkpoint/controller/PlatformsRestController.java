package com.checkpoint.controller;

import org.springframework.web.bind.annotation.*;
import com.checkpoint.model.Platform;
import com.checkpoint.repository.PlatformRepository;
import org.springframework.http.ResponseEntity;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Platform endpoints (list for users + admin manage platforms)
@RestController
@RequestMapping("/api/platforms")
@SuppressWarnings("unused") // Invoked by Spring MVC via route mapping
public class PlatformsRestController {

    private final PlatformRepository platformRepository;

    public PlatformsRestController(PlatformRepository platformRepository) {
        this.platformRepository = platformRepository;
    }

    @GetMapping
    public ResponseEntity<Iterable<Platform>> listPlatforms() {
        List<Platform> platforms = new ArrayList<>();
        platformRepository.findAll().forEach(platforms::add);
        platforms.sort(Comparator.comparing(Platform::getName, String.CASE_INSENSITIVE_ORDER));
        return ResponseEntity.ok(platforms);
    }

    @GetMapping("/{platformId}")
    public ResponseEntity<Platform> getPlatform(@PathVariable Long platformId) {
        return platformRepository.findById(platformId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}