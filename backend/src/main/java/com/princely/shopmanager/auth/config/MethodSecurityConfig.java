package com.princely.shopmanager.auth.config;

import com.princely.shopmanager.auth.security.CustomPermissionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration for method-level security with custom permission evaluation.
 *
 * This configuration enables:
 * - Method security with @PreAuthorize and @PostAuthorize annotations
 * - Custom permission evaluator for database-backed permission checks
 * - Expression-based access control with hasPermission() support
 *
 * Usage in controllers:
 * <pre>
 * {@code @PreAuthorize("hasPermission(null, 'SHOP_CREATE')")}
 * public ResponseEntity<ShopResponse> createShop(...) {
 *     // Method implementation
 * }
 * </pre>
 *
 * The hasPermission() expression will delegate to CustomPermissionEvaluator
 * which queries the database for user permissions on every request.
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class MethodSecurityConfig {

    private final CustomPermissionEvaluator customPermissionEvaluator;

    /**
     * Configure the method security expression handler with custom permission evaluator.
     *
     * @return Configured MethodSecurityExpressionHandler
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(customPermissionEvaluator);
        return expressionHandler;
    }
}
