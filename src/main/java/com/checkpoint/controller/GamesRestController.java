package com.checkpoint.controller;

import com.checkpoint.dto.GameDto;
import com.checkpoint.dto.PlatformDto;
import com.checkpoint.model.Game;
import com.checkpoint.model.User;
import com.checkpoint.repository.GameRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Master game catalog endpoints (global library + admin manage catalog)
@RestController
@RequestMapping("/api/games")
public class GamesRestController {

    private final GameRepository gameRepository;

    public GamesRestController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> listGames(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<GameDto> games = gameRepository.findAllWithPlatforms().stream()
                .map(game -> {
                    List<PlatformDto> platforms = game.getPlatforms().stream()
                            .map(platform -> new PlatformDto(platform.getId(), platform.getName()))
                            .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()))
                            .toList();

                    return new GameDto(
                            game.getId(),
                            game.getTitle(),
                            game.getCoverArtUrl(),
                            game.getReleaseYear(),
                            platforms
                    );
                })
                .toList();

        return ResponseEntity.ok(games);
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

