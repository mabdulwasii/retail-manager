package com.princely.shopmanager.embedded.config;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * JPA configuration for embedded mode.
 *
 * <p>This configuration provides JPA auditing for embedded deployments.
 * DataSource is automatically configured by {@link EmbeddedPostgreSQLConfig}.
 * JPA repositories are auto-configured by Spring Boot.
 *
 * <p>Key differences from cloud mode:
 * <ul>
 *   <li>Single embedded PostgreSQL instance (managed by EmbeddedPostgreSQLConfig)</li>
 *   <li>No multi-entity-manager setup</li>
 *   <li>Same PostgreSQL dialect as production (easy migration path)</li>
 *   <li>File-based persistence in ./data/postgres directory</li>
 * </ul>
 */
@Configuration
@Profile("embedded")
@EnableTransactionManagement
public class EmbeddedJpaConfig {

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
