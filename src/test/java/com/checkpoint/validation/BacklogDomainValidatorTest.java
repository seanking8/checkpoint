package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Game;
import com.checkpoint.model.Platform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BacklogDomainValidatorTest {

    private final BacklogDomainValidator validator = new BacklogDomainValidator();

    @Test
    @DisplayName("assertGameAvailableOnPlatform passes when platform is linked")
    void assertGameAvailableOnPlatform_linked_passes() throws Exception {
        Platform platform = platform(2L);
        Game game = new Game();
        game.setPlatforms(Set.of(platform));

        assertDoesNotThrow(() -> validator.assertGameAvailableOnPlatform(game, platform));
    }

    @Test
    @DisplayName("assertGameAvailableOnPlatform throws when platform is not linked")
    void assertGameAvailableOnPlatform_notLinked_throws() throws Exception {
        Platform linked = platform(1L);
        Platform selected = platform(2L);

        Game game = new Game();
        game.setPlatforms(Set.of(linked));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> validator.assertGameAvailableOnPlatform(game, selected)
        );

        assertEquals(ErrorCode.GAME_NOT_AVAILABLE_ON_PLATFORM, exception.getErrorCode());
    }

    @Test
    @DisplayName("assertNotAlreadyInBacklog throws when duplicate exists")
    void assertNotAlreadyInBacklog_duplicate_throws() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> validator.assertNotAlreadyInBacklog(true)
        );

        assertEquals(ErrorCode.GAME_ALREADY_IN_BACKLOG, exception.getErrorCode());
    }

    @Test
    @DisplayName("assertNotAlreadyInBacklog passes when duplicate does not exist")
    void assertNotAlreadyInBacklog_notDuplicate_passes() {
        assertDoesNotThrow(() -> validator.assertNotAlreadyInBacklog(false));
    }

    private Platform platform(Long id) throws Exception {
        Platform platform = new Platform();
        Field idField = Platform.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(platform, id);
        return platform;
    }
}

