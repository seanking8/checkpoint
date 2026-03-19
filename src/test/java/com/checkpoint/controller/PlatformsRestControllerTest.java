package com.checkpoint.controller;

import com.checkpoint.model.Platform;
import com.checkpoint.repository.PlatformRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformsRestControllerTest {

    @Mock
    private PlatformRepository platformRepository;

    @InjectMocks
    private PlatformsRestController platformsRestController;

    @Test
    void testListPlatformsSorted() throws Exception {
        Platform xbox = platform(3L, "Xbox");
        Platform ps5 = platform(2L, "playstation 5");
        Platform pc = platform(1L, "PC");

        when(platformRepository.findAll()).thenReturn(List.of(xbox, ps5, pc));

        ResponseEntity<Iterable<Platform>> response = platformsRestController.listPlatforms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        List<Platform> body = (List<Platform>) response.getBody();
        assertEquals(List.of("PC", "playstation 5", "Xbox"), body.stream().map(Platform::getName).toList());
    }

    @Test
    void testGetPlatformFound() throws Exception {
        Platform pc = platform(1L, "PC");
        when(platformRepository.findById(1L)).thenReturn(Optional.of(pc));

        ResponseEntity<Platform> response = platformsRestController.getPlatform(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PC", response.getBody().getName());
    }

    @Test
    void testGetPlatformMissing() {
        when(platformRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Platform> response = platformsRestController.getPlatform(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

