package com.princely.shopmanager.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for product unit definition information.
 * Contains catalog-level unit structure (no prices).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitDefinitionResponse {

    /**
     * Unit definition unique identifier.
     */
    private String id;

    /**
     * Product ID to which this unit definition belongs.
     */
    private String productId;

    /**
     * Product name (for display purposes).
     */
    private String productName;

    /**
     * Unit identifier.
     * Examples: piece, pack, half_pack, quarter_pack, carton, roll, custom
     */
    private String unitType;

    /**
     * Display label.
     * Examples: "Piece", "Pack (12pcs)", "Half Pack (6pcs)", "Carton (2 packs)"
     */
    private String unitLabel;

    /**
     * How many base units in this unit.
     * Example: If base unit is "piece" and this is "pack" of 12, conversionFactor = 12.0
     */
    private BigDecimal conversionFactor;

    /**
     * True for the smallest unit (usually piece, kg, liter, etc.).
     * Base unit always has conversionFactor = 1.0
     */
    private Boolean isBaseUnit;

    /**
     * Sort order for display (0 = first, higher numbers displayed later).
     */
    private Integer sortOrder;

    /**
     * Unit definition creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Unit definition last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * User who created the unit definition.
     */
    private String createdBy;

    /**
     * User who last updated the unit definition.
     */
    private String updatedBy;
}
