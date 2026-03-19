package com.checkpoint.config;

import com.checkpoint.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@ConditionalOnProperty(prefix = "app.test-seed", name = "enabled", havingValue = "true")
public class TestUserSeeder extends BaseUserSeeder {

    @Value("${app.test-seed.user.username:test_user}")
    private String userUsername;

    @Value("${app.test-seed.user.password:test_user_password}")
    private String userPassword;

    @Value("${app.test-seed.admin.username:test_admin}")
    private String adminUsername;

    @Value("${app.test-seed.admin.password:test_admin_password}")
    private String adminPassword;

    public TestUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
    }

    @Override protected String getUserUsername() { return userUsername; }
    @Override protected String getUserPassword() { return userPassword; }
    @Override protected String getAdminUsername() { return adminUsername; }
    @Override protected String getAdminPassword() { return adminPassword; }
    @Override protected String getEnvironmentName() { return "test"; }
}