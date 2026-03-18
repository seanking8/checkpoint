package com.checkpoint.config;

import com.checkpoint.security.JwtAuthFilter;
import com.checkpoint.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Stateless JWT-based security configuration
@Configuration
@EnableMethodSecurity          // enables @PreAuthorize on controller methods
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String GAMES_API_PATTERN = "/api/games/**";
    private static final String PLATFORMS_API_PATTERN = "/api/platforms/**";
    private static final String ADMIN_ROLE = "ADMIN";

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint entryPoint;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        try {
            http
                // Disable CSRF — not needed for stateless JWT APIs
                .csrf(csrf -> csrf.disable())

                // No HTTP session — each request is fully self-contained via its token
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Public auth endpoints
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                        // API docs are public
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Static frontend assets are public
                        .requestMatchers("/", "/index.html", "/app.js", "/styles.css", "/vendor/**", "/images/**").permitAll()

                        // Read-only game and platform browsing is open to any authenticated user
                        .requestMatchers(HttpMethod.GET, GAMES_API_PATTERN).authenticated()
                        .requestMatchers(HttpMethod.GET, PLATFORMS_API_PATTERN).authenticated()

                        // Write operations on catalog and platforms require ADMIN
                        .requestMatchers(HttpMethod.POST,   GAMES_API_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT,    GAMES_API_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, GAMES_API_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST,   PLATFORMS_API_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.PUT,    PLATFORMS_API_PATTERN).hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, PLATFORMS_API_PATTERN).hasRole(ADMIN_ROLE)

                        // User management is admin-only
                        .requestMatchers("/api/admin/**").hasRole(ADMIN_ROLE)

                        // All other requests (backlog etc.) require authentication
                        .anyRequest().authenticated()
                )

                // Register the JWT filter. runs before Spring's built-in username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // Return a clean 401 JSON response instead of Spring's default HTML error page
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));

            return http.build();
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to build SecurityFilterChain", ex);
        }
    }

    // DaoAuthenticationProvider wires together DB-backed UserDetailsService and BCrypt encoder so Spring Security can verify passwords
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Boot 4: UserDetailsService is passed via constructor, not a setter
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Exposes the AuthenticationManager as a bean so AuthRestController can call authenticate() directly during login
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        try {
            return config.getAuthenticationManager();
        } catch (Exception ex) {
            throw new BeanCreationException("Failed to create AuthenticationManager", ex);
        }
    }

    // BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}