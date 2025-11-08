package com.princely.shopmanager.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Schema(description = "Unit cost for this batch", example = "15.50")
    @Min(value = 0, message = "Unit cost cannot be negative")
    private BigDecimal unitCost;
}
