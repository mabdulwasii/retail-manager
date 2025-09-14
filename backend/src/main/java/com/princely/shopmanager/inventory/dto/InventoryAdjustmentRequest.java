package com.princely.shopmanager.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAdjustmentRequest {

    @NotNull(message = "New stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer newStock;

    @NotBlank(message = "Reason is required for stock adjustment")
    private String reason;
}