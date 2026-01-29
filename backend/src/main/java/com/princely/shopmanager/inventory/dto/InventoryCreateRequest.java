package com.princely.shopmanager.inventory.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateRequest {

    @NotNull(message = "Product ID is required")
    private String productId;

    @Min(value = 0, message = "Minimum stock cannot be negative")
    @Builder.Default
    private Integer minimumStock = 0;

    private Integer maximumStock;

    @Min(value = 0, message = "Reorder point cannot be negative")
    @Builder.Default
    private Integer reorderPoint = 0;

    @NotNull(message = "Cost price is required")
    @Min(value = 0, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @NotNull(message = "Selling price is required")
    @Min(value = 0, message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    /**
     * Smallest sellable unit for stock tracking (piece, kg, liter, etc.).
     * Defaults to "piece".
     */
    @Size(max = 50, message = "Base unit must not exceed 50 characters")
    @Builder.Default
    private String baseUnit = "piece";

    /**
     * Unit in which this batch was purchased (e.g., pack, carton).
     * Optional - used for purchase tracking.
     */
    @Size(max = 50, message = "Purchase unit must not exceed 50 characters")
    private String purchaseUnit;

    /**
     * Quantity purchased in purchase_unit (e.g., 10 packs).
     * Optional - used for purchase tracking.
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase quantity must be positive")
    private BigDecimal purchaseQuantity;

    /**
     * Total cost for all purchased quantity (e.g., ₦106,000 for 20 packs).
     * System will calculate cost per unit from this: totalPurchaseCost / purchaseQuantity
     * Optional - used for purchase tracking.
     */
    @DecimalMin(value = "0.0", inclusive = true, message = "Total purchase cost must be non-negative")
    private BigDecimal totalPurchaseCost;

    /**
     * Batch-specific selling prices for each unit type.
     * Optional - for multi-unit pricing support.
     */
    @Builder.Default
    private List<InventoryUnitPriceRequest> unitPrices = new ArrayList<>();

    private String location;

    private String batchNumber;

    private LocalDate expiryDate;
}