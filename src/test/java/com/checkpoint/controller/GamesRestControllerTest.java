package com.checkpoint.controller;

import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.repository.GameRepository;
import com.checkpoint.validation.GameDomainValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@Getter
class GamesRestControllerTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private GameDomainValidator gameDomainValidator;

    @InjectMocks
    private GamesRestController gamesRestController;

    private MockMvc mockMvc;
    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(gamesRestController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/games -> 401 when request is unauthenticated")
    void listGames_unauthenticated_returns401() {
        ResponseEntity<?> response = gamesRestController.listGames(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verifyNoInteractions(gameRepository);
    }

    @Test
    @DisplayName("GET /api/games -> 200 and sorted platform names for authenticated user")
    void listGames_authenticated_returnsDtos() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("sean");
        user.setRole(Role.USER);

        Platform ps5 = platform(2L, "PlayStation 5");
        Platform pc = platform(1L, "PC");

        Game game = new Game();
        game.setId(10L);
        game.setTitle("Elden Ring");
        game.setCoverArtUrl("cover.png");
        game.setReleaseYear(2022);
        game.setPlatforms(new LinkedHashSet<>(List.of(ps5, pc)));

        when(gameRepository.findAllWithPlatforms()).thenReturn(List.of(game));

        ResponseEntity<?> response = gamesRestController.listGames(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        List<com.checkpoint.dto.GameDto> body = (List<com.checkpoint.dto.GameDto>) response.getBody();
        assertEquals(1, body.size());
        assertEquals("Elden Ring", body.getFirst().getTitle());
        assertEquals(List.of("PC", "PlayStation 5"), body.getFirst().getPlatforms().stream().map(p -> p.getName()).toList());
    }

    @Test
    void testGetGameByIdMapping() throws Exception {
        Platform switchPlatform = platform(2L, "Switch");
        Platform pc = platform(1L, "PC");

        Game game = new Game();
        game.setId(44L);
        game.setTitle("Balatro");
        game.setCoverArtUrl("cover.jpg");
        game.setReleaseYear(2024);
        game.setPlatforms(new LinkedHashSet<>(List.of(switchPlatform, pc)));

        when(gameRepository.findById(44L)).thenReturn(Optional.of(game));

        ResponseEntity<com.checkpoint.dto.GameDto> response = gamesRestController.getGame(44L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Balatro", response.getBody().getTitle());
        assertEquals(List.of("PC", "Switch"), response.getBody().getPlatforms().stream().map(p -> p.getName()).toList());
    }

    @Test
    @DisplayName("POST /api/games -> 201 when request is valid and title is unique")
    void createGame_success_returns201() throws Exception {
        Platform pc = platform(1L, "PC");
        Platform ps5 = platform(2L, "PlayStation 5");

        when(gameDomainValidator.resolvePlatforms(any(), eq(true))).thenReturn(Set.of(pc, ps5));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> {
            Game g = invocation.getArgument(0);
            g.setId(99L);
            return g;
        });

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "  Hades II  ",
                                "coverArtUrl", "",
                                "releaseYear", 2024,
                                "platformIds", List.of(1, 2)
                        ))))
                .andExpect(status().isCreated());

        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());

        Game saved = captor.getValue();
        assertEquals("Hades II", saved.getTitle());
        assertEquals(2024, saved.getReleaseYear());
        assertEquals(2, saved.getPlatforms().size());
    }

    @Test
    @DisplayName("POST /api/games -> 400 when title is blank")
    void createGame_blankTitle_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "   ",
                                "releaseYear", 2024,
                                "platformIds", List.of(1)
                        ))))
                .andExpect(status().isBadRequest());

        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/games -> 400 when no platforms are selected")
    void createGame_missingPlatforms_returns400() throws Exception {
        when(gameDomainValidator.resolvePlatforms(any(), eq(true)))
                .thenThrow(new DomainException(ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "Metaphor ReFantazio",
                                "releaseYear", 2024
                        ))))
                .andExpect(status().isBadRequest());

        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("POST /api/games -> 409 when game title already exists")
    void createGame_duplicateTitle_returns409() throws Exception {
        Platform pc = platform(1L, "PC");
        when(gameDomainValidator.resolvePlatforms(any(), eq(true))).thenReturn(Set.of(pc));
        when(gameRepository.save(any(Game.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "Elden Ring",
                                "releaseYear", 2022,
                                "platformIds", List.of(1)
                        ))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/games/{id} -> 200 when game exists and payload is valid")
    void updateGame_success_returns200() throws Exception {
        Game existing = new Game();
        existing.setId(15L);
        existing.setTitle("Old Title");
        existing.setReleaseYear(2000);

        when(gameRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameDomainValidator.resolvePlatforms(any(), eq(false))).thenReturn(Set.of());

        mockMvc.perform(put("/api/games/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "New Title",
                                "releaseYear", 2024
                        ))))
                .andExpect(status().isOk());

        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        assertEquals("New Title", captor.getValue().getTitle());
        assertEquals(2024, captor.getValue().getReleaseYear());
    }

    @Test
    @DisplayName("PUT /api/games/{id} -> 404 when game does not exist")
    void updateGame_missing_returns404() throws Exception {
        when(gameRepository.findById(15L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/games/15")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of(
                                "title", "New Title",
                                "releaseYear", 2024
                        ))))
                .andExpect(status().isNotFound());

        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("DELETE /api/games/{id} -> 204 when game exists")
    void deleteGame_existing_returns204() {
        when(gameRepository.existsById(77L)).thenReturn(true);

        ResponseEntity<Void> response = gamesRestController.deleteGame(77L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(gameRepository).deleteById(77L);
    }

    @Test
    @DisplayName("DELETE /api/games/{id} -> 404 when game does not exist")
    void deleteGame_missing_returns404() {
        when(gameRepository.existsById(77L)).thenReturn(false);

        ResponseEntity<Void> response = gamesRestController.deleteGame(77L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(gameRepository, never()).deleteById(any());
    }

    private Platform platform(Long id, String name) throws Exception {
        Platform p = new Platform();
        Field idField = Platform.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(p, id);

        Field nameField = Platform.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(p, name);
        return p;
    }
}


