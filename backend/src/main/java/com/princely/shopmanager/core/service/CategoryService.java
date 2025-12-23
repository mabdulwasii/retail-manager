package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.CategoryCreateRequest;
import com.princely.shopmanager.core.dto.CategoryResponse;
import com.princely.shopmanager.core.dto.CategoryUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.ShopAwareService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing product categories.
 * Categories are shop-scoped and support hierarchical structures.
 */
@Service
@Slf4j
public class CategoryService extends ShopAwareService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository
    ) {
        super(shopAccessValidator, shopRepository);
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Helper method to find a category and validate shop access.
     */
    private Category findCategoryForUser(String categoryId, JwtPrincipal principal) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));

        if (shopAccessValidator.hasNoAccessToShop(category.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this category");
        }

        return category;
    }

    /**
     * Create a new category.
     *
     * @param request Category creation request
     * @param principal JWT principal for access control
     * @return Created category response
     */
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request, JwtPrincipal principal) {
        log.info("Creating category: {} for shop: {}", request.getName(), request.getShopId());

        // Validate shop access
        validateShopAccess(request.getShopId(), principal);

        // Validate shop exists
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + request.getShopId()));

        // Check for duplicate name within shop
        if (categoryRepository.existsByNameAndShop_Id(request.getName(), request.getShopId())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists in this shop");
        }

        // Validate and fetch parent category if provided
        Category parent = null;
        if (request.getParentId() != null && !request.getParentId().trim().isEmpty()) {
            parent = categoryRepository.findById(request.getParentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + request.getParentId()));

            // Ensure parent belongs to same shop
            if (!parent.getShop().getId().equals(request.getShopId())) {
                throw new IllegalArgumentException("Parent category must belong to the same shop");
            }

            // Prevent circular references
            if (wouldCreateCircularReference(request.getParentId(), null)) {
                throw new IllegalArgumentException("Cannot set parent - would create circular reference");
            }
        }

        // Build and save category
        Category category = Category.builder()
            .shop(shop)
            .name(request.getName())
            .description(request.getDescription())
            .parent(parent)
            .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
            .isActive(Optional.ofNullable(request.getIsActive()).orElse(true))
            .imageUrl(request.getImageUrl())
            .build();

        category = categoryRepository.save(category);
        log.info("Created category with ID: {}", category.getId());

        return mapToResponse(category);
    }

    /**
     * Get category by ID.
     *
     * @param categoryId Category ID
     * @param principal JWT principal for access control
     * @return Category response
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(String categoryId, JwtPrincipal principal) {
        log.debug("Fetching category by ID: {}", categoryId);

        Category category = findCategoryForUser(categoryId, principal);

        return mapToResponse(category);
    }

    /**
     * Get all categories for a shop.
     *
     * @param shopId Shop ID
     * @param includeTree Whether to include hierarchical tree structure
     * @param principal JWT principal for access control
     * @return List of categories
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByShop(String shopId, boolean includeTree, JwtPrincipal principal) {
        log.debug("Fetching categories for shop: {}, includeTree: {}", shopId, includeTree);

        // Validate shop access
        validateShopAccess(shopId, principal);

        // Validate shop exists
        if (!shopRepository.existsById(shopId)) {
            throw new IllegalArgumentException("Shop not found with ID: " + shopId);
        }

        List<Category> categories = categoryRepository.findByShopId(shopId);

        if (includeTree) {
            // Build hierarchical tree structure
            return buildCategoryTree(categories);
        } else {
            // Return flat list
            return categories.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        }
    }

    /**
     * Update an existing category.
     *
     * @param categoryId Category ID
     * @param request Update request
     * @param principal JWT principal for access control
     * @return Updated category response
     */
    @Transactional
    public CategoryResponse updateCategory(String categoryId, CategoryUpdateRequest request, JwtPrincipal principal) {
        log.info("Updating category: {}", categoryId);

        Category category = findCategoryForUser(categoryId, principal);

        // Update fields if provided
        if (request.getName() != null) {
            // Check for duplicate name (excluding current category)
            categoryRepository.findByNameAndShop_Id(request.getName(), category.getShop().getId())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(categoryId)) {
                        throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists in this shop");
                    }
                });
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getParentId() != null) {
            if (request.getParentId().trim().isEmpty()) {
                // Empty string means remove parent (make it a root category)
                category.setParent(null);
            } else {
                // Validate parent exists and belongs to same shop
                Category parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found with ID: " + request.getParentId()));

                if (!parent.getShop().getId().equals(category.getShop().getId())) {
                    throw new IllegalArgumentException("Parent category must belong to the same shop");
                }

                // Prevent circular references
                if (wouldCreateCircularReference(request.getParentId(), categoryId)) {
                    throw new IllegalArgumentException("Cannot set parent - would create circular reference");
                }

                category.setParent(parent);
            }
        }

        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getIsActive() != null) {
            category.setActive(request.getIsActive());
        }

        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }

        category = categoryRepository.save(category);
        log.info("Updated category with ID: {}", category.getId());

        return mapToResponse(category);
    }

    /**
     * Delete a category.
     *
     * @param categoryId Category ID
     * @param principal JWT principal for access control
     */
    @Transactional
    public void deleteCategory(String categoryId, JwtPrincipal principal) {
        log.info("Deleting category: {}", categoryId);

        Category category = findCategoryForUser(categoryId, principal);

        // Check if category has products
        long productCount = productRepository.countByCategory_Id(categoryId);
        if (productCount > 0) {
            throw new IllegalStateException("Cannot delete category with " + productCount + " products. Please reassign or delete products first.");
        }

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with child categories. Please delete or reassign child categories first.");
        }

        categoryRepository.delete(category);
        log.info("Deleted category with ID: {}", categoryId);
    }

    /**
     * Map Category entity to CategoryResponse DTO.
     */
    private CategoryResponse mapToResponse(Category category) {
        long productCount = productRepository.countByCategory_Id(category.getId());

        return CategoryResponse.builder()
            .id(category.getId())
            .shopId(category.getShop().getId())
            .name(category.getName())
            .description(category.getDescription())
            .parentId(category.getParent() != null ? category.getParent().getId() : null)
            .parentName(category.getParent() != null ? category.getParent().getName() : null)
            .productCount(productCount)
            .displayOrder(category.getDisplayOrder())
            .isActive(category.isActive())
            .imageUrl(category.getImageUrl())
            .createdAt(category.getCreatedAt())
            .updatedAt(category.getUpdatedAt())
            .createdBy(category.getCreatedBy())
            .updatedBy(category.getUpdatedBy())
            .level(calculateLevel(category))
            .fullPath(buildFullPath(category))
            .build();
    }

    /**
     * Build hierarchical category tree from flat list.
     */
    private List<CategoryResponse> buildCategoryTree(List<Category> categories) {
        // Find root categories (no parent)
        List<Category> roots = categories.stream()
            .filter(c -> c.getParent() == null)
            .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
            .collect(Collectors.toList());

        // Build tree recursively
        return roots.stream()
            .map(root -> buildCategoryNode(root, categories))
            .collect(Collectors.toList());
    }

    /**
     * Build a single category node with its children recursively.
     */
    private CategoryResponse buildCategoryNode(Category category, List<Category> allCategories) {
        CategoryResponse response = mapToResponse(category);

        // Find children
        List<Category> children = allCategories.stream()
            .filter(c -> c.getParent() != null && c.getParent().getId().equals(category.getId()))
            .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
            .collect(Collectors.toList());

        // Recursively build children
        response.setChildren(children.stream()
            .map(child -> buildCategoryNode(child, allCategories))
            .collect(Collectors.toList()));

        return response;
    }

    /**
     * Calculate hierarchical level of a category.
     */
    private int calculateLevel(Category category) {
        int level = 0;
        Category current = category.getParent();
        while (current != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    /**
     * Build full hierarchical path (e.g., "Electronics > Computers > Laptops").
     */
    private String buildFullPath(Category category) {
        List<String> path = new ArrayList<>();
        Category current = category;

        while (current != null) {
            path.add(0, current.getName());
            current = current.getParent();
        }

        return String.join(" > ", path);
    }

    /**
     * Check if setting a parent would create a circular reference.
     */
    private boolean wouldCreateCircularReference(String parentId, String categoryId) {
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

        return false;
    }
}
