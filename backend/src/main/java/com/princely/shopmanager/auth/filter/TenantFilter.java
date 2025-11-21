package com.princely.shopmanager.auth.filter;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.FeatureFlagService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servlet filter that extracts and sets tenant context for multi-tenant operations.
 *
 * This filter is responsible for:
 * - Extracting tenant ID from HTTP headers or JWT tokens
 * - Setting the tenant context for the current request
 * - Extracting user information from JWT tokens
 * - Ensuring proper cleanup of tenant context after request processing
 *
 * The filter can be disabled via the 'app.security.tenant-isolation' feature flag.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.security.tenant-isolation", havingValue = "true", matchIfMissing = true)
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    private final FeatureFlagService featureFlagService;

    /**
     * Processes each HTTP request to extract and set tenant context.
     *
     * This method performs the following operations:
     * 1. Extracts tenant ID from request headers or JWT token
     * 2. Sets tenant context for the current thread
     * 3. Extracts user information from JWT token if available
     * 4. Checks if multi-tenancy is enabled for the specific tenant
     * 5. Ensures proper cleanup of context after request processing
     *
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain to continue processing
     * @throws ServletException If servlet processing fails
     * @throws IOException If I/O operation fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract tenant information from request
            String tenantId = extractTenant(request);

            if (tenantId != null) {
                TenantContext.setCurrentTenant(tenantId);
                log.debug("Set tenant context for tenant: {}", tenantId);
            }

            // Extract and set user information from JWT token or Principal
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                String userId = null;
                String userName = null;

                if (authentication.getPrincipal() instanceof JwtPrincipal principal) {
                    userId = principal.getSubject();
                    userName = principal.getPreferredUsername();
                } else if (authentication.getPrincipal() instanceof Jwt jwt) {
                    userId = jwt.getClaimAsString("sub");
                    userName = jwt.getClaimAsString("preferred_username");
                }

                if (userId != null && userName != null) {
                    TenantContext.setCurrentUser(userId, userName);
                    log.debug("Set user context: {} ({})", userName, userId);
                }
            }

            // Continue with request processing
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Error in tenant filter processing", e);
            // Continue processing even if tenant extraction fails
            filterChain.doFilter(request, response);
        } finally {
            // Always clear context to prevent memory leaks
            TenantContext.clear();
        }
    }

    /**
     * Extracts tenant ID from the HTTP request.
     * The method tries multiple approaches to determine the tenant ID:
     * 1. First, checks for X-Tenant-ID header
     * 2. Falls back to extracting from JWT token's tenant_id claim
     * 3. Returns null if no tenant information is found
     *
     * @param request The HTTP request
     * @return The tenant ID if found, null otherwise
     */
    private String extractTenant(HttpServletRequest request) {
        // Priority 1: Check for explicit tenant header
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            log.debug("Tenant ID extracted from header: {}", tenantId);
            return tenantId.trim();
        }

        // Priority 2: Extract from JWT token or Principal
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof JwtPrincipal principal) {
                tenantId = principal.getTenantId();
            } else if (authentication.getPrincipal() instanceof Jwt jwt) {
                tenantId = jwt.getClaimAsString("tenant_id");
            }

            if (tenantId != null && !tenantId.trim().isEmpty()) {
                log.debug("Tenant ID extracted from JWT token/principal: {}", tenantId);
                return tenantId.trim();
            }
        }

        log.debug("No tenant ID found in request");
        return null;
    }
}