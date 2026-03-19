package com.checkpoint.config;

import com.checkpoint.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.dev-seed", name = "enabled", havingValue = "true")
public class DevUserSeeder extends BaseUserSeeder {

    @Value("${app.dev-seed.user.username:dev_user}")
    private String userUsername;

    @Value("${app.dev-seed.user.password:dev_user_password}")
    private String userPassword;

    @Value("${app.dev-seed.admin.username:dev_admin}")
    private String adminUsername;

    @Value("${app.dev-seed.admin.password:dev_admin_password}")
    private String adminPassword;

    public DevUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
    }

    @Override protected String getUserUsername() { return userUsername; }
    @Override protected String getUserPassword() { return userPassword; }
    @Override protected String getAdminUsername() { return adminUsername; }
    @Override protected String getAdminPassword() { return adminPassword; }
    @Override protected String getEnvironmentName() { return "dev"; }
}