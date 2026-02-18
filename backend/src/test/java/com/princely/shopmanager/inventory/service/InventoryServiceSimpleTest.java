package com.princely.shopmanager.inventory.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.dto.InventoryResponse;
import com.princely.shopmanager.inventory.repository.InventoryHistoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

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

    @Mock
    private ShopAccessValidator shopAccessValidator;

    @Mock
    private com.princely.shopmanager.inventory.repository.InventoryUnitPriceRepository inventoryUnitPriceRepository;

    @Mock
    private com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository productUnitDefRepository;

    @Mock
    private InventoryCostCalculator costCalculator;

    private InventoryService inventoryService;

    private Inventory testInventory;
    private JwtPrincipal testPrincipal;

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
        testInventory.setPurchaseQuantity(BigDecimal.valueOf(100));
        testInventory.setCurrentStock(100L); // 100 units in base units
        testInventory.setMinimumStock(10);
        testInventory.setCostPrice(BigDecimal.valueOf(15.50));
        testInventory.setSellingPrice(BigDecimal.valueOf(25.00));

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
        assertThat(result.getCostPrice()).isEqualByComparingTo(BigDecimal.valueOf(15.50));
        assertThat(result.getShopName()).isEqualTo("Test Shop");
        assertThat(result.getProductName()).isEqualTo("Test Product");
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
    void getTotalInventoryValue_ShouldReturnTotalValue() {
        // Arrange
        BigDecimal expectedValue = BigDecimal.valueOf(1500.00);
        when(inventoryRepository.calculateTotalInventoryValue("shop-1")).thenReturn(expectedValue);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        BigDecimal result = inventoryService.getTotalInventoryValue("shop-1", testPrincipal);

        // Assert
        assertThat(result).isEqualByComparingTo(expectedValue);
        verify(inventoryRepository).calculateTotalInventoryValue("shop-1");
    }

    @Test
    void getInventoryById_WithPackPurchaseUnit_ShouldCalculateProjectedSalesInBaseUnits() {
        // Arrange: product with piece (base) and pack (12 pieces per pack) unit definitions
        Shop testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        Product productWithUnits = new Product();
        productWithUnits.setId("product-2");
        productWithUnits.setName("Water Sachets");

        ProductUnitDefinition pieceDef = new ProductUnitDefinition();
        pieceDef.setUnitType("piece");
        pieceDef.setUnitLabel("Piece");
        pieceDef.setConversionFactor(BigDecimal.ONE);
        pieceDef.setIsBaseUnit(true);
        pieceDef.setProduct(productWithUnits);

        ProductUnitDefinition packDef = new ProductUnitDefinition();
        packDef.setUnitType("pack");
        packDef.setUnitLabel("Pack");
        packDef.setConversionFactor(BigDecimal.valueOf(12));
        packDef.setIsBaseUnit(false);
        packDef.setProduct(productWithUnits);

        productWithUnits.setUnitDefinitions(new ArrayList<>(List.of(pieceDef, packDef)));

        // 20 packs purchased, ₦500/piece selling price, ₦106,000 total cost
        // 20 packs × 12 pieces/pack = 240 pieces in base units
        Inventory packInventory = new Inventory();
        packInventory.setId("inventory-pack");
        packInventory.setShop(testShop);
        packInventory.setProduct(productWithUnits);
        packInventory.setPurchaseUnit("pack");
        packInventory.setPurchaseQuantity(BigDecimal.valueOf(20)); // 20 packs (original purchase)
        packInventory.setCurrentStock(240L); // 240 pieces = 20 packs × 12
        packInventory.setTotalPurchaseCost(BigDecimal.valueOf(106000)); // ₦106,000
        packInventory.setCostPrice(BigDecimal.valueOf(441.67)); // ₦441.67/piece
        packInventory.setSellingPrice(BigDecimal.valueOf(500)); // ₦500/piece
        packInventory.setMinimumStock(5);

        when(inventoryRepository.findById("inventory-pack")).thenReturn(Optional.of(packInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.getInventoryById("inventory-pack", testPrincipal);

        // Assert: 20 packs × 12 pieces/pack = 240 pieces
        assertThat(result).isNotNull();
        // Projected sales = ₦500/piece × 240 pieces = ₦120,000
        assertThat(result.getItemProjectedSales())
            .isNotNull()
            .isEqualByComparingTo(BigDecimal.valueOf(120000));
        // Projected profit = ₦120,000 - ₦106,000 = ₦14,000
        assertThat(result.getItemProjectedProfit())
            .isNotNull()
            .isEqualByComparingTo(BigDecimal.valueOf(14000));
        // Total cost uses totalPurchaseCost directly
        assertThat(result.getItemTotalCost())
            .isEqualByComparingTo(BigDecimal.valueOf(106000));
    }

    @Test
    void getInventoryById_WithPieceAsPurchaseUnit_ShouldCalculateProjectedSalesCorrectly() {
        // Arrange: product purchased in pieces (base unit), no conversion needed
        Shop testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        Product productWithPiece = new Product();
        productWithPiece.setId("product-3");
        productWithPiece.setName("Single Item Product");

        ProductUnitDefinition pieceDef = new ProductUnitDefinition();
        pieceDef.setUnitType("piece");
        pieceDef.setUnitLabel("Piece");
        pieceDef.setConversionFactor(BigDecimal.ONE);
        pieceDef.setIsBaseUnit(true);
        pieceDef.setProduct(productWithPiece);

        productWithPiece.setUnitDefinitions(new ArrayList<>(List.of(pieceDef)));

        Inventory pieceInventory = new Inventory();
        pieceInventory.setId("inventory-piece");
        pieceInventory.setShop(testShop);
        pieceInventory.setProduct(productWithPiece);
        pieceInventory.setPurchaseUnit("piece");
        pieceInventory.setPurchaseQuantity(BigDecimal.valueOf(50)); // 50 pieces purchased
        pieceInventory.setCurrentStock(50L); // 50 pieces in base units (piece = base unit)
        pieceInventory.setTotalPurchaseCost(BigDecimal.valueOf(5000)); // ₦5,000 total
        pieceInventory.setSellingPrice(BigDecimal.valueOf(120)); // ₦120/piece
        pieceInventory.setMinimumStock(5);

        when(inventoryRepository.findById("inventory-piece")).thenReturn(Optional.of(pieceInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        InventoryResponse result = inventoryService.getInventoryById("inventory-piece", testPrincipal);

        // Assert: 50 pieces × ₦120 = ₦6,000
        assertThat(result.getItemProjectedSales())
            .isNotNull()
            .isEqualByComparingTo(BigDecimal.valueOf(6000));
        // Profit = ₦6,000 - ₦5,000 = ₦1,000
        assertThat(result.getItemProjectedProfit())
            .isNotNull()
            .isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void updateInventoryStatus_ShouldUpdateStatus() {
        // Arrange
        testInventory.setStatus(Inventory.InventoryStatus.ACTIVE);
        when(inventoryRepository.findById("inventory-1")).thenReturn(Optional.of(testInventory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // Act
        InventoryResponse result = inventoryService.updateInventoryStatus("inventory-1", Inventory.InventoryStatus.DISCONTINUED, testPrincipal);

        // Assert
        assertThat(result).isNotNull();
        verify(inventoryRepository).findById("inventory-1");
        verify(inventoryRepository).save(testInventory);
        verify(auditService).logEntityModification(eq("Inventory"), eq("inventory-1"), anyString());
    }
}