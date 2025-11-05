package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Role;
import com.princely.shopmanager.core.dto.RoleAssignmentRequest;
import com.princely.shopmanager.core.dto.RoleCreateRequest;
import com.princely.shopmanager.core.dto.RolePermissionUpdateRequest;
import com.princely.shopmanager.core.dto.RoleResponse;
import com.princely.shopmanager.core.dto.RoleUpdateRequest;
import com.princely.shopmanager.core.service.RoleService;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for role management operations.
 * Provides endpoints for viewing roles and managing role assignments.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
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
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_LIST)")
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
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_READ)")
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
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_LIST)")
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
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_ASSIGN)")
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
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_ASSIGN)")
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

    // ============== Role CRUD Operations ==============

    /**
     * Create a new custom role.
     * System roles cannot be created via API.
     *
     * @param request Role creation request
     * @return Created role response
     */
    @Operation(
        summary = "Create custom role",
        description = "Create a new custom role with specific permissions. OWNER and SYSTEM_ADMIN only."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Role created successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request or role name already exists"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping("/roles")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_CREATE)")
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleCreateRequest request) {
        log.info("Creating custom role: {}", request.getName());
        Role role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(RoleResponse.fromEntity(role));
    }

    /**
     * Update a role's details.
     * System roles cannot be updated.
     *
     * @param roleId Role ID
     * @param request Update request
     * @return Updated role response
     */
    @Operation(
        summary = "Update role",
        description = "Update a custom role's details. System roles cannot be modified."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Role updated successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions or attempting to modify system role"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found"
        )
    })
    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_UPDATE)")
    public ResponseEntity<RoleResponse> updateRole(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId,
        @Valid @RequestBody RoleUpdateRequest request
    ) {
        log.info("Updating role: {}", roleId);
        Role role = roleService.updateRole(roleId, request);
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }

    /**
     * Delete a custom role.
     * System roles cannot be deleted.
     * Roles assigned to users cannot be deleted.
     *
     * @param roleId Role ID
     * @return No content response
     */
    @Operation(
        summary = "Delete custom role",
        description = "Delete a custom role. System roles and roles assigned to users cannot be deleted."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Role deleted successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Role is assigned to users"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions or attempting to delete system role"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found"
        )
    })
    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_DELETE)")
    public ResponseEntity<Void> deleteRole(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId
    ) {
        log.info("Deleting role: {}", roleId);
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    // ============== Permission Management ==============

    /**
     * Add a permission to a role.
     *
     * @param roleId Role ID
     * @param permissionId Permission ID or name
     * @return No content response
     */
    @Operation(
        summary = "Add permission to role",
        description = "Add a single permission to a role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Permission added successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role or permission not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_PERMISSION_ADD)")
    public ResponseEntity<Void> addPermissionToRole(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId,
        @Parameter(description = "Permission ID or name", example = "PRODUCT_CREATE")
        @PathVariable String permissionId
    ) {
        log.info("Adding permission {} to role {}", permissionId, roleId);
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Remove a permission from a role.
     *
     * @param roleId Role ID
     * @param permissionId Permission ID
     * @return No content response
     */
    @Operation(
        summary = "Remove permission from role",
        description = "Remove a permission from a role"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Permission removed successfully"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role or permission not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_PERMISSION_REMOVE)")
    public ResponseEntity<Void> removePermissionFromRole(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId,
        @Parameter(description = "Permission ID", example = "perm-product-create")
        @PathVariable String permissionId
    ) {
        log.info("Removing permission {} from role {}", permissionId, roleId);
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Bulk update role permissions.
     * Replaces all existing permissions with the provided set.
     *
     * @param roleId Role ID
     * @param request Permission update request
     * @return Updated role response
     */
    @Operation(
        summary = "Bulk update role permissions",
        description = "Replace all role permissions with a new set of permissions"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Permissions updated successfully",
            content = @Content(schema = @Schema(implementation = RoleResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid permission identifiers"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Role not found"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions"
        )
    })
    @PutMapping("/roles/{roleId}/permissions")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).ROLE_UPDATE)")
    public ResponseEntity<RoleResponse> bulkUpdateRolePermissions(
        @Parameter(description = "Role ID", example = "role-123")
        @PathVariable String roleId,
        @Valid @RequestBody RolePermissionUpdateRequest request
    ) {
        log.info("Bulk updating permissions for role {}", roleId);
        roleService.bulkUpdateRolePermissions(roleId, request.getPermissionIdentifiers());
        Role role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(RoleResponse.fromEntity(role));
    }
}
