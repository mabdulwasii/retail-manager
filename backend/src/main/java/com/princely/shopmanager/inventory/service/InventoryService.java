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
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

        auditService.logEvent("INVENTORY_CREATED", "inventory", inventory.getId(),
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

        auditService.logEvent("INVENTORY_ADJUSTED", "inventory", inventory.getId(),
            String.format("Stock adjusted from %d to %d. Reason: %s", previousStock, newStock, request.getReason()));

        return mapToResponse(inventory);
    }

    public void reserveStock(String inventoryId, int quantity, String referenceId, InventoryHistory.ReferenceType referenceType) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
            .orElseThrow(() -> new EntityNotFoundException("Inventory not found"));

        int previousReserved = inventory.getReservedStock();
        inventory.reserveStock(quantity);
        inventory = inventoryRepository.save(inventory);

        recordHistoryEntry(inventory, InventoryHistory.ChangeType.RESERVATION,
            quantity, inventory.getCurrentStock(), inventory.getCurrentStock(),
            referenceId, referenceType, "Stock reserved");

        log.debug("Reserved {} units for inventory {}", quantity, inventoryId);
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

        auditService.logEvent("INVENTORY_SOLD", "inventory", inventory.getId(),
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

        auditService.logEvent("INVENTORY_RETURNED", "inventory", inventory.getId(),
            String.format("Returned %d units, new stock: %d", quantity, inventory.getCurrentStock()));
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockItems(String shopId) {
        return inventoryRepository.findLowStockItems(shopId)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getExpiringItems(String shopId, int daysThreshold) {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(daysThreshold);
        return inventoryRepository.findExpiringItems(shopId, startDate, endDate)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
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

        auditService.logEvent("INVENTORY_STATUS_CHANGED", "inventory", inventory.getId(),
            String.format("Status changed from %s to %s", previousStatus, status));

        return mapToResponse(inventory);
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