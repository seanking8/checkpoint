package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthDomainValidatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthDomainValidator validator;

    @Test
    @DisplayName("assertUsernameAvailable throws USERNAME_TAKEN when username exists")
    void assertUsernameAvailable_existingUsername_throws() {
        when(userRepository.existsByUsername("sean")).thenReturn(true);

        DomainException exception = assertThrows(
                DomainException.class,
                () -> validator.assertUsernameAvailable("sean")
        );

        assertEquals(ErrorCode.USERNAME_TAKEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("assertUsernameAvailable does not throw when username is free")
    void assertUsernameAvailable_freeUsername_passes() {
        when(userRepository.existsByUsername("new_user")).thenReturn(false);

        assertDoesNotThrow(() -> validator.assertUsernameAvailable("new_user"));
    }
}

