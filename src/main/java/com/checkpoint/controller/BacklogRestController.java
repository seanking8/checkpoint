package com.checkpoint.controller;

import com.checkpoint.dto.AddToBacklogRequestDto;
import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.dto.UpdateStatusRequestDto;
import com.checkpoint.model.User;
import com.checkpoint.service.BacklogService;
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
public class BacklogRestController {

    private final BacklogService backlogService;

    public BacklogRestController(BacklogService backlogService) {
        this.backlogService = backlogService;
    }

    @GetMapping
    public ResponseEntity<List<BacklogItemDto>> listBacklog(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(backlogService.listBacklogForUser(user.getId()));
    }

    @PostMapping
    public ResponseEntity<Void> addToBacklog(
            @AuthenticationPrincipal User user,
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
    public ResponseEntity<Void> updateBacklogItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long backlogId,
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
    public ResponseEntity<Void> removeFromBacklog(
            @AuthenticationPrincipal User user,
            @PathVariable Long backlogId
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean deleted = backlogService.removeFromBacklog(user.getId(), backlogId);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
