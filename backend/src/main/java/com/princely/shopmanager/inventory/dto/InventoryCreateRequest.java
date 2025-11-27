package com.princely.shopmanager.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCreateRequest {

    @NotNull(message = "Product ID is required")
    private String productId;

    @Min(value = 0, message = "Current stock cannot be negative")
    @Builder.Default
    private Integer currentStock = 0;

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

    private String location;

    private String batchNumber;

    private LocalDate expiryDate;
}