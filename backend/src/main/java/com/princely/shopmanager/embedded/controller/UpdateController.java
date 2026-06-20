package com.princely.shopmanager.embedded.controller;

import com.princely.shopmanager.embedded.dto.UpdateCheckResponse;
import com.princely.shopmanager.embedded.service.UpdateCheckService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for update notification management.
 * Provides endpoints for checking application updates and viewing release information.
 */
@RestController
@RequestMapping("/api/updates")
@Profile("embedded")
@ConditionalOnProperty(name = "app.update-check.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class UpdateController {

    private final UpdateCheckService updateCheckService;

    /**
     * Manually trigger an update check.
     * Checks cloud API for latest version and returns comparison with current version.
     *
     * @param principal Authenticated user principal
     * @return UpdateCheckResponse with version information
     */
    @PostMapping("/check")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<UpdateCheckResponse> checkForUpdates(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.info("Manual update check triggered by user: {} (tenant: {})",
                principal.getUsername(), principal.getTenantId());

        UpdateCheckResponse response = updateCheckService.checkForUpdates();

        return ResponseEntity.ok(response);
    }

    /**
     * Get cached update status without making a new API call.
     * Returns the result of the last update check (scheduled or manual).
     *
     * @param principal Authenticated user principal
     * @return Cached UpdateCheckResponse or empty if no check has been performed
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER', 'MANAGER', 'EMPLOYEE')")
    public ResponseEntity<UpdateCheckResponse> getUpdateStatus(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.debug("Getting cached update status for user: {} (tenant: {})",
                principal.getUsername(), principal.getTenantId());

        UpdateCheckResponse cachedStatus = updateCheckService.getCachedStatus();

        if (cachedStatus == null) {
            // No check has been performed yet
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(cachedStatus);
    }
}
