package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryHistory;
import com.princely.shopmanager.inventory.domain.InventoryUnitPrice;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.dto.InventorySummaryDto;
import com.princely.shopmanager.inventory.dto.InventoryUnitPriceRequest;
import com.princely.shopmanager.inventory.dto.InventoryUnitPriceResponse;
import com.princely.shopmanager.inventory.dto.InventoryUpdateRequest;
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryUnitPriceRepository;
import com.princely.shopmanager.inventory.specification.InventorySpecifications;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.events.InventoryLowStockEvent;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.service.ShopAwareService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.CacheEvict;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class InventoryService extends ShopAwareService {

    // Entity type constant for audit logging
    private static final String ENTITY_TYPE_INVENTORY = "Inventory";

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    private final ProductRepository productRepository;
    private final ProductUnitDefinitionRepository productUnitDefRepository;
    private final InventoryCostCalculator costCalculator;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            InventoryRepository inventoryRepository,
            InventoryHistoryRepository historyRepository,
            ProductRepository productRepository,
            ProductUnitDefinitionRepository productUnitDefRepository,
            InventoryCostCalculator costCalculator,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        super(shopAccessValidator, shopRepository);
        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
        this.productUnitDefRepository = productUnitDefRepository;
        this.costCalculator = costCalculator;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Finds inventory by ID with shop access validation.
     */
    private Inventory findInventoryForUser(String inventoryId, JwtPrincipal principal) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + inventoryId));

        if (shopAccessValidator.hasNoAccessToShop(inventory.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this inventory");
        }

        return inventory;
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public InventoryResponse createInventory(String shopId, InventoryCreateRequest request, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // Auto-generate batch number if not provided
        String batchNumber = request.getBatchNumber();
        if (batchNumber == null || batchNumber.isBlank()) {
            batchNumber = generateBatchNumber(shop, product);
        }

        Inventory inventory = Inventory.builder()
            .shop(shop)
            .product(product)
            .minimumStock(request.getMinimumStock())
            .costPrice(request.getCostPrice())
            .sellingPrice(request.getSellingPrice())
            .baseUnit(request.getBaseUnit() != null ? request.getBaseUnit() : "piece")
            .purchaseUnit(request.getPurchaseUnit())
            .purchaseQuantity(request.getPurchaseQuantity())
            .totalPurchaseCost(request.getTotalPurchaseCost())
            .location(request.getLocation())
            .batchNumber(batchNumber)
            .expiryDate(request.getExpiryDate())
            .lastStockUpdate(LocalDateTime.now())
            .build();

        inventory = inventoryRepository.save(inventory);

        // Initialise current_stock in base units = purchaseQuantity × conversionFactor
        if (request.getPurchaseQuantity() != null && request.getPurchaseQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal conversionFactor = findConversionFactor(product.getId(), request.getPurchaseUnit());
            long baseUnits = request.getPurchaseQuantity().multiply(conversionFactor)
                .setScale(0, java.math.RoundingMode.HALF_UP).longValue();
            inventory.setCurrentStock(baseUnits);
        }

        // Auto-create a purchase unit if missing and calculate unit costs
        if (request.getPurchaseUnit() != null && request.getTotalPurchaseCost() != null &&
            request.getPurchaseQuantity() != null) {

            ensurePurchaseUnitExists(product, request.getPurchaseUnit());

            // Calculate costs for all unit types
            List<ProductUnitDefinition> unitDefs = productUnitDefRepository.findByProductId(product.getId());
            Map<String, BigDecimal> unitCosts = costCalculator.calculateCostsForAllUnits(
                request.getTotalPurchaseCost(),
                request.getPurchaseQuantity(),
                request.getPurchaseUnit(),
                unitDefs
            );

            // Update unit prices with calculated costs
            updateUnitPrices(inventory, unitCosts, request.getUnitPrices());
        } else if (request.getUnitPrices() != null && !request.getUnitPrices().isEmpty()) {
            // Handle unit prices if provided without total cost calculation
            handleUnitPrices(inventory, request.getUnitPrices());
        }

        Integer initialStock = inventory.getCurrentStock();
        recordHistoryEntry(inventory, InventoryHistory.ChangeType.STOCK_IN,
            initialStock, 0, initialStock,
            null, InventoryHistory.ReferenceType.PROCUREMENT, "Initial stock");

        auditService.logEntityCreation(ENTITY_TYPE_INVENTORY, inventory.getId(),
            "Created inventory for product: " + product.getName() + " with stock: " + initialStock);

        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventory(String shopId, Specification<Inventory> spec, Pageable pageable, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return inventoryRepository.findAll(spec, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(String id, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(id, principal);
        return mapToResponse(inventory);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public InventoryResponse adjustStock(String inventoryId, InventoryAdjustmentRequest request, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        int previousStock = inventory.getCurrentStock();
        int newStock = request.getNewStock();
        int quantityChange = newStock - previousStock;

        inventory.adjustStock(newStock, request.getReason());

        // Update total purchase cost and recalculate unit costs if provided.
        // NOTE: purchaseQuantity (original purchase amount in purchase units) is intentionally
        // NOT updated here — it remains as the historical record of what was purchased.
        if (request.getTotalPurchaseCost() != null && inventory.getPurchaseUnit() != null) {
            inventory.setTotalPurchaseCost(request.getTotalPurchaseCost());

            if (inventory.getPurchaseQuantity() != null) {
                // Recalculate unit costs using the original purchase quantity
                List<ProductUnitDefinition> unitDefs = productUnitDefRepository
                    .findByProductId(inventory.getProduct().getId());
                Map<String, BigDecimal> unitCosts = costCalculator.calculateCostsForAllUnits(
                    request.getTotalPurchaseCost(),
                    inventory.getPurchaseQuantity(),
                    inventory.getPurchaseUnit(),
                    unitDefs
                );

                // Update unit prices with recalculated costs
                updateUnitPrices(inventory, unitCosts, null);
            }
        }

        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.ADJUSTMENT,
            quantityChange, previousStock, newStock,
            null, InventoryHistory.ReferenceType.ADJUSTMENT, request.getReason());

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Stock adjusted from %d to %d. Reason: %s", previousStock, newStock, request.getReason()));

        return mapToResponse(inventory);
    }

    public void reserveStock(String inventoryId, int quantity, String referenceId, InventoryHistory.ReferenceType referenceType, JwtPrincipal principal) {
        reserveStock(StockReservationRequest.builder()
            .inventoryId(inventoryId)
            .quantity(quantity)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .reason("Stock reserved")
            .build(), principal);
    }

    public void reserveStock(StockReservationRequest request, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(request.getInventoryId(), principal);

        int previousReserved = inventory.getReservedStock();
        inventory.reserveStock(request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RESERVATION,
            request.getQuantity(), inventory.getCurrentStock(), inventory.getCurrentStock(),
            request.getReferenceId(), request.getReferenceType(),
            request.getReason() != null ? request.getReason() : "Stock reserved");

        log.debug("Reserved {} units for inventory {}", request.getQuantity(), request.getInventoryId());
    }

    public void releaseReservedStock(String inventoryId, int quantity, String referenceId, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        int previousReserved = inventory.getReservedStock();
        inventory.releaseReservedStock(quantity);
        inventory = inventoryRepository.save(inventory);
        int newReserved = inventory.getReservedStock();

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RESERVATION_RELEASE,
            -quantity, previousReserved, newReserved,
            referenceId, referenceId != null ? InventoryHistory.ReferenceType.SALE : null,
            "Stock reservation released");

        log.debug("Released {} reserved units for inventory {}", quantity, inventoryId);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void sellStock(String inventoryId, int quantity, String saleId, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        if (!inventory.canSell(quantity)) {
            throw new IllegalStateException("Cannot sell " + quantity + " units. Available: " + inventory.getAvailableStock());
        }

        int previousStock = inventory.getCurrentStock();
        inventory.removeStock(quantity);
        inventory.releaseReservedStock(quantity);
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.SALE,
            -quantity, previousStock, inventory.getCurrentStock(),
            saleId, InventoryHistory.ReferenceType.SALE, "Stock sold");

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Sold %d units, remaining stock: %d", quantity, inventory.getCurrentStock()));
    }

    public void returnStock(String inventoryId, int quantity, String returnId, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        int previousStock = inventory.getCurrentStock();
        inventory.addStock(quantity);
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RETURN,
            quantity, previousStock, inventory.getCurrentStock(),
            returnId, InventoryHistory.ReferenceType.RETURN, "Stock returned");

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Returned %d units, new stock: %d", quantity, inventory.getCurrentStock()));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(String shopId, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return inventoryRepository.findLowStockItems(shopId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getExpiringItems(String shopId, int daysThreshold, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysThreshold);
        return inventoryRepository.findExpiringItems(shopId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue(String shopId, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return inventoryRepository.calculateTotalInventoryValue(shopId);
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getInventoryHistory(String inventoryId, JwtPrincipal principal) {
        // Validate access to the inventory first
        findInventoryForUser(inventoryId, principal);
        return historyRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public InventoryResponse updateInventoryStatus(String inventoryId, Inventory.InventoryStatus status, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        Inventory.InventoryStatus previousStatus = inventory.getStatus();
        inventory.setStatus(status);
        inventory = inventoryRepository.save(inventory);

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Status changed from %s to %s", previousStatus, status));

        // Publish inventory updated event
        eventPublisher.publishEvent(new InventoryUpdatedEvent(
            inventory.getId(),
            inventory.getProduct().getId(),
            inventory.getShop().getId(),
            previousStatus.ordinal(),
            status.ordinal(),
            "STATUS_CHANGE"
        ));

        return mapToResponse(inventory);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public InventoryResponse updateInventory(String inventoryId, InventoryUpdateRequest request, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        // Track changes for audit
        StringBuilder changes = new StringBuilder();

        if (request.getBatchNumber() != null) {
            String oldValue = inventory.getBatchNumber();
            inventory.setBatchNumber(request.getBatchNumber());
            changes.append(String.format("Batch number: %s → %s; ", oldValue, request.getBatchNumber()));
        }

        if (request.getLocation() != null) {
            String oldValue = inventory.getLocation();
            inventory.setLocation(request.getLocation());
            changes.append(String.format("Location: %s → %s; ", oldValue, request.getLocation()));
        }

        if (request.getExpiryDate() != null) {
            LocalDate oldValue = inventory.getExpiryDate();
            inventory.setExpiryDate(request.getExpiryDate());
            changes.append(String.format("Expiry date: %s → %s; ", oldValue, request.getExpiryDate()));
        }

        if (request.getMinimumStock() != null) {
            Integer oldValue = inventory.getMinimumStock();
            inventory.setMinimumStock(request.getMinimumStock());
            changes.append(String.format("Min stock: %s → %s; ", oldValue, request.getMinimumStock()));
        }

        if (request.getCostPrice() != null) {
            BigDecimal oldValue = inventory.getCostPrice();
            inventory.setCostPrice(request.getCostPrice());
            changes.append(String.format("Cost price: %s → %s; ", oldValue, request.getCostPrice()));
        }

        if (request.getSellingPrice() != null) {
            BigDecimal oldValue = inventory.getSellingPrice();
            inventory.setSellingPrice(request.getSellingPrice());
            changes.append(String.format("Selling price: %s → %s; ", oldValue, request.getSellingPrice()));
        }

        if (request.getBaseUnit() != null) {
            String oldValue = inventory.getBaseUnit();
            inventory.setBaseUnit(request.getBaseUnit());
            changes.append(String.format("Base unit: %s → %s; ", oldValue, request.getBaseUnit()));
        }

        if (request.getPurchaseUnit() != null) {
            String oldValue = inventory.getPurchaseUnit();
            inventory.setPurchaseUnit(request.getPurchaseUnit());
            changes.append(String.format("Purchase unit: %s → %s; ", oldValue, request.getPurchaseUnit()));
        }

        if (request.getPurchaseQuantity() != null) {
            BigDecimal oldValue = inventory.getPurchaseQuantity();
            inventory.setPurchaseQuantity(request.getPurchaseQuantity());
            changes.append(String.format("Purchase quantity: %s → %s; ", oldValue, request.getPurchaseQuantity()));
        }

        if (request.getTotalPurchaseCost() != null) {
            BigDecimal oldValue = inventory.getTotalPurchaseCost();
            inventory.setTotalPurchaseCost(request.getTotalPurchaseCost());
            changes.append(String.format("Total purchase cost: %s → %s; ", oldValue, request.getTotalPurchaseCost()));

            // Recalculate unit costs if we have purchase info
            if (inventory.getPurchaseUnit() != null && inventory.getPurchaseQuantity() != null) {
                List<ProductUnitDefinition> unitDefs = productUnitDefRepository
                    .findByProductId(inventory.getProduct().getId());
                Map<String, BigDecimal> unitCosts = costCalculator.calculateCostsForAllUnits(
                    request.getTotalPurchaseCost(),
                    inventory.getPurchaseQuantity(),
                    inventory.getPurchaseUnit(),
                    unitDefs
                );
                updateUnitPrices(inventory, unitCosts, request.getUnitPrices());
                changes.append("Unit costs recalculated. ");
            }
        } else if (request.getUnitPrices() != null && !request.getUnitPrices().isEmpty()) {
            // Handle unit prices if provided without cost recalculation
            handleUnitPrices(inventory, request.getUnitPrices());
            changes.append("Unit prices updated. ");
        }

        inventory = inventoryRepository.save(inventory);

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            "Updated inventory: " + changes.toString());

        // Publish inventory updated event
        eventPublisher.publishEvent(new InventoryUpdatedEvent(
            inventory.getId(),
            inventory.getProduct().getId(),
            inventory.getShop().getId(),
            0, // previousValue - not applicable for metadata update
            0, // newValue - not applicable for metadata update
            "METADATA_UPDATE"
        ));

        return mapToResponse(inventory);
    }

    public void deleteInventory(String inventoryId, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        // Validate: Cannot delete inventory with active stock
        if (inventory.getCurrentStock() > 0) {
            throw new IllegalStateException(
                String.format("Cannot delete inventory with active stock. Current stock: %d. " +
                    "Please adjust stock to zero before deleting.", inventory.getCurrentStock()));
        }

        // Validate: Cannot delete inventory with reserved stock
        if (inventory.getReservedStock() > 0) {
            throw new IllegalStateException(
                String.format("Cannot delete inventory with reserved stock. Reserved: %d. " +
                    "Please release all reservations before deleting.", inventory.getReservedStock()));
        }

        String productName = inventory.getProduct().getName();
        String shopId = inventory.getShop().getId();

        // Delete inventory
        inventoryRepository.delete(inventory);

        auditService.logEntityDeletion(ENTITY_TYPE_INVENTORY, inventoryId,
            String.format("Deleted inventory for product: %s (batch: %s)",
                productName, inventory.getBatchNumber()));

        log.info("Deleted inventory {} for product {} in shop {}", inventoryId, productName, shopId);
    }

    // Advanced Inventory Management Features

    @Scheduled(fixedRate = 3600000) // Every hour
    @ConditionalOnProperty(name = "features.inventory.auto-reorder.enabled", havingValue = "true", matchIfMissing = false)
    public void processAutomaticReorders() {
        log.info("Processing automatic reorders for low stock items");

        List<Inventory> lowStockItems = inventoryRepository.findAll(
            InventorySpecifications.hasLowStock()
        );

        for (Inventory inventory : lowStockItems) {
            if (isAutoReorderEnabled(inventory)) {
                createReorderSuggestion(inventory);

                // Publish low stock event
                eventPublisher.publishEvent(new InventoryLowStockEvent(
                    inventory.getId(),
                    inventory.getProduct().getName(),
                    inventory.getShop().getId(),
                    inventory.getAvailableStock(),
                    inventory.getMinimumStock()
                ));
            }
        }
    }

    public void forecastDemand(String productId, int forecastDays) {
        log.info("Forecasting demand for product {} for {} days", productId, forecastDays);

        // Note: This is a placeholder implementation
        // In a real system, this would integrate with historical sales data
        // and use machine learning algorithms for demand forecasting

        List<Inventory> productInventories = inventoryRepository.findAll(
            InventorySpecifications.forProduct(productId)
        );

        for (Inventory inventory : productInventories) {
            // Simple demand forecasting based on current consumption rate
            int currentStock = inventory.getAvailableStock();
            int minimumStock = inventory.getMinimumStock();

            // Log forecast information for monitoring
            log.debug("Demand forecast for inventory {}: current={}, minimum={}, forecastDays={}",
                inventory.getId(), currentStock, minimumStock, forecastDays);

            // Note: Reorder suggestions are now handled by the createReorderSuggestion method
            // which uses minimumStock * 3 as the target reorder quantity
        }
    }

    private boolean isAutoReorderEnabled(Inventory inventory) {
        // In a real implementation, this would check inventory or product configuration
        // For now, assume auto-reorder is enabled for all items
        return true;
    }

    private void createReorderSuggestion(Inventory inventory) {
        log.info("Creating reorder suggestion for inventory {}", inventory.getId());

        // Calculate suggested reorder quantity
        // Use minimumStock * 3 as default target
        int targetStock = inventory.getMinimumStock() * 3;

        int suggestedQuantity = Math.max(
            targetStock - inventory.getAvailableStock(),
            inventory.getMinimumStock() * 2
        );

        // Log the reorder suggestion
        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Reorder suggestion created: %d units (current stock: %d, minimum: %d)",
                suggestedQuantity, inventory.getAvailableStock(), inventory.getMinimumStock()));

        // In a real implementation, this would create a purchase order or notification
        // For now, we just log it
        log.info("Reorder suggestion: {} units of {} for shop {}",
            suggestedQuantity, inventory.getProduct().getName(), inventory.getShop().getName());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findInventoryBySpecification(Specification<Inventory> spec) {
        return inventoryRepository.findAll(spec)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findLowStockItems(String shopId, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return findInventoryBySpecification(
            InventorySpecifications.forShop(shopId)
                .and(InventorySpecifications.hasLowStock())
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findExpiringItems(String shopId, int daysThreshold, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        return findInventoryBySpecification(
            InventorySpecifications.forShop(shopId)
                .and(InventorySpecifications.expiresWithinDays(daysThreshold))
        );
    }

    private void recordHistoryEntry(Inventory inventory, InventoryHistory.ChangeType changeType,
                                  int quantityChange, int previousStock, int newStock,
                                  String referenceId, InventoryHistory.ReferenceType referenceType,
                                  String reason) {
        InventoryHistory history = InventoryHistory.builder()
            .inventory(inventory)
            .changeType(changeType)
            .quantityChange(quantityChange)
            .previousStock(previousStock)
            .newStock(newStock)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .reason(reason)
            .performedAt(LocalDateTime.now())
            .createdAt(LocalDateTime.now())
            .build();

        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto getInventorySummary(String shopId, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);
        List<Inventory> allInventory = inventoryRepository.findAll(
            InventorySpecifications.forShop(shopId)
        );

        // Use totalPurchaseCost if available, fallback to costPrice × base unit count
        BigDecimal totalValue = allInventory.stream()
            .map(inv -> {
                if (inv.getTotalPurchaseCost() != null) {
                    return inv.getTotalPurchaseCost();
                } else if (inv.getCostPrice() != null) {
                    BigDecimal baseUnits = getStockInBaseUnits(inv);
                    return inv.getCostPrice().multiply(baseUnits);
                }
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int lowStockCount = (int) allInventory.stream()
            .filter(Inventory::isLowStock)
            .count();

        int expiredCount = (int) allInventory.stream()
            .filter(Inventory::isExpired)
            .count();

        int expiringSoonCount = (int) allInventory.stream()
            .filter(inv -> inv.isExpiringSoon(30))
            .count();

        // Group by category for breakdown
        Map<String, List<Inventory>> categoryGroups = allInventory.stream()
            .collect(Collectors.groupingBy(inv ->
                inv.getProduct().getCategory() != null ?
                    inv.getProduct().getCategory().getName() :
                    "Uncategorized"));

        List<InventorySummaryDto.CategoryBreakdown> categoryBreakdown = categoryGroups.entrySet().stream()
            .map(entry -> {
                String category = entry.getKey();
                List<Inventory> categoryItems = entry.getValue();

                BigDecimal categoryValue = categoryItems.stream()
                    .map(inv -> {
                        if (inv.getTotalPurchaseCost() != null) {
                            return inv.getTotalPurchaseCost();
                        } else if (inv.getCostPrice() != null) {
                            BigDecimal baseUnits = getStockInBaseUnits(inv);
                            return inv.getCostPrice().multiply(baseUnits);
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                int categoryLowStockCount = (int) categoryItems.stream()
                    .filter(Inventory::isLowStock)
                    .count();

                return InventorySummaryDto.CategoryBreakdown.builder()
                    .category(category)
                    .itemCount(categoryItems.size())
                    .totalValue(categoryValue)
                    .lowStockCount(categoryLowStockCount)
                    .build();
            })
            .sorted((a, b) -> b.getItemCount().compareTo(a.getItemCount()))
            .toList();

        // Calculate financial projections using totalPurchaseCost
        BigDecimal totalInventoryCost = allInventory.stream()
            .map(inv -> {
                if (inv.getTotalPurchaseCost() != null) {
                    return inv.getTotalPurchaseCost();
                } else if (inv.getCostPrice() != null) {
                    BigDecimal baseUnits = getStockInBaseUnits(inv);
                    return inv.getCostPrice().multiply(baseUnits);
                }
                return BigDecimal.ZERO;
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Use base unit count (purchase qty × conversion factor) for accurate sales projection
        BigDecimal projectedTotalSales = allInventory.stream()
            .filter(inv -> inv.getSellingPrice() != null)
            .map(inv -> {
                BigDecimal baseUnits = getStockInBaseUnits(inv);
                return inv.getSellingPrice().multiply(baseUnits);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedProfit = projectedTotalSales.subtract(totalInventoryCost);

        BigDecimal projectedProfitMargin = BigDecimal.ZERO;
        if (totalInventoryCost.compareTo(BigDecimal.ZERO) > 0) {
            projectedProfitMargin = projectedProfit
                .divide(totalInventoryCost, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }

        return InventorySummaryDto.builder()
            .totalItems(allInventory.size())
            .totalValue(totalValue)
            .lowStockItems(lowStockCount)
            .expiredItems(expiredCount)
            .expiringSoonItems(expiringSoonCount)
            .categoryBreakdown(categoryBreakdown)
            .totalInventoryCost(totalInventoryCost)
            .projectedTotalSales(projectedTotalSales)
            .projectedProfit(projectedProfit)
            .projectedProfitMargin(projectedProfitMargin)
            .build();
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        // Calculate purchase unit cost from original purchase data
        BigDecimal purchaseUnitCost = null;
        if (inventory.getTotalPurchaseCost() != null &&
            inventory.getPurchaseQuantity() != null &&
            inventory.getPurchaseQuantity().compareTo(BigDecimal.ZERO) > 0) {

            purchaseUnitCost = inventory.getTotalPurchaseCost()
                .divide(inventory.getPurchaseQuantity(), 2, RoundingMode.HALF_UP);
        }

        // Compute current stock breakdown: X packs + Y pieces
        // currentStock is in base units; divide by conversionFactor to get purchase units
        Integer currentStockInPurchaseUnit = null;
        Integer stockRemainder = null;
        int currentStockBaseUnits = inventory.getCurrentStock();

        if (inventory.getPurchaseUnit() != null && inventory.getProduct().getUnitDefinitions() != null) {
            BigDecimal conversionFactor = inventory.getProduct().getUnitDefinitions().stream()
                .filter(ud -> ud.getUnitType().equalsIgnoreCase(inventory.getPurchaseUnit()))
                .findFirst()
                .map(com.princely.shopmanager.core.domain.ProductUnitDefinition::getConversionFactor)
                .orElse(null);

            if (conversionFactor != null && conversionFactor.compareTo(BigDecimal.ONE) > 0) {
                long factor = conversionFactor.longValue();
                currentStockInPurchaseUnit = (int) (currentStockBaseUnits / factor);
                stockRemainder = (int) (currentStockBaseUnits % factor);
            }
        }

        // Use currentStock (base units) directly for financial projections
        BigDecimal stockInBaseUnits = getStockInBaseUnits(inventory);

        // Calculate financial projections using totalPurchaseCost if available
        BigDecimal itemTotalCost = null;
        BigDecimal itemProjectedSales = null;
        BigDecimal itemProjectedProfit = null;

        if (inventory.getTotalPurchaseCost() != null) {
            // Use actual total purchase cost
            itemTotalCost = inventory.getTotalPurchaseCost();
        } else if (inventory.getCostPrice() != null && stockInBaseUnits.compareTo(BigDecimal.ZERO) > 0) {
            // Fallback: cost per base unit × total base units
            itemTotalCost = inventory.getCostPrice().multiply(stockInBaseUnits);
        }

        if (inventory.getSellingPrice() != null && stockInBaseUnits.compareTo(BigDecimal.ZERO) > 0) {
            // Projected sales = selling price per base unit × total base units
            itemProjectedSales = inventory.getSellingPrice().multiply(stockInBaseUnits);
        }

        if (itemTotalCost != null && itemProjectedSales != null) {
            itemProjectedProfit = itemProjectedSales.subtract(itemTotalCost);
        }

        // Map unit prices if available
        List<InventoryUnitPriceResponse> unitPriceResponses = new java.util.ArrayList<>();
        if (inventory.getUnitPrices() != null && !inventory.getUnitPrices().isEmpty()) {
            unitPriceResponses = inventory.getUnitPrices().stream()
                .map(this::mapUnitPriceToResponse)
                .toList();
        }

        // Map product unit definitions for POS multi-unit selection
        List<com.princely.shopmanager.core.dto.ProductUnitDefinitionResponse> unitDefResponses = new java.util.ArrayList<>();
        if (inventory.getProduct().getUnitDefinitions() != null && !inventory.getProduct().getUnitDefinitions().isEmpty()) {
            unitDefResponses = inventory.getProduct().getUnitDefinitions().stream()
                .map(this::mapProductUnitDefinitionToResponse)
                .toList();
        }

        return InventoryResponse.builder()
            .id(inventory.getId())
            .shopId(inventory.getShop().getId())
            .shopName(inventory.getShop().getName())
            .productId(inventory.getProduct().getId())
            .productName(inventory.getProduct().getName())
            .productSku(inventory.getProduct().getSku())
            .productCategory(inventory.getProduct().getCategory() != null
                ? inventory.getProduct().getCategory().getName() : null)
            .currentStock(inventory.getCurrentStock())
            .currentStockInPurchaseUnit(currentStockInPurchaseUnit)
            .stockRemainder(stockRemainder)
            .reservedStock(inventory.getReservedStock())
            .availableStock(inventory.getAvailableStock())
            .minimumStock(inventory.getMinimumStock())
            .costPrice(inventory.getCostPrice())
            .sellingPrice(inventory.getSellingPrice())
            .baseUnit(inventory.getBaseUnit())
            .purchaseUnit(inventory.getPurchaseUnit())
            .purchaseQuantity(inventory.getPurchaseQuantity())
            .totalPurchaseCost(inventory.getTotalPurchaseCost())
            .purchaseUnitCost(purchaseUnitCost)
            .unitPrices(unitPriceResponses)
            .unitDefinitions(unitDefResponses)
            .location(inventory.getLocation())
            .batchNumber(inventory.getBatchNumber())
            .expiryDate(inventory.getExpiryDate())
            .status(inventory.getStatus())
            .lastStockUpdate(inventory.getLastStockUpdate())
            .isLowStock(inventory.isLowStock())
            .isExpired(inventory.isExpired())
            .isExpiringSoon(inventory.isExpiringSoon(30))
            .itemTotalCost(itemTotalCost)
            .itemProjectedSales(itemProjectedSales)
            .itemProjectedProfit(itemProjectedProfit)
            .build();
    }

    /**
     * Generate unique batch number: BATCH-{SHOP_CODE}-{YYYYMMDD}-{SEQ}
     * Example: BATCH-GOM-20251128-001
     */
    private String generateBatchNumber(Shop shop, Product product) {
        String shopCode = shop.getName().length() >= 3 ?
            shop.getName().substring(0, 3).toUpperCase() :
            shop.getName().toUpperCase();

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // Get count of inventory records for this product today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long countToday = inventoryRepository.countByProductIdAndCreatedAtAfter(product.getId(), startOfDay);

        return String.format("BATCH-%s-%s-%03d", shopCode, dateStr, countToday + 1);
    }

    /**
     * Ensures that the purchase unit exists in ProductUnitDefinitions.
     * If not found, throws exception requiring manual creation.
     *
     * @param product Product entity
     * @param purchaseUnit Purchase unit type
     * @throws IllegalArgumentException if purchase unit doesn't exist
     */
    private void ensurePurchaseUnitExists(Product product, String purchaseUnit) {
        List<ProductUnitDefinition> existing = productUnitDefRepository
            .findByProductId(product.getId());

        boolean exists = existing.stream()
            .anyMatch(ud -> ud.getUnitType().equalsIgnoreCase(purchaseUnit));

        if (!exists) {
            throw new IllegalArgumentException(
                String.format("Purchase unit '%s' not found for product '%s'. " +
                    "Please create unit definition first with conversion factor.",
                    purchaseUnit, product.getName())
            );
        }

        log.debug("Purchase unit '{}' exists for product '{}'", purchaseUnit, product.getName());
    }

    /**
     * Updates or creates unit prices for an inventory batch based on calculated costs.
     * Merges calculated costs with user-provided selling prices.
     *
     * @param inventory Inventory entity
     * @param unitCosts Calculated cost prices for each unit type
     * @param userProvidedPrices Optional user-provided unit price requests
     */
    private void updateUnitPrices(Inventory inventory, Map<String, BigDecimal> unitCosts,
                                   List<InventoryUnitPriceRequest> userProvidedPrices) {
        // Clear existing unit prices
        inventory.getUnitPrices().clear();

        // Create map of user-provided selling prices
        Map<String, BigDecimal> sellingPrices = new HashMap<>();
        if (userProvidedPrices != null) {
            sellingPrices = userProvidedPrices.stream()
                .collect(Collectors.toMap(
                    InventoryUnitPriceRequest::getUnitType,
                    InventoryUnitPriceRequest::getSellingPrice,
                    (v1, v2) -> v2 // Keep last value if duplicate
                ));
        }

        // Create new unit prices from calculated costs
        for (Map.Entry<String, BigDecimal> entry : unitCosts.entrySet()) {
            String unitType = entry.getKey();
            BigDecimal costPrice = entry.getValue();
            BigDecimal sellingPrice = sellingPrices.get(unitType);

            InventoryUnitPrice unitPrice = InventoryUnitPrice.builder()
                .inventory(inventory)
                .unitType(unitType)
                .costPrice(costPrice)
                .sellingPrice(sellingPrice) // May be null if not provided
                .build();

            inventory.getUnitPrices().add(unitPrice);
        }

        log.debug("Updated unit prices for inventory: {}, count: {}",
            inventory.getId(), unitCosts.size());
    }

    /**
     * Handles creating or updating unit prices for an inventory batch.
     * Replaces all existing unit prices with the new ones.
     *
     * @param inventory Inventory entity
     * @param unitPriceRequests List of unit price requests
     */
    private void handleUnitPrices(Inventory inventory, List<InventoryUnitPriceRequest> unitPriceRequests) {
        // Clear existing unit prices
        inventory.getUnitPrices().clear();

        // Create new unit prices
        for (InventoryUnitPriceRequest request : unitPriceRequests) {
            InventoryUnitPrice unitPrice = InventoryUnitPrice.builder()
                .inventory(inventory)
                .unitType(request.getUnitType())
                .sellingPrice(request.getSellingPrice())
                .build();

            inventory.getUnitPrices().add(unitPrice);
        }

        log.debug("Updated unit prices for inventory: {}, count: {}",
            inventory.getId(), unitPriceRequests.size());
    }

    /**
     * Maps an InventoryUnitPrice entity to an InventoryUnitPriceResponse DTO.
     *
     * @param unitPrice Unit price entity
     * @return Unit price response DTO
     */
    private InventoryUnitPriceResponse mapUnitPriceToResponse(InventoryUnitPrice unitPrice) {
        return InventoryUnitPriceResponse.builder()
            .id(unitPrice.getId())
            .inventoryId(unitPrice.getInventory().getId())
            .productName(unitPrice.getInventory().getProduct().getName())
            .batchNumber(unitPrice.getInventory().getBatchNumber())
            .unitType(unitPrice.getUnitType())
            .costPrice(unitPrice.getCostPrice())
            .sellingPrice(unitPrice.getSellingPrice())
            .createdAt(unitPrice.getCreatedAt())
            .updatedAt(unitPrice.getUpdatedAt())
            .createdBy(unitPrice.getCreatedBy())
            .updatedBy(unitPrice.getUpdatedBy())
            .build();
    }

    /**
     * Returns the current stock in base units.
     * Uses the authoritative {@code currentStock} field (base units) rather than
     * deriving from purchaseQuantity × conversionFactor, which would give the
     * original purchase amount rather than the remaining stock.
     *
     * @param inventory Inventory entity
     * @return Remaining stock in base units
     */
    private BigDecimal getStockInBaseUnits(Inventory inventory) {
        int stock = inventory.getCurrentStock();
        return stock > 0 ? BigDecimal.valueOf(stock) : BigDecimal.ZERO;
    }

    /**
     * Looks up the conversion factor for a purchase unit from the product's unit definitions.
     * Returns BigDecimal.ONE if no matching definition is found (purchase unit = base unit).
     */
    private BigDecimal findConversionFactor(String productId, String purchaseUnit) {
        if (purchaseUnit == null) {
            return BigDecimal.ONE;
        }
        return productUnitDefRepository.findByProductId(productId).stream()
            .filter(ud -> ud.getUnitType().equalsIgnoreCase(purchaseUnit))
            .findFirst()
            .map(com.princely.shopmanager.core.domain.ProductUnitDefinition::getConversionFactor)
            .orElse(BigDecimal.ONE);
    }

    /**
     * Maps a ProductUnitDefinition entity to a ProductUnitDefinitionResponse DTO.
     * Used to include product unit definitions in inventory responses for POS.
     *
     * @param unitDef Unit definition entity
     * @return Unit definition response DTO
     */
    private com.princely.shopmanager.core.dto.ProductUnitDefinitionResponse mapProductUnitDefinitionToResponse(
            com.princely.shopmanager.core.domain.ProductUnitDefinition unitDef) {
        return com.princely.shopmanager.core.dto.ProductUnitDefinitionResponse.builder()
            .id(unitDef.getId())
            .productId(unitDef.getProduct().getId())
            .productName(unitDef.getProduct().getName())
            .unitType(unitDef.getUnitType())
            .unitLabel(unitDef.getUnitLabel())
            .conversionFactor(unitDef.getConversionFactor())
            .isBaseUnit(unitDef.getIsBaseUnit())
            .sortOrder(unitDef.getSortOrder())
            .createdAt(unitDef.getCreatedAt())
            .updatedAt(unitDef.getUpdatedAt())
            .build();
    }
}