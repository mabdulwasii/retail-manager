package com.princely.shopmanager.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Request DTO for updating inventory metadata.
 * Used to correct data entry errors or update batch information.
 *
 * <p>Note: This does NOT update stock quantities. Use the adjust-stock endpoint for that.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update inventory metadata (batch info, thresholds, location)")
public class InventoryUpdateRequest {

    @Schema(description = "Batch or lot number for tracking", example = "BATCH-2025-001")
    @Size(max = 100, message = "Batch number cannot exceed 100 characters")
    private String batchNumber;

    @Schema(description = "Storage location", example = "Warehouse A - Shelf 3")
    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    @Schema(description = "Product expiry date (if applicable)", example = "2025-12-31")
    private LocalDate expiryDate;

    @Schema(description = "Minimum stock level before reorder", example = "10")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock;

    @Schema(description = "Maximum stock capacity", example = "1000")
    @Min(value = 0, message = "Maximum stock cannot be negative")
    private Integer maximumStock;

    @Schema(description = "Reorder point threshold", example = "20")
    @Min(value = 0, message = "Reorder point cannot be negative")
    private Integer reorderPoint;

    @Schema(description = "Cost price for this batch", example = "15.50")
    @Min(value = 0, message = "Cost price cannot be negative")
    private BigDecimal costPrice;

    @Schema(description = "Selling price for this batch", example = "25.00")
    @Min(value = 0, message = "Selling price cannot be negative")
    private BigDecimal sellingPrice;

    @Schema(description = "Smallest sellable unit for stock tracking", example = "piece")
    @Size(max = 50, message = "Base unit must not exceed 50 characters")
    private String baseUnit;

    @Schema(description = "Unit in which this batch was purchased", example = "pack")
    @Size(max = 50, message = "Purchase unit must not exceed 50 characters")
    private String purchaseUnit;

    @Schema(description = "Quantity purchased in purchase_unit", example = "10")
    @DecimalMin(value = "0.0", inclusive = false, message = "Purchase quantity must be positive")
    private BigDecimal purchaseQuantity;

    @Schema(description = "Cost per purchase_unit", example = "12000.00")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase unit cost must be non-negative")
    private BigDecimal purchaseUnitCost;

    @Schema(description = "Batch-specific selling prices for each unit type")
    @Builder.Default
    private List<InventoryUnitPriceRequest> unitPrices = new ArrayList<>();
}

