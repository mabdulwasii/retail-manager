package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for role information response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Role information response")
public class RoleResponse {

    @Schema(description = "Role's unique ID", example = "role-123")
    private String id;

    @Schema(description = "Role name", example = "MANAGER")
    private String name;

    @Schema(description = "Role description", example = "Shop manager with full shop access")
    private String description;

    @Schema(description = "Whether this is a system role", example = "false")
    private Boolean isSystem;

    @Schema(description = "Tenant ID for custom roles (null for system roles)", example = "tenant-123")
    private String tenantId;

    @Schema(description = "Permissions assigned to this role")
    private Set<String> permissions;

    /**
     * Convert Role entity to RoleResponse DTO.
     *
     * @param role Role entity
     * @return RoleResponse DTO
     */
    public static RoleResponse fromEntity(Role role) {
        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .isSystem(role.isSystem())
            .tenantId(role.getTenant() != null ? role.getTenant().getId() : null)
            .permissions(role.getPermissions() != null ?
                role.getPermissions().stream()
                    .map(p -> p.getName())
                    .collect(Collectors.toSet()) :
                Set.of())
            .build();
    }
}
