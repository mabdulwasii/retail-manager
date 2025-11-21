package com.princely.shopmanager.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for category information.
 * Supports hierarchical representation with parent and children.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    /**
     * Category unique identifier.
     */
    private String id;

    /**
     * Shop ID to which this category belongs.
     */
    private String shopId;

    /**
     * Category name.
     */
    private String name;

    /**
     * Category description.
     */
    private String description;

    /**
     * Parent category ID (null for root categories).
     */
    private String parentId;

    /**
     * Parent category name (for display purposes).
     */
    private String parentName;

    /**
     * Child categories (for tree representation).
     * Empty list if no children.
     */
    @Builder.Default
    private List<CategoryResponse> children = new ArrayList<>();

    /**
     * Number of products in this category.
     */
    @Builder.Default
    private Long productCount = 0L;

    /**
     * Display order for sorting.
     */
    private Integer displayOrder;

    /**
     * Active status.
     */
    private Boolean isActive;

    /**
     * Category image URL.
     */
    private String imageUrl;

    /**
     * Category creation timestamp.
     */
    private LocalDateTime createdAt;

    /**
     * Category last update timestamp.
     */
    private LocalDateTime updatedAt;

    /**
     * User who created the category.
     */
    private String createdBy;

    /**
     * User who last updated the category.
     */
    private String updatedBy;

    /**
     * Hierarchical level (0 for root, 1 for first level children, etc.).
     * Useful for tree rendering.
     */
    @Builder.Default
    private Integer level = 0;

    /**
     * Full path from root to this category (e.g., "Electronics > Computers > Laptops").
     */
    private String fullPath;
}
