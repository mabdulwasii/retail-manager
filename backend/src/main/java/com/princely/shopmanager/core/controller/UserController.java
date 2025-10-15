package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.core.dto.UserProfileResponse;
import com.princely.shopmanager.core.service.UserService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Profile", description = "User profile management operations")
@SecurityRequirement(name = "bearer-jwt")
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
}