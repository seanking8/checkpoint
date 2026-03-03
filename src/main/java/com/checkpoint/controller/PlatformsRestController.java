package com.checkpoint.controller;

import com.checkpoint.model.Platform;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Platform endpoints (list for users + admin manage platforms)
@RequestMapping("/api/platforms")
public class PlatformsRestController {

    @GetMapping
    public ResponseEntity<Iterable<Platform>> listPlatforms() {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{platformId}")
    public ResponseEntity<Platform> getPlatform(@PathVariable Long platformId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PostMapping
    public ResponseEntity<Platform> createPlatform(@RequestBody Platform body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PutMapping("/{platformId}")
    public ResponseEntity<Platform> updatePlatform(@PathVariable Long platformId, @RequestBody Platform body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @DeleteMapping("/{platformId}")
    public ResponseEntity<Void> deletePlatform(@PathVariable Long platformId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }
}

