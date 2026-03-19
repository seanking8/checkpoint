package com.checkpoint.config;

import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

public abstract class BaseUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BaseUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    protected BaseUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Abstract methods to let sub-classes provide environment-specific values
    protected abstract String getUserUsername();
    protected abstract String getUserPassword();
    protected abstract String getAdminUsername();
    protected abstract String getAdminPassword();
    protected abstract String getEnvironmentName();

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(getUserUsername()) || isBlank(getUserPassword()) || isBlank(getAdminUsername()) || isBlank(getAdminPassword())) {
            log.warn("Skipping {} user seeding because one or more credentials are blank.", getEnvironmentName());
            return;
        }

        seedIfMissing(getUserUsername().trim(), getUserPassword(), Role.USER);
        seedIfMissing(getAdminUsername().trim(), getAdminPassword(), Role.ADMIN);
    }

    private void seedIfMissing(String username, String rawPassword, Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);

        log.info("Seeded {} account '{}' with role {}", getEnvironmentName(), username, role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}