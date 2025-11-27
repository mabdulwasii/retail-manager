package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import org.springframework.context.ApplicationEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceSimpleTest {

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

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        Shop testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        Product testProduct = new Product();
        testProduct.setId("product-1");
        testProduct.setName("Test Product");

        testInventory = new Inventory();
        testInventory.setId("inventory-1");
        testInventory.setShop(testShop);
        testInventory.setProduct(testProduct);
        testInventory.setCurrentStock(100);
        testInventory.setMinimumStock(10);
        testInventory.setMaximumStock(500);
        testInventory.setReorderPoint(25);
        testInventory.setCostPrice(BigDecimal.valueOf(15.50));
        testInventory.setSellingPrice(BigDecimal.valueOf(25.00));
    }

    @Test
    void getInventoryById_WithValidId_ShouldReturnInventory() {
        // Arrange
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));

        // Act
        InventoryResponse result = inventoryService.getInventoryById("inventory-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("inventory-1");
        assertThat(result.getCurrentStock()).isEqualTo(100);
        assertThat(result.getCostPrice()).isEqualByComparingTo(BigDecimal.valueOf(15.50));
        assertThat(result.getShopName()).isEqualTo("Test Shop");
        assertThat(result.getProductName()).isEqualTo("Test Product");
    }

    @Test
    void getInventoryById_WithInvalidId_ShouldThrowException() {
        // Arrange
        when(inventoryRepository.findById("invalid-id")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> inventoryService.getInventoryById("invalid-id"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Inventory not found");
    }

    @Test
    void getTotalInventoryValue_ShouldReturnTotalValue() {
        // Arrange
        BigDecimal expectedValue = BigDecimal.valueOf(1500.00);
        when(inventoryRepository.calculateTotalInventoryValue("shop-1")).thenReturn(expectedValue);

        // Act
        BigDecimal result = inventoryService.getTotalInventoryValue("shop-1");

        // Assert
        assertThat(result).isEqualByComparingTo(expectedValue);
        verify(inventoryRepository).calculateTotalInventoryValue("shop-1");
    }

    @Test
    void updateInventoryStatus_ShouldUpdateStatus() {
        // Arrange
        testInventory.setStatus(Inventory.InventoryStatus.ACTIVE);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponse result = inventoryService.updateInventoryStatus("inventory-1", Inventory.InventoryStatus.DISCONTINUED);

        // Assert
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById("inventory-1");
        verify(inventoryRepository).save(testInventory);
        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }
}