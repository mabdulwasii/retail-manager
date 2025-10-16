package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.auth.context.TenantContext;
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
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.inventory.repository.InventorySpecifications;
import com.princely.shopmanager.shared.events.InventoryLowStockEvent;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import com.princely.shopmanager.shared.service.AuditService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public InventoryResponse createInventory(InventoryCreateRequest request) {
        String shopId = TenantContext.getCurrentTenant();
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Inventory inventory = Inventory.builder()
            .shop(shop)
            .product(product)
            .currentStock(request.getCurrentStock())
            .minimumStock(request.getMinimumStock())
            .maximumStock(request.getMaximumStock())
            .reorderPoint(request.getReorderPoint())
            .unitCost(request.getUnitCost())
            .location(request.getLocation())
            .batchNumber(request.getBatchNumber())
            .expiryDate(request.getExpiryDate())
            .lastStockUpdate(LocalDateTime.now())
            .build();

        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.STOCK_IN,
            request.getCurrentStock(), 0, request.getCurrentStock(),
            null, InventoryHistory.ReferenceType.PROCUREMENT, "Initial stock");

        auditService.logEntityCreation("Inventory", inventory.getId(),
            "Created inventory for product: " + product.getName() + " with stock: " + request.getCurrentStock());

        return mapToResponse(inventory);
    }

    @Transactional(readOnly = true)
    public Page<InventoryResponse> getInventory(String shopId, Specification<Inventory> spec, Pageable pageable) {
        return inventoryRepository.findAll(spec, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(String id) {
        Inventory inventory = inventoryRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));
        return mapToResponse(inventory);
    }

    public InventoryResponse adjustStock(String inventoryId, InventoryAdjustmentRequest request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int previousStock = inventory.getCurrentStock();
        int newStock = request.getNewStock();
        int quantityChange = newStock - previousStock;

        inventory.adjustStock(newStock, request.getReason());
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.ADJUSTMENT,
            quantityChange, previousStock, newStock,
            null, InventoryHistory.ReferenceType.ADJUSTMENT, request.getReason());

        auditService.logEntityModification("Inventory", inventory.getId(),
            String.format("Stock adjusted from %d to %d. Reason: %s", previousStock, newStock, request.getReason()));

        return mapToResponse(inventory);
    }

    public void reserveStock(String inventoryId, int quantity, String referenceId, InventoryHistory.ReferenceType referenceType) {
        reserveStock(StockReservationRequest.builder()
            .inventoryId(inventoryId)
            .quantity(quantity)
            .referenceId(referenceId)
            .referenceType(referenceType)
            .reason("Stock reserved")
            .build());
    }

    public void reserveStock(StockReservationRequest request) {
        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int previousReserved = inventory.getReservedStock();
        inventory.reserveStock(request.getQuantity());
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RESERVATION,
            request.getQuantity(), inventory.getCurrentStock(), inventory.getCurrentStock(),
            request.getReferenceId(), request.getReferenceType(),
            request.getReason() != null ? request.getReason() : "Stock reserved");

        log.debug("Reserved {} units for inventory {}", request.getQuantity(), request.getInventoryId());
    }

    public void releaseReservedStock(String inventoryId, int quantity, String referenceId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        inventory.releaseReservedStock(quantity);
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RESERVATION_RELEASE,
            -quantity, inventory.getCurrentStock(), inventory.getCurrentStock(),
            referenceId, InventoryHistory.ReferenceType.SALE, "Stock reservation released");

        log.debug("Released {} reserved units for inventory {}", quantity, inventoryId);
    }

    public void sellStock(String inventoryId, int quantity, String saleId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

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

        auditService.logEntityModification("Inventory", inventory.getId(),
            String.format("Sold %d units, remaining stock: %d", quantity, inventory.getCurrentStock()));
    }

    public void returnStock(String inventoryId, int quantity, String returnId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int previousStock = inventory.getCurrentStock();
        inventory.addStock(quantity);
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RETURN,
            quantity, previousStock, inventory.getCurrentStock(),
            returnId, InventoryHistory.ReferenceType.RETURN, "Stock returned");

        auditService.logEntityModification("Inventory", inventory.getId(),
            String.format("Returned %d units, new stock: %d", quantity, inventory.getCurrentStock()));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(String shopId) {
        return inventoryRepository.findLowStockItems(shopId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getExpiringItems(String shopId, int daysThreshold) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysThreshold);
        return inventoryRepository.findExpiringItems(shopId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInventoryValue(String shopId) {
        return inventoryRepository.calculateTotalInventoryValue(shopId);
    }

    @Transactional(readOnly = true)
    public List<InventoryHistory> getInventoryHistory(String inventoryId) {
        return historyRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
    }

    public InventoryResponse updateInventoryStatus(String inventoryId, Inventory.InventoryStatus status) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        Inventory.InventoryStatus previousStatus = inventory.getStatus();
        inventory.setStatus(status);
        inventory = inventoryRepository.save(inventory);

        auditService.logEntityModification("Inventory", inventory.getId(),
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
        int suggestedQuantity = Math.max(
            inventory.getMaximumStock() - inventory.getAvailableStock(),
            inventory.getMinimumStock() * 2
        );

        // Log the reorder suggestion
        auditService.logEntityModification("Inventory", inventory.getId(),
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

        auditService.logEntityModification("Inventory", inventory.getId(),
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
    public List<InventoryResponse> findLowStockItems(String shopId) {
        return findInventoryBySpecification(
            InventorySpecifications.forShop(shopId)
                .and(InventorySpecifications.hasLowStock())
        );
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> findExpiringItems(String shopId, int daysThreshold) {
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
            .createdAt(LocalDateTime.now())
            .build();

        historyRepository.save(history);
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto getInventorySummary(String shopId) {
        List<Inventory> allInventory = inventoryRepository.findAll(
            InventorySpecifications.forShop(shopId)
        );

        BigDecimal totalValue = allInventory.stream()
            .filter(inv -> inv.getUnitCost() != null)
            .map(inv -> inv.getUnitCost().multiply(BigDecimal.valueOf(inv.getCurrentStock())))
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
                    .filter(inv -> inv.getUnitCost() != null)
                    .map(inv -> inv.getUnitCost().multiply(BigDecimal.valueOf(inv.getCurrentStock())))
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
            .unitCost(inventory.getUnitCost())
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
}