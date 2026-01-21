package com.princely.shopmanager.sales.service;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository;
import com.princely.shopmanager.core.service.ProductService;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for allocating inventory for sales transactions.
 * Uses FEFO (First Expiry, First Out) strategy to minimize waste.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryAllocationService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductUnitDefinitionRepository productUnitDefinitionRepository;
    private final ProductService productService;

    /**
     * Validates product availability and allocates inventory for all line items.
     *
     * @param lineItems Line items from sales transaction request
     * @param shopId Shop ID
     * @param principal JWT principal for access control
     * @return List of inventory allocations
     * @throws IllegalArgumentException if product not found or invalid
     * @throws IllegalStateException if insufficient stock
     */
    public List<InventoryAllocation> validateAndAllocate(
            List<SalesTransactionCreateRequest.LineItemRequest> lineItems,
            String shopId,
            JwtPrincipal principal) {

        log.debug("Validating and allocating inventory for {} line items", lineItems.size());

        List<InventoryAllocation> allocations = new ArrayList<>();

        for (SalesTransactionCreateRequest.LineItemRequest lineItemRequest : lineItems) {
            // Validate productId is not null or empty
            if (lineItemRequest.getProductId() == null || lineItemRequest.getProductId().isBlank()) {
                throw new IllegalArgumentException("Product ID is required for all line items");
            }

            Product product = productRepository.findById(lineItemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + lineItemRequest.getProductId()));

            // Handle unit conversion for multi-unit pricing
            String unitType = lineItemRequest.getUnitType();
            BigDecimal conversionFactor = BigDecimal.ONE;
            String unitLabel = null;
            int baseUnitQuantity = lineItemRequest.getQuantity();

            if (unitType != null && !unitType.isBlank()) {
                // Look up unit definition for conversion
                Optional<ProductUnitDefinition> unitDefOpt =
                    productUnitDefinitionRepository.findByProductIdAndUnitType(product.getId(), unitType);

                if (unitDefOpt.isPresent()) {
                    ProductUnitDefinition unitDef = unitDefOpt.get();
                    conversionFactor = unitDef.getConversionFactor();
                    unitLabel = unitDef.getUnitLabel();

                    // Convert to base units for FEFO allocation
                    // Example: 5 packs × 12 = 60 pieces
                    baseUnitQuantity = conversionFactor.multiply(BigDecimal.valueOf(lineItemRequest.getQuantity()))
                        .intValue();

                    log.debug("Converting {} {} (unit: {}) to {} base units using factor {}",
                        lineItemRequest.getQuantity(), product.getName(), unitType,
                        baseUnitQuantity, conversionFactor);
                } else {
                    log.warn("Unit type '{}' not found for product {}. Using quantity as-is.",
                        unitType, product.getName());
                }
            }

            // Check if sufficient stock available (in base units)
            if (!productService.hasAvailableStock(product.getId(), baseUnitQuantity, principal)) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName() +
                    ". Required: " + baseUnitQuantity + " (base units)");
            }

            // Allocate inventory using FEFO (First Expiry, First Out) strategy with base unit quantity
            List<Inventory> allocated = allocateInventory(shopId, product.getId(), baseUnitQuantity);
            allocations.add(new InventoryAllocation(product, lineItemRequest, allocated,
                unitType, unitLabel, conversionFactor, baseUnitQuantity));
        }

        log.debug("Successfully allocated inventory for {} products", allocations.size());
        return allocations;
    }

    /**
     * Allocates inventory for a product using FEFO (First Expiry, First Out) strategy.
     * Prioritizes batches that expire sooner to minimize waste.
     *
     * @param shopId Shop ID
     * @param productId Product ID
     * @param quantity Required quantity
     * @return List of inventory records to use, sorted by priority
     * @throws IllegalStateException if insufficient stock
     */
    private List<Inventory> allocateInventory(String shopId, String productId, int quantity) {
        List<Inventory> availableInventories = inventoryRepository.findByProductId(productId).stream()
            .filter(inv -> inv.getShop().getId().equals(shopId))
            .filter(inv -> inv.getStatus() == Inventory.InventoryStatus.ACTIVE)
            .filter(inv -> !inv.isExpired())
            .filter(inv -> inv.getAvailableStock() > 0)
            .sorted(Comparator
                // First: prioritize expiring batches (FEFO)
                .comparing((Inventory inv) -> inv.getExpiryDate() != null ? inv.getExpiryDate() : java.time.LocalDate.MAX)
                // Second: older batches first (FIFO for same expiry)
                .thenComparing(Inventory::getCreatedAt))
            .toList();

        int totalAvailable = availableInventories.stream()
            .mapToInt(Inventory::getAvailableStock)
            .sum();

        if (totalAvailable < quantity) {
            throw new IllegalStateException("Insufficient available stock. Required: " + quantity +
                ", Available: " + totalAvailable);
        }

        // Return inventories that will be used (may be multiple batches)
        List<Inventory> allocated = new ArrayList<>();
        int remaining = quantity;
        for (Inventory inv : availableInventories) {
            allocated.add(inv);
            remaining -= inv.getAvailableStock();
            if (remaining <= 0) {
                break;
            }
        }

        return allocated;
    }

    /**
     * Helper class to track inventory allocation for a line item with multi-unit support
     */
    public static class InventoryAllocation {
        public final Product product;
        public final SalesTransactionCreateRequest.LineItemRequest request;
        public final List<Inventory> inventories;
        public final String unitType;              // Unit type sold (e.g., "pack", "piece")
        public final String unitLabel;             // Display label (e.g., "Pack (12pcs)")
        public final BigDecimal conversionFactor;  // Conversion to base units (e.g., 12.0)
        public final int baseUnitQuantity;         // Quantity in base units for FEFO
        int remainingQuantity;

        public InventoryAllocation(Product product, SalesTransactionCreateRequest.LineItemRequest request,
                           List<Inventory> inventories, String unitType, String unitLabel,
                           BigDecimal conversionFactor, int baseUnitQuantity) {
            this.product = product;
            this.request = request;
            this.inventories = inventories;
            this.unitType = unitType;
            this.unitLabel = unitLabel;
            this.conversionFactor = conversionFactor;
            this.baseUnitQuantity = baseUnitQuantity;
            this.remainingQuantity = baseUnitQuantity; // Track remaining in base units
        }
    }
}
