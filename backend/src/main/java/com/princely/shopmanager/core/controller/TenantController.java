package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.TenantConfiguration;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.TenantConfigurationRequest;
import com.princely.shopmanager.core.dto.TenantConfigurationResponse;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.core.dto.UserResponse;
import com.princely.shopmanager.core.service.TenantConfigurationService;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for tenant management operations.
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Management", description = "Operations for managing tenants, users, and configurations")
@SecurityRequirement(name = "bearerAuth")
public class TenantController {

    private final UserService userService;
    private final TenantConfigurationService configurationService;

    /**
     * Create a new user in the tenant.
     *
     * @param tenantId Tenant ID
     * @param request User creation request
     * @return Created user
     */
    @Operation(
        summary = "Create user in tenant",
        description = "Creates a new user within the specified tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request data or username/email already exists"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping("/{tenantId}/users")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_CREATE)")
    public ResponseEntity<UserResponse> createUserInTenant(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("Creating user {} in tenant {}", request.getUsername(), tenantId);
        User user = userService.createUser(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    /**
     * Get all users in a tenant.
     *
     * @param tenantId Tenant ID
     * @return List of users in the tenant
     */
    @Operation(
        summary = "Get tenant users",
        description = "Retrieves all users within the specified tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Tenant not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @GetMapping("/{tenantId}/users")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_LIST)")
    public ResponseEntity<List<UserResponse>> getTenantUsers(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId
    ) {
        log.debug("Retrieving users for tenant {}", tenantId);
        List<User> users = userService.getUsersByTenant(tenantId);
        List<UserResponse> response = users.stream()
            .map(UserResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ==================== Configuration Endpoints ====================

    /**
     * Get all configurations for a tenant.
     */
    @Operation(
        summary = "Get all tenant configurations",
        description = "Retrieves all configuration settings for the specified tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configurations retrieved successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/{tenantId}/configurations")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_READ)")
    public ResponseEntity<List<TenantConfigurationResponse>> getAllConfigurations(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Only active configurations") @RequestParam(required = false) Boolean activeOnly
    ) {
        log.debug("Retrieving configurations for tenant: {}, activeOnly: {}", tenantId, activeOnly);
        List<TenantConfiguration> configs = Boolean.TRUE.equals(activeOnly)
            ? configurationService.getActiveConfigurations(tenantId)
            : configurationService.getAllConfigurations(tenantId);

        List<TenantConfigurationResponse> response = configs.stream()
            .map(TenantConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get configurations by category.
     */
    @Operation(
        summary = "Get configurations by category",
        description = "Retrieves all configurations in a specific category for the tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configurations retrieved successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/{tenantId}/configurations/category/{category}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_READ)")
    public ResponseEntity<List<TenantConfigurationResponse>> getConfigurationsByCategory(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Configuration category") @PathVariable TenantConfiguration.ConfigCategory category
    ) {
        log.debug("Retrieving configurations for tenant: {} and category: {}", tenantId, category);
        List<TenantConfiguration> configs = configurationService.getConfigurationsByCategory(tenantId, category);
        List<TenantConfigurationResponse> response = configs.stream()
            .map(TenantConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific configuration by key.
     */
    @Operation(
        summary = "Get configuration by key",
        description = "Retrieves a specific configuration setting by its key."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration retrieved successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @GetMapping("/{tenantId}/configurations/{key}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_READ)")
    public ResponseEntity<TenantConfigurationResponse> getConfiguration(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Configuration key") @PathVariable String key
    ) {
        log.debug("Retrieving configuration {} for tenant: {}", key, tenantId);
        TenantConfiguration config = configurationService.getConfiguration(tenantId, key);
        return ResponseEntity.ok(TenantConfigurationResponse.fromEntity(config));
    }

    /**
     * Create a new configuration.
     */
    @Operation(
        summary = "Create tenant configuration",
        description = "Creates a new configuration setting for the tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Configuration created successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request or configuration already exists"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/{tenantId}/configurations")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_CREATE)")
    public ResponseEntity<TenantConfigurationResponse> createConfiguration(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Valid @RequestBody TenantConfigurationRequest request
    ) {
        log.info("Creating configuration {} for tenant: {}", request.getKey(), tenantId);
        TenantConfiguration config = configurationService.createConfiguration(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(TenantConfigurationResponse.fromEntity(config));
    }

    /**
     * Update an existing configuration.
     */
    @Operation(
        summary = "Update tenant configuration",
        description = "Updates an existing configuration setting for the tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration updated successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request or configuration not editable"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PutMapping("/{tenantId}/configurations/{key}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_UPDATE)")
    public ResponseEntity<TenantConfigurationResponse> updateConfiguration(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Configuration key") @PathVariable String key,
        @Valid @RequestBody TenantConfigurationRequest request
    ) {
        log.info("Updating configuration {} for tenant: {}", key, tenantId);
        TenantConfiguration config = configurationService.updateConfiguration(tenantId, key, request);
        return ResponseEntity.ok(TenantConfigurationResponse.fromEntity(config));
    }

    /**
     * Update configuration value only.
     */
    @Operation(
        summary = "Update configuration value",
        description = "Updates only the value of a configuration setting."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configuration value updated successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Configuration not editable"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PatchMapping("/{tenantId}/configurations/{key}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_UPDATE)")
    public ResponseEntity<TenantConfigurationResponse> updateConfigurationValue(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Configuration key") @PathVariable String key,
        @Parameter(description = "New value") @RequestBody String value
    ) {
        log.info("Updating value for configuration {} in tenant: {}", key, tenantId);
        TenantConfiguration config = configurationService.updateConfigurationValue(tenantId, key, value);
        return ResponseEntity.ok(TenantConfigurationResponse.fromEntity(config));
    }

    /**
     * Delete a configuration.
     */
    @Operation(
        summary = "Delete tenant configuration",
        description = "Deletes a configuration setting from the tenant."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Configuration deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Configuration not editable"),
        @ApiResponse(responseCode = "404", description = "Configuration not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @DeleteMapping("/{tenantId}/configurations/{key}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_DELETE)")
    public ResponseEntity<Void> deleteConfiguration(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Parameter(description = "Configuration key") @PathVariable String key
    ) {
        log.info("Deleting configuration {} for tenant: {}", key, tenantId);
        configurationService.deleteConfiguration(tenantId, key);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk create or update configurations.
     */
    @Operation(
        summary = "Bulk upsert configurations",
        description = "Creates or updates multiple configurations in a single request."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Configurations processed successfully",
            content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Tenant not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/{tenantId}/configurations/bulk")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).TENANT_CONFIG_CREATE)")
    public ResponseEntity<List<TenantConfigurationResponse>> bulkUpsertConfigurations(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Valid @RequestBody List<TenantConfigurationRequest> requests
    ) {
        log.info("Bulk upserting {} configurations for tenant: {}", requests.size(), tenantId);
        List<TenantConfiguration> configs = configurationService.bulkUpsertConfigurations(tenantId, requests);
        List<TenantConfigurationResponse> response = configs.stream()
            .map(TenantConfigurationResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
