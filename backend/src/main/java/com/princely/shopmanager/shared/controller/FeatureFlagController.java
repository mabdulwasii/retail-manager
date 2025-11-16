package com.princely.shopmanager.shared.controller;

import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.domain.FeatureFlag;
import com.princely.shopmanager.shared.dto.*;
import com.princely.shopmanager.shared.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for feature flag management.
 *
 * This controller provides endpoints for managing feature flags, which enable
 * dynamic feature toggling at global and shop-specific levels.
 *
 * Key capabilities:
 * - Create, update, and delete feature flags
 * - Check feature status for shops or globally
 * - Schedule feature activation/deactivation
 * - Configure feature-specific settings
 *
 * @author Shop Manager Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/feature-flags")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Feature Flags", description = "Operations for managing feature flags and feature toggles")
@SecurityRequirement(name = "bearerAuth")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    /**
     * Checks if a specific feature is enabled for a shop or globally.
     *
     * This endpoint performs hierarchical feature resolution:
     * 1. Check shop-specific flag if shopId is provided
     * 2. Fall back to global flag if no shop-specific flag exists
     * 3. Return false if no flag is found
     *
     * @param featureName The name of the feature to check
     * @param shopId Optional shop ID for shop-specific check
     * @return Feature status response
     */
    @Operation(
        summary = "Check if a feature is enabled",
        description = "Checks if a specific feature is enabled for a shop or globally. Uses hierarchical resolution."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feature status retrieved successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagStatusResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/check")
    public ResponseEntity<FeatureFlagStatusResponse> checkFeature(
            @Parameter(description = "Feature name to check", example = "analytics.enabled", required = true)
            @RequestParam String featureName,
            @Parameter(description = "Shop ID for shop-specific check (optional)", example = "shop-123")
            @RequestParam(required = false) String shopId) {

        boolean enabled = featureFlagService.isFeatureEnabled(shopId, featureName);

        // Determine source
        String source = "NOT_FOUND";
        if (enabled) {
            source = shopId != null ? "SHOP_SPECIFIC" : "GLOBAL";
        }

        FeatureFlagStatusResponse response = FeatureFlagStatusResponse.builder()
            .featureName(featureName)
            .shopId(shopId)
            .enabled(enabled)
            .source(source)
            .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new feature flag.
     *
     * Only users with SYSTEM_ADMIN or TENANT_ADMIN roles can create feature flags.
     * Shop-specific flags require the shopId in the request.
     *
     * @param request Feature flag creation request
     * @param principal Authenticated user principal
     * @return Created feature flag details
     */
    @Operation(
        summary = "Create a new feature flag",
        description = "Creates a new feature flag. Can be shop-specific or global. Requires admin privileges."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Feature flag created successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN or TENANT_ADMIN role"
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FeatureFlagResponse> createFeatureFlag(
            @Valid @RequestBody FeatureFlagCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating feature flag '{}' for shop '{}'", request.getFeatureName(), request.getShopId());

        FeatureFlag featureFlag = featureFlagService.createFeatureFlag(
            request.getShopId(),
            request.getFeatureName(),
            request.isEnabled(),
            request.getDescription(),
            principal.getUsername()
        );

        // Update schedule if provided
        if (request.getEffectiveFrom() != null || request.getEffectiveUntil() != null) {
            featureFlag = featureFlagService.updateFeatureFlagSchedule(
                featureFlag.getId(),
                request.getEffectiveFrom(),
                request.getEffectiveUntil(),
                principal.getUsername()
            );
        }

        // Update configuration if provided
        if (request.getConfiguration() != null && !request.getConfiguration().isEmpty()) {
            featureFlag = featureFlagService.updateFeatureFlagConfiguration(
                featureFlag.getId(),
                request.getConfiguration(),
                principal.getUsername()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(FeatureFlagResponse.fromEntity(featureFlag));
    }

    /**
     * Retrieves all feature flags for a specific shop or global flags.
     *
     * @param shopId Optional shop ID. If null, returns global feature flags.
     * @return List of feature flags
     */
    @Operation(
        summary = "Get feature flags for a shop or globally",
        description = "Retrieves all feature flags for a specific shop or global feature flags if no shopId is provided."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feature flags retrieved successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping
    public ResponseEntity<List<FeatureFlagResponse>> getFeatureFlags(
            @Parameter(description = "Shop ID (optional - returns global flags if not provided)", example = "shop-123")
            @RequestParam(required = false) String shopId) {

        List<FeatureFlag> flags = featureFlagService.getFeatureFlagsForShop(shopId);
        List<FeatureFlagResponse> response = flags.stream()
            .map(FeatureFlagResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves ALL feature flags in the system (System Admin only).
     *
     * @return List of all feature flags
     */
    @Operation(
        summary = "Get all feature flags in the system (System Admin only)",
        description = "Retrieves all feature flags across all shops and global flags. Only accessible to system administrators."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All feature flags retrieved successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN role"
        )
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<FeatureFlagResponse>> getAllFeatureFlags() {
        List<FeatureFlag> flags = featureFlagService.getAllFeatureFlags();
        List<FeatureFlagResponse> response = flags.stream()
            .map(FeatureFlagResponse::fromEntity)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Updates a feature flag.
     *
     * Supports partial updates - only non-null fields in the request will be updated.
     *
     * @param featureFlagId Feature flag ID to update
     * @param request Update request with optional fields
     * @param principal Authenticated user principal
     * @return Updated feature flag details
     */
    @Operation(
        summary = "Update a feature flag",
        description = "Updates an existing feature flag with partial update support. Only non-null fields are updated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Feature flag updated successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Feature flag not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN or TENANT_ADMIN role"
        )
    })
    @PutMapping("/{featureFlagId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FeatureFlagResponse> updateFeatureFlag(
            @Parameter(description = "Feature flag ID", example = "ff-123", required = true)
            @PathVariable String featureFlagId,
            @Valid @RequestBody FeatureFlagUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating feature flag '{}'", featureFlagId);

        FeatureFlag featureFlag = null;

        // Update enabled status if provided
        if (request.getEnabled() != null) {
            featureFlag = featureFlagService.updateFeatureFlag(
                featureFlagId,
                request.getEnabled(),
                principal.getUsername()
            );
        }

        // Update schedule if provided
        if (request.getEffectiveFrom() != null || request.getEffectiveUntil() != null) {
            featureFlag = featureFlagService.updateFeatureFlagSchedule(
                featureFlagId,
                request.getEffectiveFrom(),
                request.getEffectiveUntil(),
                principal.getUsername()
            );
        }

        // Update configuration if provided
        if (request.getConfiguration() != null) {
            featureFlag = featureFlagService.updateFeatureFlagConfiguration(
                featureFlagId,
                request.getConfiguration(),
                principal.getUsername()
            );
        }

        // If no updates were made, fetch the current flag
        if (featureFlag == null) {
            List<FeatureFlag> allFlags = featureFlagService.getAllFeatureFlags();
            featureFlag = allFlags.stream()
                .filter(f -> f.getId().equals(featureFlagId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureFlagId));
        }

        return ResponseEntity.ok(FeatureFlagResponse.fromEntity(featureFlag));
    }

    @Operation(
        summary = "Partially update a feature flag (PATCH)",
        description = "Partially update a feature flag (PATCH). All fields are optional. Preferred over PUT for partial updates."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Feature flag updated successfully",
            content = @Content(schema = @Schema(implementation = FeatureFlagResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Feature flag not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN or TENANT_ADMIN role"
        )
    })
    @PatchMapping("/{featureFlagId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FeatureFlagResponse> patchFeatureFlag(
            @Parameter(description = "Feature flag ID", example = "ff-123", required = true)
            @PathVariable String featureFlagId,
            @Valid @RequestBody FeatureFlagUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Patching feature flag '{}'", featureFlagId);

        // Reuse the same update logic - current implementation already does partial updates
        FeatureFlag featureFlag = null;

        // Update enabled status if provided
        if (request.getEnabled() != null) {
            featureFlag = featureFlagService.updateFeatureFlag(
                featureFlagId,
                request.getEnabled(),
                principal.getUsername()
            );
        }

        // Update schedule if provided
        if (request.getEffectiveFrom() != null || request.getEffectiveUntil() != null) {
            featureFlag = featureFlagService.updateFeatureFlagSchedule(
                featureFlagId,
                request.getEffectiveFrom(),
                request.getEffectiveUntil(),
                principal.getUsername()
            );
        }

        // Update configuration if provided
        if (request.getConfiguration() != null) {
            featureFlag = featureFlagService.updateFeatureFlagConfiguration(
                featureFlagId,
                request.getConfiguration(),
                principal.getUsername()
            );
        }

        // If no updates were made, fetch the current flag
        if (featureFlag == null) {
            List<FeatureFlag> allFlags = featureFlagService.getAllFeatureFlags();
            featureFlag = allFlags.stream()
                .filter(f -> f.getId().equals(featureFlagId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Feature flag not found: " + featureFlagId));
        }

        return ResponseEntity.ok(FeatureFlagResponse.fromEntity(featureFlag));
    }

    /**
     * Deletes a feature flag.
     *
     * @param featureFlagId Feature flag ID to delete
     * @return No content response
     */
    @Operation(
        summary = "Delete a feature flag",
        description = "Permanently deletes a feature flag. This action cannot be undone."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Feature flag deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Feature flag not found",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - requires SYSTEM_ADMIN role"
        )
    })
    @DeleteMapping("/{featureFlagId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteFeatureFlag(
            @Parameter(description = "Feature flag ID", example = "ff-123", required = true)
            @PathVariable String featureFlagId) {

        log.info("Deleting feature flag '{}'", featureFlagId);
        featureFlagService.deleteFeatureFlag(featureFlagId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets configuration value for a feature flag.
     *
     * @param featureName Feature name
     * @param configKey Configuration key
     * @param shopId Optional shop ID
     * @param defaultValue Optional default value
     * @return Configuration value
     */
    @Operation(
        summary = "Get feature flag configuration value",
        description = "Retrieves a specific configuration value from a feature flag."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration value retrieved successfully",
            content = @Content(schema = @Schema(implementation = String.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/config")
    public ResponseEntity<String> getFeatureFlagConfig(
            @Parameter(description = "Feature name", example = "analytics.enabled", required = true)
            @RequestParam String featureName,
            @Parameter(description = "Configuration key", example = "max_retention_days", required = true)
            @RequestParam String configKey,
            @Parameter(description = "Shop ID (optional)", example = "shop-123")
            @RequestParam(required = false) String shopId,
            @Parameter(description = "Default value if not found", example = "30")
            @RequestParam(required = false) String defaultValue) {

        String value = featureFlagService.getFeatureFlagConfig(shopId, featureName, configKey, defaultValue);
        return ResponseEntity.ok(value);
    }

    /**
     * Convenience endpoint to check if investment features are enabled.
     *
     * @param shopId Shop ID to check
     * @return Feature status response
     */
    @Operation(
        summary = "Check if investment features are enabled",
        description = "Convenience endpoint to check investment feature status for a shop."
    )
    @GetMapping("/check/investment")
    public ResponseEntity<FeatureFlagStatusResponse> checkInvestmentEnabled(
            @Parameter(description = "Shop ID", example = "shop-123", required = true)
            @RequestParam String shopId) {

        boolean enabled = featureFlagService.isInvestmentEnabled(shopId);
        return ResponseEntity.ok(FeatureFlagStatusResponse.builder()
            .featureName(FeatureFlag.INVESTMENT_ENABLED)
            .shopId(shopId)
            .enabled(enabled)
            .source(enabled ? "RESOLVED" : "NOT_FOUND")
            .build());
    }

    /**
     * Convenience endpoint to check if analytics features are enabled.
     *
     * @param shopId Shop ID to check
     * @return Feature status response
     */
    @Operation(
        summary = "Check if analytics features are enabled",
        description = "Convenience endpoint to check analytics feature status for a shop."
    )
    @GetMapping("/check/analytics")
    public ResponseEntity<FeatureFlagStatusResponse> checkAnalyticsEnabled(
            @Parameter(description = "Shop ID", example = "shop-123", required = true)
            @RequestParam String shopId) {

        boolean enabled = featureFlagService.isAnalyticsEnabled(shopId);
        return ResponseEntity.ok(FeatureFlagStatusResponse.builder()
            .featureName(FeatureFlag.ANALYTICS_ENABLED)
            .shopId(shopId)
            .enabled(enabled)
            .source(enabled ? "RESOLVED" : "NOT_FOUND")
            .build());
    }

    /**
     * Convenience endpoint to check if fraud detection features are enabled.
     *
     * @param shopId Shop ID to check
     * @return Feature status response
     */
    @Operation(
        summary = "Check if fraud detection features are enabled",
        description = "Convenience endpoint to check fraud detection feature status for a shop."
    )
    @GetMapping("/check/fraud")
    public ResponseEntity<FeatureFlagStatusResponse> checkFraudDetectionEnabled(
            @Parameter(description = "Shop ID", example = "shop-123", required = true)
            @RequestParam String shopId) {

        boolean enabled = featureFlagService.isFraudDetectionEnabled(shopId);
        return ResponseEntity.ok(FeatureFlagStatusResponse.builder()
            .featureName(FeatureFlag.FRAUD_ENABLED)
            .shopId(shopId)
            .enabled(enabled)
            .source(enabled ? "RESOLVED" : "NOT_FOUND")
            .build());
    }

    /**
     * Convenience endpoint to check if advanced reporting features are enabled.
     *
     * @param shopId Shop ID to check
     * @return Feature status response
     */
    @Operation(
        summary = "Check if advanced reporting features are enabled",
        description = "Convenience endpoint to check advanced reporting feature status for a shop."
    )
    @GetMapping("/check/reporting")
    public ResponseEntity<FeatureFlagStatusResponse> checkAdvancedReportingEnabled(
            @Parameter(description = "Shop ID", example = "shop-123", required = true)
            @RequestParam String shopId) {

        boolean enabled = featureFlagService.isAdvancedReportingEnabled(shopId);
        return ResponseEntity.ok(FeatureFlagStatusResponse.builder()
            .featureName(FeatureFlag.ADVANCED_REPORTING)
            .shopId(shopId)
            .enabled(enabled)
            .source(enabled ? "RESOLVED" : "NOT_FOUND")
            .build());
    }
}
