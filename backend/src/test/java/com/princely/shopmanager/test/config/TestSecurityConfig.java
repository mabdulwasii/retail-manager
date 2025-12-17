package com.princely.shopmanager.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration that disables OAuth2 JWT validation.
 * <p>
 * This configuration allows integration tests to use @WithMockPermissions
 * and mock JWT tokens without requiring a running Keycloak server.
 * <p>
 * This configuration is activated only in the 'test' profile.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("test")
public class TestSecurityConfig {

    /**
     * Configures security filter chain for tests.
     * <p>
     * This configuration:
     * - Permits all requests (authentication is mocked via @WithMockPermissions)
     * - Disables CSRF (not needed for stateless API tests)
     * - Uses stateless session management
     * - Does NOT configure OAuth2 JWT validation (allows mock tokens)
     *
     * @param http HttpSecurity configuration
     * @return Security filter chain
     * @throws Exception if configuration fails
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());  // Permit all - authentication mocked in tests

        return http.build();
    }
}