package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryHistory;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.dto.InventorySummaryDto;
import com.princely.shopmanager.inventory.dto.InventoryUpdateRequest;
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.inventory.specification.InventorySpecifications;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.events.InventoryLowStockEvent;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.service.ShopAwareService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryService(
            ShopAccessValidator shopAccessValidator,
            ShopRepository shopRepository,
            InventoryRepository inventoryRepository,
            InventoryHistoryRepository historyRepository,
            ProductRepository productRepository,
            AuditService auditService,
            ApplicationEventPublisher eventPublisher) {
        super(shopAccessValidator, shopRepository);
        this.inventoryRepository = inventoryRepository;
        this.historyRepository = historyRepository;
        this.productRepository = productRepository;
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
            .currentStock(request.getCurrentStock())
            .minimumStock(request.getMinimumStock())
            .maximumStock(request.getMaximumStock())
            .reorderPoint(request.getReorderPoint())
            .costPrice(request.getCostPrice())
            .sellingPrice(request.getSellingPrice())
            .location(request.getLocation())
            .batchNumber(batchNumber)
            .expiryDate(request.getExpiryDate())
            .lastStockUpdate(LocalDateTime.now())
            .build();

        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.STOCK_IN,
            request.getCurrentStock(), 0, request.getCurrentStock(),
            null, InventoryHistory.ReferenceType.PROCUREMENT, "Initial stock");

        auditService.logEntityCreation(ENTITY_TYPE_INVENTORY, inventory.getId(),
            "Created inventory for product: " + product.getName() + " with stock: " + request.getCurrentStock());

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

    public InventoryResponse adjustStock(String inventoryId, InventoryAdjustmentRequest request, JwtPrincipal principal) {
        Inventory inventory = findInventoryForUser(inventoryId, principal);

        int previousStock = inventory.getCurrentStock();
        int newStock = request.getNewStock();
        int quantityChange = newStock - previousStock;

        inventory.adjustStock(newStock, request.getReason());
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

        if (request.getMaximumStock() != null) {
            Integer oldValue = inventory.getMaximumStock();
            inventory.setMaximumStock(request.getMaximumStock());
            changes.append(String.format("Max stock: %s → %s; ", oldValue, request.getMaximumStock()));
        }

        if (request.getReorderPoint() != null) {
            Integer oldValue = inventory.getReorderPoint();
            inventory.setReorderPoint(request.getReorderPoint());
            changes.append(String.format("Reorder point: %s → %s; ", oldValue, request.getReorderPoint()));
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

            // Calculate suggested reorder point
            int suggestedReorderPoint = Math.max(minimumStock,
                (int) (currentStock * 0.2 * forecastDays / 30.0));

            if (suggestedReorderPoint != inventory.getReorderPoint()) {
                updateReorderPoint(inventory.getId(), suggestedReorderPoint);
            }
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
        // Use maximumStock if available, otherwise use minimumStock * 3 as default target
        int targetStock = inventory.getMaximumStock() != null
            ? inventory.getMaximumStock()
            : inventory.getMinimumStock() * 3;

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

    private void updateReorderPoint(String inventoryId, int newReorderPoint) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int oldReorderPoint = inventory.getReorderPoint();
        inventory.setReorderPoint(newReorderPoint);
        inventoryRepository.save(inventory);

        auditService.logEntityModification(ENTITY_TYPE_INVENTORY, inventory.getId(),
            String.format("Reorder point updated from %d to %d", oldReorderPoint, newReorderPoint));
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

        BigDecimal totalValue = allInventory.stream()
            .filter(inv -> inv.getCostPrice() != null)
            .map(inv -> inv.getCostPrice().multiply(BigDecimal.valueOf(inv.getCurrentStock())))
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
                    .filter(inv -> inv.getCostPrice() != null)
                    .map(inv -> inv.getCostPrice().multiply(BigDecimal.valueOf(inv.getCurrentStock())))
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

        return InventorySummaryDto.builder()
            .totalItems(allInventory.size())
            .totalValue(totalValue)
            .lowStockItems(lowStockCount)
            .expiredItems(expiredCount)
            .expiringSoonItems(expiringSoonCount)
            .categoryBreakdown(categoryBreakdown)
            .build();
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
            .id(inventory.getId())
            .shopId(inventory.getShop().getId())
            .shopName(inventory.getShop().getName())
            .productId(inventory.getProduct().getId())
            .productName(inventory.getProduct().getName())
            .productSku(inventory.getProduct().getSku())
            .currentStock(inventory.getCurrentStock())
            .reservedStock(inventory.getReservedStock())
            .availableStock(inventory.getAvailableStock())
            .minimumStock(inventory.getMinimumStock())
            .maximumStock(inventory.getMaximumStock())
            .reorderPoint(inventory.getReorderPoint())
            .costPrice(inventory.getCostPrice())
            .sellingPrice(inventory.getSellingPrice())
            .location(inventory.getLocation())
            .batchNumber(inventory.getBatchNumber())
            .expiryDate(inventory.getExpiryDate())
            .status(inventory.getStatus())
            .lastStockUpdate(inventory.getLastStockUpdate())
            .isLowStock(inventory.isLowStock())
            .isExpired(inventory.isExpired())
            .isExpiringSoon(inventory.isExpiringSoon(30))
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
}