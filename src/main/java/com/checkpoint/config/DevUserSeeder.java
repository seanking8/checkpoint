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
@Profile("dev")
@ConditionalOnProperty(prefix = "app.dev-seed", name = "enabled", havingValue = "true")
public class DevUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev-seed.user.username:dev_user}")
    private String userUsername;

    @Value("${app.dev-seed.user.password:dev_user_password}")
    private String userPassword;

    @Value("${app.dev-seed.admin.username:dev_admin}")
    private String adminUsername;

    @Value("${app.dev-seed.admin.password:dev_admin_password}")
    private String adminPassword;

    public DevUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(userUsername) || isBlank(userPassword) || isBlank(adminUsername) || isBlank(adminPassword)) {
            log.warn("Skipping dev user seeding because one or more credentials are blank.");
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

        log.info("Seeded dev account '{}' with role {}", username, role);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

