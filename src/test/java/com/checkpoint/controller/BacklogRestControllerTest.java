package com.checkpoint.controller;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.model.GameStatus;
import com.checkpoint.model.User;
import com.checkpoint.service.BacklogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BacklogRestControllerTest {

    @Mock
    private BacklogService backlogService;

    @InjectMocks
    private BacklogRestController backlogRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(backlogRestController)
                .build();
    }

    @Test
    @DisplayName("GET /api/me/backlog -> 200 with backlog rows for authenticated user")
    void listBacklog_authenticated_returnsRows() throws Exception {
        User user = new User();
        user.setId(42L);
        user.setUsername("sean");

        when(backlogService.listBacklogForUser(42L)).thenReturn(List.of(
                new BacklogItemDto(1L, 10L, "Elden Ring", null, 2L, "PlayStation 5", GameStatus.IN_PROGRESS)
        ));

        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        mockMvc.perform(get("/api/me/backlog").principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$[0].platformName").value("PlayStation 5"))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));

        verify(backlogService).listBacklogForUser(42L);
    }

    @Test
    @DisplayName("GET /api/me/backlog -> 401 when request is not authenticated")
    void listBacklog_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/me/backlog"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(backlogService);
    }

    @Test
    @DisplayName("PUT /api/me/backlog/{id}/status -> 204 when item belongs to authenticated user")
    void updateBacklogItem_success_returns204() throws Exception {
        User user = new User();
        user.setId(42L);
        user.setUsername("sean");

        when(backlogService.updateStatus(42L, 9L, GameStatus.COMPLETED)).thenReturn(true);

        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        mockMvc.perform(put("/api/me/backlog/9/status")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNoContent());

        verify(backlogService).updateStatus(42L, 9L, GameStatus.COMPLETED);
    }

    @Test
    @DisplayName("PUT /api/me/backlog/{id}/status -> 404 when item does not belong to user")
    void updateBacklogItem_notFound_returns404() throws Exception {
        User user = new User();
        user.setId(42L);
        user.setUsername("sean");

        when(backlogService.updateStatus(42L, 9L, GameStatus.COMPLETED)).thenReturn(false);

        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        mockMvc.perform(put("/api/me/backlog/9/status")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/me/backlog/{id} -> 204 when item belongs to authenticated user")
    void removeFromBacklog_success_returns204() throws Exception {
        User user = new User();
        user.setId(42L);
        user.setUsername("sean");

        when(backlogService.removeFromBacklog(42L, 9L)).thenReturn(true);

        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        mockMvc.perform(delete("/api/me/backlog/9").principal(principal))
                .andExpect(status().isNoContent());

        verify(backlogService).removeFromBacklog(42L, 9L);
    }
}

