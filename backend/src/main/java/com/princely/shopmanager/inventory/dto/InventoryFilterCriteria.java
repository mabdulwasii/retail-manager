package com.princely.shopmanager.inventory.dto;

import com.princely.shopmanager.inventory.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter criteria for inventory search operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFilterCriteria {

    private String searchQuery;
    private Inventory.InventoryStatus status;
    private String category;
    private String location;
    private Boolean isLowStock;
    private Boolean isExpired;
    private Boolean isExpiringSoon;
    private Integer minStock;
    private Integer maxStock;
    private Integer expiringDays;
}