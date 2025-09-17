package com.princely.shopmanager.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Security configuration specifically for Swagger UI access control.
 *
 * This configuration provides:
 * - Basic authentication for Swagger UI endpoints
 * - Separate security filter chain for documentation access
 * - Configurable credentials for API documentation access
 * - Protection against unauthorized API documentation viewing
 *
 * The Swagger UI is protected with basic authentication to prevent
 * unauthorized access to API documentation in production environments.
 */
@Configuration
@EnableWebSecurity
public class SwaggerSecurityConfig {

    @Value("${app.swagger.security.username:swagger-admin}")
    private String swaggerUsername;

    // SECURITY: Use secure password - override in production environments
    // This must match the default in application.yml
    @Value("${app.swagger.security.password:Sw@gg3r!SecureP@ss2024#}")
    private String swaggerPassword;

    @Value("${app.swagger.security.enabled:true}")
    private boolean swaggerSecurityEnabled;

    /**
     * Security filter chain specifically for Swagger UI and API documentation endpoints.
     *
     * This configuration:
     * - Applies only to Swagger-related URLs
     * - Uses basic authentication for access control
     * - Has higher precedence than main security configuration
     * - Provides separate authentication for documentation access
     *
     * @param http HttpSecurity configuration
     * @return SecurityFilterChain for Swagger endpoints
     * @throws Exception if configuration fails
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        if (!swaggerSecurityEnabled) {
            // If Swagger security is disabled, allow unrestricted access
            http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                .authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll()
                )
                .csrf(csrf -> csrf.disable());
        } else {
            // Enable basic authentication for Swagger access
            http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html")
                .authorizeHttpRequests(authz -> authz
                    .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                    .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));
        }

        return http.build();
    }

    /**
     * User details service for Swagger UI authentication.
     *
     * Creates an in-memory user store with credentials for accessing
     * the API documentation. The credentials can be configured via
     * application properties.
     *
     * @return UserDetailsService with Swagger access credentials
     */
    @Bean
    public UserDetailsService swaggerUserDetailsService() {
        if (!swaggerSecurityEnabled) {
            // Return empty user details service if security is disabled
            return new InMemoryUserDetailsManager();
        }

        return new InMemoryUserDetailsManager(
            User.builder()
                .username(swaggerUsername)
                .password(passwordEncoder().encode(swaggerPassword))
                .roles("SWAGGER_ADMIN")
                .build()
        );
    }

    /**
     * Password encoder for Swagger authentication.
     *
     * Uses BCrypt encoding for secure password storage.
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}