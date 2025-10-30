package com.princely.shopmanager.core.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing product category.
 * All fields are optional - only provided fields will be updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {

    /**
     * Category name (optional, 1-100 characters).
     */
    @Size(min = 1, max = 100, message = "Category name must be between 1 and 100 characters")
    private String name;

    /**
     * Category description (optional, max 500 characters).
     */
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    /**
     * Parent category ID for hierarchical categories (optional).
     * Set to null to make this a root-level category.
     */
    private String parentId;

    /**
     * Display order for sorting categories (optional).
     */
    private Integer displayOrder;

    /**
     * Active status (optional).
     */
    private Boolean isActive;

    /**
     * Category image URL (optional).
     */
    @Size(max = 255, message = "Image URL must not exceed 255 characters")
    private String imageUrl;
}
