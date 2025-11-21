package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Permission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for permission information response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permission information response")
public class PermissionResponse {

    @Schema(description = "Permission's unique ID", example = "perm-product-create")
    private String id;

    @Schema(description = "Permission name", example = "PRODUCT_CREATE")
    private String name;

    @Schema(description = "Permission description", example = "Create new products")
    private String description;

    @Schema(description = "Resource the permission applies to", example = "PRODUCT")
    private String resource;

    @Schema(description = "Action the permission allows", example = "CREATE")
    private String action;

    /**
     * Convert Permission entity to PermissionResponse DTO.
     *
     * @param permission Permission entity
     * @return PermissionResponse DTO
     */
    public static PermissionResponse fromEntity(Permission permission) {
        return PermissionResponse.builder()
            .id(permission.getId())
            .name(permission.getName())
            .description(permission.getDescription())
            .resource(permission.getResource())
            .action(permission.getAction())
            .build();
    }
}
