package com.checkpoint.controller;

import com.checkpoint.dto.GameDto;
import com.checkpoint.dto.GameRequestDto;
import com.checkpoint.dto.PlatformDto;
import com.checkpoint.model.Game;
import com.checkpoint.model.User;
import com.checkpoint.repository.GameRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

    private GameDto toDto(Game game) {
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
    }

    @GetMapping
    public ResponseEntity<List<GameDto>> listGames(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<GameDto> games = gameRepository.findAllWithPlatforms().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGame(@PathVariable Long gameId) {
        return gameRepository.findById(gameId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createGame(@Valid @RequestBody GameRequestDto body) {
        try {
            Game game = new Game();
            game.setTitle(body.getTitle().trim());
            game.setCoverArtUrl(body.getCoverArtUrl());
            game.setReleaseYear(body.getReleaseYear());

            Game saved = gameRepository.save(game);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Game title already exists");
        }
    }

    @PutMapping("/{gameId}")
    public ResponseEntity<?> updateGame(@PathVariable Long gameId, @Valid @RequestBody GameRequestDto body) {
        return gameRepository.findById(gameId)
                .map(existing -> {
                    existing.setTitle(body.getTitle().trim());
                    existing.setCoverArtUrl(body.getCoverArtUrl());
                    existing.setReleaseYear(body.getReleaseYear());

                    try {
                        Game saved = gameRepository.save(existing);
                        return ResponseEntity.ok(toDto(saved));
                    } catch (DataIntegrityViolationException ex) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).body("Game title already exists");
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            return ResponseEntity.notFound().build();
        }

        gameRepository.deleteById(gameId);
        return ResponseEntity.noContent().build();
    }
}

