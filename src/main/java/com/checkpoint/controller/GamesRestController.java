package com.checkpoint.controller;

import com.checkpoint.dto.GameDto;
import com.checkpoint.dto.GameRequestDto;
import com.checkpoint.dto.PlatformDto;
import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import com.checkpoint.model.User;
import com.checkpoint.repository.GameRepository;
import com.checkpoint.repository.PlatformRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// Master game catalog endpoints (global library + admin manage catalog)
@RestController
@RequestMapping("/api/games")
public class GamesRestController {

    private final GameRepository gameRepository;
    private final PlatformRepository platformRepository;

    public GamesRestController(GameRepository gameRepository, PlatformRepository platformRepository) {
        this.gameRepository = gameRepository;
        this.platformRepository = platformRepository;
    }

    private ResponseEntity<String> applyPlatforms(Game game, GameRequestDto body, boolean required) {
        if (body.getPlatformIds() == null) {
            if (required) {
                return ResponseEntity.badRequest().body("At least one platform is required");
            }
            return null;
        }

        Set<Long> requestedIds = body.getPlatformIds().stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        if (requestedIds.isEmpty()) {
            return ResponseEntity.badRequest().body("At least one platform is required");
        }

        Set<Platform> selectedPlatforms = new java.util.HashSet<>();
        platformRepository.findAllById(requestedIds).forEach(selectedPlatforms::add);

        if (selectedPlatforms.size() != requestedIds.size()) {
            return ResponseEntity.badRequest().body("One or more selected platforms are invalid");
        }

        game.setPlatforms(selectedPlatforms);
        return null;
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

            ResponseEntity<String> platformValidation = applyPlatforms(game, body, true);
            if (platformValidation != null) {
                return platformValidation;
            }

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

                    ResponseEntity<String> platformValidation = applyPlatforms(existing, body, false);
                    if (platformValidation != null) {
                        return platformValidation;
                    }

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

