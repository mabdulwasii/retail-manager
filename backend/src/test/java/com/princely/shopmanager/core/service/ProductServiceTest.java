package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ProductCreateRequest;
import com.princely.shopmanager.core.dto.ProductResponse;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.events.ProductCreatedEvent;
import com.princely.shopmanager.shared.exception.ShopNotFoundException;
import com.princely.shopmanager.shared.service.AuditService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ShopAccessValidator shopAccessValidator;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProductFieldUpdater productFieldUpdater;

    @Mock
    private com.princely.shopmanager.core.repository.ProductUnitDefinitionRepository productUnitDefinitionRepository;

    private ProductService productService;

    private Shop testShop;
    private Category testCategory;
    private JwtPrincipal testPrincipal;
    private Product testProduct;
    private ProductCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        testCategory = new Category();
        testCategory.setId("category-1");
        testCategory.setName("Beverages");

        testProduct = Product.builder()
            .id("product-1")
            .name("Coca-Cola 500ml")
            .barcode("5449000000996")
            .shop(testShop)
            .category(testCategory)
            .status(Product.ProductStatus.ACTIVE)
            .build();

        createRequest = ProductCreateRequest.builder()
            .name("Coca-Cola 500ml")
            .barcode("5449000000996")
            .shopId("shop-1")
            .categoryId("category-1")
            .build();

        testPrincipal = JwtPrincipal.builder()
            .subject("test-user")
            .preferredUsername("testuser")
            .roles(List.of("MANAGER"))
            .tenantId("tenant-1")
            .shopId("shop-1")
            .build();

        productService = new ProductService(
            shopAccessValidator,
            shopRepository,
            productRepository,
            categoryRepository,
            productUnitDefinitionRepository,
            inventoryRepository,
            auditService,
            eventPublisher,
            productFieldUpdater
        );
    }

    @Test
    void createProduct_WithValidData_ShouldCreateProduct() {
        // Arrange
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        // Mock auto-generated SKU check (any SKU format should return false for uniqueness)
        when(productRepository.existsBySkuAndShopId(anyString(), eq("shop-1"))).thenReturn(false);
        when(productRepository.existsByBarcodeAndShopId("5449000000996", "shop-1")).thenReturn(false);

        // Mock save to return the same product with ID assigned
        Product[] savedProductHolder = new Product[1];  // Array to capture saved product
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            // Simulate database assigning an ID
            Product saved = Product.builder()
                .id("product-1")
                .name(product.getName())
                .sku(product.getSku())  // Keep the auto-generated SKU
                .barcode(product.getBarcode())
                .shop(product.getShop())
                .category(product.getCategory())
                .status(product.getStatus())
                .unit(product.getUnit())
                .weightInGrams(product.getWeightInGrams())
                .dimensions(product.getDimensions())
                .supplierName(product.getSupplierName())
                .supplierContact(product.getSupplierContact())
                .imageUrl(product.getImageUrl())
                .isTaxable(product.isTaxable())
                .isDiscountable(product.isDiscountable())
                .metadata(product.getMetadata())
                .build();
            savedProductHolder[0] = saved;  // Store for findById mock
            return saved;
        });

        // Mock findById to return the saved product (needed by getInventorySummary)
        when(productRepository.findById("product-1")).thenAnswer(invocation ->
            Optional.ofNullable(savedProductHolder[0]));
        when(inventoryRepository.findByProductId("product-1")).thenReturn(Collections.emptyList());

        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        ProductResponse response = productService.createProduct(createRequest, testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Coca-Cola 500ml");
        // SKU is auto-generated, just verify it exists and follows pattern
        assertThat(response.getSku()).isNotNull();
        assertThat(response.getSku()).matches("[A-Z]{3,4}-[A-Z]{3}-\\d{8}-[A-Z0-9]{4}");
        assertThat(response.getTotalStock()).isZero();
        assertThat(response.getAvailableStock()).isZero();

        verify(productRepository).save(any(Product.class));
        verify(auditService).logEntityCreation(eq("Product"), anyString(), anyString());
        verify(eventPublisher).publishEvent(any(ProductCreatedEvent.class));
    }

    // Note: SKU duplicate test removed because SKUs are auto-generated and uniqueness is guaranteed
    // by the generateUniqueSku() method which retries up to 10 times if a duplicate is found

    @Test
    void createProduct_WithDuplicateBarcode_ShouldThrowException() {
        // Arrange
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        lenient().when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        lenient().when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        // Mock auto-generated SKU check
        lenient().when(productRepository.existsBySkuAndShopId(anyString(), eq("shop-1"))).thenReturn(false);
        when(productRepository.existsByBarcodeAndShopId("5449000000996", "shop-1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest, testPrincipal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("barcode")
            .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithInvalidShop_ShouldThrowException() {
        // Arrange
        when(shopRepository.existsById("shop-1")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest, testPrincipal))
            .isInstanceOf(ShopNotFoundException.class)
            .hasMessageContaining("Shop not found");
    }

    @Test
    void createProduct_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        lenient().when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        // Mock auto-generated SKU check
        lenient().when(productRepository.existsBySkuAndShopId(anyString(), eq("shop-1"))).thenReturn(false);
        when(categoryRepository.findById("category-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest, testPrincipal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void updateProduct_WithValidData_ShouldUpdateProduct() {
        // Arrange
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
            .name("Coca-Cola 500ml (Updated)")
            .build();

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryRepository.findByProductId("product-1")).thenReturn(Collections.emptyList());

        // Mock ProductFieldUpdater to actually update the product and populate changes
        org.mockito.Mockito.doAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ProductUpdateRequest request = invocation.getArgument(1);
            StringBuilder changes = invocation.getArgument(2);
            if (request.getName() != null && !request.getName().equals(product.getName())) {
                changes.append("Name: ").append(product.getName()).append(" → ").append(request.getName()).append("; ");
                product.setName(request.getName());
            }
            return null;
        }).when(productFieldUpdater).updateBasicFields(any(), any(), any());

        // Act
        ProductResponse response = productService.updateProduct("product-1", updateRequest, testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(auditService).logEntityModification(eq("Product"), eq("product-1"), anyString());
    }

    @Test
    void updateProduct_WithNonExistentProduct_ShouldThrowException() {
        // Arrange
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct("non-existent", updateRequest, testPrincipal))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Product not found");
    }

    @Test
    void getProductById_WithInventory_ShouldReturnProductWithStock() {
        // Arrange
        Inventory inventory1 = createInventory("inv-1", 100, 10);
        Inventory inventory2 = createInventory("inv-2", 150, 5);

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Arrays.asList(inventory1, inventory2));

        // Act
        ProductResponse response = productService.getProductById("product-1", true, testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getTotalStock()).isEqualTo(250);
        assertThat(response.getAvailableStock()).isEqualTo(235); // 250 - 15 reserved
        assertThat(response.getReservedStock()).isEqualTo(15);
        assertThat(response.getInventoryCount()).isEqualTo(2);
    }

    @Test
    void getProductById_WithoutInventory_ShouldReturnProductWithoutStock() {
        // Arrange
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));

        // Act
        ProductResponse response = productService.getProductById("product-1", false, testPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Coca-Cola 500ml");
        assertThat(response.getTotalStock()).isNull();
        assertThat(response.getAvailableStock()).isNull();
    }

    @Test
    void getProducts_WithFilters_ShouldReturnFilteredProducts() {
        // Arrange
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(testProduct));
        lenient().when(shopRepository.existsById("shop-1")).thenReturn(true);
        lenient().when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(productPage);

        lenient().when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // Act
        Page<ProductResponse> response = productService.getProducts(
            "shop-1",
            (root, query, cb) -> cb.equal(root.get("shop").get("id"), "shop-1"),
            Pageable.unpaged(),
            false,  // Don't include inventory to avoid extra mocking
            testPrincipal
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void deleteProduct_ShouldSoftDelete() {
        // Arrange
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.deleteProduct("product-1", testPrincipal);

        // Assert
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getStatus()).isEqualTo(Product.ProductStatus.DISCONTINUED);
        verify(auditService).logEntityModification(eq("Product"), eq("product-1"), anyString());
    }

    @Test
    void getInventorySummary_WithMultipleBatches_ShouldAggregateCorrectly() {
        // Arrange
        Inventory inventory1 = createInventory("inv-1", 100, 10);
        Inventory inventory2 = createInventory("inv-2", 150, 5);
        Inventory inventory3 = createInventory("inv-3", 80, 0);

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Arrays.asList(inventory1, inventory2, inventory3));

        // Act
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1", testPrincipal);

        // Assert
        assertThat(summary).isNotNull();
        assertThat(summary.getTotalStock()).isEqualTo(330); // 100 + 150 + 80
        // Available = (100-10) + (150-5) + (80-0) = 90 + 145 + 80 = 315
        assertThat(summary.getAvailableStock()).isEqualTo(315);
        assertThat(summary.getReservedStock()).isEqualTo(15); // 10 + 5 + 0
        assertThat(summary.getInventoryCount()).isEqualTo(3);
    }

    @Test
    void getInventorySummary_WithLowStock_ShouldDetect() {
        // Arrange
        Inventory lowStockInventory = createInventory("inv-1", 5, 0);
        lowStockInventory.setMinimumStock(10); // Low stock: 5 < 10

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(lowStockInventory));

        // Act
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1", testPrincipal);

        // Assert
        assertThat(summary.getHasLowStock()).isTrue();
    }

    @Test
    void getInventorySummary_WithExpiredBatch_ShouldDetect() {
        // Arrange
        Inventory expiredInventory = createInventory("inv-1", 100, 0);
        expiredInventory.setExpiryDate(LocalDate.now().minusDays(1)); // Expired yesterday

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(expiredInventory));

        // Act
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1", testPrincipal);

        // Assert
        assertThat(summary.getHasExpiredBatches()).isTrue();
    }

    @Test
    void hasAvailableStock_WithSufficientStock_ShouldReturnTrue() {
        // Arrange
        Inventory inventory = createInventory("inv-1", 100, 10);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(inventory));

        // Act
        boolean hasStock = productService.hasAvailableStock("product-1", 50, testPrincipal);

        // Assert
        assertThat(hasStock).isTrue(); // Available: 100 - 10 = 90 >= 50
    }

    @Test
    void hasAvailableStock_WithInsufficientStock_ShouldReturnFalse() {
        // Arrange
        Inventory inventory = createInventory("inv-1", 100, 90);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(inventory));

        // Act
        boolean hasStock = productService.hasAvailableStock("product-1", 50, testPrincipal);

        // Assert
        assertThat(hasStock).isFalse(); // Available: 100 - 90 = 10 < 50
    }

    @Test
    void getProductsWithLowStock_ShouldReturnLowStockProducts() {
        // Arrange
        Inventory lowStockInventory = createInventory("inv-1", 5, 0);
        lowStockInventory.setMinimumStock(10);
        lowStockInventory.setProduct(testProduct);

        lenient().when(shopRepository.existsById("shop-1")).thenReturn(true);
        lenient().when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        lenient().when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(inventoryRepository.findLowStockItems("shop-1"))
            .thenReturn(Collections.singletonList(lowStockInventory));
        when(productRepository.findAllById(anyList()))
            .thenReturn(Collections.singletonList(testProduct));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(lowStockInventory));

        // Act
        List<ProductResponse> response = productService.getProductsWithLowStock("shop-1", testPrincipal);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getHasLowStock()).isTrue();
    }

    @Test
    void getProductsWithNoStock_ShouldReturnOutOfStockProducts() {
        // Arrange
        lenient().when(shopRepository.existsById("shop-1")).thenReturn(true);
        lenient().when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        lenient().when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(productRepository.findByShopId("shop-1"))
            .thenReturn(Collections.singletonList(testProduct));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.emptyList()); // No inventory = no stock

        // Act
        List<ProductResponse> response = productService.getProductsWithNoStock("shop-1", testPrincipal);

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.getFirst().getTotalStock()).isZero();
    }

    @Test
    void createProduct_ShouldPublishProductCreatedEvent() {
        // Arrange
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySkuAndShopId(anyString(), anyString())).thenReturn(false);

        Product savedProduct = Product.builder()
            .id("product-1")
            .name("Coca-Cola 500ml")
            .shop(testShop)
            .category(testCategory)
            .status(Product.ProductStatus.ACTIVE)
            .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(savedProduct));
        when(inventoryRepository.findByProductId(anyString())).thenReturn(Collections.emptyList());

        // Act
        productService.createProduct(createRequest, testPrincipal);

        // Assert
        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ProductCreatedEvent event = eventCaptor.getValue();
        assertThat(event.productId()).isEqualTo("product-1");
        assertThat(event.shopId()).isEqualTo("shop-1");
        assertThat(event.productName()).isEqualTo("Coca-Cola 500ml");
    }

    // Helper methods

    private Inventory createInventory(String id, int currentStock, int reservedStock) {
        return Inventory.builder()
            .id(id)
            .shop(testShop)
            .product(testProduct)
            .purchaseQuantity(BigDecimal.valueOf(currentStock))
            .reservedStock(reservedStock)
            .minimumStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();
    }
}
