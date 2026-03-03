package com.checkpoint.controller;

import com.checkpoint.model.UserGame;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Personal backlog endpoints: link game + platform to the logged-in user, update status, and remove items
@RestController
@RequestMapping("/api/me/backlog")
public class BacklogRestController {

    @GetMapping
    public ResponseEntity<Iterable<UserGame>> listBacklog() {
        // TODO: implement
        return ResponseEntity.status(501).build();
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
