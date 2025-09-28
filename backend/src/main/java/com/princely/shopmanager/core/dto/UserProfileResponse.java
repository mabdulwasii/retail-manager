package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user profile information.
 */
@Data
@Builder
@Schema(description = "User profile information")
public class UserProfileResponse {

    @Schema(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
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

    @Schema(description = "Whether user is an investor", example = "false")
    private boolean isInvestor;

    @Schema(description = "User roles", example = "[\"SHOP_MANAGER\", \"SHOP_EMPLOYEE\"]")
    private List<String> roles;

    @Schema(description = "Tenant ID", example = "tenant-123")
    private String tenantId;

    @Schema(description = "Shop ID", example = "shop-456")
    private String shopId;

    @Schema(description = "Account creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Last update date")
    private LocalDateTime updatedAt;
}