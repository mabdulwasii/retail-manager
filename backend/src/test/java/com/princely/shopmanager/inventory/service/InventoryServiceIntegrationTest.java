package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.auth.context.TenantContext;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.dto.InventoryAdjustmentRequest;
import com.princely.shopmanager.inventory.dto.InventoryCreateRequest;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.dto.InventorySummaryDto;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Inventory Service Integration Tests")
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private Shop testShop;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create and save tenant first
        Tenant testTenant = Tenant.builder()
            .id(UUID.randomUUID().toString())
            .name("Test Tenant")
            .contactEmail("test@tenant.com")
            .build());
        testTenant = tenantRepository.save(testTenant);

        testShop = Shop.builder()
            .id(UUID.randomUUID().toString())
            .name("Test Shop")
            .email("test@shop.com")
            .tenant(testTenant)
            .build();
        testShop = shopRepository.save(testShop);

        testProduct = Product.builder()
            .id(UUID.randomUUID().toString())
            .name("Test Product")
            .description("A test product")
            .price(BigDecimal.valueOf(100.00))
            .sku("TEST-001")
            .build();
        testProduct = productRepository.save(testProduct);

        TenantContext.setCurrentTenant(testShop.getId());
    }

    @Test
    @DisplayName("Should create inventory item successfully")
    void shouldCreateInventorySuccessfully() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(100)
            .minimumStock(20)
            .maximumStock(200)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .location("A1-B2")
            .batchNumber("BATCH001")
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        InventoryResponse response = inventoryService.createInventory(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getProductId()).isEqualTo(testProduct.getId());
        assertThat(response.getCurrentStock()).isEqualTo(100);
        assertThat(response.getMinimumStock()).isEqualTo(20);
        assertThat(response.getStatus()).isEqualTo(Inventory.InventoryStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should adjust stock levels correctly")
    void shouldAdjustStockLevelsCorrectly() {
        // Create initial inventory
        InventoryCreateRequest createRequest = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(100)
            .minimumStock(20)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .build();

        InventoryResponse created = inventoryService.createInventory(createRequest);

        // Adjust stock
        InventoryAdjustmentRequest adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(150)
            .reason("Stock adjustment for testing")
            .build();

        InventoryResponse adjusted = inventoryService.adjustStock(created.getId(), adjustmentRequest);

        assertThat(adjusted.getCurrentStock()).isEqualTo(150);
        assertThat(adjusted.getAvailableStock()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should calculate low stock correctly")
    void shouldCalculateLowStockCorrectly() {
        // Create inventory with low stock
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(15)
            .minimumStock(20)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .build();

        InventoryResponse response = inventoryService.createInventory(request);

        assertThat(response.isLowStock()).isTrue();
        assertThat(response.getCurrentStock()).isLessThan(response.getMinimumStock());
    }

    @Test
    @DisplayName("Should generate inventory summary correctly")
    void shouldGenerateInventorySummaryCorrectly() {
        // Create multiple inventory items
        for (int i = 0; i < 3; i++) {
            Product product = Product.builder()
                .id(UUID.randomUUID().toString())
                .name("Product " + i)
                .price(BigDecimal.valueOf(100.00))
                .sku("PROD-" + i)
                .build();
            productRepository.save(product);

            InventoryCreateRequest request = InventoryCreateRequest.builder()
                .productId(product.getId())
                .currentStock(100 - (i * 30))
                .minimumStock(20)
                .reorderPoint(30)
                .unitCost(BigDecimal.valueOf(50.00))
                .build();

            inventoryService.createInventory(request);
        }

        InventorySummaryDto summary = inventoryService.getInventorySummary(testShop.getId());

        assertThat(summary.getTotalItems()).isEqualTo(3);
        assertThat(summary.getTotalValue()).isGreaterThan(BigDecimal.ZERO);
        assertThat(summary.getLowStockItems()).isGreaterThan(0);
        assertThat(summary.getCategoryBreakdown()).hasSize(1);
        assertThat(summary.getCategoryBreakdown().get(0).getCategory()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should validate business rules for stock operations")
    void shouldValidateBusinessRulesForStockOperations() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(50)
            .minimumStock(20)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .build();

        InventoryResponse inventory = inventoryService.createInventory(request);

        // Test selling more stock than available should fail
        assertThatThrownBy(() -> inventoryService.sellStock(inventory.getId(), 100, "sale-123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot sell");
    }

    @Test
    @DisplayName("Should handle stock reservations correctly")
    void shouldHandleStockReservationsCorrectly() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(100)
            .minimumStock(20)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .build();

        InventoryResponse inventory = inventoryService.createInventory(request);

        // Reserve stock
        inventoryService.reserveStock(inventory.getId(), 30, "sale-123",
            com.princely.shopmanager.inventory.domain.InventoryHistory.ReferenceType.SALE);

        InventoryResponse updated = inventoryService.getInventoryById(inventory.getId());
        assertThat(updated.getReservedStock()).isEqualTo(30);
        assertThat(updated.getAvailableStock()).isEqualTo(70);

        // Release reserved stock
        inventoryService.releaseReservedStock(inventory.getId(), 30, "sale-123");

        InventoryResponse released = inventoryService.getInventoryById(inventory.getId());
        assertThat(released.getReservedStock()).isEqualTo(0);
        assertThat(released.getAvailableStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should track inventory history correctly")
    void shouldTrackInventoryHistoryCorrectly() {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
            .productId(testProduct.getId())
            .currentStock(100)
            .minimumStock(20)
            .reorderPoint(30)
            .unitCost(BigDecimal.valueOf(50.00))
            .build();

        InventoryResponse inventory = inventoryService.createInventory(request);

        // Perform some operations
        InventoryAdjustmentRequest adjustment = InventoryAdjustmentRequest.builder()
            .newStock(150)
            .reason("Stock adjustment")
            .build();
        inventoryService.adjustStock(inventory.getId(), adjustment);

        var history = inventoryService.getInventoryHistory(inventory.getId());

        assertThat(history).hasSize(2); // Initial stock + adjustment
        assertThat(history.get(0).getChangeType())
            .isEqualTo(com.princely.shopmanager.inventory.domain.InventoryHistory.ChangeType.ADJUSTMENT);
        assertThat(history.get(1).getChangeType())
            .isEqualTo(com.princely.shopmanager.inventory.domain.InventoryHistory.ChangeType.STOCK_IN);
    }
}