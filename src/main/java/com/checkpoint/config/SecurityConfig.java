package com.checkpoint.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/styles.css", "/script.js").permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .defaultSuccessUrl("/", true)
                )

                .logout(logout -> logout.logoutSuccessUrl("/login?logout"));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {

        UserDetails viewer = User.withUsername("viewer")
                .password("{noop}viewer123")
                .roles("VIEW")
                .build();

        UserDetails editor = User.withUsername("editor")
                .password("{noop}editor123")
                .roles("EDIT")
                .build();

        return new InMemoryUserDetailsManager(viewer, editor);
    }
}