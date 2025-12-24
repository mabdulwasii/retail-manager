package com.princely.shopmanager.inventory.specification;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.inventory.domain.Inventory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventorySpecifications Tests")
class InventorySpecificationsTest {

    @Mock
    private Root<Inventory> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> path;

    @Mock
    private Path<Integer> intPath;

    @Mock
    private Path<LocalDate> datePath;

    @Mock
    private Predicate predicate;

    @BeforeEach
    void setUp() {
        when(root.get(anyString())).thenReturn(path);
        when(criteriaBuilder.equal(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.lessThanOrEqualTo(any(Path.class), any())).thenReturn(predicate);
        when(criteriaBuilder.greaterThanOrEqualTo(any(Path.class), any())).thenReturn(predicate);
        when(criteriaBuilder.lessThan(any(Path.class), any(LocalDate.class))).thenReturn(predicate);
        when(criteriaBuilder.isNotNull(any())).thenReturn(predicate);
        when(criteriaBuilder.and(any(), any())).thenReturn(predicate);
        when(criteriaBuilder.like(any(), anyString())).thenReturn(predicate);
        when(criteriaBuilder.lower(any())).thenReturn(path);
        when(criteriaBuilder.diff(any(), any())).thenReturn(intPath);
    }

    @Test
    @DisplayName("hasLowStock - Should create specification for low stock items")
    void hasLowStock_ShouldCreateSpecificationForLowStockItems() {
        // Given
        Specification<Inventory> spec = InventorySpecifications.hasLowStock();

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).diff(any(), any()); // currentStock - reservedStock
        verify(criteriaBuilder).lessThanOrEqualTo(any(Path.class), any()); // <= minimumStock
    }

    @Test
    @DisplayName("expiresWithinDays - Should create specification for items expiring soon")
    void expiresWithinDays_ShouldCreateSpecificationForItemsExpiringSoon() {
        // Given
        int days = 7;
        Specification<Inventory> spec = InventorySpecifications.expiresWithinDays(days);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).isNotNull(any());
        verify(criteriaBuilder).lessThanOrEqualTo(any(Path.class), any());
        verify(criteriaBuilder).and(any(), any());
    }

    @Test
    @DisplayName("forShop - Should create specification filtering by shop ID")
    void forShop_ShouldCreateSpecificationFilteringByShopId() {
        // Given
        String shopId = "shop-1";
        Path<Shop> shopPath = mock(Path.class);
        Path<String> idPath = mock(Path.class);

        when(root.get("shop")).thenReturn(shopPath);
        when(shopPath.get("id")).thenReturn(idPath);

        Specification<Inventory> spec = InventorySpecifications.forShop(shopId);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(idPath, shopId);
    }

    @Test
    @DisplayName("hasStatus - Should create specification filtering by status")
    void hasStatus_ShouldCreateSpecificationFilteringByStatus() {
        // Given
        Inventory.InventoryStatus status = Inventory.InventoryStatus.ACTIVE;
        Specification<Inventory> spec = InventorySpecifications.hasStatus(status);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(any(), any());
    }

    @Test
    @DisplayName("forProduct - Should create specification filtering by product ID")
    void forProduct_ShouldCreateSpecificationFilteringByProductId() {
        // Given
        String productId = "product-1";
        Path<Product> productPath = mock(Path.class);
        Path<String> idPath = mock(Path.class);

        when(root.get("product")).thenReturn(productPath);
        when(productPath.get("id")).thenReturn(idPath);

        Specification<Inventory> spec = InventorySpecifications.forProduct(productId);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(idPath, productId);
    }

    @Test
    @DisplayName("atLocation - Should create specification filtering by location")
    void atLocation_ShouldCreateSpecificationFilteringByLocation() {
        // Given
        String location = "Warehouse A";
        Specification<Inventory> spec = InventorySpecifications.atLocation(location);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(any(), any());
    }

    @Test
    @DisplayName("hasStockAvailable - Should create specification for minimum available quantity")
    void hasStockAvailable_ShouldCreateSpecificationForMinimumAvailableQuantity() {
        // Given
        int minimumQuantity = 10;
        Specification<Inventory> spec = InventorySpecifications.hasStockAvailable(minimumQuantity);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).diff(any(), any()); // currentStock - reservedStock
        verify(criteriaBuilder).greaterThanOrEqualTo(any(Path.class), any()); // >= minimumQuantity
    }

    @Test
    @DisplayName("isExpired - Should create specification for expired items")
    void isExpired_ShouldCreateSpecificationForExpiredItems() {
        // Given
        Specification<Inventory> spec = InventorySpecifications.isExpired();

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).isNotNull(any());
        verify(criteriaBuilder).lessThan(any(Path.class), any(LocalDate.class));
        verify(criteriaBuilder).and(any(), any());
    }

    @Test
    @DisplayName("forCategory - Should create specification filtering by category ID")
    void forCategory_ShouldCreateSpecificationFilteringByCategoryId() {
        // Given
        String categoryId = "category-1";
        Path<Product> productPath = mock(Path.class);
        Path<Category> categoryPath = mock(Path.class);
        Path<String> idPath = mock(Path.class);

        when(root.get("product")).thenReturn(productPath);
        when(productPath.get("category")).thenReturn(categoryPath);
        when(categoryPath.get("id")).thenReturn(idPath);

        Specification<Inventory> spec = InventorySpecifications.forCategory(categoryId);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).equal(idPath, categoryId);
    }

    @Test
    @DisplayName("productNameContains - Should create case-insensitive LIKE specification")
    void productNameContains_ShouldCreateCaseInsensitiveLikeSpecification() {
        // Given
        String productName = "Apple";
        Path<Product> productPath = mock(Path.class);
        Path<String> namePath = mock(Path.class);

        when(root.get("product")).thenReturn(productPath);
        when(productPath.get("name")).thenReturn(namePath);
        when(criteriaBuilder.lower(namePath)).thenReturn(path);

        Specification<Inventory> spec = InventorySpecifications.productNameContains(productName);

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
        verify(criteriaBuilder).lower(namePath);
        verify(criteriaBuilder).like(any(), any());
    }

    @Test
    @DisplayName("Specifications can be combined using and/or")
    void specifications_CanBeCombined() {
        // Given
        Specification<Inventory> spec = InventorySpecifications.forShop("shop-1")
            .and(InventorySpecifications.hasStatus(Inventory.InventoryStatus.ACTIVE))
            .and(InventorySpecifications.hasLowStock());

        // When
        Predicate result = spec.toPredicate(root, query, criteriaBuilder);

        // Then
        assertThat(result).isNotNull();
    }
}
