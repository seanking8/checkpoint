package com.checkpoint.service;

import com.checkpoint.dto.BacklogItemDto;
import com.checkpoint.model.GameStatus;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
}


