package com.princely.shopmanager.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for inventory unit price information.
 * Contains batch-specific selling prices for each unit type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUnitPriceResponse {

    /**
     * Unit price unique identifier.
     */
    private String id;

    /**
     * Inventory ID to which this unit price belongs.
     */
    private String inventoryId;

    /**
     * Product name (for display purposes).
     */
    private String productName;

    /**
     * Batch number (for display purposes).
     */
    private String batchNumber;

    /**
     * Unit type.
     * Examples: piece, pack, half_pack, carton, roll
     */
    private String unitType;

    /**
     * Unit label from product definition (for display purposes).
     * Examples: "Piece", "Pack (12pcs)", "Half Pack (6pcs)"
     */
    private String unitLabel;

    /**
     * Conversion factor from product definition (for reference).
     * Shows how many base units are in this unit.
     */
    private BigDecimal conversionFactor;

    /**
     * Selling price for this unit in this specific batch.
     * Example: ₦12,000 for a pack, ₦1,050 for a piece
     */
    private BigDecimal sellingPrice;

    /**
     * Unit price creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Unit price last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * User who created the unit price.
     */
    private String createdBy;

    /**
     * User who last updated the unit price.
     */
    private String updatedBy;
}
