package com.princely.shopmanager.core.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.dto.CategoryCreateRequest;
import com.princely.shopmanager.core.dto.CategoryResponse;
import com.princely.shopmanager.core.dto.CategoryUpdateRequest;
import com.princely.shopmanager.core.repository.CategoryRepository;
import com.princely.shopmanager.core.repository.ProductRepository;
import com.princely.shopmanager.core.repository.ShopRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService Unit Tests")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private ShopAccessValidator shopAccessValidator;

    private CategoryService categoryService;

    private Shop testShop;
    private Category testCategory;
    private Category parentCategory;
    private JwtPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(
            shopAccessValidator,
            shopRepository,
            categoryRepository,
            productRepository
        );

        testShop = new Shop();
        testShop.setId("shop-1");
        testShop.setName("Test Shop");

        parentCategory = Category.builder()
            .id("parent-cat-1")
            .name("Parent Category")
            .shop(testShop)
            .isActive(true)
            .build();

        testCategory = Category.builder()
            .id("cat-1")
            .name("Electronics")
            .description("Electronic devices")
            .shop(testShop)
            .parent(null)
            .displayOrder(1)
            .isActive(true)
            .imageUrl("https://example.com/electronics.jpg")
            .build();

        testPrincipal = JwtPrincipal.builder()
            .subject("user-1")
            .email("test@example.com")
            .tenantId("tenant-1")
            .shopId("shop-1")
            .roles(List.of("OWNER"))
            .build();
    }

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategory() {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .shopId("shop-1")
            .name("Electronics")
            .description("Electronic devices")
            .displayOrder(1)
            .isActive(true)
            .imageUrl("https://example.com/electronics.jpg")
            .build();

        // Mock shop access validation
        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));

        when(categoryRepository.existsByNameAndShop_Id("Electronics", "shop-1")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse response = categoryService.createCategory(request, testPrincipal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Electronics");
        assertThat(response.getDescription()).isEqualTo("Electronic devices");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should create category with parent")
    void shouldCreateCategoryWithParent() {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .shopId("shop-1")
            .name("Smartphones")
            .parentId("parent-cat-1")
            .build();

        Category childCategory = Category.builder()
            .id("child-1")
            .name("Smartphones")
            .shop(testShop)
            .parent(parentCategory)
            .build();

        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(categoryRepository.findById("parent-cat-1")).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByNameAndShop_Id("Smartphones", "shop-1")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(childCategory);

        // When
        CategoryResponse response = categoryService.createCategory(request, testPrincipal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Smartphones");
        verify(categoryRepository).findById("parent-cat-1");
    }

    @Test
    @DisplayName("Should throw exception when shop not found during creation")
    void shouldThrowExceptionWhenShopNotFound() {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .shopId("non-existent-shop")
            .name("Electronics")
            .build();

        // ShopAwareService.validateShopAccess checks existsById first
        when(shopRepository.existsById("non-existent-shop")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> categoryService.createCategory(request, testPrincipal))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw exception when duplicate category name")
    void shouldThrowExceptionWhenDuplicateName() {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
            .shopId("shop-1")
            .name("Electronics")
            .build();

        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(categoryRepository.existsByNameAndShop_Id("Electronics", "shop-1")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.createCategory(request, testPrincipal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should get category by ID successfully")
    void shouldGetCategoryById() {
        // Given
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);

        // When
        CategoryResponse response = categoryService.getCategoryById("cat-1", testPrincipal);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("cat-1");
        assertThat(response.getName()).isEqualTo("Electronics");
    }

    @Test
    @DisplayName("Should throw exception when category not found")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Given
        when(categoryRepository.findById("non-existent")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById("non-existent", testPrincipal))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Category not found");
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when user has no access to category")
    void shouldThrowAccessDeniedWhenNoAccess() {
        // Given
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById("cat-1", testPrincipal))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("don't have permission");
    }

    @Test
    @DisplayName("Should get categories by shop")
    void shouldGetCategoriesByShop() {
        // Given
        List<Category> categories = List.of(testCategory, parentCategory);

        when(shopRepository.existsById("shop-1")).thenReturn(true);
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(categoryRepository.findByShopId("shop-1")).thenReturn(categories);

        // When
        List<CategoryResponse> responses = categoryService.getCategoriesByShop("shop-1", false, testPrincipal);

        // Then
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        verify(categoryRepository).findByShopId("shop-1");
    }

    @Test
    @DisplayName("Should throw exception when shop not found for listing")
    void shouldThrowExceptionWhenShopNotFoundForListing() {
        // Given
        when(shopRepository.existsById("non-existent-shop")).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoriesByShop("non-existent-shop", false, testPrincipal))
            .isInstanceOf(Exception.class)
            .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategory() {
        // Given
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
            .name("Updated Electronics")
            .description("Updated description")
            .isActive(false)
            .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(categoryRepository.findByNameAndShop_Id("Updated Electronics", "shop-1")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // When
        CategoryResponse response = categoryService.updateCategory("cat-1", request, testPrincipal);

        // Then
        assertThat(response).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to duplicate name")
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        // Given
        CategoryUpdateRequest request = CategoryUpdateRequest.builder()
            .name("Duplicate Name")
            .build();

        Category duplicateCategory = Category.builder()
            .id("other-cat-id")
            .name("Duplicate Name")
            .shop(testShop)
            .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(categoryRepository.findByNameAndShop_Id("Duplicate Name", "shop-1")).thenReturn(Optional.of(duplicateCategory));

        // When/Then
        assertThatThrownBy(() -> categoryService.updateCategory("cat-1", request, testPrincipal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategory() {
        // Given
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(productRepository.countByCategory_Id("cat-1")).thenReturn(0L);
        doNothing().when(categoryRepository).delete(any(Category.class));

        // When
        categoryService.deleteCategory("cat-1", testPrincipal);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    @DisplayName("Should throw exception when deleting category with products")
    void shouldThrowExceptionWhenDeletingCategoryWithProducts() {
        // Given
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(productRepository.countByCategory_Id("cat-1")).thenReturn(5L);

        // When/Then
        assertThatThrownBy(() -> categoryService.deleteCategory("cat-1", testPrincipal))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("products");
    }

    @Test
    @DisplayName("Should throw exception when deleting category with subcategories")
    void shouldThrowExceptionWhenDeletingCategoryWithSubcategories() {
        // Given
        Category childCategory = Category.builder()
            .id("child-1")
            .name("Child Category")
            .parent(testCategory)
            .shop(testShop)
            .build();

        testCategory.getChildren().add(childCategory);

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(shopAccessValidator.hasNoAccessToShop("shop-1", testPrincipal)).thenReturn(false);
        when(productRepository.countByCategory_Id("cat-1")).thenReturn(0L);

        // When/Then
        assertThatThrownBy(() -> categoryService.deleteCategory("cat-1", testPrincipal))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("child categories");
    }
}
