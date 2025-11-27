package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for product responses.
 * Includes product catalog information and optional inventory summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product response with catalog information")
public class ProductResponse {

    @Schema(description = "Product ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Product name", example = "Coca-Cola 500ml")
    private String name;

    @Schema(description = "Product description", example = "Refreshing carbonated soft drink")
    private String description;

    @Schema(description = "Stock Keeping Unit", example = "COCA-500ML")
    private String sku;

    @Schema(description = "Product barcode", example = "5449000000996")
    private String barcode;

    @Schema(description = "Shop ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String shopId;

    @Schema(description = "Shop name", example = "Downtown Store")
    private String shopName;

    @Schema(description = "Category ID", example = "789e0123-e45b-67d8-a901-234567890000")
    private String categoryId;

    @Schema(description = "Category name", example = "Beverages")
    private String categoryName;

    @Schema(description = "Unit of measurement", example = "bottle")
    private String unit;

    @Schema(description = "Weight in grams", example = "520.5")
    private Double weightInGrams;

    @Schema(description = "Product location", example = "Aisle 3, Shelf B")
    private String location;

    @Schema(description = "Product dimensions", example = "20cm x 10cm x 25cm")
    private String dimensions;

    @Schema(description = "Supplier name", example = "Coca-Cola Bottling Company")
    private String supplierName;

    @Schema(description = "Supplier contact", example = "+234-800-COCA-COLA")
    private String supplierContact;

    @Schema(description = "Product image URL", example = "https://cdn.example.com/products/coca-cola-500ml.jpg")
    private String imageUrl;

    @Schema(description = "Product status", example = "ACTIVE")
    private Product.ProductStatus status;

    @Schema(description = "Is product taxable", example = "true")
    private boolean isTaxable;

    @Schema(description = "Is product eligible for discounts", example = "true")
    private boolean isDiscountable;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;

    @Schema(description = "Total stock across all inventory records (if included)", example = "330")
    private Integer totalStock;

    @Schema(description = "Available stock (current - reserved) across all inventory (if included)", example = "315")
    private Integer availableStock;

    @Schema(description = "Reserved stock across all inventory (if included)", example = "15")
    private Integer reservedStock;

    @Schema(description = "Number of inventory batches/locations (if included)", example = "3")
    private Integer inventoryCount;

    @Schema(description = "Has low stock in any inventory location (if included)", example = "false")
    private Boolean hasLowStock;

    @Schema(description = "Has expired inventory batches (if included)", example = "false")
    private Boolean hasExpiredBatches;

    @Schema(description = "Creation timestamp", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2024-06-20T14:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Created by user ID", example = "user-123")
    private String createdBy;

    @Schema(description = "Last updated by user ID", example = "user-456")
    private String updatedBy;
}
