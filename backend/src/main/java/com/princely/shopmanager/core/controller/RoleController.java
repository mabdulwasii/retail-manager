package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.dto.RoleAssignmentRequest;
import com.princely.shopmanager.core.dto.RoleResponse;
import com.princely.shopmanager.core.service.RoleService;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for role management operations.
 * Provides endpoints for viewing roles and managing role assignments.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Management", description = "Operations for managing roles and role assignments")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    /**
     * Get all available roles.
     *
     * @return List of all roles
     */
    @Operation(
        summary = "Get all roles",
        description = "Retrieves all available roles in the system. Available to all authenticated users."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        log.debug("Retrieving all roles");
        List<Role> roles = roleService.getAllRoles();
        List<RoleResponse> response = roles.stream()
            .map(RoleResponse::fromEntity)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    /**
     * Get a specific role by ID.
     *
     * @param roleId Role ID
     * @return Role details
     */
    @Operation(
        summary = "Get role by ID",
        description = "Retrieves details of a specific role."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/roles/{roleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RoleResponse> getRoleById(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId
    ) {
        log.debug("Retrieving role: {}", roleId);
        Role role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }

    /**
     * Get all roles assigned to a user.
     *
     * @param userId User ID
     * @return Set of user's roles
     */
    @Operation(
        summary = "Get user roles",
        description = "Retrieves all roles assigned to a specific user."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roles retrieved successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<Set<RoleResponse>> getUserRoles(
        @Parameter(description = "User ID", example = "user-123")
        @PathVariable String userId
    ) {
        log.debug("Retrieving roles for user: {}", userId);
        Set<Role> roles = roleService.getUserRoles(userId);
        Set<RoleResponse> response = roles.stream()
            .map(RoleResponse::fromEntity)
            .collect(Collectors.toSet());
        return ResponseEntity.ok(response);
    }

    /**
     * Assign a role to a user.
     * TENANT_ADMIN and OWNER can assign roles within their tenant.
     * MANAGER can assign roles within their shop.
     *
     * @param userId User ID
     * @param request Role assignment request
     * @return No content response
     */
    @Operation(
        summary = "Assign role to user",
        description = "Assigns a role to a user. TENANT_ADMIN/OWNER can assign within tenant, MANAGER within shop."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role assigned successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid role or user"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or role not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<Void> assignRoleToUser(
        @Parameter(description = "User ID", example = "user-123")
        @PathVariable String userId,
        @Valid @RequestBody RoleAssignmentRequest request
    ) {
        log.info("Assigning role {} to user {}", request.getRole(), userId);
        roleService.assignRoleToUser(userId, request.getRole());
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove a role from a user.
     *
     * @param userId User ID
     * @param roleId Role ID
     * @return No content response
     */
    @Operation(
        summary = "Remove role from user",
        description = "Removes a role from a user. TENANT_ADMIN/OWNER can remove within tenant, MANAGER within shop."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role removed successfully"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or role not found"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @DeleteMapping("/users/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<Void> removeRoleFromUser(
        @Parameter(description = "User ID", example = "user-123")
        @PathVariable String userId,
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId
    ) {
        log.info("Removing role {} from user {}", roleId, userId);
        roleService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
