package com.princely.shopmanager.embedded.controller;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.dto.CloudSyncConfigDto;
import com.princely.shopmanager.embedded.dto.CloudSyncStatusDto;
import com.princely.shopmanager.embedded.service.CloudRegistrationService;
import com.princely.shopmanager.embedded.service.CloudSyncConfigurationService;
import com.princely.shopmanager.embedded.service.TransactionValidationService;
import com.princely.shopmanager.embedded.sync.service.CloudSyncScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * REST controller for managing cloud sync configuration.
 * Provides endpoints for setup, status, and manual sync triggers.
 */
@RestController
@RequestMapping("/api/cloud-sync")
@Profile("embedded")
@RequiredArgsConstructor
@Slf4j
public class CloudSyncController {

    private final CloudSyncConfigurationService cloudSyncConfigurationService;
    private final CloudRegistrationService cloudRegistrationService;
    private final TransactionValidationService transactionValidationService;
    private final CloudSyncScheduler cloudSyncScheduler;

    /**
     * Get cloud sync configuration for current tenant
     */
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<CloudSyncConfigDto> getConfiguration(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.debug("Getting cloud sync config for tenant: {}", principal.getTenantId());

        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantId(principal.getTenantId())
                .orElse(null);

        if (config == null) {
            return ResponseEntity.ok(CloudSyncConfigDto.notConfigured());
        }

        return ResponseEntity.ok(CloudSyncConfigDto.fromEntity(config));
    }

    /**
     * Get cloud sync status for current tenant
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<CloudSyncStatusDto> getStatus(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.debug("Getting cloud sync status for tenant: {}", principal.getTenantId());

        TransactionValidationService.CloudSyncStatus status =
                transactionValidationService.getCloudSyncStatus(principal.getTenantId());

        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantId(principal.getTenantId())
                .orElse(null);

        return ResponseEntity.ok(CloudSyncStatusDto.builder()
                .configured(status.configured())
                .active(status.active())
                .status(status.status())
                .message(status.message())
                .lastSyncAt(config != null ? config.getLastSyncAt() : null)
                .lastError(config != null ? config.getLastError() : null)
                .cloudApiUrl(config != null ? config.getCloudApiUrl() : null)
                .build());
    }

    /**
     * Register tenant with cloud aggregator
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<CloudSyncConfigDto> registerWithCloud(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CloudRegistrationRequest request) {
        log.info("Registering tenant {} with cloud aggregator", principal.getTenantId());

        CloudSyncConfig config = cloudRegistrationService.registerTenant(
                principal.getTenantId(),
                request.cloudApiUrl()
        );

        return ResponseEntity.ok(CloudSyncConfigDto.fromEntity(config));
    }

    /**
     * Update cloud sync configuration
     */
    @PutMapping("/config")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<CloudSyncConfigDto> updateConfiguration(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody CloudSyncUpdateRequest request) {
        log.info("Updating cloud sync config for tenant: {}", principal.getTenantId());

        CloudSyncConfig config = cloudSyncConfigurationService.getConfigByTenantIdOrThrow(principal.getTenantId());

        if (request.cloudApiUrl() != null) {
            config.setCloudApiUrl(request.cloudApiUrl());
        }

        CloudSyncConfig updated = cloudSyncConfigurationService.saveConfiguration(config);

        return ResponseEntity.ok(CloudSyncConfigDto.fromEntity(updated));
    }

    /**
     * Enable cloud sync
     */
    @PostMapping("/enable")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<CloudSyncConfigDto> enableSync(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.info("Enabling cloud sync for tenant: {}", principal.getTenantId());

        CloudSyncConfig config = cloudSyncConfigurationService.enableSync(principal.getTenantId());

        return ResponseEntity.ok(CloudSyncConfigDto.fromEntity(config));
    }

    /**
     * Disable cloud sync
     */
    @PostMapping("/disable")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN')")
    public ResponseEntity<CloudSyncConfigDto> disableSync(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.info("Disabling cloud sync for tenant: {}", principal.getTenantId());

        CloudSyncConfig config = cloudSyncConfigurationService.disableSync(principal.getTenantId());

        return ResponseEntity.ok(CloudSyncConfigDto.fromEntity(config));
    }

    /**
     * Trigger manual sync
     */
    @PostMapping("/sync")
    @PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'TENANT_ADMIN', 'OWNER')")
    public ResponseEntity<ManualSyncResponse> triggerManualSync(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.info("Manual sync triggered for tenant: {}", principal.getTenantId());

        cloudSyncScheduler.triggerManualSyncForTenant(principal.getTenantId());

        return ResponseEntity.ok(new ManualSyncResponse(
                "Manual sync started for tenant: " + principal.getTenantId(),
                true
        ));
    }

    /**
     * Unregister from cloud
     */
    @DeleteMapping("/unregister")
    @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<Void> unregisterFromCloud(
            @AuthenticationPrincipal JwtPrincipal principal) {
        log.warn("Unregistering tenant {} from cloud aggregator", principal.getTenantId());

        cloudRegistrationService.unregisterTenant(principal.getTenantId());

        return ResponseEntity.noContent().build();
    }

    // DTOs
    public record CloudRegistrationRequest(String cloudApiUrl) {}

    public record CloudSyncUpdateRequest(String cloudApiUrl) {}

    public record ManualSyncResponse(String message, boolean started) {}
}
