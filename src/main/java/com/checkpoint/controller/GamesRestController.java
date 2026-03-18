package com.checkpoint.controller;

import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.checkpoint.dto.GameDto;
import com.checkpoint.dto.GameRequestDto;
import com.checkpoint.dto.PlatformDto;
import com.checkpoint.error.ApiErrorResponse;
import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import com.checkpoint.model.User;
import com.checkpoint.repository.GameRepository;
import com.checkpoint.validation.GameDomainValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Set;

// Master game catalog endpoints (global library + admin manage catalog)
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "Browse and manage the game catalog")
@SecurityRequirement(name = "bearerAuth")
public class GamesRestController {

    private final GameRepository gameRepository;
    private final GameDomainValidator gameDomainValidator;


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
    @Operation(summary = "List all games")
    @ApiResponse(responseCode = "200", description = "Games returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<List<GameDto>> listGames(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<GameDto> games = gameRepository.findAllWithPlatforms().stream()
                .map(this::toDto)
                .toList();

        return ResponseEntity.ok(games);
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Get one game by id")
    @ApiResponse(responseCode = "200", description = "Game found")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GameDto> getGame(@Parameter(description = "Game id") @PathVariable Long gameId) {
        return gameRepository.findById(gameId)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create a game", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "201", description = "Game created")
    @ApiResponse(responseCode = "400", description = "Validation or platform selection error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Game title already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GameDto> createGame(@Valid @RequestBody GameRequestDto body) {
        try {
            Game game = new Game();
            game.setTitle(body.getTitle().trim());
            game.setCoverArtUrl(body.getCoverArtUrl());
            game.setReleaseYear(body.getReleaseYear());
            game.setPlatforms(gameDomainValidator.resolvePlatforms(body.getPlatformIds(), true));

            Game saved = gameRepository.save(game);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
        } catch (DataIntegrityViolationException _) {
            throw new DomainException(ErrorCode.GAME_TITLE_EXISTS);
        }
    }

    @PutMapping("/{gameId}")
    @Operation(summary = "Update a game", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "200", description = "Game updated")
    @ApiResponse(responseCode = "400", description = "Validation or platform selection error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Game title already exists",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<GameDto> updateGame(
            @Parameter(description = "Game id") @PathVariable Long gameId,
            @Valid @RequestBody GameRequestDto body
    ) {
        return gameRepository.findById(gameId)
                .map(existing -> {
                    existing.setTitle(body.getTitle().trim());
                    existing.setCoverArtUrl(body.getCoverArtUrl());
                    existing.setReleaseYear(body.getReleaseYear());

                    Set<Platform> resolvedPlatforms = gameDomainValidator.resolvePlatforms(body.getPlatformIds(), false);
                    if (body.getPlatformIds() != null) {
                        existing.setPlatforms(resolvedPlatforms);
                    }

                    try {
                        Game saved = gameRepository.save(existing);
                        return ResponseEntity.ok(toDto(saved));
                    } catch (DataIntegrityViolationException _) {
                        throw new DomainException(ErrorCode.GAME_TITLE_EXISTS);
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{gameId}")
    @Operation(summary = "Delete a game", description = "Requires ADMIN role")
    @ApiResponse(responseCode = "204", description = "Game deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "403", description = "Forbidden (ADMIN required)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Game not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> deleteGame(@Parameter(description = "Game id") @PathVariable Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            return ResponseEntity.notFound().build();
        }

        gameRepository.deleteById(gameId);
        return ResponseEntity.noContent().build();
    }
}

