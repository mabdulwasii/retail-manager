package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for bulk updating role permissions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to bulk update role permissions")
public class RolePermissionUpdateRequest {

    @NotEmpty(message = "Permission identifiers are required")
    @Schema(description = "Set of permission IDs or names to assign to the role (replaces all existing)")
    private Set<String> permissionIdentifiers;
}
