package com.princely.shopmanager.core.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating product unit definitions.
 * Unit definitions are catalog-level structures (no prices).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDefinitionRequest {

    /**
     * Product ID to which this unit definition belongs.
     * Automatically set from path parameter in controller.
     */
    private String productId;

    /**
     * Unit identifier (required, 1-50 characters).
     * Examples: piece, pack, half_pack, quarter_pack, carton, roll, custom
     * Must be unique per product.
     */
    @NotBlank(message = "Unit type is required")
    @Size(min = 1, max = 50, message = "Unit type must be between 1 and 50 characters")
    private String unitType;

    /**
     * Display label (required, 1-100 characters).
     * Examples: "Piece", "Pack (12pcs)", "Half Pack (6pcs)", "Carton (2 packs)"
     */
    @NotBlank(message = "Unit label is required")
    @Size(min = 1, max = 100, message = "Unit label must be between 1 and 100 characters")
    private String unitLabel;

    /**
     * How many base units in this unit (required, must be positive).
     * Example: If base unit is "piece" and this is "pack" of 12, conversionFactor = 12.0
     */
    @NotNull(message = "Conversion factor is required")
    @DecimalMin(value = "0.0001", inclusive = true, message = "Conversion factor must be greater than 0")
    private BigDecimal conversionFactor;

    /**
     * True for the smallest unit (usually piece, kg, liter, etc.).
     * Base unit always has conversionFactor = 1.0
     * Defaults to false.
     */
    @Builder.Default
    private Boolean isBaseUnit = false;

    /**
     * Sort order for display (0 = first, higher numbers displayed later).
     * Defaults to 0.
     */
    @Builder.Default
    @Min(value = 0, message = "Sort order must be non-negative")
    private Integer sortOrder = 0;
}
