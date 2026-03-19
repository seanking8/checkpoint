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
class TestUserSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TestUserSeeder testUserSeeder;

    @Test
    void testRunSkipsWhenCredentialsBlank() throws Exception {
        setField("userUsername", "   ");
        setField("userPassword", "user-pass");
        setField("adminUsername", "admin");
        setField("adminPassword", "admin-pass");

        testUserSeeder.run(null);

        verify(userRepository, never()).existsByUsername("admin");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void testRunSkipsWhenUsersAlreadyExist() throws Exception {
        setField("userUsername", "test_user");
        setField("userPassword", "user-pass");
        setField("adminUsername", "test_admin");
        setField("adminPassword", "admin-pass");

        when(userRepository.existsByUsername("test_user")).thenReturn(true);
        when(userRepository.existsByUsername("test_admin")).thenReturn(true);

        testUserSeeder.run(null);

        verify(userRepository, times(1)).existsByUsername("test_user");
        verify(userRepository, times(1)).existsByUsername("test_admin");
        verify(passwordEncoder, never()).encode("user-pass");
        verify(passwordEncoder, never()).encode("admin-pass");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void testRunSeedsMissingUsers() throws Exception {
        setField("userUsername", " test_user ");
        setField("userPassword", "user-pass");
        setField("adminUsername", "test_admin");
        setField("adminPassword", "admin-pass");

        when(userRepository.existsByUsername("test_user")).thenReturn(false);
        when(userRepository.existsByUsername("test_admin")).thenReturn(false);
        when(passwordEncoder.encode("user-pass")).thenReturn("encoded-user");
        when(passwordEncoder.encode("admin-pass")).thenReturn("encoded-admin");

        testUserSeeder.run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(2)).save(captor.capture());

        User firstSaved = captor.getAllValues().get(0);
        User secondSaved = captor.getAllValues().get(1);

        assertEquals("test_user", firstSaved.getUsername());
        assertEquals("encoded-user", firstSaved.getPasswordHash());
        assertEquals(Role.USER, firstSaved.getRole());

        assertEquals("test_admin", secondSaved.getUsername());
        assertEquals("encoded-admin", secondSaved.getPasswordHash());
        assertEquals(Role.ADMIN, secondSaved.getRole());
        assertEquals(2, captor.getAllValues().size());
    }

    private void setField(String name, String value) throws Exception {
        Field field = TestUserSeeder.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(testUserSeeder, value);
    }
}

