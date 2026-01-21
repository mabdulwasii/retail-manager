package com.princely.shopmanager.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating inventory unit prices.
 * Unit prices are batch-specific (different batches can have different prices).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUnitPriceRequest {

    /**
     * Inventory ID to which this unit price belongs.
     * Automatically set from path parameter in controller.
     */
    private String inventoryId;

    /**
     * Unit type (required, 1-50 characters).
     * Must match a unit_type in product_unit_definitions.
     * Examples: piece, pack, half_pack, carton, roll
     */
    @NotBlank(message = "Unit type is required")
    @Size(min = 1, max = 50, message = "Unit type must be between 1 and 50 characters")
    private String unitType;

    /**
     * Selling price for this unit in this specific batch (required, must be non-negative).
     * Example: ₦12,000 for a pack, ₦1,050 for a piece
     */
    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price must be non-negative")
    private BigDecimal sellingPrice;
}
