package com.checkpoint.security;

import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void testLoadUserByUsernameFound() {
        User user = new User();
        user.setUsername("sean");
        user.setPasswordHash("hash");
        user.setRole(Role.USER);

        when(userRepository.findByUsername("sean")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("sean");

        assertEquals("sean", userDetails.getUsername());
        assertEquals("hash", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsernameMissing() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost")
        );

        assertEquals("No user found with username: ghost", ex.getMessage());
    }
}

