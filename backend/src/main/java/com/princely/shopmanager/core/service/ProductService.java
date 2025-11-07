package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.shared.events.ProductCreatedEvent;
import com.princely.shopmanager.shared.security.TenantSecurityValidator;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing product catalog operations.
 *
 * Products represent the master catalog (what you sell).
 * Stock tracking is handled separately through Inventory records.
 *
 * This service provides:
 * - CRUD operations for products with validation
 * - SKU and barcode uniqueness checks
 * - Category assignment
 * - Inventory aggregation and summary
 * - Multi-tenant context handling
 * - Audit logging
 */
@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "products")
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final TenantSecurityValidator tenantSecurityValidator;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new product in the catalog.
     * Stock is NOT created here - use Inventory API to add stock.
     *
     * @param request Product creation request with validation
     * @return Created product response
     * @throws IllegalArgumentException if SKU/barcode already exists or shop not found
     */
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating new product: {} with SKU: {}", request.getName(), request.getSku());

        // Get shop and validate tenant access
        Shop shop = shopRepository.findById(request.getShopId())
            .orElseThrow(() -> new IllegalArgumentException("Shop not found: " + request.getShopId()));
        tenantSecurityValidator.validateShopAccess(shop);

        // Check SKU uniqueness within shop
        if (productRepository.existsBySkuAndShopId(request.getSku(), shop.getId())) {
            throw new IllegalArgumentException("Product with SKU '" + request.getSku() + "' already exists in this shop");
        }

        // Check barcode uniqueness if provided
        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            if (productRepository.existsByBarcodeAndShopId(request.getBarcode(), shop.getId())) {
                throw new IllegalArgumentException("Product with barcode '" + request.getBarcode() + "' already exists in this shop");
            }
        }

        // Get category if provided
        Category category = null;
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));
        }

        // Build product entity
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .sku(request.getSku())
            .barcode(request.getBarcode())
            .shop(shop)
            .category(category)
            .price(request.getPrice())
            .costPrice(request.getCostPrice())
            .unit(request.getUnit())
            .weightInGrams(request.getWeightInGrams())
            .location(request.getLocation())
            .dimensions(request.getDimensions())
            .supplierName(request.getSupplierName())
            .supplierContact(request.getSupplierContact())
            .imageUrl(request.getImageUrl())
            .isTaxable(request.getIsTaxable() != null ? request.getIsTaxable() : true)
            .isDiscountable(request.getIsDiscountable() != null ? request.getIsDiscountable() : true)
            .metadata(request.getMetadata())
            .status(request.getStatus() != null ? request.getStatus() : Product.ProductStatus.ACTIVE)
            .build();

        product = productRepository.save(product);

        // Audit the creation
        auditService.logEntityCreation("Product", product.getId(),
            "Product created: " + product.getName() + " (SKU: " + product.getSku() + ")");

        // Publish product created event
        eventPublisher.publishEvent(new ProductCreatedEvent(
            product.getId(),
            product.getShop().getId(),
            product.getName(),
            product.getSku()
        ));

        log.info("Successfully created product with ID: {} for shop: {}", product.getId(), shop.getId());
        return mapToResponse(product, true);
    }

    /**
     * Updates an existing product.
     * Only provided fields are updated (partial update).
     *
     * @param productId Product ID to update
     * @param request Update request with optional fields
     * @return Updated product response
     * @throws EntityNotFoundException if product not found
     * @throws IllegalArgumentException if SKU/barcode uniqueness violated
     */
    @CacheEvict(key = "#productId")
    public ProductResponse updateProduct(String productId, ProductUpdateRequest request) {
        log.info("Updating product: {}", productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        // Verify tenant access
        tenantSecurityValidator.validateShopAccess(product.getShop());

        // Track changes for audit
        StringBuilder changes = new StringBuilder();

        // Update fields if provided
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            changes.append("Name: ").append(product.getName()).append(" → ").append(request.getName()).append("; ");
            product.setName(request.getName());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getBarcode() != null && !request.getBarcode().equals(product.getBarcode())) {
            // Check barcode uniqueness
            if (productRepository.existsByBarcodeAndShopId(request.getBarcode(), product.getShop().getId())) {
                throw new IllegalArgumentException("Barcode already exists: " + request.getBarcode());
            }
            product.setBarcode(request.getBarcode());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getPrice() != null && !request.getPrice().equals(product.getPrice())) {
            changes.append("Price: ").append(product.getPrice()).append(" → ").append(request.getPrice()).append("; ");
            product.setPrice(request.getPrice());
        }

        if (request.getCostPrice() != null) {
            product.setCostPrice(request.getCostPrice());
        }

        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }

        if (request.getWeightInGrams() != null) {
            product.setWeightInGrams(request.getWeightInGrams());
        }

        if (request.getLocation() != null) {
            product.setLocation(request.getLocation());
        }

        if (request.getDimensions() != null) {
            product.setDimensions(request.getDimensions());
        }

        if (request.getSupplierName() != null) {
            product.setSupplierName(request.getSupplierName());
        }

        if (request.getSupplierContact() != null) {
            product.setSupplierContact(request.getSupplierContact());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        if (request.getStatus() != null && request.getStatus() != product.getStatus()) {
            changes.append("Status: ").append(product.getStatus()).append(" → ").append(request.getStatus()).append("; ");
            product.setStatus(request.getStatus());
        }

        if (request.getIsTaxable() != null) {
            product.setTaxable(request.getIsTaxable());
        }

        if (request.getIsDiscountable() != null) {
            product.setDiscountable(request.getIsDiscountable());
        }

        if (request.getMetadata() != null) {
            product.setMetadata(request.getMetadata());
        }

        product = productRepository.save(product);

        // Audit if there were changes
        if (changes.length() > 0) {
            auditService.logEntityModification("Product", product.getId(),
                "Product updated: " + changes.toString());
        }

        log.info("Successfully updated product: {}", productId);
        return mapToResponse(product, true);
    }

    /**
     * Gets a product by ID with inventory summary.
     *
     * @param productId Product ID
     * @param includeInventory Whether to include inventory aggregation
     * @return Product response with optional inventory data
     */
    @Transactional(readOnly = true)
    @Cacheable(key = "#productId + '_' + #includeInventory")
    public ProductResponse getProductById(String productId, boolean includeInventory) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        tenantSecurityValidator.validateShopAccess(product.getShop());
        return mapToResponse(product, includeInventory);
    }

    /**
     * Gets all products for a shop with optional filtering.
     *
     * @param shopId Shop ID
     * @param spec Specification for filtering
     * @param pageable Pagination parameters
     * @param includeInventory Whether to include inventory aggregation
     * @return Page of products
     */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String shopId, Specification<Product> spec,
                                              Pageable pageable, boolean includeInventory) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + shopId));
        tenantSecurityValidator.validateShopAccess(shop);

        return productRepository.findAll(spec, pageable)
            .map(product -> mapToResponse(product, includeInventory));
    }

    /**
     * Soft deletes a product by marking it as DISCONTINUED.
     * Does not delete inventory records.
     *
     * @param productId Product ID to delete
     */
    @CacheEvict(key = "#productId")
    public void deleteProduct(String productId) {
        log.info("Soft deleting product: {}", productId);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        tenantSecurityValidator.validateShopAccess(product.getShop());

        product.setStatus(Product.ProductStatus.DISCONTINUED);
        productRepository.save(product);

        auditService.logEntityModification("Product", product.getId(),
            "Product discontinued: " + product.getName());

        log.info("Successfully discontinued product: {}", productId);
    }

    /**
     * Gets inventory summary for a specific product.
     * Aggregates stock across all inventory records (batches/locations).
     *
     * @param productId Product ID
     * @return Inventory summary with total, available, and reserved stock
     */
    @Transactional(readOnly = true)
    public InventorySummary getInventorySummary(String productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        tenantSecurityValidator.validateShopAccess(product.getShop());

        List<Inventory> inventories = inventoryRepository.findByProductId(productId);

        int totalStock = inventories.stream()
            .mapToInt(Inventory::getCurrentStock)
            .sum();

        int reservedStock = inventories.stream()
            .mapToInt(Inventory::getReservedStock)
            .sum();

        int availableStock = inventories.stream()
            .mapToInt(Inventory::getAvailableStock)
            .sum();

        boolean hasLowStock = inventories.stream()
            .anyMatch(Inventory::isLowStock);

        boolean hasExpiredBatches = inventories.stream()
            .anyMatch(Inventory::isExpired);

        return InventorySummary.builder()
            .productId(productId)
            .totalStock(totalStock)
            .availableStock(availableStock)
            .reservedStock(reservedStock)
            .inventoryCount(inventories.size())
            .hasLowStock(hasLowStock)
            .hasExpiredBatches(hasExpiredBatches)
            .build();
    }

    /**
     * Checks if a product has sufficient available inventory.
     *
     * @param productId Product ID
     * @param quantity Required quantity
     * @return true if sufficient stock available
     */
    @Transactional(readOnly = true)
    public boolean hasAvailableStock(String productId, int quantity) {
        InventorySummary summary = getInventorySummary(productId);
        return summary.getAvailableStock() >= quantity;
    }

    /**
     * Gets products with low stock (any inventory below minimum).
     *
     * @param shopId Shop ID
     * @return List of products with low stock
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsWithLowStock(String shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException("Shop not found: " + shopId));
        tenantSecurityValidator.validateShopAccess(shop);

        List<Inventory> lowStockInventories = inventoryRepository.findLowStockItems(shopId);
        List<String> productIds = lowStockInventories.stream()
            .map(inv -> inv.getProduct().getId())
            .distinct()
            .collect(Collectors.toList());

        return productRepository.findAllById(productIds).stream()
            .map(product -> mapToResponse(product, true))
            .collect(Collectors.toList());
    }

    /**
     * Gets products with no inventory (zero total stock).
     *
     * @param shopId Shop ID
     * @return List of products with no stock
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsWithNoStock(String shopId) {
        List<Product> allProducts = productRepository.findByShopId(shopId);

        return allProducts.stream()
            .filter(product -> {
                int totalStock = inventoryRepository.findByProductId(product.getId()).stream()
                    .mapToInt(Inventory::getCurrentStock)
                    .sum();
                return totalStock == 0;
            })
            .map(product -> mapToResponse(product, true))
            .collect(Collectors.toList());
    }

    /**
     * Maps Product entity to ProductResponse DTO.
     *
     * @param product Product entity
     * @param includeInventory Whether to include inventory aggregation
     * @return ProductResponse DTO
     */
    private ProductResponse mapToResponse(Product product, boolean includeInventory) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .barcode(product.getBarcode())
            .shopId(product.getShop().getId())
            .shopName(product.getShop().getName())
            .price(product.getPrice())
            .costPrice(product.getCostPrice())
            .unit(product.getUnit())
            .weightInGrams(product.getWeightInGrams())
            .location(product.getLocation())
            .dimensions(product.getDimensions())
            .supplierName(product.getSupplierName())
            .supplierContact(product.getSupplierContact())
            .imageUrl(product.getImageUrl())
            .status(product.getStatus())
            .isTaxable(product.isTaxable())
            .isDiscountable(product.isDiscountable())
            .metadata(product.getMetadata())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .createdBy(product.getCreatedBy())
            .updatedBy(product.getUpdatedBy());

        // Add category info if present
        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getId())
                   .categoryName(product.getCategory().getName());
        }

        // Calculate profit margin if cost price is available
        if (product.getCostPrice() != null && product.getCostPrice().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profitMargin = product.getPrice().subtract(product.getCostPrice());
            BigDecimal profitMarginPercentage = profitMargin
                .divide(product.getCostPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

            builder.profitMargin(profitMargin)
                   .profitMarginPercentage(profitMarginPercentage.setScale(2, RoundingMode.HALF_UP));
        }

        // Include inventory summary if requested
        if (includeInventory) {
            InventorySummary summary = getInventorySummary(product.getId());
            builder.totalStock(summary.getTotalStock())
                   .availableStock(summary.getAvailableStock())
                   .reservedStock(summary.getReservedStock())
                   .inventoryCount(summary.getInventoryCount())
                   .hasLowStock(summary.getHasLowStock())
                   .hasExpiredBatches(summary.getHasExpiredBatches());
        }

        return builder.build();
    }

    /**
     * Inner class for inventory summary data.
     */
    @lombok.Data
    @lombok.Builder
    public static class InventorySummary {
        private String productId;
        private Integer totalStock;
        private Integer availableStock;
        private Integer reservedStock;
        private Integer inventoryCount;
        private Boolean hasLowStock;
        private Boolean hasExpiredBatches;
    }
}
