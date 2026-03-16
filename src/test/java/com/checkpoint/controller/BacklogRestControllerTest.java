package com.checkpoint.controller;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.dto.UpdateStatusRequestDto;
import com.checkpoint.dto.AddToBacklogRequestDto;
import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.GameStatus;
import com.checkpoint.model.User;
import com.checkpoint.service.BacklogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BacklogRestControllerTest {

    @Mock
    private BacklogService backlogService;

    @InjectMocks
    private BacklogRestController backlogRestController;

    private User buildUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }

    @Test
    @DisplayName("GET /api/me/backlog -> 200 with backlog rows for authenticated user")
    void listBacklog_authenticated_returnsRows() {
        User user = buildUser(42L, "sean");

        when(backlogService.listBacklogForUser(42L)).thenReturn(List.of(
                new BacklogItemDto(1L, 10L, "Elden Ring", null, 2L, "PlayStation 5", GameStatus.IN_PROGRESS)
        ));

        ResponseEntity<List<BacklogItemDto>> response = backlogRestController.listBacklog(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Elden Ring", response.getBody().getFirst().getGameTitle());
        assertEquals("PlayStation 5", response.getBody().getFirst().getPlatformName());
        assertEquals(GameStatus.IN_PROGRESS, response.getBody().getFirst().getStatus());

        verify(backlogService).listBacklogForUser(42L);
    }

    @Test
    @DisplayName("GET /api/me/backlog -> 401 when request is not authenticated")
    void listBacklog_unauthenticated_returns401() {
        ResponseEntity<List<BacklogItemDto>> response = backlogRestController.listBacklog(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(backlogService);
    }

    @Test
    @DisplayName("PUT /api/me/backlog/{id}/status -> 204 when item belongs to authenticated user")
    void updateBacklogItem_success_returns204() {
        User user = buildUser(42L, "sean");

        when(backlogService.updateStatus(42L, 9L, GameStatus.COMPLETED)).thenReturn(true);

        UpdateStatusRequestDto body = new UpdateStatusRequestDto();
        body.setStatus(GameStatus.COMPLETED);

        ResponseEntity<Void> response = backlogRestController.updateBacklogItem(user, 9L, body);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(backlogService).updateStatus(42L, 9L, GameStatus.COMPLETED);
    }

    @Test
    @DisplayName("PUT /api/me/backlog/{id}/status -> 404 when item does not belong to user")
    void updateBacklogItem_notFound_returns404() {
        User user = buildUser(42L, "sean");

        when(backlogService.updateStatus(42L, 9L, GameStatus.COMPLETED)).thenReturn(false);

        UpdateStatusRequestDto body = new UpdateStatusRequestDto();
        body.setStatus(GameStatus.COMPLETED);

        ResponseEntity<Void> response = backlogRestController.updateBacklogItem(user, 9L, body);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /api/me/backlog/{id} -> 204 when item belongs to authenticated user")
    void removeFromBacklog_success_returns204() {
        User user = buildUser(42L, "sean");

        when(backlogService.removeFromBacklog(42L, 9L)).thenReturn(true);

        ResponseEntity<Void> response = backlogRestController.removeFromBacklog(user, 9L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(backlogService).removeFromBacklog(42L, 9L);
    }

    @Test
    @DisplayName("POST /api/me/backlog -> 201 when game/platform is added")
    void addToBacklog_success_returns201() {
        User user = buildUser(42L, "sean");
        AddToBacklogRequestDto body = new AddToBacklogRequestDto();
        body.setGameId(5L);
        body.setPlatformId(2L);

        ResponseEntity<Void> response = backlogRestController.addToBacklog(user, body);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(backlogService).addToBacklog(42L, 5L, 2L);
    }

    @Test
    @DisplayName("POST /api/me/backlog -> 409 when same game/platform already exists for user")
    void addToBacklog_duplicate_returns409() {
        User user = buildUser(42L, "sean");
        AddToBacklogRequestDto body = new AddToBacklogRequestDto();
        body.setGameId(5L);
        body.setPlatformId(2L);

        doThrow(new DomainException(ErrorCode.GAME_ALREADY_IN_BACKLOG))
                .when(backlogService).addToBacklog(42L, 5L, 2L);

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> backlogRestController.addToBacklog(user, body)
        );

        assertEquals(ErrorCode.GAME_ALREADY_IN_BACKLOG, exception.getErrorCode());
    }

    @Test
    @DisplayName("POST /api/me/backlog -> 400 when game/platform combination is invalid")
    void addToBacklog_invalidCombination_returns400() {
        User user = buildUser(42L, "sean");
        AddToBacklogRequestDto body = new AddToBacklogRequestDto();
        body.setGameId(5L);
        body.setPlatformId(99L);

        doThrow(new DomainException(ErrorCode.GAME_NOT_AVAILABLE_ON_PLATFORM))
                .when(backlogService).addToBacklog(42L, 5L, 99L);

        DomainException exception = org.junit.jupiter.api.Assertions.assertThrows(
                DomainException.class,
                () -> backlogRestController.addToBacklog(user, body)
        );

        assertEquals(ErrorCode.GAME_NOT_AVAILABLE_ON_PLATFORM, exception.getErrorCode());
    }

    @Test
    @DisplayName("POST /api/me/backlog -> 401 when request is not authenticated")
    void addToBacklog_unauthenticated_returns401() {
        AddToBacklogRequestDto body = new AddToBacklogRequestDto();
        body.setGameId(5L);
        body.setPlatformId(2L);

        ResponseEntity<Void> response = backlogRestController.addToBacklog(null, body);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(backlogService);
    }
}

