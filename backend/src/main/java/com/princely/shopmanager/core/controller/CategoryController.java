package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.dto.CategoryCreateRequest;
import com.princely.shopmanager.core.dto.CategoryResponse;
import com.princely.shopmanager.core.dto.CategoryUpdateRequest;
import com.princely.shopmanager.core.service.CategoryService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for product category management.
 * Categories are shop-scoped and support hierarchical structures (parent-child).
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Product category management operations")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(
        summary = "Create new category",
        description = "Create a new product category for a shop. Supports hierarchical categories via parentId."
    )
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or duplicate category name")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PostMapping("/shops/{shopId}/categories")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_CREATE)")
    public ResponseEntity<CategoryResponse> createCategory(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Valid @RequestBody CategoryCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating category for shop: {}, name: {}, user: {}",
                shopId, request.getName(), principal.getUsername());

        // Ensure shopId from path matches request
        request.setShopId(shopId);

        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get categories for shop",
        description = "Retrieve all categories for a shop. Optionally returns hierarchical tree structure."
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @GetMapping("/shops/{shopId}/categories")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_LIST)")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Include hierarchical tree structure") @RequestParam(defaultValue = "true") boolean tree,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.debug("Fetching categories for shop: {}, tree: {}, user: {}",
                shopId, tree, principal.getUsername());

        List<CategoryResponse> categories = categoryService.getCategoriesByShop(shopId, tree);
        return ResponseEntity.ok(categories);
    }

    @Operation(
        summary = "Get category by ID",
        description = "Retrieve a specific category by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Category retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @GetMapping("/categories/{id}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_READ)")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category ID") @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.debug("Fetching category by ID: {}, user: {}", id, principal.getUsername());

        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @Operation(
        summary = "Update category",
        description = "Update an existing category. All fields are optional - only provided fields will be updated."
    )
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PutMapping("/categories/{id}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_UPDATE)")
    public ResponseEntity<CategoryResponse> updateCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Valid @RequestBody CategoryUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating category: {}, user: {}", id, principal.getUsername());

        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Partially update category",
        description = "Partially update an existing category (PATCH). All fields are optional - only provided fields will be updated. Preferred over PUT for partial updates."
    )
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @PatchMapping("/categories/{id}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_UPDATE)")
    public ResponseEntity<CategoryResponse> patchCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @Valid @RequestBody CategoryUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Patching category: {}, user: {}", id, principal.getUsername());

        // Reuse the same update logic - current implementation already does partial updates
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete category",
        description = "Delete a category. Category must not have products or child categories."
    )
    @ApiResponse(responseCode = "204", description = "Category deleted successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "400", description = "Category has products or children - cannot delete")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).CATEGORY_DELETE)")
    public ResponseEntity<Void> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable String id,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting category: {}, user: {}", id, principal.getUsername());

        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
