package com.princely.shopmanager.core.service;

import com.princely.shopmanager.core.domain.Category;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryHierarchyValidator Tests")
class CategoryHierarchyValidatorTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryHierarchyValidator categoryHierarchyValidator;

    private Shop testShop;
    private Category rootCategory;
    private Category childCategory;
    private Category grandchildCategory;

    @BeforeEach
    void setUp() {
        testShop = Shop.builder()
            .id("shop-1")
            .name("Test Shop")
            .build();

        rootCategory = Category.builder()
            .id("cat-1")
            .name("Electronics")
            .shop(testShop)
            .parent(null)
            .build();

        childCategory = Category.builder()
            .id("cat-2")
            .name("Computers")
            .shop(testShop)
            .parent(rootCategory)
            .build();

        grandchildCategory = Category.builder()
            .id("cat-3")
            .name("Laptops")
            .shop(testShop)
            .parent(childCategory)
            .build();
    }

    // validateAndSetParent Tests
    @Test
    @DisplayName("validateAndSetParent - Should do nothing when parentId is null")
    void validateAndSetParent_ShouldDoNothingWhenParentIdIsNull() {
        // Given
        Category category = Category.builder().id("cat-new").shop(testShop).build();

        // When
        categoryHierarchyValidator.validateAndSetParent(category, null, null);

        // Then
        assertThat(category.getParent()).isNull();
        verify(categoryRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("validateAndSetParent - Should remove parent when parentId is empty string")
    void validateAndSetParent_ShouldRemoveParentWhenParentIdIsEmptyString() {
        // Given
        Category category = Category.builder()
            .id("cat-new")
            .shop(testShop)
            .parent(rootCategory)
            .build();

        // When
        categoryHierarchyValidator.validateAndSetParent(category, "", null);

        // Then
        assertThat(category.getParent()).isNull();
        verify(categoryRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("validateAndSetParent - Should set valid parent")
    void validateAndSetParent_ShouldSetValidParent() {
        // Given
        Category category = Category.builder()
            .id("cat-new")
            .shop(testShop)
            .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(rootCategory));

        // When
        categoryHierarchyValidator.validateAndSetParent(category, "cat-1", null);

        // Then
        assertThat(category.getParent()).isEqualTo(rootCategory);
        verify(categoryRepository).findById("cat-1");
    }

    @Test
    @DisplayName("validateAndSetParent - Should throw exception when parent not found")
    void validateAndSetParent_ShouldThrowExceptionWhenParentNotFound() {
        // Given
        Category category = Category.builder()
            .id("cat-new")
            .shop(testShop)
            .build();

        when(categoryRepository.findById("cat-999")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateAndSetParent(category, "cat-999", null)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Parent category not found");
    }

    @Test
    @DisplayName("validateAndSetParent - Should throw exception when parent belongs to different shop")
    void validateAndSetParent_ShouldThrowExceptionWhenParentBelongsToDifferentShop() {
        // Given
        Shop differentShop = Shop.builder()
            .id("shop-2")
            .name("Different Shop")
            .build();

        Category differentShopCategory = Category.builder()
            .id("cat-other")
            .shop(differentShop)
            .build();

        Category category = Category.builder()
            .id("cat-new")
            .shop(testShop)
            .build();

        when(categoryRepository.findById("cat-other")).thenReturn(Optional.of(differentShopCategory));

        // When/Then
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateAndSetParent(category, "cat-other", null)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must belong to the same shop");
    }

    @Test
    @DisplayName("validateAndSetParent - Should throw exception when would create circular reference")
    void validateAndSetParent_ShouldThrowExceptionWhenWouldCreateCircularReference() {
        // Given
        Category category = Category.builder()
            .id("cat-1") // This is the root
            .shop(testShop)
            .build();

        // Try to set child as parent (circular reference)
        lenient().when(categoryRepository.findById("cat-2")).thenReturn(Optional.of(childCategory));
        lenient().when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(rootCategory));

        // When/Then
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateAndSetParent(category, "cat-2", "cat-1")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("circular reference");
    }

    // wouldCreateCircularReference Tests
    @Test
    @DisplayName("wouldCreateCircularReference - Should return false for new category (null categoryId)")
    void wouldCreateCircularReference_ShouldReturnFalseForNewCategory() {
        // When
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-1", null);

        // Then
        assertThat(result).isFalse();
        verify(categoryRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should return true when category is its own parent")
    void wouldCreateCircularReference_ShouldReturnTrueWhenCategoryIsItsOwnParent() {
        // When
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-1", "cat-1");

        // Then
        assertThat(result).isTrue();
        verify(categoryRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should return false when no circular reference exists")
    void wouldCreateCircularReference_ShouldReturnFalseWhenNoCircularReferenceExists() {
        // Given - Setting grandchild's parent to a new category (no circular reference)
        when(categoryRepository.findById("cat-new")).thenReturn(Optional.of(
            Category.builder().id("cat-new").shop(testShop).parent(null).build()
        ));

        // When
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-new", "cat-3");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should return true when direct circular reference detected")
    void wouldCreateCircularReference_ShouldReturnTrueWhenDirectCircularReferenceDetected() {
        // Given - Try to set parent to its own child
        when(categoryRepository.findById("cat-2")).thenReturn(Optional.of(childCategory));

        // When - Setting root's parent to child (root -> child -> root would be circular)
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-2", "cat-1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should return true when nested circular reference detected")
    void wouldCreateCircularReference_ShouldReturnTrueWhenNestedCircularReferenceDetected() {
        // Given - Try to set parent to a descendant (creates circular reference)
        when(categoryRepository.findById("cat-3")).thenReturn(Optional.of(grandchildCategory));

        // When - Setting root's parent to grandchild (root -> child -> grandchild -> root would be circular)
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-3", "cat-1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should traverse full parent chain")
    void wouldCreateCircularReference_ShouldTraverseFullParentChain() {
        // Given - Deep hierarchy: cat-1 -> cat-2 -> cat-3 -> cat-4
        Category cat4 = Category.builder()
            .id("cat-4")
            .shop(testShop)
            .parent(grandchildCategory)
            .build();

        when(categoryRepository.findById("cat-4")).thenReturn(Optional.of(cat4));

        // When - Try to set cat-1's parent to cat-4 (would create circular reference)
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-4", "cat-1");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should return false when parent not found")
    void wouldCreateCircularReference_ShouldReturnFalseWhenParentNotFound() {
        // Given
        when(categoryRepository.findById("cat-999")).thenReturn(Optional.empty());

        // When
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-999", "cat-1");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("wouldCreateCircularReference - Should handle null parent in chain")
    void wouldCreateCircularReference_ShouldHandleNullParentInChain() {
        // Given - Category with no parent (root)
        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(rootCategory));

        // When - Setting another category's parent to root (no circular reference)
        boolean result = categoryHierarchyValidator.wouldCreateCircularReference("cat-1", "cat-new");

        // Then
        assertThat(result).isFalse();
    }

    // validateNameUniqueness Tests
    @Test
    @DisplayName("validateNameUniqueness - Should not throw exception when name is unique")
    void validateNameUniqueness_ShouldNotThrowExceptionWhenNameIsUnique() {
        // Given
        when(categoryRepository.findByNameAndShop_Id("NewCategory", "shop-1"))
            .thenReturn(Optional.empty());

        // When/Then - Should not throw exception
        categoryHierarchyValidator.validateNameUniqueness("NewCategory", "shop-1", null);

        verify(categoryRepository).findByNameAndShop_Id("NewCategory", "shop-1");
    }

    @Test
    @DisplayName("validateNameUniqueness - Should not throw exception when same category is being updated")
    void validateNameUniqueness_ShouldNotThrowExceptionWhenSameCategoryIsBeingUpdated() {
        // Given
        when(categoryRepository.findByNameAndShop_Id("Electronics", "shop-1"))
            .thenReturn(Optional.of(rootCategory));

        // When/Then - Should not throw exception when updating the same category
        categoryHierarchyValidator.validateNameUniqueness("Electronics", "shop-1", "cat-1");

        verify(categoryRepository).findByNameAndShop_Id("Electronics", "shop-1");
    }

    @Test
    @DisplayName("validateNameUniqueness - Should throw exception when name already exists for different category")
    void validateNameUniqueness_ShouldThrowExceptionWhenNameAlreadyExistsForDifferentCategory() {
        // Given
        when(categoryRepository.findByNameAndShop_Id("Electronics", "shop-1"))
            .thenReturn(Optional.of(rootCategory));

        // When/Then - Should throw exception when creating a new category with existing name
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateNameUniqueness("Electronics", "shop-1", "cat-new")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("validateNameUniqueness - Should throw exception when updating to existing name")
    void validateNameUniqueness_ShouldThrowExceptionWhenUpdatingToExistingName() {
        // Given
        when(categoryRepository.findByNameAndShop_Id("Electronics", "shop-1"))
            .thenReturn(Optional.of(rootCategory));

        // When/Then - Should throw exception when updating cat-2 to have same name as cat-1
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateNameUniqueness("Electronics", "shop-1", "cat-2")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("already exists");
    }

    // Integration Tests
    @Test
    @DisplayName("Complex hierarchy validation - Multi-level circular reference prevention")
    void complexHierarchyValidation_MultiLevelCircularReferencePrevention() {
        // Given - Hierarchy: A -> B -> C -> D
        Category catA = Category.builder().id("A").shop(testShop).parent(null).build();
        Category catB = Category.builder().id("B").shop(testShop).parent(catA).build();
        Category catC = Category.builder().id("C").shop(testShop).parent(catB).build();
        Category catD = Category.builder().id("D").shop(testShop).parent(catC).build();

        when(categoryRepository.findById("D")).thenReturn(Optional.of(catD));

        // When/Then - Trying to set A's parent to D would create circular reference
        assertThatThrownBy(() ->
            categoryHierarchyValidator.validateAndSetParent(catA, "D", "A")
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("circular reference");
    }

    @Test
    @DisplayName("Complex hierarchy validation - Reparenting to sibling allowed")
    void complexHierarchyValidation_ReparentingToSiblingAllowed() {
        // Given - Two siblings under same parent
        Category sibling1 = Category.builder()
            .id("sib-1")
            .shop(testShop)
            .parent(rootCategory)
            .build();

        Category sibling2 = Category.builder()
            .id("sib-2")
            .shop(testShop)
            .parent(rootCategory)
            .build();

        when(categoryRepository.findById("sib-2")).thenReturn(Optional.of(sibling2));

        // When - Setting sibling1's parent to sibling2 (no circular reference)
        categoryHierarchyValidator.validateAndSetParent(sibling1, "sib-2", "sib-1");

        // Then
        assertThat(sibling1.getParent()).isEqualTo(sibling2);
    }
}
