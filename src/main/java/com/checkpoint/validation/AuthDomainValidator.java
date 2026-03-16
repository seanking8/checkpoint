package com.checkpoint.validation;

import com.checkpoint.error.DomainException;
import com.checkpoint.error.ErrorCode;
import com.checkpoint.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class AuthDomainValidator {

    private final UserRepository userRepository;

    public AuthDomainValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void assertUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new DomainException(ErrorCode.USERNAME_TAKEN);
        }
    }
}

