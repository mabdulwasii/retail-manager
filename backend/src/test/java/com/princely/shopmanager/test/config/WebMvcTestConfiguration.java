package com.princely.shopmanager.test.config;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Minimal test configuration for controller tests.
 * Excludes JPA and data-related configurations that are not needed for web layer testing.
 * Configures method security for @PreAuthorize testing.
 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {
    org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
    org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
    org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,
    org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration.class
})
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebMvcTestConfiguration {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.getWriter().write("Unauthorized");
                })
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(403);
                    response.getWriter().write("Forbidden");
                })
            )
            .build();
    }

    @ControllerAdvice
    public static class TestExceptionHandler {

        @ExceptionHandler(AuthorizationDeniedException.class)
        public void handleAuthorizationDenied(HttpServletResponse response) throws IOException {
            response.setStatus(403);
            response.getWriter().write("Forbidden");
        }

        @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
        public void handleAuthenticationMissing(HttpServletResponse response) throws IOException {
            response.setStatus(401);
            response.getWriter().write("Unauthorized");
        }
    }
}