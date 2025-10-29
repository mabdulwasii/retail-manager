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
import com.princely.shopmanager.inventory.domain.Inventory;
import com.princely.shopmanager.inventory.repository.InventoryRepository;
import com.princely.shopmanager.shared.events.ProductCreatedEvent;
import com.princely.shopmanager.shared.security.TenantSecurityValidator;
import com.princely.shopmanager.shared.service.AuditService;
import jakarta.persistence.EntityNotFoundException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    private TenantSecurityValidator tenantSecurityValidator;

    @Mock
    private AuditService auditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ProductService productService;

    private Shop testShop;
    private Category testCategory;
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
            .sku("COCA-500ML")
            .barcode("5449000000996")
            .shop(testShop)
            .category(testCategory)
            .price(new BigDecimal("500.00"))
            .costPrice(new BigDecimal("350.00"))
            .status(Product.ProductStatus.ACTIVE)
            .build();

        createRequest = ProductCreateRequest.builder()
            .name("Coca-Cola 500ml")
            .sku("COCA-500ML")
            .barcode("5449000000996")
            .shopId("shop-1")
            .categoryId("category-1")
            .price(new BigDecimal("500.00"))
            .costPrice(new BigDecimal("350.00"))
            .build();
    }

    @Test
    void createProduct_WithValidData_ShouldCreateProduct() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySkuAndShopId("COCA-500ML", "shop-1")).thenReturn(false);
        when(productRepository.existsByBarcodeAndShopId("5449000000996", "shop-1")).thenReturn(false);

        // Mock save to return product with ID set
        Product savedProduct = Product.builder()
            .id("product-1")
            .name("Coca-Cola 500ml")
            .sku("COCA-500ML")
            .barcode("5449000000996")
            .shop(testShop)
            .category(testCategory)
            .price(new BigDecimal("500.00"))
            .costPrice(new BigDecimal("350.00"))
            .status(Product.ProductStatus.ACTIVE)
            .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(savedProduct));
        when(inventoryRepository.findByProductId("product-1")).thenReturn(Collections.emptyList());

        // Act
        ProductResponse response = productService.createProduct(createRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Coca-Cola 500ml");
        assertThat(response.getSku()).isEqualTo("COCA-500ML");
        assertThat(response.getPrice()).isEqualByComparingTo("500.00");
        assertThat(response.getTotalStock()).isZero();
        assertThat(response.getAvailableStock()).isZero();

        verify(tenantSecurityValidator, atLeastOnce()).validateShopAccess(testShop);
        verify(productRepository).save(any(Product.class));
        verify(auditService).logEntityCreation(eq("Product"), eq("product-1"), anyString());
        verify(eventPublisher).publishEvent(any(ProductCreatedEvent.class));
    }

    @Test
    void createProduct_WithDuplicateSKU_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.existsBySkuAndShopId("COCA-500ML", "shop-1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SKU")
            .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createProduct_WithDuplicateBarcode_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.existsBySkuAndShopId("COCA-500ML", "shop-1")).thenReturn(false);
        when(productRepository.existsByBarcodeAndShopId("5449000000996", "shop-1")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("barcode")
            .hasMessageContaining("already exists");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_WithInvalidShop_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Shop not found");
    }

    @Test
    void createProduct_WithInvalidCategory_ShouldThrowException() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.existsBySkuAndShopId("COCA-500ML", "shop-1")).thenReturn(false);
        when(categoryRepository.findById("category-1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    void updateProduct_WithValidData_ShouldUpdateProduct() {
        // Arrange
        ProductUpdateRequest updateRequest = ProductUpdateRequest.builder()
            .name("Coca-Cola 500ml (Updated)")
            .price(new BigDecimal("550.00"))
            .build();

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(inventoryRepository.findByProductId("product-1")).thenReturn(Collections.emptyList());

        // Act
        ProductResponse response = productService.updateProduct("product-1", updateRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(tenantSecurityValidator, atLeastOnce()).validateShopAccess(testShop);
        verify(productRepository).save(any(Product.class));
        verify(auditService).logEntityModification(eq("Product"), eq("product-1"), anyString());
    }

    @Test
    void updateProduct_WithNonExistentProduct_ShouldThrowException() {
        // Arrange
        ProductUpdateRequest updateRequest = new ProductUpdateRequest();
        when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.updateProduct("non-existent", updateRequest))
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
        ProductResponse response = productService.getProductById("product-1", true);

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
        ProductResponse response = productService.getProductById("product-1", false);

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
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(productPage);

        // Act
        Page<ProductResponse> response = productService.getProducts(
            "shop-1",
            (root, query, cb) -> cb.equal(root.get("shop").get("id"), "shop-1"),
            Pageable.unpaged(),
            false  // Don't include inventory to avoid extra mocking
        );

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(tenantSecurityValidator).validateShopAccess(testShop);
    }

    @Test
    void deleteProduct_ShouldSoftDelete() {
        // Arrange
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.deleteProduct("product-1");

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
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1");

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
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1");

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
        ProductService.InventorySummary summary = productService.getInventorySummary("product-1");

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
        boolean hasStock = productService.hasAvailableStock("product-1", 50);

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
        boolean hasStock = productService.hasAvailableStock("product-1", 50);

        // Assert
        assertThat(hasStock).isFalse(); // Available: 100 - 90 = 10 < 50
    }

    @Test
    void getProductsWithLowStock_ShouldReturnLowStockProducts() {
        // Arrange
        Inventory lowStockInventory = createInventory("inv-1", 5, 0);
        lowStockInventory.setMinimumStock(10);
        lowStockInventory.setProduct(testProduct);

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(inventoryRepository.findLowStockItems("shop-1"))
            .thenReturn(Collections.singletonList(lowStockInventory));
        when(productRepository.findAllById(anyList()))
            .thenReturn(Collections.singletonList(testProduct));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.singletonList(lowStockInventory));

        // Act
        List<ProductResponse> response = productService.getProductsWithLowStock("shop-1");

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getHasLowStock()).isTrue();
    }

    @Test
    void getProductsWithNoStock_ShouldReturnOutOfStockProducts() {
        // Arrange
        when(productRepository.findByShopId("shop-1"))
            .thenReturn(Collections.singletonList(testProduct));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(inventoryRepository.findByProductId("product-1"))
            .thenReturn(Collections.emptyList()); // No inventory = no stock

        // Act
        List<ProductResponse> response = productService.getProductsWithNoStock("shop-1");

        // Assert
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTotalStock()).isZero();
    }

    @Test
    void createProduct_ShouldPublishProductCreatedEvent() {
        // Arrange
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(categoryRepository.findById("category-1")).thenReturn(Optional.of(testCategory));
        when(productRepository.existsBySkuAndShopId(anyString(), anyString())).thenReturn(false);

        Product savedProduct = Product.builder()
            .id("product-1")
            .name("Coca-Cola 500ml")
            .sku("COCA-500ML")
            .shop(testShop)
            .category(testCategory)
            .price(new BigDecimal("500.00"))
            .status(Product.ProductStatus.ACTIVE)
            .build();
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(savedProduct));
        when(inventoryRepository.findByProductId(anyString())).thenReturn(Collections.emptyList());

        // Act
        productService.createProduct(createRequest);

        // Assert
        ArgumentCaptor<ProductCreatedEvent> eventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        ProductCreatedEvent event = eventCaptor.getValue();
        assertThat(event.productId()).isEqualTo("product-1");
        assertThat(event.shopId()).isEqualTo("shop-1");
        assertThat(event.productName()).isEqualTo("Coca-Cola 500ml");
        assertThat(event.sku()).isEqualTo("COCA-500ML");
    }

    // Helper methods

    private Inventory createInventory(String id, int currentStock, int reservedStock) {
        Inventory inventory = Inventory.builder()
            .id(id)
            .shop(testShop)
            .product(testProduct)
            .currentStock(currentStock)
            .reservedStock(reservedStock)
            .minimumStock(0)
            .status(Inventory.InventoryStatus.ACTIVE)
            .build();
        return inventory;
    }
}
