package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for validating category hierarchy operations.
 * Handles parent validation, circular reference detection, and shop validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryHierarchyValidator {

    private final CategoryRepository categoryRepository;

    /**
     * Validates and sets the parent category based on the request.
     * Handles empty parent (root category), parent validation, and circular reference prevention.
     *
     * @param category Category being updated
     * @param parentId Requested parent ID (can be empty for root category)
     * @param currentCategoryId Current category ID (for circular reference check)
     * @throws IllegalArgumentException if parent is invalid or would create circular reference
     */
    public void validateAndSetParent(Category category, String parentId, String currentCategoryId) {
        if (parentId == null) {
            return; // No parent update requested
        }

        if (parentId.trim().isEmpty()) {
            // Empty string means remove parent (make it a root category)
            category.setParent(null);
            return;
        }

        // Validate parent exists and belongs to same shop
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + parentId));

        if (!parent.getShop().getId().equals(category.getShop().getId())) {
            throw new IllegalArgumentException("Parent category must belong to the same shop");
        }

        // Prevent circular references
        if (wouldCreateCircularReference(parentId, currentCategoryId)) {
            throw new IllegalArgumentException("Cannot set parent - would create circular reference");
        }

        category.setParent(parent);
    }

    /**
     * Checks if setting a parent would create a circular reference in the category hierarchy.
     *
     * @param parentId ID of the proposed parent
     * @param categoryId ID of the category being updated (null for new categories)
     * @return true if circular reference would be created, false otherwise
     */
    public boolean wouldCreateCircularReference(String parentId, String categoryId) {
        if (categoryId == null) {
            return false; // New category, no circular reference possible
        }

        if (parentId.equals(categoryId)) {
            return true; // Category cannot be its own parent
        }

        // Traverse up the parent chain to check for circular reference
        Category current = categoryRepository.findById(parentId).orElse(null);
        while (current != null) {
            if (current.getId().equals(categoryId)) {
                return true; // Found circular reference
            }
            current = current.getParent();
        }

        return false; // No circular reference found
    }

    /**
     * Validates that a category name is unique within a shop (excluding a specific category).
     *
     * @param name Category name to check
     * @param shopId Shop ID
     * @param excludeCategoryId Category ID to exclude from uniqueness check (for updates)
     * @throws IllegalArgumentException if name already exists
     */
    public void validateNameUniqueness(String name, String shopId, String excludeCategoryId) {
        categoryRepository.findByNameAndShop_Id(name, shopId)
            .ifPresent(existing -> {
                if (!existing.getId().equals(excludeCategoryId)) {
                    throw new IllegalArgumentException("Category with name '" + name + "' already exists in this shop");
                }
            });
    }
}
