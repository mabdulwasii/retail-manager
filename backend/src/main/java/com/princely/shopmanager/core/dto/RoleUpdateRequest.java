package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating role details.
 * Note: This does NOT update permissions - use RolePermissionUpdateRequest for that.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update role details")
public class RoleUpdateRequest {

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Updated role description")
    private String description;
}
