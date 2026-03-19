package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.model.Platform;
import com.checkpoint.repository.PlatformRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameDomainValidatorTest {

    @Mock
    private PlatformRepository platformRepository;

    @InjectMocks
    private GameDomainValidator validator;

    @Test
    @DisplayName("resolvePlatforms throws when required and platformIds is null")
    void resolvePlatforms_requiredAndNull_throws() {
        List<Long> platformIds = null;
        Executable action = () -> validator.resolvePlatforms(platformIds, true);

        DomainException exception = assertThrows(
                DomainException.class,
                action
        );

        assertEquals(ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("resolvePlatforms returns empty set when optional and platformIds is null")
    void resolvePlatforms_optionalAndNull_returnsEmpty() {
        Set<Platform> result = validator.resolvePlatforms(null, false);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("resolvePlatforms throws when all requested ids are invalid")
    void resolvePlatforms_onlyInvalidIds_throws() {
        List<Long> platformIds = List.of(0L, -1L);
        Executable action = () -> validator.resolvePlatforms(platformIds, true);

        DomainException exception = assertThrows(
                DomainException.class,
                action
        );

        assertEquals(ErrorCode.AT_LEAST_ONE_PLATFORM_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("resolvePlatforms throws when selected platform ids include unknown ids")
    void resolvePlatforms_unknownPlatform_throws() throws Exception {
        Platform p1 = platform(1L, "PC");
        when(platformRepository.findAllById(any())).thenReturn(List.of(p1));

        List<Long> platformIds = List.of(1L, 2L);
        Executable action = () -> validator.resolvePlatforms(platformIds, true);

        DomainException exception = assertThrows(
                DomainException.class,
                action
        );

        assertEquals(ErrorCode.INVALID_PLATFORM_SELECTION, exception.getErrorCode());
    }

    @Test
    @DisplayName("resolvePlatforms returns resolved platform set when ids are valid")
    void resolvePlatforms_validIds_returnsSet() throws Exception {
        Platform p1 = platform(1L, "PC");
        Platform p2 = platform(2L, "PlayStation 5");
        when(platformRepository.findAllById(any())).thenReturn(List.of(p1, p2));

        Set<Platform> result = validator.resolvePlatforms(List.of(1L, 2L, 2L), true);

        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    private Platform platform(Long id, String name) throws Exception {
        Platform platform = new Platform();

        Field idField = Platform.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(platform, id);

        Field nameField = Platform.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(platform, name);

        return platform;
    }
}


