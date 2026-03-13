package com.checkpoint.controller;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.model.User;
import com.checkpoint.model.UserGame;
import com.checkpoint.service.BacklogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<UserGame> addToBacklog(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    /**
     * Update a backlog entry identified by (gameId, platformId). For example, update status.
     */
    @PutMapping
    public ResponseEntity<UserGame> updateBacklogItem(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }

    /**
     * Remove a backlog entry identified by (gameId, platformId).
     */
    @DeleteMapping
    public ResponseEntity<Void> removeFromBacklog(@RequestBody Object body) {
        // TODO: implement
        return ResponseEntity.status(501).build();
    }
}
