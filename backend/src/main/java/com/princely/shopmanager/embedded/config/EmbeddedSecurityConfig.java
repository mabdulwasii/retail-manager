package com.princely.shopmanager.embedded.config;

import com.princely.shopmanager.auth.config.AppSecurityConfig;
import com.princely.shopmanager.embedded.security.JwtAuthenticationFilter;
import com.princely.shopmanager.embedded.security.JwtTokenProvider;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

/**
 * Security configuration for embedded mode.
 * Replaces Keycloak with local JWT authentication.
 */
@Configuration
@Profile("embedded")
@EnableWebSecurity
@EnableMethodSecurity
@ConditionalOnProperty(name = "application.mode", havingValue = "embedded", matchIfMissing = true)
@RequiredArgsConstructor
public class EmbeddedSecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final AppSecurityConfig appSecurityConfig;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Frontend static resources and auth
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/assets/**",
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/actuator/health",
                    "/actuator/info",
                    "/h2-console/**",
                    "/error"
                ).permitAll()
                // All API requests require authentication
                .requestMatchers("/api/**").authenticated()
                // All other requests (SPA routes) are public
                .anyRequest().permitAll()
            )
            .addFilterBefore(
                new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class
            );

        // Allow H2 console access in embedded mode
        http.headers(headers -> headers
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        AppSecurityConfig.Cors corsConfig = appSecurityConfig.getCors();
        configuration.setAllowedOrigins(corsConfig.getAllowedOrigins());
        configuration.setAllowedMethods(corsConfig.getAllowedMethods());
        configuration.setAllowedHeaders(corsConfig.getAllowedHeaders());
        configuration.setAllowCredentials(corsConfig.isAllowCredentials());
        configuration.setMaxAge(corsConfig.getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
