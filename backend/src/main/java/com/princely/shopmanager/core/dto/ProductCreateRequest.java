package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Product.ProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Data Transfer Object for creating a new product.
 * Product represents the master catalog item (what you sell).
 * Stock tracking is handled separately through Inventory records.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for creating a new product in the catalog")
public class ProductCreateRequest {

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 200;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_BARCODE_LENGTH = 100;
    public static final int MAX_UNIT_LENGTH = 50;
    public static final int MAX_SUPPLIER_NAME_LENGTH = 200;
    public static final int MAX_SUPPLIER_CONTACT_LENGTH = 200;
    public static final int MAX_DIMENSIONS_LENGTH = 100;

    @Schema(description = "Product name", example = "Coca-Cola 500ml", required = true)
    @NotBlank(message = "Product name is required")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH,
          message = "Product name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters")
    private String name;

    @Schema(description = "Product description", example = "Refreshing carbonated soft drink")
    @Size(max = MAX_DESCRIPTION_LENGTH, message = "Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters")
    private String description;

    @Schema(description = "Product barcode (EAN, UPC, etc.)", example = "5449000000996")
    @Size(max = MAX_BARCODE_LENGTH, message = "Barcode must not exceed " + MAX_BARCODE_LENGTH + " characters")
    private String barcode;

    @Schema(description = "Shop ID", required = true)
    @NotBlank(message = "Shop ID is required")
    private String shopId;

    @Schema(description = "Category ID")
    private String categoryId;

    @Schema(description = "Unit of measurement", example = "bottle")
    @Size(max = MAX_UNIT_LENGTH, message = "Unit must not exceed " + MAX_UNIT_LENGTH + " characters")
    private String unit;

    @Schema(description = "Weight in grams", example = "520.5")
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private Double weightInGrams;

    @Schema(description = "Product dimensions (L x W x H)", example = "20cm x 10cm x 25cm")
    @Size(max = MAX_DIMENSIONS_LENGTH, message = "Dimensions must not exceed " + MAX_DIMENSIONS_LENGTH + " characters")
    private String dimensions;

    @Schema(description = "Supplier name", example = "Coca-Cola Bottling Company")
    @Size(max = MAX_SUPPLIER_NAME_LENGTH, message = "Supplier name must not exceed " + MAX_SUPPLIER_NAME_LENGTH + " characters")
    private String supplierName;

    @Schema(description = "Supplier contact information", example = "+234-800-COCA-COLA")
    @Size(max = MAX_SUPPLIER_CONTACT_LENGTH, message = "Supplier contact must not exceed " + MAX_SUPPLIER_CONTACT_LENGTH + " characters")
    private String supplierContact;

    @Schema(description = "Product image URL", example = "https://cdn.example.com/products/coca-cola-500ml.jpg")
    private String imageUrl;

    @Schema(description = "Is product taxable", example = "true")
    @Builder.Default
    private Boolean isTaxable = true;

    @Schema(description = "Is product eligible for discounts", example = "true")
    @Builder.Default
    private Boolean isDiscountable = true;

    @Schema(description = "Product status", example = "ACTIVE")
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @Schema(description = "Additional metadata (JSON format)")
    private Map<String, Object> metadata;
}
