package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.RoleResponse;
import com.princely.shopmanager.core.dto.UserProfileResponse;
import com.princely.shopmanager.core.dto.UserResponse;
import com.princely.shopmanager.core.dto.UserShopTransferRequest;
import com.princely.shopmanager.core.dto.UserUpdateRequest;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

/**
 * REST controller for user profile operations.
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "User profile and management operations")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Gets the current user's profile information.
     *
     * @param principal The authenticated user principal
     * @return The user's profile information
     */
    @GetMapping("/profile")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the authenticated user's profile information including personal details, roles, and permissions from the database. Available to all authenticated users."
    )
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.debug("Getting profile for user: {} (sub: {}, email: {})",
            principal.getUsername(), principal.getSubject(), principal.getEmail());

        try {
            User user = null;

            // Primary lookup: by Keycloak ID (sub claim)
            if (principal.getSubject() != null && !principal.getSubject().isBlank()) {
                user = userService.getUserByKeycloakId(principal.getSubject());
            }

            // Fallback: lookup by email if keycloakId lookup fails
            if (user == null && principal.getEmail() != null && !principal.getEmail().isBlank()) {
                log.warn("User not found by keycloakId: {}. Falling back to email lookup: {}",
                    principal.getSubject(), principal.getEmail());
                user = userService.getUserByEmail(principal.getEmail());
            }

            if (user == null) {
                log.error("User not found in database. KeycloakId: {}, Email: {}, Username: {}",
                    principal.getSubject(), principal.getEmail(), principal.getUsername());
                throw new IllegalStateException(
                    "User not found in database. Please contact administrator to sync your account."
                );
            }

            // Convert database roles to RoleResponse DTOs
            List<RoleResponse> roleResponses = user.getRoles().stream()
                .map(RoleResponse::fromEntity)
                .toList();

            // Convert User entity to response DTO
            // IMPORTANT: Use database values for tenantId/shopId, NOT JWT principal
            // JWT can be stale after user updates (e.g., shop transfer)
            UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus().name())
                .roles(roleResponses) // Database roles with permissions
                .tenantId(user.getTenant().getId()) // From DB, not JWT (prevents staleness)
                .shopId(user.getShop() != null ? user.getShop().getId() : null) // From DB, not JWT
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

            log.debug("Successfully retrieved profile for user: {} with {} roles",
                user.getUsername(), roleResponses.size());

            // Prevent caching of profile data
            return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);

        } catch (Exception e) {
            log.error("Error retrieving user profile for {}: {}", principal.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get a specific user.
     *
     * @param userId User ID
     * @return User details
     */
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves details of a specific user."
    )
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_READ)")
    public ResponseEntity<UserResponse> getUserById(
        @Parameter(description = "User ID") @PathVariable String userId
    ) {
        log.debug("Retrieving user {}", userId);
        User user = userService.getUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Update a user.
     *
     * @param userId User ID
     * @param request User update request
     * @return Updated user
     */
    @Operation(
        summary = "Update user",
        description = "Partially updates an existing user. Only provided fields will be updated (PATCH semantics)."
    )
    @PatchMapping("/users/{userId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_UPDATE)")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "User ID") @PathVariable String userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("Updating user {}", userId);
        User user = userService.updateUser(userId, request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Transfer user to a different shop within the same tenant.
     *
     * @param userId User ID
     * @param request Shop transfer request
     * @return Updated user
     */
    @Operation(
        summary = "Transfer user to different shop",
        description = "Transfers a user to a different shop within the same tenant. Updates both database and Keycloak."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User transferred successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or shops in different tenants"),
        @ApiResponse(responseCode = "404", description = "User or shop not found"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PatchMapping("/users/{userId}/transfer-shop")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_TRANSFER_SHOP)")
    public ResponseEntity<UserResponse> transferUserToShop(
        @Parameter(description = "User ID") @PathVariable String userId,
        @Valid @RequestBody UserShopTransferRequest request
    ) {
        log.info("Transferring user {} to shop {}", userId, request.getNewShopId());
        User user = userService.transferUserToShop(userId, request);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    /**
     * Delete (deactivate) a user.
     *
     * @param userId User ID
     * @return No content
     */
    @Operation(
        summary = "Delete user",
        description = "Soft deletes a user by setting status to INACTIVE and removes from Keycloak."
    )
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_DELETE)")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID") @PathVariable String userId
    ) {
        log.info("Deleting user {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all users in the system.
     * Only accessible by System Admins.
     *
     * @param status Optional status filter
     * @return List of all users
     */
    @Operation(
        summary = "Get all users",
        description = "Retrieves all users in the system. Only accessible by System Admins. Optionally filter by status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Insufficient permissions - System Admin only"
        )
    })
    @GetMapping
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).USER_LIST_ALL)")
    public ResponseEntity<List<UserResponse>> getAllUsers(
        @Parameter(description = "Optional status filter (ACTIVE, INACTIVE, PENDING)")
        @RequestParam(required = false) User.UserStatus status
    ) {
        log.debug("Retrieving all users with status filter: {}", status);
        List<User> users = userService.getAllUsers(status);
        List<UserResponse> response = users.stream()
            .map(UserResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(response);
    }
}