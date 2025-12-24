package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.ProductUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductFieldUpdater Tests")
class ProductFieldUpdaterTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductFieldUpdater productFieldUpdater;

    private Shop testShop;
    private Product product;
    private Category category;
    private StringBuilder changes;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        category = Category.builder()
            .id("cat-1")
            .name("Electronics")
            .shop(testShop)
            .build();

        product = Product.builder()
            .id("prod-1")
            .shop(testShop)
            .name("Laptop")
            .description("Gaming laptop")
            .status(Product.ProductStatus.ACTIVE)
            .barcode("BAR123")
            .unit("PIECE")
            .build();

        changes = new StringBuilder();
    }

    // updateBasicFields Tests
    @Test
    @DisplayName("updateBasicFields - Should update name and track change")
    void updateBasicFields_ShouldUpdateNameAndTrackChange() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .name("Updated Laptop")
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(product.getName()).isEqualTo("Updated Laptop");
        assertThat(changes.toString()).contains("Name: Laptop → Updated Laptop");
    }

    @Test
    @DisplayName("updateBasicFields - Should not update name when null")
    void updateBasicFields_ShouldNotUpdateNameWhenNull() {
        // Given
        String originalName = product.getName();
        ProductUpdateRequest request = ProductUpdateRequest.builder().build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(product.getName()).isEqualTo(originalName);
        assertThat(changes.toString()).isEmpty();
    }

    @Test
    @DisplayName("updateBasicFields - Should not track change when name unchanged")
    void updateBasicFields_ShouldNotTrackChangeWhenNameUnchanged() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .name("Laptop") // Same as current
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(changes.toString()).isEmpty();
    }

    @Test
    @DisplayName("updateBasicFields - Should update description")
    void updateBasicFields_ShouldUpdateDescription() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .description("High-performance gaming laptop")
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(product.getDescription()).isEqualTo("High-performance gaming laptop");
    }

    @Test
    @DisplayName("updateBasicFields - Should update status and track change")
    void updateBasicFields_ShouldUpdateStatusAndTrackChange() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .status(Product.ProductStatus.DISCONTINUED)
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(product.getStatus()).isEqualTo(Product.ProductStatus.DISCONTINUED);
        assertThat(changes.toString()).contains("Status: ACTIVE → DISCONTINUED");
    }

    @Test
    @DisplayName("updateBasicFields - Should not track status change when unchanged")
    void updateBasicFields_ShouldNotTrackStatusChangeWhenUnchanged() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .status(Product.ProductStatus.ACTIVE) // Same as current
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(changes.toString()).isEmpty();
    }

    @Test
    @DisplayName("updateBasicFields - Should update image URL")
    void updateBasicFields_ShouldUpdateImageUrl() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .imageUrl("https://example.com/laptop.jpg")
            .build();

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);

        // Then
        assertThat(product.getImageUrl()).isEqualTo("https://example.com/laptop.jpg");
    }

    // updateCatalogFields Tests
    @Test
    @DisplayName("updateCatalogFields - Should update barcode when unique")
    void updateCatalogFields_ShouldUpdateBarcodeWhenUnique() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .barcode("NEW123")
            .build();

        when(productRepository.existsByBarcodeAndShopId("NEW123", "shop-1")).thenReturn(false);

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getBarcode()).isEqualTo("NEW123");
    }

    @Test
    @DisplayName("updateCatalogFields - Should not update barcode when unchanged")
    void updateCatalogFields_ShouldNotUpdateBarcodeWhenUnchanged() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .barcode("BAR123") // Same as current
            .build();

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getBarcode()).isEqualTo("BAR123");
    }

    @Test
    @DisplayName("updateCatalogFields - Should throw exception when barcode already exists")
    void updateCatalogFields_ShouldThrowExceptionWhenBarcodeAlreadyExists() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .barcode("EXISTING123")
            .build();

        when(productRepository.existsByBarcodeAndShopId("EXISTING123", "shop-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productFieldUpdater.updateCatalogFields(product, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Barcode already exists");
    }

    @Test
    @DisplayName("updateCatalogFields - Should update category when found")
    void updateCatalogFields_ShouldUpdateCategoryWhenFound() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .categoryId("cat-1")
            .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getCategory()).isEqualTo(category);
    }

    @Test
    @DisplayName("updateCatalogFields - Should throw exception when category not found")
    void updateCatalogFields_ShouldThrowExceptionWhenCategoryNotFound() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .categoryId("cat-999")
            .build();

        when(categoryRepository.findById("cat-999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productFieldUpdater.updateCatalogFields(product, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("updateCatalogFields - Should update unit")
    void updateCatalogFields_ShouldUpdateUnit() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .unit("KILOGRAM")
            .build();

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getUnit()).isEqualTo("KILOGRAM");
    }

    @Test
    @DisplayName("updateCatalogFields - Should update weight in grams")
    void updateCatalogFields_ShouldUpdateWeightInGrams() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .weightInGrams(2500.0)
            .build();

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getWeightInGrams()).isEqualTo(2500);
    }

    @Test
    @DisplayName("updateCatalogFields - Should update dimensions")
    void updateCatalogFields_ShouldUpdateDimensions() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .dimensions("35cm x 25cm x 2cm")
            .build();

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getDimensions()).isEqualTo("35cm x 25cm x 2cm");
    }

    // updateSupplierAndPricingFields Tests
    @Test
    @DisplayName("updateSupplierAndPricingFields - Should update supplier name")
    void updateSupplierAndPricingFields_ShouldUpdateSupplierName() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .supplierName("Acme Suppliers")
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.getSupplierName()).isEqualTo("Acme Suppliers");
    }

    @Test
    @DisplayName("updateSupplierAndPricingFields - Should update supplier contact")
    void updateSupplierAndPricingFields_ShouldUpdateSupplierContact() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .supplierContact("supplier@acme.com")
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.getSupplierContact()).isEqualTo("supplier@acme.com");
    }

    @Test
    @DisplayName("updateSupplierAndPricingFields - Should update isTaxable")
    void updateSupplierAndPricingFields_ShouldUpdateIsTaxable() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .isTaxable(false)
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.isTaxable()).isFalse();
    }

    @Test
    @DisplayName("updateSupplierAndPricingFields - Should update isDiscountable")
    void updateSupplierAndPricingFields_ShouldUpdateIsDiscountable() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .isDiscountable(false)
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.isDiscountable()).isFalse();
    }

    @Test
    @DisplayName("updateSupplierAndPricingFields - Should update metadata")
    void updateSupplierAndPricingFields_ShouldUpdateMetadata() {
        // Given
        Map<String, Object> metadata = Map.of(
            "warranty", "2 years",
            "color", "silver"
        );
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .metadata(metadata)
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.getMetadata()).hasSize(2);
        assertThat(product.getMetadata()).containsEntry("warranty", "2 years");
        assertThat(product.getMetadata()).containsEntry("color", "silver");
    }

    // Integration Tests
    @Test
    @DisplayName("Full update - Should update all catalog fields together")
    void fullUpdate_ShouldUpdateAllCatalogFieldsTogether() {
        // Given
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .barcode("NEW456")
            .categoryId("cat-1")
            .unit("LITER")
            .weightInGrams(1500.0)
            .dimensions("10cm x 10cm x 20cm")
            .build();

        when(productRepository.existsByBarcodeAndShopId("NEW456", "shop-1")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        // When
        productFieldUpdater.updateCatalogFields(product, request);

        // Then
        assertThat(product.getBarcode()).isEqualTo("NEW456");
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getUnit()).isEqualTo("LITER");
        assertThat(product.getWeightInGrams()).isEqualTo(1500);
        assertThat(product.getDimensions()).isEqualTo("10cm x 10cm x 20cm");
    }

    @Test
    @DisplayName("Full update - Should update all supplier and pricing fields together")
    void fullUpdate_ShouldUpdateAllSupplierAndPricingFieldsTogether() {
        // Given
        Map<String, Object> metadata = Map.of("brand", "Dell");
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .supplierName("Tech Distributors")
            .supplierContact("tech@dist.com")
            .isTaxable(true)
            .isDiscountable(true)
            .metadata(metadata)
            .build();

        // When
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.getSupplierName()).isEqualTo("Tech Distributors");
        assertThat(product.getSupplierContact()).isEqualTo("tech@dist.com");
        assertThat(product.isTaxable()).isTrue();
        assertThat(product.isDiscountable()).isTrue();
        assertThat(product.getMetadata()).containsEntry("brand", "Dell");
    }

    @Test
    @DisplayName("Complete product update - Should handle all fields")
    void completeProductUpdate_ShouldHandleAllFields() {
        // Given
        Map<String, Object> metadata = Map.of("model", "XPS 15");
        ProductUpdateRequest request = ProductUpdateRequest.builder()
            .name("Dell XPS 15")
            .description("Professional laptop")
            .status(Product.ProductStatus.ACTIVE)
            .imageUrl("https://example.com/xps15.jpg")
            .barcode("DELL123")
            .categoryId("cat-1")
            .unit("PIECE")
            .weightInGrams(1800.0)
            .dimensions("35cm x 23cm x 1.5cm")
            .supplierName("Dell Official")
            .supplierContact("dell@official.com")
            .isTaxable(true)
            .isDiscountable(false)
            .metadata(metadata)
            .build();

        when(productRepository.existsByBarcodeAndShopId("DELL123", "shop-1")).thenReturn(false);
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(category));

        // When
        productFieldUpdater.updateBasicFields(product, request, changes);
        productFieldUpdater.updateCatalogFields(product, request);
        productFieldUpdater.updateSupplierAndPricingFields(product, request);

        // Then
        assertThat(product.getName()).isEqualTo("Dell XPS 15");
        assertThat(product.getDescription()).isEqualTo("Professional laptop");
        assertThat(product.getBarcode()).isEqualTo("DELL123");
        assertThat(product.getCategory()).isEqualTo(category);
        assertThat(product.getSupplierName()).isEqualTo("Dell Official");
        assertThat(product.isTaxable()).isTrue();
        assertThat(product.isDiscountable()).isFalse();
    }
}
