package com.princely.shopmanager.inventory.service;

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
import com.princely.shopmanager.inventory.dto.InventoryUpdateRequest;
import com.princely.shopmanager.inventory.dto.StockReservationRequest;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.events.InventoryUpdatedEvent;
import com.princely.shopmanager.auth.context.TenantContext;
import org.springframework.context.ApplicationEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository historyRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ShopAccessValidator shopAccessValidator;

    @Mock
    private com.princely.shopmanager.inventory.repository.InventoryUnitPriceRepository inventoryUnitPriceRepository;

    @Mock
    private com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository productUnitDefRepository;

    @Mock
    private InventoryCostCalculator costCalculator;

    private InventoryService inventoryService;

    private Shop testShop;
    private Product testProduct;
    private Inventory testInventory;
    private InventoryCreateRequest createRequest;
    private JwtPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        testProduct = new Product();
        testProduct.setId("product-1");
        testProduct.setName("Test Product");

        testInventory = new Inventory();
        testInventory.setId("inventory-1");
        testInventory.setShop(testShop);
        testInventory.setProduct(testProduct);
        testInventory.setPurchaseQuantity(BigDecimal.valueOf(100));
        testInventory.setCurrentStock(100L); // 100 units in base units
        testInventory.setMinimumStock(10);
        testInventory.setCostPrice(BigDecimal.valueOf(15.50));
        testInventory.setSellingPrice(BigDecimal.valueOf(25.00));

        createRequest = InventoryCreateRequest.builder()
            .productId("product-1")
            .purchaseQuantity(BigDecimal.valueOf(100))
            .minimumStock(10)
            .costPrice(BigDecimal.valueOf(15.50))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .location("A1-B2")
            .batchNumber("BATCH001")
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        testPrincipal = JwtPrincipal.builder()
            .subject("test-user")
            .preferredUsername("testuser")
            .roles(List.of("MANAGER"))
            .tenantId("tenant-1")
            .shopId("shop-1")
            .build();

        // Mock shop repository for shop existence check (lenient to avoid unnecessary stubbing errors)
        lenient().when(shopRepository.existsById("shop-1")).thenReturn(true);

        inventoryService = new InventoryService(
            shopAccessValidator,
            shopRepository,
            inventoryRepository,
            historyRepository,
            productRepository,
            productUnitDefRepository,
            costCalculator,
            auditService,
            eventPublisher
        );
    }

    @Test
    void createInventory_ShouldCreateInventorySuccessfully() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn("shop-1");

            // Act
            InventoryResponse result = inventoryService.createInventory("shop-1", createRequest, testPrincipal);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("inventory-1");
            assertThat(result.getCurrentStock()).isEqualTo(100);

            verify(inventoryRepository, times(2)).save(any(Inventory.class)); // first: initial save, second: after setting currentStock
            verify(historyRepository).save(any(InventoryHistory.class));
            verify(auditService).logEntityCreation(eq("Inventory"), eq("inventory-1"), anyString());
        }
    }

    @Test
    void createInventory_WithMissingShop_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.empty());
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn("shop-1");

            // Act & Assert
            assertThatThrownBy(() -> inventoryService.createInventory("shop-1", createRequest, testPrincipal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shop not found");

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void createInventory_WithMissingProduct_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.findById("product-1")).thenReturn(Optional.empty());
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        try (var mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenant).thenReturn("shop-1");

            // Act & Assert
            assertThatThrownBy(() -> inventoryService.createInventory("shop-1", createRequest, testPrincipal))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");

            verify(inventoryRepository, never()).save(any());
        }
    }

    @Test
    void getInventory_ShouldReturnPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Inventory> mockPage = new PageImpl<>(List.of(testInventory));
        when(inventoryRepository.findAll(isNull(Specification.class), eq(pageable))).thenReturn(mockPage);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        Page<InventoryResponse> result = inventoryService.getInventory("shop-1", null, pageable, testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getId()).isEqualTo("inventory-1");
    }

    @Test
    void getInventoryById_WithValidId_ShouldReturnInventory() {
        // Arrange
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.getInventoryById("inventory-1", testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("inventory-1");
        assertThat(result.getCurrentStock()).isEqualTo(100);
    }

    @Test
    void getInventoryById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.getInventoryById("invalid-id", testPrincipal))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Inventory not found");
    }

    @Test
    void adjustStock_ShouldUpdateStockAndCreateHistory() {
        // Arrange
        InventoryAdjustmentRequest adjustmentRequest = InventoryAdjustmentRequest.builder()
            .newStock(150)
            .reason("Stock received")
            .build();

        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.adjustStock("inventory-1", adjustmentRequest, testPrincipal);

        // Assert
        assertThat(result).isNotNull();

        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.ADJUSTMENT);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(50); // 150 - 100
        assertThat(savedHistory.getPreviousStock()).isEqualTo(100);
        assertThat(savedHistory.getNewStock()).isEqualTo(150);
        assertThat(savedHistory.getReason()).isEqualTo("Stock received");

        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void reserveStock_ShouldReserveStockSuccessfully() {
        // Arrange
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.reserveStock("inventory-1", 20, "order-123", InventoryHistory.ReferenceType.SALE, testPrincipal);

        // Assert
        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.RESERVATION);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(20);
        assertThat(savedHistory.getReferenceId()).isEqualTo("order-123");
        assertThat(savedHistory.getReferenceType()).isEqualTo(InventoryHistory.ReferenceType.SALE);
    }

    @Test
    void reserveStockWithRequest_ShouldReserveStockSuccessfully() {
        // Arrange
        StockReservationRequest request = StockReservationRequest.builder()
            .inventoryId("inventory-1")
            .quantity(30)
            .referenceId("order-456")
            .referenceType(InventoryHistory.ReferenceType.SALE)
            .reason("Order reservation")
            .build();

        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.reserveStock(request, testPrincipal);

        // Assert
        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.RESERVATION);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(30);
        assertThat(savedHistory.getReason()).isEqualTo("Order reservation");
    }

    @Test
    void releaseReservedStock_ShouldReleaseStockSuccessfully() {
        // Arrange
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.releaseReservedStock("inventory-1", 15, "order-789", testPrincipal);

        // Assert
        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.RESERVATION_RELEASE);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(-15);
        assertThat(savedHistory.getReferenceId()).isEqualTo("order-789");
    }

    @Test
    void sellStock_WithSufficientStock_ShouldSellSuccessfully() {
        // Arrange
        testInventory.setReservedStock(25);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.sellStock("inventory-1", 20, "sale-123", testPrincipal);

        // Assert
        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.SALE);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(-20);
        assertThat(savedHistory.getReferenceId()).isEqualTo("sale-123");

        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void sellStock_WithInsufficientStock_ShouldThrowException() {
        // Arrange
        testInventory.setCurrentStock(10L); // only 10 units available
        testInventory.setReservedStock(5);  // 5 reserved → only 5 available
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.sellStock("inventory-1", 20, "sale-123", testPrincipal))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot sell");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void returnStock_ShouldReturnStockSuccessfully() {
        // Arrange
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.returnStock("inventory-1", 10, "return-123", testPrincipal);

        // Assert
        ArgumentCaptor<InventoryHistory> historyCaptor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(historyRepository).save(historyCaptor.capture());

        InventoryHistory savedHistory = historyCaptor.getValue();
        assertThat(savedHistory.getChangeType()).isEqualTo(InventoryHistory.ChangeType.RETURN);
        assertThat(savedHistory.getQuantityChange()).isEqualTo(10);
        assertThat(savedHistory.getReferenceId()).isEqualTo("return-123");

        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void getLowStockItems_ShouldReturnLowStockItems() {
        // Arrange
        List<Inventory> lowStockItems = Arrays.asList(testInventory);
        when(inventoryRepository.findLowStockItems("shop-1")).thenReturn(lowStockItems);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        List<InventoryResponse> result = inventoryService.getLowStockItems("shop-1", testPrincipal);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("inventory-1");
    }

    @Test
    void getExpiringItems_ShouldReturnExpiringItems() {
        // Arrange
        List<Inventory> expiringItems = Arrays.asList(testInventory);
        when(inventoryRepository.findExpiringItems(eq("shop-1"), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(expiringItems);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        List<InventoryResponse> result = inventoryService.getExpiringItems("shop-1", 30, testPrincipal);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("inventory-1");
    }

    @Test
    void getTotalInventoryValue_ShouldReturnTotalValue() {
        // Arrange
        BigDecimal expectedValue = BigDecimal.valueOf(1500.00);
        when(inventoryRepository.calculateTotalInventoryValue("shop-1")).thenReturn(expectedValue);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        BigDecimal result = inventoryService.getTotalInventoryValue("shop-1", testPrincipal);

        // Assert
        assertThat(result).isEqualByComparingTo(expectedValue);
    }

    @Test
    void getInventoryHistory_ShouldReturnHistory() {
        // Arrange
        List<InventoryHistory> historyList = Arrays.asList(new InventoryHistory(), new InventoryHistory());
        when(historyRepository.findByInventoryIdOrderByCreatedAtDesc("inventory-1")).thenReturn(historyList);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        List<InventoryHistory> result = inventoryService.getInventoryHistory("inventory-1", testPrincipal);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void updateInventoryStatus_ShouldUpdateStatus() {
        // Arrange
        testInventory.setStatus(Inventory.InventoryStatus.ACTIVE);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.updateInventoryStatus("inventory-1", Inventory.InventoryStatus.DISCONTINUED, testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void updateInventory_ShouldUpdateMetadataSuccessfully() {
        // Arrange
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .batchNumber("BATCH002")
            .location("B2-C3")
            .expiryDate(LocalDate.now().plusMonths(12))
            .minimumStock(20)
            .costPrice(BigDecimal.valueOf(18.00))
            .sellingPrice(BigDecimal.valueOf(50.00))
            .build();

        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.updateInventory("inventory-1", updateRequest, testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
        verify(eventPublisher).publishEvent(any(InventoryUpdatedEvent.class));
    }

    @Test
    void updateInventory_WithPartialUpdate_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .location("C3-D4")
            .minimumStock(15)
            .build();

        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.updateInventory("inventory-1", updateRequest, testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        verify(inventoryRepository).save(any(Inventory.class));
        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void updateInventory_WithInvalidId_ShouldThrowException() {
        // Arrange
        InventoryUpdateRequest updateRequest = InventoryUpdateRequest.builder()
            .location("Test Location")
            .build();

        when(inventoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.updateInventory("invalid-id", updateRequest, testPrincipal))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Inventory not found");

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void deleteInventory_WithZeroStock_ShouldDeleteSuccessfully() {
        // Arrange
        testInventory.setCurrentStock(0L); // no stock remaining
        testInventory.setReservedStock(0);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        inventoryService.deleteInventory("inventory-1", testPrincipal);

        // Assert
        verify(inventoryRepository).delete(testInventory);
        verify(auditService).logEntityDeletion(eq("Inventory"), eq("inventory-1"), anyString());
    }

    @Test
    void deleteInventory_WithActiveStock_ShouldThrowException() {
        // Arrange
        testInventory.setCurrentStock(50L); // active stock present
        testInventory.setReservedStock(0);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.deleteInventory("inventory-1", testPrincipal))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot delete inventory with active stock");

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void deleteInventory_WithReservedStock_ShouldThrowException() {
        // Arrange
        testInventory.setCurrentStock(0L); // no active stock (so we get past the first guard)
        testInventory.setReservedStock(10);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.deleteInventory("inventory-1", testPrincipal))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot delete inventory with reserved stock");

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }

    @Test
    void deleteInventory_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.deleteInventory("invalid-id", testPrincipal))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Inventory not found");

        verify(inventoryRepository, never()).delete(any(Inventory.class));
    }
}