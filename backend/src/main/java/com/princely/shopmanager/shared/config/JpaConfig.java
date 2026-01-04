package com.princely.shopmanager.shared.config;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Configuration for auditing.
 * We don't use @EnableJpaRepositories - Spring Boot autoconfigures it.
 */
@Configuration
@Profile("!embedded")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }

            // Extract username from JwtPrincipal if available
            // This prevents storing the entire principal toString() which can exceed VARCHAR(255)
            Object principal = authentication.getPrincipal();
            if (principal instanceof JwtPrincipal jwtPrincipal) {
                return Optional.of(jwtPrincipal.getUsername());
            }

            // Fallback to authentication name for other principal types
            return Optional.of(authentication.getName());
        }
    }
}