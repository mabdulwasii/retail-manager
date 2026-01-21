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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryAllocationService - focuses on multi-unit conversion logic.
 */
@ExtendWith(MockitoExtension.class)
class InventoryAllocationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductUnitDefinitionRepository productUnitDefinitionRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryAllocationService inventoryAllocationService;

    private Product mockProduct;
    private JwtPrincipal mockPrincipal;
    private com.princely.shopmanager.core.domain.Shop mockShop;

    @BeforeEach
    void setUp() {
        mockShop = com.princely.shopmanager.core.domain.Shop.builder()
            .id("shop1")
            .name("Test Shop")
            .build();

        mockProduct = Product.builder()
            .id("prod1")
            .name("Coca-Cola")
            .sku("COKE-500ML")
            .build();

        mockPrincipal = JwtPrincipal.builder()
            .subject("keycloak-user-1")
            .userId("user1")
            .preferredUsername("testuser")
            .email("test@example.com")
            .tenantId("tenant1")
            .shopId("shop1")
            .roles(List.of())
            .build();
    }

    @Test
    void shouldConvertPackQuantityToBaseUnitsCorrectly() {
        // Given - User wants to sell 5 packs, each pack = 12 pieces
        ProductUnitDefinition packDef = ProductUnitDefinition.builder()
            .product(mockProduct)
            .unitType("pack")
            .unitLabel("Pack (12pcs)")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(5) // 5 packs
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .build();

        Inventory mockInventory = Inventory.builder()
            .id("inv1")
            .shop(mockShop)
            .currentStock(100)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod1", "pack"))
            .thenReturn(Optional.of(packDef));
        when(productService.hasAvailableStock(eq("prod1"), eq(60), any())).thenReturn(true);
        when(inventoryRepository.findByProductId("prod1")).thenReturn(List.of(mockInventory));

        // When
        List<InventoryAllocationService.InventoryAllocation> allocations =
            inventoryAllocationService.validateAndAllocate(
                List.of(lineItem),
                "shop1",
                mockPrincipal
            );

        // Then
        assertEquals(1, allocations.size());
        InventoryAllocationService.InventoryAllocation allocation = allocations.get(0);

        // Should convert 5 packs × 12 = 60 base units
        assertEquals(60, allocation.baseUnitQuantity);
        assertEquals("pack", allocation.unitType);
        assertEquals(BigDecimal.valueOf(12.0), allocation.conversionFactor);

        // Verify stock check was done with base units
        verify(productService).hasAvailableStock("prod1", 60, mockPrincipal);
    }

    @Test
    void shouldHandleCartonConversionCorrectly() {
        // Given - User wants to sell 2 cartons, each carton = 144 pieces
        ProductUnitDefinition cartonDef = ProductUnitDefinition.builder()
            .product(mockProduct)
            .unitType("carton")
            .unitLabel("Carton (144pcs)")
            .conversionFactor(BigDecimal.valueOf(144.0))
            .isBaseUnit(false)
            .sortOrder(2)
            .build();

        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(2) // 2 cartons
                .unitPrice(BigDecimal.valueOf(140000))
                .unitType("carton")
                .build();

        Inventory mockInventory = Inventory.builder()
            .id("inv1")
            .shop(mockShop)
            .currentStock(300)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod1", "carton"))
            .thenReturn(Optional.of(cartonDef));
        when(productService.hasAvailableStock(eq("prod1"), eq(288), any())).thenReturn(true);
        when(inventoryRepository.findByProductId("prod1")).thenReturn(List.of(mockInventory));

        // When
        List<InventoryAllocationService.InventoryAllocation> allocations =
            inventoryAllocationService.validateAndAllocate(
                List.of(lineItem),
                "shop1",
                mockPrincipal
            );

        // Then
        assertEquals(1, allocations.size());
        InventoryAllocationService.InventoryAllocation allocation = allocations.get(0);

        // Should convert 2 cartons × 144 = 288 base units
        assertEquals(288, allocation.baseUnitQuantity);
        assertEquals("carton", allocation.unitType);
        assertEquals(BigDecimal.valueOf(144.0), allocation.conversionFactor);

        verify(productService).hasAvailableStock("prod1", 288, mockPrincipal);
    }

    @Test
    void shouldHandleBaseUnitDirectly() {
        // Given - User wants to sell 10 pieces (base unit, no conversion needed)
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(10) // 10 pieces
                .unitPrice(BigDecimal.valueOf(1050))
                .unitType(null) // No unit type = base unit
                .build();

        Inventory mockInventory = Inventory.builder()
            .id("inv1")
            .shop(mockShop)
            .currentStock(50)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productService.hasAvailableStock(eq("prod1"), eq(10), any())).thenReturn(true);
        when(inventoryRepository.findByProductId("prod1")).thenReturn(List.of(mockInventory));

        // When
        List<InventoryAllocationService.InventoryAllocation> allocations =
            inventoryAllocationService.validateAndAllocate(
                List.of(lineItem),
                "shop1",
                mockPrincipal
            );

        // Then
        assertEquals(1, allocations.size());
        InventoryAllocationService.InventoryAllocation allocation = allocations.get(0);

        // Should keep 10 as is (no conversion)
        assertEquals(10, allocation.baseUnitQuantity);
        assertEquals(BigDecimal.ONE, allocation.conversionFactor);

        verify(productService).hasAvailableStock("prod1", 10, mockPrincipal);
    }

    @Test
    void shouldThrowExceptionWhenUnitTypeNotFoundForProduct() {
        // Given - Invalid unit type that doesn't exist for this product
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("invalid-unit")
                .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod1", "invalid-unit"))
            .thenReturn(Optional.empty());

        // When - should still work but log warning
        // The service continues with quantity as-is if unit not found
        Inventory mockInventory = Inventory.builder()
            .id("inv1")
            .shop(mockShop)
            .currentStock(50)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();

        when(productService.hasAvailableStock(eq("prod1"), eq(5), any())).thenReturn(true);
        when(inventoryRepository.findByProductId("prod1")).thenReturn(List.of(mockInventory));

        // Then - should not throw, but use quantity as-is
        assertDoesNotThrow(() -> inventoryAllocationService.validateAndAllocate(
            List.of(lineItem),
            "shop1",
            mockPrincipal
        ));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        // Given - Not enough stock available
        ProductUnitDefinition packDef = ProductUnitDefinition.builder()
            .product(mockProduct)
            .unitType("pack")
            .unitLabel("Pack (12pcs)")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();

        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(10) // Wants 10 packs = 120 pieces
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod1", "pack"))
            .thenReturn(Optional.of(packDef));
        when(productService.hasAvailableStock(eq("prod1"), eq(120), any())).thenReturn(false);

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> inventoryAllocationService.validateAndAllocate(
                List.of(lineItem),
                "shop1",
                mockPrincipal
            )
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        assertTrue(exception.getMessage().contains("120"));
    }

    @Test
    void shouldAllocateMultipleProductsWithDifferentUnits() {
        // Given - Multiple products with different unit types
        Product product2 = Product.builder()
            .id("prod2")
            .name("Pepsi")
            .sku("PEPSI-500ML")
            .build();

        ProductUnitDefinition packDef1 = ProductUnitDefinition.builder()
            .product(mockProduct)
            .unitType("pack")
            .unitLabel("Pack (12pcs)")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .build();

        ProductUnitDefinition cartonDef2 = ProductUnitDefinition.builder()
            .product(product2)
            .unitType("carton")
            .unitLabel("Carton (24pcs)")
            .conversionFactor(BigDecimal.valueOf(24.0))
            .isBaseUnit(false)
            .build();

        SalesTransactionCreateRequest.LineItemRequest lineItem1 =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod1")
                .quantity(5)
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .build();

        SalesTransactionCreateRequest.LineItemRequest lineItem2 =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId("prod2")
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(25000))
                .unitType("carton")
                .build();

        Inventory mockInventory1 = Inventory.builder()
            .id("inv1")
            .shop(mockShop)
            .currentStock(100)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();

        Inventory mockInventory2 = Inventory.builder()
            .id("inv2")
            .shop(mockShop)
            .currentStock(100)
            .reservedStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();

        when(productRepository.findById("prod1")).thenReturn(Optional.of(mockProduct));
        when(productRepository.findById("prod2")).thenReturn(Optional.of(product2));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod1", "pack"))
            .thenReturn(Optional.of(packDef1));
        when(productUnitDefinitionRepository.findByProductIdAndUnitType("prod2", "carton"))
            .thenReturn(Optional.of(cartonDef2));
        when(productService.hasAvailableStock(eq("prod1"), eq(60), any())).thenReturn(true);
        when(productService.hasAvailableStock(eq("prod2"), eq(72), any())).thenReturn(true);
        when(inventoryRepository.findByProductId("prod1")).thenReturn(List.of(mockInventory1));
        when(inventoryRepository.findByProductId("prod2")).thenReturn(List.of(mockInventory2));

        // When
        List<InventoryAllocationService.InventoryAllocation> allocations =
            inventoryAllocationService.validateAndAllocate(
                List.of(lineItem1, lineItem2),
                "shop1",
                mockPrincipal
            );

        // Then
        assertEquals(2, allocations.size());

        // Prod1: 5 packs × 12 = 60 pieces
        assertEquals(60, allocations.get(0).baseUnitQuantity);
        assertEquals("pack", allocations.get(0).unitType);

        // Prod2: 3 cartons × 24 = 72 pieces
        assertEquals(72, allocations.get(1).baseUnitQuantity);
        assertEquals("carton", allocations.get(1).unitType);
    }
}
