package com.princely.shopmanager.embedded.controller;

import com.princely.shopmanager.embedded.domain.SystemSettings;
import com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import com.princely.shopmanager.embedded.dto.SystemSettingDTO;
import com.princely.shopmanager.embedded.dto.UpdateSettingRequest;
import com.princely.shopmanager.embedded.dto.BulkUpdateSettingsRequest;
import com.princely.shopmanager.embedded.service.SystemSettingsService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System Settings Controller (Embedded Mode Only)
 *
 * REST API for managing system-wide configuration settings in Docker Lite deployments.
 *
 * IMPORTANT: This controller is ONLY active in embedded mode (@Profile("embedded")).
 * In cloud mode, configuration is managed via Kubernetes ConfigMaps and Secrets,
 * and these endpoints will not be available.
 *
 * Security:
 * - VIEW: SYSTEM_ADMIN, TENANT_ADMIN, OWNER
 * - UPDATE: SYSTEM_ADMIN, TENANT_ADMIN, OWNER
 * - MANAGE: SYSTEM_ADMIN only
 *
 * @see com.princely.shopmanager.embedded.service.SystemSettingsService
 * @see com.princely.shopmanager.embedded.domain.SystemSettings
 * @author Claude Code
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Profile("embedded")  // ← CRITICAL: Only active in embedded mode
@Slf4j
@Tag(name = "System Settings", description = "System configuration management (embedded mode only)")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    /**
     * Get all system settings
     *
     * @param principal Authenticated user
     * @return List of all settings (sensitive values masked)
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Get all system settings", description = "Returns all system settings grouped by category")
    public ResponseEntity<List<SystemSettingDTO>> getAllSettings(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} fetching all system settings", principal.getUsername());

        List<SystemSettings> settings = settingsService.getAllSettings();
        List<SystemSettingDTO> dtos = settings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get settings by category
     *
     * @param category  Setting category
     * @param principal Authenticated user
     * @return List of settings in the category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Get settings by category", description = "Returns all settings in a specific category")
    public ResponseEntity<List<SystemSettingDTO>> getSettingsByCategory(
            @PathVariable SettingCategory category,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} fetching settings for category: {}", principal.getUsername(), category);

        List<SystemSettings> settings = settingsService.getSettingsByCategory(category);
        List<SystemSettingDTO> dtos = settings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Get settings grouped by category
     *
     * @param principal Authenticated user
     * @return Map of category to list of settings
     */
    @GetMapping("/grouped")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Get settings grouped by category", description = "Returns settings organized by category")
    public ResponseEntity<Map<SettingCategory, List<SystemSettingDTO>>> getSettingsGrouped(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} fetching settings grouped by category", principal.getUsername());

        Map<SettingCategory, List<SystemSettings>> grouped = settingsService.getSettingsGroupedByCategory();

        Map<SettingCategory, List<SystemSettingDTO>> result = grouped.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList())
                ));

        return ResponseEntity.ok(result);
    }

    /**
     * Get a single setting by key
     *
     * @param key       Setting key
     * @param principal Authenticated user
     * @return Setting DTO
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Get setting by key", description = "Returns a specific setting by its key")
    public ResponseEntity<SystemSettingDTO> getSettingByKey(
            @PathVariable String key,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} fetching setting: {}", principal.getUsername(), key);

        SystemSettings setting = settingsService.getSettingByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        return ResponseEntity.ok(toDTO(setting));
    }

    /**
     * Update a single setting
     *
     * @param key       Setting key
     * @param request   Update request with new value
     * @param principal Authenticated user
     * @return Updated setting with restart requirement flag
     */
    @PutMapping("/{key}")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Update setting", description = "Updates a setting value and returns restart requirement if applicable")
    public ResponseEntity<Map<String, Object>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSettingRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} updating setting: {} to value: {}", principal.getUsername(), key, maskIfSensitive(key, request.getValue()));

        SystemSettings updated = settingsService.updateSetting(key, request.getValue(), principal.getUserId());

        Map<String, Object> response = Map.of(
                "setting", toDTO(updated),
                "requiresRestart", updated.getRequiresRestart(),
                "restartCommand", updated.getRequiresRestart()
                        ? "docker-compose -f docker-compose-lite.yml restart"
                        : "",
                "message", updated.getRequiresRestart()
                        ? "Setting updated successfully. Container restart required for changes to take effect."
                        : "Setting updated successfully."
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Bulk update multiple settings
     *
     * @param request   Bulk update request with map of key to value
     * @param principal Authenticated user
     * @return List of updated settings with overall restart requirement
     */
    @PutMapping("/bulk")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Bulk update settings", description = "Updates multiple settings at once")
    public ResponseEntity<Map<String, Object>> bulkUpdateSettings(
            @Valid @RequestBody BulkUpdateSettingsRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} bulk updating {} settings", principal.getUsername(), request.getUpdates().size());

        List<SystemSettings> updated = settingsService.bulkUpdateSettings(
                request.getUpdates(),
                principal.getUserId()
        );

        boolean anyRequireRestart = updated.stream()
                .anyMatch(SystemSettings::getRequiresRestart);

        Map<String, Object> response = Map.of(
                "settings", updated.stream().map(this::toDTO).collect(Collectors.toList()),
                "requiresRestart", anyRequireRestart,
                "restartCommand", anyRequireRestart
                        ? "docker-compose -f docker-compose-lite.yml restart"
                        : "",
                "message", anyRequireRestart
                        ? String.format("%d settings updated. Container restart required for changes to take effect.", updated.size())
                        : String.format("%d settings updated successfully.", updated.size())
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Reset a setting to its default value
     *
     * @param key       Setting key
     * @param principal Authenticated user
     * @return Reset setting
     */
    @PostMapping("/{key}/reset")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Reset setting to default", description = "Resets a setting to its default value")
    public ResponseEntity<SystemSettingDTO> resetToDefault(
            @PathVariable String key,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} resetting setting {} to default", principal.getUsername(), key);

        SystemSettings reset = settingsService.resetToDefault(key, principal.getUserId());

        return ResponseEntity.ok(toDTO(reset));
    }

    /**
     * Get all modified settings (different from default)
     *
     * @param principal Authenticated user
     * @return List of modified settings
     */
    @GetMapping("/modified")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Get modified settings", description = "Returns all settings that have been changed from their default values")
    public ResponseEntity<List<SystemSettingDTO>> getModifiedSettings(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} fetching modified settings", principal.getUsername());

        List<SystemSettings> settings = settingsService.getModifiedSettings();
        List<SystemSettingDTO> dtos = settings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Search settings by keyword
     *
     * @param query     Search query
     * @param principal Authenticated user
     * @return List of matching settings
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('SYSTEM_SETTING_VIEW', 'SYSTEM_SETTING_UPDATE', 'SYSTEM_SETTING_MANAGE')")
    @Operation(summary = "Search settings", description = "Searches settings by key or description")
    public ResponseEntity<List<SystemSettingDTO>> searchSettings(
            @RequestParam String query,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        log.info("User {} searching settings with query: {}", principal.getUsername(), query);

        List<SystemSettings> settings = settingsService.searchSettings(query);
        List<SystemSettingDTO> dtos = settings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Convert entity to DTO (with sensitive value masking)
     *
     * @param setting Setting entity
     * @return DTO
     */
    private SystemSettingDTO toDTO(SystemSettings setting) {
        return SystemSettingDTO.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getMaskedValue())  // Masked if sensitive
                .category(setting.getCategory())
                .dataType(setting.getDataType())
                .description(setting.getDescription())
                .requiresRestart(setting.getRequiresRestart())
                .isSensitive(setting.getIsSensitive())
                .defaultValue(setting.getDefaultValue())
                .isModified(setting.isModified())
                .updatedBy(setting.getUpdatedBy())
                .updatedAt(setting.getUpdatedAt())
                .version(setting.getVersion())
                .build();
    }

    /**
     * Mask value if setting is sensitive
     *
     * @param key   Setting key
     * @param value Value to potentially mask
     * @return Masked or original value
     */
    private String maskIfSensitive(String key, String value) {
        return settingsService.getSettingByKey(key)
                .map(setting -> setting.getIsSensitive() ? "********" : value)
                .orElse(value);
    }
}
