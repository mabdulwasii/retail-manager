package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for grouped permissions by resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Permissions grouped by resource")
public class PermissionGroupResponse {

    @Schema(description = "Resource name", example = "PRODUCT")
    private String resource;

    @Schema(description = "List of permissions for this resource")
    private List<PermissionResponse> permissions;

    @Schema(description = "Total count of permissions in this group")
    private int count;
}
