package com.princely.shopmanager.auth.listener;

import com.princely.shopmanager.auth.service.UserSyncService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Listener for authentication success events.
 * Automatically syncs Keycloak users to the database upon successful login.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationSuccessListener {

    private final UserSyncService userSyncService;

    /**
     * Handles authentication success events.
     * Syncs the authenticated user from Keycloak to the database.
     *
     * @param event Authentication success event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            // Check if this is a JWT authentication
            if (event.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                JwtPrincipal principal = JwtPrincipal.fromJwt(jwt);

                log.debug("Authentication success for user: {}, syncing to database", principal.getUsername());

                // Sync user to database asynchronously to avoid blocking authentication
                syncUserAsync(principal);
            }
        } catch (Exception e) {
            // Log error but don't fail authentication
            log.error("Error syncing user on authentication success", e);
        }
    }

    /**
     * Syncs user to database.
     * This could be made truly async with @Async if needed.
     */
    private void syncUserAsync(JwtPrincipal principal) {
        try {
            userSyncService.syncUserFromKeycloak(principal);
            log.debug("User synced successfully: {}", principal.getUsername());
        } catch (Exception e) {
            log.error("Failed to sync user {}: {}", principal.getUsername(), e.getMessage(), e);
        }
    }
}
