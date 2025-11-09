package com.princely.shopmanager.core.controller;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.core.service.ProductService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for product catalog management.
 * Products represent the master catalog (what you sell).
 * For stock management, use the Inventory API.
 *
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product catalog management operations")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    private final ProductService productService;

    @Operation(
        summary = "Create new product",
        description = "Create a new product in the catalog. Stock is NOT created here - use Inventory API to add stock."
    )
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or SKU/barcode already exists")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PostMapping("/shops/{shopId}/products")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_CREATE)")
    public ResponseEntity<ProductResponse> createProduct(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Valid @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating product for shop: {}, name: {}, user: {}",
                shopId, request.getName(), principal.getUsername());

        // Ensure shopId from path matches request
        request.setShopId(shopId);

        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get products",
        description = "Retrieve products for a shop with filtering, searching, and pagination"
    )
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/products")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_LIST)")
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Search query for product name or SKU") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) String categoryId,
            @Parameter(description = "Filter by status") @RequestParam(required = false) Product.ProductStatus status,
            @Parameter(description = "Minimum price filter") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Maximum price filter") @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Include inventory summary") @RequestParam(defaultValue = "true") boolean includeInventory,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {

        // Build specification based on filters
        Specification<Product> spec = (root, query, cb) ->
            cb.equal(root.get("shop").get("id"), shopId);

        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("name")), searchPattern),
                    cb.like(cb.lower(root.get("sku")), searchPattern),
                    cb.like(cb.lower(root.get("barcode")), searchPattern)
                )
            );
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("status"), status));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponse> response = productService.getProducts(shopId, spec, pageable, includeInventory);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get product by ID",
        description = "Retrieve a specific product by its ID with inventory summary"
    )
    @ApiResponse(responseCode = "200", description = "Product retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/products/{productId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_READ)")
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Parameter(description = "Include inventory summary") @RequestParam(defaultValue = "true") boolean includeInventory) {

        ProductResponse response = productService.getProductById(productId, includeInventory);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Search product by barcode",
        description = "Search for a product using its barcode (for barcode scanner integration)"
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/products/search")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_READ)")
    public ResponseEntity<ProductResponse> searchByBarcode(
            @Parameter(description = "Product barcode", required = true) @RequestParam String barcode,
            @Parameter(description = "Shop ID", required = true) @RequestParam String shopId,
            @Parameter(description = "Include inventory summary") @RequestParam(defaultValue = "true") boolean includeInventory) {

        log.debug("Searching product by barcode: {} in shop: {}", barcode, shopId);
        ProductResponse response = productService.searchByBarcode(barcode, shopId, includeInventory);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update product",
        description = "Update an existing product's catalog information (not stock - use Inventory API)"
    )
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PutMapping("/products/{productId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_UPDATE)")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @Valid @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating product: {}, user: {}", productId, principal.getUsername());

        ProductResponse response = productService.updateProduct(productId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete product",
        description = "Soft delete a product by marking it as DISCONTINUED. Inventory records are not deleted."
    )
    @ApiResponse(responseCode = "204", description = "Product deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).PRODUCT_DELETE)")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable String productId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting product: {}, user: {}", productId, principal.getUsername());

        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get inventory summary for product",
        description = "Get aggregated inventory summary across all batches/locations for a product"
    )
    @ApiResponse(responseCode = "200", description = "Inventory summary retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @GetMapping("/products/{productId}/inventory-summary")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_READ)")
    public ResponseEntity<ProductService.InventorySummary> getInventorySummary(
            @Parameter(description = "Product ID") @PathVariable String productId) {

        ProductService.InventorySummary summary = productService.getInventorySummary(productId);
        return ResponseEntity.ok(summary);
    }

    @Operation(
        summary = "Get products with low stock",
        description = "Retrieve all products that have low stock in any inventory location"
    )
    @ApiResponse(responseCode = "200", description = "Low stock products retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/products/low-stock")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_LIST)")
    public ResponseEntity<?> getProductsWithLowStock(
            @Parameter(description = "Shop ID") @PathVariable String shopId) {

        var response = productService.getProductsWithLowStock(shopId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get products with no stock",
        description = "Retrieve all products that have zero total inventory"
    )
    @ApiResponse(responseCode = "200", description = "Out of stock products retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/products/out-of-stock")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVENTORY_LIST)")
    public ResponseEntity<?> getProductsWithNoStock(
            @Parameter(description = "Shop ID") @PathVariable String shopId) {

        var response = productService.getProductsWithNoStock(shopId);
        return ResponseEntity.ok(response);
    }
}
