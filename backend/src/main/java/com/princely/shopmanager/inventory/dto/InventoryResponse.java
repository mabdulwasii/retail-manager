package com.princely.shopmanager.inventory.dto;

import com.princely.shopmanager.inventory.domain.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {

    private String id;
    private String shopId;
    private String shopName;
    private String productId;
    private String productName;
    private String productSku;
    private Integer currentStock;
    private Integer reservedStock;
    private Integer availableStock;
    private Integer minimumStock;
    private Integer maximumStock;
    private Integer reorderPoint;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String location;
    private String batchNumber;
    private LocalDate expiryDate;
    private Inventory.InventoryStatus status;
    private LocalDateTime lastStockUpdate;
    private boolean isLowStock;
    private boolean isExpired;
    private boolean isExpiringSoon;

    // Financial projections for this item
    private BigDecimal itemTotalCost;          // currentStock × costPrice
    private BigDecimal itemProjectedSales;     // currentStock × sellingPrice
    private BigDecimal itemProjectedProfit;    // itemProjectedSales - itemTotalCost

    // Multi-unit pricing fields
    private String baseUnit;                   // Smallest sellable unit (piece, kg, liter)
    private String purchaseUnit;               // Unit in which batch was purchased
    private BigDecimal purchaseQuantity;       // Quantity purchased in purchase_unit
    private BigDecimal purchaseUnitCost;       // Cost per purchase_unit

    // Batch-specific unit prices
    @Builder.Default
    private List<InventoryUnitPriceResponse> unitPrices = new ArrayList<>();
}