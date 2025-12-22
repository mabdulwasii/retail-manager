package com.princely.shopmanager.test.config;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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
@Profile("test & !test-security")
public class TestSecurityConfig {

    /**
     * Configures security filter chain for tests.
     * <p>
     * This configuration:
     * - Permits all requests (authentication is mocked via @WithMockPermissions)
     * - Disables CSRF (not needed for stateless API tests)
     * - Uses stateless session management
     * - Does NOT configure OAuth2 JWT validation (allows mock tokens)
     * - Adds TestAuthenticationFilter to create JwtPrincipal from test headers
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
                .anyRequest().permitAll())  // Permit all - authentication mocked in tests
            .addFilterBefore(testAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Test authentication filter that creates JwtPrincipal from test headers.
     * <p>
     * Reads X-Test-User, X-Test-Tenant, X-Test-Shop, and X-Test-Roles headers
     * and creates a proper Authentication object with JwtPrincipal.
     *
     * @return Test authentication filter
     */
    @Bean
    public OncePerRequestFilter testAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                String username = request.getHeader("X-Test-User");
                String tenantId = request.getHeader("X-Test-Tenant");
                String shopId = request.getHeader("X-Test-Shop");
                String rolesHeader = request.getHeader("X-Test-Roles");

                if (username != null && tenantId != null) {
                    // Parse roles
                    List<String> roles = rolesHeader != null
                        ? Arrays.asList(rolesHeader.split(","))
                        : List.of();

                    // Determine email: use username as-is if it contains @, otherwise append @test.com
                    String email = username.contains("@") ? username : username + "@test.com";

                    // Create JwtPrincipal
                    JwtPrincipal principal = JwtPrincipal.builder()
                        .subject(username)
                        .userId(username)
                        .preferredUsername(username)
                        .tenantId(tenantId)
                        .shopId(shopId)
                        .email(email)
                        .roles(roles)
                        .claims(new HashMap<>())
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .issuer("test")
                        .build();

                    // Create authorities from roles
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                    // Create authentication
                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);

                    // Set in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // Set tenant context (since TenantFilter is disabled in tests)
                    TenantContext.setCurrentTenant(tenantId);
                    TenantContext.setCurrentUser(username, username);
                }

                try {
                    filterChain.doFilter(request, response);
                } finally {
                    // Clear tenant context after request
                    TenantContext.clear();
                }
            }
        };
    }

    /**
     * Mock PermissionEvaluator that always grants all permissions in tests.
     * <p>
     * This allows @PreAuthorize annotations to pass without needing real permission checking.
     *
     * @return PermissionEvaluator that always returns true
     */
    @Bean
    @Primary
    public PermissionEvaluator testPermissionEvaluator() {
        return new PermissionEvaluator() {
            @Override
            public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
                return true; // Grant all permissions in tests
            }

            @Override
            public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
                return true; // Grant all permissions in tests
            }
        };
    }

    /**
     * Configures method security expression handler with the mock permission evaluator.
     *
     * @return MethodSecurityExpressionHandler configured with test permission evaluator
     */
    @Bean
    @Primary
    public MethodSecurityExpressionHandler testMethodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(testPermissionEvaluator());
        return expressionHandler;
    }
}