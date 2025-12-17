package com.princely.shopmanager.core.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for transferring a user to a different shop within the same tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to transfer a user to a different shop")
public class UserShopTransferRequest {

    @NotBlank(message = "New shop ID is required")
    @Schema(description = "ID of the shop to transfer the user to", example = "shop-456")
    private String newShopId;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Schema(description = "Reason for the transfer (for audit trail)", example = "User relocated to new branch")
    private String reason;
}
