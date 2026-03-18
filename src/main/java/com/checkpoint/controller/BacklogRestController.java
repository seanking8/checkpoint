package com.checkpoint.controller;

import com.checkpoint.dto.AddToBacklogRequestDto;
import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.dto.UpdateStatusRequestDto;
import com.checkpoint.error.ApiErrorResponse;
import com.checkpoint.model.User;
import com.checkpoint.service.BacklogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Personal backlog endpoints: link game + platform to the logged-in user, update status, and remove items
@RestController
@RequestMapping("/api/me/backlog")
@Tag(name = "Backlog", description = "Manage the authenticated user's backlog entries")
@SecurityRequirement(name = "bearerAuth")
public class BacklogRestController {

    private final BacklogService backlogService;

    public BacklogRestController(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @GetMapping
    @Operation(summary = "List my backlog")
    @ApiResponse(responseCode = "200", description = "Backlog returned")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<List<BacklogItemDto>> listBacklog(@Parameter(hidden = true) @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(backlogService.listBacklogForUser(user.getId()));
    }

    @PostMapping
    @Operation(summary = "Add a game/platform pair to my backlog")
    @ApiResponse(responseCode = "201", description = "Backlog entry created")
    @ApiResponse(responseCode = "400", description = "Validation or domain validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Game or platform not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Game already in backlog for platform",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> addToBacklog(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToBacklogRequestDto body
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        backlogService.addToBacklog(user.getId(), body.getGameId(), body.getPlatformId());
        return ResponseEntity.status(201).build();
    }

    /**
     * Update only the status for a backlog entry that belongs to the logged-in user.
     */
    @PutMapping("/{backlogId}/status")
    @Operation(summary = "Update the status of one backlog entry")
    @ApiResponse(responseCode = "204", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Backlog entry not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> updateBacklogItem(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "Backlog entry id") @PathVariable Long backlogId,
            @Valid @RequestBody UpdateStatusRequestDto body
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean updated = backlogService.updateStatus(user.getId(), backlogId, body.getStatus());
        return updated ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    /**
     * Remove a backlog entry that belongs to the logged-in user.
     */
    @DeleteMapping("/{backlogId}")
    @Operation(summary = "Remove one backlog entry")
    @ApiResponse(responseCode = "204", description = "Backlog entry removed")
    @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Backlog entry not found",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> removeFromBacklog(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "Backlog entry id") @PathVariable Long backlogId
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = backlogService.removeFromBacklog(user.getId(), backlogId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
