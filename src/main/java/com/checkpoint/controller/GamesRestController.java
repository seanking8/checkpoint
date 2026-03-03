package com.checkpoint.controller;

import com.checkpoint.model.Game;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Master game catalog endpoints (global library + admin manage catalog)
@RestController
@RequestMapping("/api/games")
public class GamesRestController {

    @GetMapping
    public ResponseEntity<Iterable<Game>> listGames() {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable Long gameId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PostMapping
    public ResponseEntity<Game> createGame(@RequestBody Game body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<Game> updateGame(@PathVariable Long gameId, @RequestBody Game body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long gameId) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }
}

