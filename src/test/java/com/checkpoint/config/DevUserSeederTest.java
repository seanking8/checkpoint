package com.checkpoint.config;

import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevUserSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DevUserSeeder devUserSeeder;

    @Test
    void testRunSkipsWhenCredentialsBlank() throws Exception {
        setField("userUsername", "dev_user");
        setField("userPassword", "");
        setField("adminUsername", "dev_admin");
        setField("adminPassword", "admin-pass");

        devUserSeeder.run(null);

        verify(userRepository, never()).existsByUsername("dev_user");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void testRunSkipsExistingUserAndSeedsAdmin() throws Exception {
        setField("userUsername", "dev_user");
        setField("userPassword", "user-pass");
        setField("adminUsername", "dev_admin");
        setField("adminPassword", "admin-pass");

        when(userRepository.existsByUsername("dev_user")).thenReturn(true);
        when(userRepository.existsByUsername("dev_admin")).thenReturn(false);
        when(passwordEncoder.encode("admin-pass")).thenReturn("encoded-admin");

        devUserSeeder.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());

        User saved = captor.getValue();
        assertEquals("dev_admin", saved.getUsername());
        assertEquals("encoded-admin", saved.getPasswordHash());
        assertEquals(Role.ADMIN, saved.getRole());
    }

    @Test
    void testRunSeedsBothUsersWhenMissing() throws Exception {
        setField("userUsername", "dev_user");
        setField("userPassword", "user-pass");
        setField("adminUsername", "dev_admin");
        setField("adminPassword", "admin-pass");

        when(userRepository.existsByUsername("dev_user")).thenReturn(false);
        when(userRepository.existsByUsername("dev_admin")).thenReturn(false);
        when(passwordEncoder.encode("user-pass")).thenReturn("encoded-user");
        when(passwordEncoder.encode("admin-pass")).thenReturn("encoded-admin");

        devUserSeeder.run(null);

        verify(userRepository, times(2)).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    private void setField(String name, String value) throws Exception {
        Field field = DevUserSeeder.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(devUserSeeder, value);
    }
}

