package com.princely.shopmanager.sales;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.ProductUnitDefinition;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.core.repository.TenantRepository;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.domain.InventoryUnitPrice;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.inventory.repository.InventoryUnitPriceRepository;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.dto.SalesTransactionCreateRequest;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.sales.service.SalesTransactionService;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for multi-unit pricing sales flow.
 * Tests the complete flow from product creation with units to sales with unit conversion.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MultiUnitSalesFlowIT {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductUnitDefinitionRepository productUnitDefinitionRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryUnitPriceRepository inventoryUnitPriceRepository;

    @Autowired
    private SalesTransactionService salesTransactionService;

    @Autowired
    private SalesTransactionRepository salesTransactionRepository;

    private Tenant tenant;
    private Shop shop;
    private Category category;
    private Product product;
    private ProductUnitDefinition pieceUnit;
    private ProductUnitDefinition packUnit;
    private Inventory inventory;
    private JwtPrincipal principal;

    @BeforeEach
    void setUp() {
        // Create tenant
        tenant = Tenant.builder()
            .name("Test Retail Store")
            .contactEmail("test@retail.com")
            .status(Tenant.TenantStatus.ACTIVE)
            .build();
        tenant = tenantRepository.save(tenant);

        // Create shop
        shop = Shop.builder()
            .name("Main Branch")
            .tenant(tenant)
            .status(Shop.ShopStatus.ACTIVE)
            .build();
        shop = shopRepository.save(shop);

        // Create category
        category = Category.builder()
            .name("Beverages")
            .description("Drinks and beverages")
            .shop(shop)
            .build();
        category = categoryRepository.save(category);

        // Create product
        product = Product.builder()
            .name("Coca-Cola 500ml")
            .sku("COKE-500ML")
            .category(category)
            .shop(shop)
            .status(Product.ProductStatus.ACTIVE)
            .build();
        product = productRepository.save(product);

        // Create unit definitions
        pieceUnit = ProductUnitDefinition.builder()
            .product(product)
            .unitType("piece")
            .unitLabel("Piece")
            .conversionFactor(BigDecimal.ONE)
            .isBaseUnit(true)
            .sortOrder(0)
            .build();
        pieceUnit = productUnitDefinitionRepository.save(pieceUnit);

        packUnit = ProductUnitDefinition.builder()
            .product(product)
            .unitType("pack")
            .unitLabel("Pack (12pcs)")
            .conversionFactor(BigDecimal.valueOf(12.0))
            .isBaseUnit(false)
            .sortOrder(1)
            .build();
        packUnit = productUnitDefinitionRepository.save(packUnit);

        // Create inventory with 120 pieces
        inventory = Inventory.builder()
            .product(product)
            .shop(shop)
            .currentStock(120)
            .availableStock(120)
            .reservedStock(0)
            .minimumStock(10)
            .reorderPoint(20)
            .costPrice(BigDecimal.valueOf(900))
            .sellingPrice(BigDecimal.valueOf(1050))
            .baseUnit("piece")
            .status(Inventory.InventoryStatus.ACTIVE)
            .expiryDate(LocalDate.now().plusMonths(6))
            .build();
        inventory = inventoryRepository.save(inventory);

        // Create unit prices
        InventoryUnitPrice piecePrice = InventoryUnitPrice.builder()
            .inventory(inventory)
            .unitType("piece")
            .sellingPrice(BigDecimal.valueOf(1050))
            .build();
        inventoryUnitPriceRepository.save(piecePrice);

        InventoryUnitPrice packPrice = InventoryUnitPrice.builder()
            .inventory(inventory)
            .unitType("pack")
            .sellingPrice(BigDecimal.valueOf(12000))
            .build();
        inventoryUnitPriceRepository.save(packPrice);

        // Create JWT principal
        principal = new JwtPrincipal(
            "user1",
            "testuser",
            "test@example.com",
            List.of(),
            tenant.getId(),
            shop.getId(),
            List.of("SALES_CREATE")
        );
    }

    @Test
    void shouldCreateSaleWithPackUnitAndConvertToBaseUnits() {
        // Given - Customer wants to buy 5 packs (5 × 12 = 60 pieces)
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(5) // 5 packs
                .unitPrice(BigDecimal.valueOf(12000)) // Pack price
                .unitType("pack")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(shop.getId())
            .lineItems(List.of(lineItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When
        SalesTransaction sale = salesTransactionService.createSalesTransaction(request, principal);

        // Then
        assertNotNull(sale);
        assertEquals(1, sale.getLineItems().size());

        // Verify line item details
        var saleLineItem = sale.getLineItems().get(0);
        assertEquals(5, saleLineItem.getQuantity()); // Original quantity: 5 packs
        assertEquals("pack", saleLineItem.getUnitType());
        assertEquals("Pack (12pcs)", saleLineItem.getUnitLabel());
        assertEquals(BigDecimal.valueOf(12.0), saleLineItem.getUnitConversionFactor());
        assertEquals(60, saleLineItem.getBaseUnitQuantity()); // Converted: 5 × 12 = 60

        // Verify inventory was deducted by base units (60 pieces)
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(60, updatedInventory.getAvailableStock()); // 120 - 60 = 60 remaining
        assertEquals(60, updatedInventory.getCurrentStock());

        // Verify total amount
        assertEquals(BigDecimal.valueOf(60000.0), sale.getTotalAmount()); // 5 packs × ₦12,000
    }

    @Test
    void shouldCreateSaleWithPieceUnitDirectly() {
        // Given - Customer wants to buy 10 pieces
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(10) // 10 pieces
                .unitPrice(BigDecimal.valueOf(1050)) // Piece price
                .unitType("piece")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(shop.getId())
            .lineItems(List.of(lineItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When
        SalesTransaction sale = salesTransactionService.createSalesTransaction(request, principal);

        // Then
        assertNotNull(sale);

        var saleLineItem = sale.getLineItems().get(0);
        assertEquals(10, saleLineItem.getQuantity());
        assertEquals("piece", saleLineItem.getUnitType());
        assertEquals(10, saleLineItem.getBaseUnitQuantity()); // No conversion needed

        // Verify inventory deduction
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(110, updatedInventory.getAvailableStock()); // 120 - 10 = 110

        // Verify total
        assertEquals(BigDecimal.valueOf(10500.0), sale.getTotalAmount()); // 10 × ₦1,050
    }

    @Test
    void shouldHandleMixedUnitTypesInSingleTransaction() {
        // Given - Customer buys both packs and pieces
        SalesTransactionCreateRequest.LineItemRequest packItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(2) // 2 packs = 24 pieces
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest.LineItemRequest pieceItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(6) // 6 pieces
                .unitPrice(BigDecimal.valueOf(1050))
                .unitType("piece")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(shop.getId())
            .lineItems(List.of(packItem, pieceItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When
        SalesTransaction sale = salesTransactionService.createSalesTransaction(request, principal);

        // Then
        assertNotNull(sale);
        assertEquals(2, sale.getLineItems().size());

        // Total deduction should be: (2 × 12) + 6 = 30 pieces
        Inventory updatedInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(90, updatedInventory.getAvailableStock()); // 120 - 30 = 90

        // Verify total amount: (2 × 12000) + (6 × 1050) = 24000 + 6300 = 30300
        assertEquals(BigDecimal.valueOf(30300.0), sale.getTotalAmount());
    }

    @Test
    void shouldFailWhenInsufficientStockForPackPurchase() {
        // Given - Try to buy 15 packs but only have 120 pieces (10 packs)
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(15) // 15 packs = 180 pieces, but only 120 available
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(shop.getId())
            .lineItems(List.of(lineItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When/Then
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> salesTransactionService.createSalesTransaction(request, principal)
        );

        assertTrue(exception.getMessage().contains("Insufficient stock"));

        // Verify inventory was NOT modified
        Inventory unchangedInventory = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(120, unchangedInventory.getAvailableStock());
    }

    @Test
    void shouldTrackUnitInformationInLineItem() {
        // Given
        SalesTransactionCreateRequest.LineItemRequest lineItem =
            SalesTransactionCreateRequest.LineItemRequest.builder()
                .productId(product.getId())
                .quantity(3)
                .unitPrice(BigDecimal.valueOf(12000))
                .unitType("pack")
                .discount(BigDecimal.ZERO)
                .build();

        SalesTransactionCreateRequest request = SalesTransactionCreateRequest.builder()
            .shopId(shop.getId())
            .lineItems(List.of(lineItem))
            .paymentMethod(SalesTransaction.PaymentMethod.CASH)
            .build();

        // When
        SalesTransaction sale = salesTransactionService.createSalesTransaction(request, principal);

        // Then - Verify all unit tracking fields are stored
        var saleLineItem = sale.getLineItems().get(0);
        assertNotNull(saleLineItem.getUnitType());
        assertNotNull(saleLineItem.getUnitLabel());
        assertNotNull(saleLineItem.getUnitConversionFactor());
        assertNotNull(saleLineItem.getBaseUnitQuantity());

        assertEquals("pack", saleLineItem.getUnitType());
        assertEquals("Pack (12pcs)", saleLineItem.getUnitLabel());
        assertEquals(BigDecimal.valueOf(12.0), saleLineItem.getUnitConversionFactor());
        assertEquals(36, saleLineItem.getBaseUnitQuantity()); // 3 × 12 = 36
    }
}
