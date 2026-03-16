package com.checkpoint.service;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.model.Game;
import com.checkpoint.model.GameStatus;
import com.checkpoint.model.Platform;
import com.checkpoint.model.User;
import com.checkpoint.model.UserGame;
import com.checkpoint.repository.GameRepository;
import com.checkpoint.repository.PlatformRepository;
import com.checkpoint.repository.UserGameRepository;
import com.checkpoint.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BacklogServiceTest {

    @Mock
    private UserGameRepository userGameRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BacklogService backlogService;

    @Test
    @DisplayName("listBacklogForUser delegates to repository projection query")
    void listBacklogForUser_returnsRepositoryRows() {
        List<BacklogItemDto> expected = List.of(
                new BacklogItemDto(1L, 3L, "Hades", null, 1L, "PC", GameStatus.COMPLETED)
        );
        when(userGameRepository.findBacklogItemsByUserId(7L)).thenReturn(expected);

        List<BacklogItemDto> actual = backlogService.listBacklogForUser(7L);

        assertEquals(1, actual.size());
        assertEquals("Hades", actual.getFirst().getGameTitle());
        assertEquals("PC", actual.getFirst().getPlatformName());
        assertEquals(GameStatus.COMPLETED, actual.getFirst().getStatus());
        verify(userGameRepository).findBacklogItemsByUserId(7L);
    }

    @Test
    @DisplayName("updateStatus returns true when one row is updated")
    void updateStatus_returnsTrueWhenUpdated() {
        when(userGameRepository.updateStatusByIdAndUserId(11L, 7L, GameStatus.IN_PROGRESS)).thenReturn(1);

        boolean updated = backlogService.updateStatus(7L, 11L, GameStatus.IN_PROGRESS);

        assertTrue(updated);
        verify(userGameRepository).updateStatusByIdAndUserId(11L, 7L, GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("removeFromBacklog returns false when no row is deleted")
    void removeFromBacklog_returnsFalseWhenMissing() {
        when(userGameRepository.deleteByIdAndUserId(11L, 7L)).thenReturn(0L);

        boolean deleted = backlogService.removeFromBacklog(7L, 11L);

        assertFalse(deleted);
        verify(userGameRepository).deleteByIdAndUserId(11L, 7L);
    }

    @Test
    @DisplayName("addToBacklog saves a new entry with default WANT_TO_PLAY status")
    void addToBacklog_setsDefaultStatusAndSaves() throws Exception {
        User user = new User();
        user.setId(7L);

        Platform platform = new Platform();
        Field platformId = Platform.class.getDeclaredField("id");
        platformId.setAccessible(true);
        platformId.set(platform, 2L);

        Game game = new Game();
        game.setId(11L);
        game.setPlatforms(Set.of(platform));

        when(gameRepository.findById(11L)).thenReturn(Optional.of(game));
        when(platformRepository.findById(2L)).thenReturn(Optional.of(platform));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userGameRepository.existsByUserIdAndGameIdAndPlatformId(7L, 11L, 2L)).thenReturn(false);

        backlogService.addToBacklog(7L, 11L, 2L);

        ArgumentCaptor<UserGame> captor = ArgumentCaptor.forClass(UserGame.class);
        verify(userGameRepository).save(captor.capture());

        UserGame saved = captor.getValue();
        assertNotNull(saved);

        Field statusField = UserGame.class.getDeclaredField("status");
        statusField.setAccessible(true);
        assertEquals(GameStatus.WANT_TO_PLAY, statusField.get(saved));
    }

    @Test
    @DisplayName("addToBacklog throws IllegalStateException when game/platform already exists for user")
    void addToBacklog_duplicate_throwsIllegalStateException() {
        User user = new User();
        user.setId(7L);

        Platform platform = new Platform();
        Game game = new Game();
        game.setId(11L);
        game.setPlatforms(Set.of(platform));

        when(gameRepository.findById(11L)).thenReturn(Optional.of(game));
        when(platformRepository.findById(2L)).thenReturn(Optional.of(platform));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userGameRepository.existsByUserIdAndGameIdAndPlatformId(7L, 11L, 2L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> backlogService.addToBacklog(7L, 11L, 2L));
    }

    @Test
    @DisplayName("addToBacklog throws IllegalArgumentException when game is not available on selected platform")
    void addToBacklog_platformNotLinked_throwsIllegalArgumentException() {
        User user = new User();
        user.setId(7L);

        Platform linkedPlatform = new Platform();
        Platform selectedPlatform = new Platform();
        Game game = new Game();
        game.setId(11L);
        game.setPlatforms(Set.of(linkedPlatform));

        when(gameRepository.findById(11L)).thenReturn(Optional.of(game));
        when(platformRepository.findById(2L)).thenReturn(Optional.of(selectedPlatform));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> backlogService.addToBacklog(7L, 11L, 2L));
    }
}


