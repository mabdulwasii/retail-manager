package com.princely.shopmanager.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new product category.
 * Categories are shop-scoped and support hierarchical structures (parent-child).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {

    /**
     * Shop ID to which this category belongs.
     * Automatically set from path parameter in controller.
     */
    private String shopId;

    /**
     * Category name (required, 1-100 characters).
     * Must be unique within the shop.
     */
    @NotBlank(message = "Category name is required")
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    /**
     * Category description (optional, max 500 characters).
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Parent category ID for hierarchical categories (optional).
     * If null, this is a root-level category.
     */
    private String parentId;

    /**
     * Display order for sorting categories (optional, defaults to 0).
     */
    @Builder.Default
    private Integer displayOrder = 0;

    /**
     * Active status (optional, defaults to true).
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Category image URL (optional).
     */
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;
}
