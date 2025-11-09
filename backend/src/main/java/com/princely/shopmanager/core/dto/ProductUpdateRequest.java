package com.princely.shopmanager.core.dto;

import com.princely.shopmanager.core.domain.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Data Transfer Object for updating an existing product.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating a product")
public class ProductUpdateRequest {

    @Schema(description = "Product name", example = "Coca-Cola 500ml")
    @Size(min = ProductCreateRequest.MIN_NAME_LENGTH, max = ProductCreateRequest.MAX_NAME_LENGTH,
          message = "Product name must be between " + ProductCreateRequest.MIN_NAME_LENGTH +
                    " and " + ProductCreateRequest.MAX_NAME_LENGTH + " characters")
    private String name;

    @Schema(description = "Product description", example = "Refreshing carbonated soft drink")
    @Size(max = ProductCreateRequest.MAX_DESCRIPTION_LENGTH,
          message = "Description must not exceed " + ProductCreateRequest.MAX_DESCRIPTION_LENGTH + " characters")
    private String description;

    @Schema(description = "Product barcode", example = "5449000000996")
    @Size(max = ProductCreateRequest.MAX_BARCODE_LENGTH,
          message = "Barcode must not exceed " + ProductCreateRequest.MAX_BARCODE_LENGTH + " characters")
    private String barcode;

    @Schema(description = "Category ID")
    private String categoryId;

    @Schema(description = "Selling price", example = "500.00")
    @DecimalMin(value = "0.01", message = "Price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal price;

    @Schema(description = "Cost price", example = "350.00")
    @DecimalMin(value = "0.00", message = "Cost price cannot be negative")
    @Digits(integer = 10, fraction = 2, message = "Cost price must have at most 10 integer digits and 2 decimal places")
    private BigDecimal costPrice;

    @Schema(description = "Unit of measurement", example = "bottle")
    @Size(max = ProductCreateRequest.MAX_UNIT_LENGTH,
          message = "Unit must not exceed " + ProductCreateRequest.MAX_UNIT_LENGTH + " characters")
    private String unit;

    @Schema(description = "Weight in grams", example = "520.5")
    @DecimalMin(value = "0.0", message = "Weight cannot be negative")
    private Double weightInGrams;

    @Schema(description = "Product dimensions", example = "20cm x 10cm x 25cm")
    @Size(max = ProductCreateRequest.MAX_DIMENSIONS_LENGTH,
          message = "Dimensions must not exceed " + ProductCreateRequest.MAX_DIMENSIONS_LENGTH + " characters")
    private String dimensions;

    @Schema(description = "Supplier name", example = "Coca-Cola Bottling Company")
    @Size(max = ProductCreateRequest.MAX_SUPPLIER_NAME_LENGTH,
          message = "Supplier name must not exceed " + ProductCreateRequest.MAX_SUPPLIER_NAME_LENGTH + " characters")
    private String supplierName;

    @Schema(description = "Supplier contact", example = "+234-800-COCA-COLA")
    @Size(max = ProductCreateRequest.MAX_SUPPLIER_CONTACT_LENGTH,
          message = "Supplier contact must not exceed " + ProductCreateRequest.MAX_SUPPLIER_CONTACT_LENGTH + " characters")
    private String supplierContact;

    @Schema(description = "Product image URL", example = "https://cdn.example.com/products/coca-cola-500ml.jpg")
    private String imageUrl;

    @Schema(description = "Product status", example = "ACTIVE")
    private Product.ProductStatus status;

    @Schema(description = "Is product taxable", example = "true")
    private Boolean isTaxable;

    @Schema(description = "Is product eligible for discounts", example = "true")
    private Boolean isDiscountable;

    @Schema(description = "Additional metadata")
    private Map<String, Object> metadata;
}
