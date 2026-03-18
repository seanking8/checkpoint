package com.checkpoint.config;

import com.checkpoint.model.Role;
import com.checkpoint.model.User;
import com.checkpoint.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@ConditionalOnProperty(prefix = "app.test-seed", name = "enabled", havingValue = "true")
public class TestUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.test-seed.user.username:test_user}")
    private String userUsername;

    @Value("${app.test-seed.user.password:test_user_password}")
    private String userPassword;

    @Value("${app.test-seed.admin.username:test_admin}")
    private String adminUsername;

    @Value("${app.test-seed.admin.password:test_admin_password}")
    private String adminPassword;

    public TestUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(userUsername) || isBlank(userPassword) || isBlank(adminUsername) || isBlank(adminPassword)) {
            log.warn("Skipping test user seeding because one or more credentials are blank.");
            return;
        }

        seedIfMissing(userUsername.trim(), userPassword, Role.USER);
        seedIfMissing(adminUsername.trim(), adminPassword, Role.ADMIN);
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

        log.info("Seeded test account '{}' with role {}", username, role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

