package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Permission;
import com.princely.shopmanager.core.dto.PermissionGroupResponse;
import com.princely.shopmanager.core.dto.PermissionResponse;
import com.princely.shopmanager.core.service.PermissionService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for permission management operations.
 * Provides endpoints for viewing available permissions in the system.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission Management", description = "Operations for viewing system permissions")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * Get all available permissions in the system.
     *
     * @return List of all permissions
     */
    @Operation(
        summary = "Get all permissions",
        description = "Retrieves all available permissions in the system. Available to users with PERMISSION_LIST permission."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionResponse.class))
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
    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).PERMISSION_LIST)")
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        log.debug("Retrieving all permissions");
        List<Permission> permissions = permissionService.getAllPermissions();
        List<PermissionResponse> response = permissions.stream()
            .map(PermissionResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all permissions grouped by resource.
     *
     * @return List of permission groups
     */
    @Operation(
        summary = "Get permissions grouped by resource",
        description = "Retrieves all permissions grouped by their resource (e.g., PRODUCT, SALES, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permission groups retrieved successfully",
            content = @Content(schema = @Schema(implementation = PermissionGroupResponse.class))
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
    @GetMapping("/permissions/grouped")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).PERMISSION_LIST)")
    public ResponseEntity<List<PermissionGroupResponse>> getPermissionsGrouped() {
        log.debug("Retrieving permissions grouped by resource");
        List<PermissionGroupResponse> response = permissionService.getPermissionGroupsAsResponse();
        return ResponseEntity.ok(response);
    }
}
