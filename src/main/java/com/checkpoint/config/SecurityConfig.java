package com.checkpoint.config;

import com.checkpoint.security.JwtAuthFilter;
import com.checkpoint.security.JwtAuthenticationEntryPoint;
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
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint entryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          UserDetailsService userDetailsService,
                          JwtAuthenticationEntryPoint entryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.entryPoint = entryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — not needed for stateless JWT APIs
            .csrf(csrf -> csrf.disable())

            // No HTTP session — each request is fully self-contained via its token
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    // Auth endpoints are public — anyone can register or login
                    .requestMatchers("/api/auth/**").permitAll()

                    // Static frontend assets are public
                    .requestMatchers("/", "/index.html", "/app.js", "/styles.css").permitAll()

                    // Read-only game and platform browsing is open to any authenticated user
                    .requestMatchers(HttpMethod.GET, "/api/games/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/platforms/**").authenticated()

                    // Write operations on catalog and platforms require ADMIN
                    .requestMatchers(HttpMethod.POST,   "/api/games/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/games/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/games/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST,   "/api/platforms/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT,    "/api/platforms/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/platforms/**").hasRole("ADMIN")

                    // User management is admin-only
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // All other requests (backlog etc.) require authentication
                    .anyRequest().authenticated()
            )

            // Register the JWT filter — runs before Spring's built-in username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Return a clean 401 JSON response instead of Spring's default HTML error page
            .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint));

        return http.build();
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
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    // BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}