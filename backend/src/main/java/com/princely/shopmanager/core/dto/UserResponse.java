package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for user response with complete user information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User's unique ID", example = "user-123")
    private String id;

    @Schema(description = "Username", example = "john.doe")
    private String username;

    @Schema(description = "Email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "User status", example = "ACTIVE")
    private String status;

    @Schema(description = "Keycloak user ID", example = "keycloak-user-id")
    private String keycloakId;

    @Schema(description = "Tenant ID", example = "tenant-123")
    private String tenantId;

    @Schema(description = "Shop ID", example = "shop-123")
    private String shopId;

    @Schema(description = "User's assigned roles")
    private Set<RoleResponse> roles;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Convert User entity to UserResponse DTO.
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .fullName(user.getFullName())
            .phoneNumber(user.getPhoneNumber())
            .status(user.getStatus() != null ? user.getStatus().name() : null)
            .keycloakId(user.getKeycloakId())
            .tenantId(user.getTenant() != null ? user.getTenant().getId() : null)
            .shopId(user.getShop() != null ? user.getShop().getId() : null)
            .roles(user.getRoles() != null ?
                user.getRoles().stream()
                    .map(RoleResponse::fromEntity)
                    .collect(Collectors.toSet()) :
                Set.of())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
