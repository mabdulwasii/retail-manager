package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserCreateRequest;
import com.princely.shopmanager.core.dto.UserProfileResponse;
import com.princely.shopmanager.core.dto.UserResponse;
import com.princely.shopmanager.core.dto.UserUpdateRequest;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
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
 * REST controller for user profile operations.
 */
@RestController
@RequestMapping("/api")
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
    @GetMapping("/users/profile")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the authenticated user's profile information including personal details and roles. Available to all authenticated users."
    )
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.debug("Getting profile for user: {}", principal.getUsername());

        try {
            // Get user from database by Keycloak ID
            User user = userService.getUserByKeycloakId(principal.getSubject());

            if (user == null) {
                log.warn("User not found in database for Keycloak ID: {}", principal.getSubject());
                // Return profile info from JWT token if user not in database
                UserProfileResponse response = UserProfileResponse.builder()
                    .id(principal.getSubject())
                    .username(principal.getUsername())
                    .email(principal.getEmail())
                    .firstName(principal.getFirstName())
                    .lastName(principal.getLastName())
                    .fullName(principal.getFullName())
                    .roles(principal.getRoles())
                    .tenantId(principal.getTenantId())
                    .shopId(principal.getClaimAsString("shop_id"))
                    .build();

                return ResponseEntity.ok(response);
            }

            // Convert User entity to response DTO
            UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus().name())
                .isInvestor(user.isInvestor())
                .roles(principal.getRoles()) // Use roles from JWT for latest info
                .tenantId(principal.getTenantId())
                .shopId(principal.getClaimAsString("shop_id"))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

            log.debug("Successfully retrieved profile for user: {}", user.getUsername());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving user profile for {}: {}", principal.getUsername(), e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Create a new user for a tenant.
     *
     * @param tenantId Tenant ID
     * @param request User creation request
     * @return Created user
     */
    @Operation(
        summary = "Create user in tenant",
        description = "Creates a new user in a tenant. Available to TENANT_ADMIN and OWNER."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/tenants/{tenantId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER')")
    public ResponseEntity<UserResponse> createUserInTenant(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId,
        @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("Creating user in tenant {}", tenantId);
        User user = userService.createUser(tenantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    /**
     * Create a new user for a shop.
     *
     * @param shopId Shop ID
     * @param request User creation request
     * @return Created user
     */
    @Operation(
        summary = "Create user in shop",
        description = "Creates a new user in a shop. Available to MANAGER."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PostMapping("/shops/{shopId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<UserResponse> createUserInShop(
        @Parameter(description = "Shop ID") @PathVariable String shopId,
        @Valid @RequestBody UserCreateRequest request
    ) {
        log.info("Creating user in shop {}", shopId);
        request.setShopId(shopId);
        // Get tenant ID from shop - this should be validated in service
        User user = userService.createUser(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    /**
     * Get users for a tenant.
     *
     * @param tenantId Tenant ID
     * @return List of users
     */
    @Operation(
        summary = "Get tenant users",
        description = "Retrieves all users in a tenant."
    )
    @GetMapping("/tenants/{tenantId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER')")
    public ResponseEntity<List<UserResponse>> getTenantUsers(
        @Parameter(description = "Tenant ID") @PathVariable String tenantId
    ) {
        log.debug("Retrieving users for tenant {}", tenantId);
        List<User> users = userService.getUsersByTenant(tenantId);
        return ResponseEntity.ok(users.stream().map(UserResponse::fromEntity).collect(Collectors.toList()));
    }

    /**
     * Get users for a shop.
     *
     * @param shopId Shop ID
     * @return List of users
     */
    @Operation(
        summary = "Get shop users",
        description = "Retrieves all users in a shop."
    )
    @GetMapping("/shops/{shopId}/users")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<List<UserResponse>> getShopUsers(
        @Parameter(description = "Shop ID") @PathVariable String shopId
    ) {
        log.debug("Retrieving users for shop {}", shopId);
        List<User> users = userService.getUsersByShop(shopId);
        return ResponseEntity.ok(users.stream().map(UserResponse::fromEntity).collect(Collectors.toList()));
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
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
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
        description = "Updates an existing user."
    )
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<UserResponse> updateUser(
        @Parameter(description = "User ID") @PathVariable String userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("Updating user {}", userId);
        User user = userService.updateUser(userId, request);
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
        description = "Soft deletes a user by setting status to INACTIVE."
    )
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('TENANT_ADMIN') or hasRole('OWNER') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteUser(
        @Parameter(description = "User ID") @PathVariable String userId
    ) {
        log.info("Deleting user {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}