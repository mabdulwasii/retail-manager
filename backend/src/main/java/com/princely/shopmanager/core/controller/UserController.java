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