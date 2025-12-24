package com.princely.shopmanager.expenses.service;

import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.domain.ExpenseCategory;
import com.princely.shopmanager.expenses.dto.ExpenseUpdateRequest;
import com.princely.shopmanager.expenses.repository.ExpenseCategoryRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.BusinessRuleViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpenseFieldUpdater Tests")
class ExpenseFieldUpdaterTest {

    @Mock
    private ExpenseCategoryRepository categoryRepository;

    @InjectMocks
    private ExpenseFieldUpdater expenseFieldUpdater;

    private Expense expense;
    private ExpenseCategory category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();

        expense = Expense.builder()
            .id(UUID.randomUUID())
            .shopId("shop-1")
            .title("Office Supplies")
            .description("Pens and paper")
            .amount(BigDecimal.valueOf(100.00))
            .expenseDate(LocalDate.now())
            .build();

        category = ExpenseCategory.builder()
            .id(categoryId)
            .shopId("shop-1")
            .name("Office")
            .isActive(true)
            .build();
    }

    // updateBasicFields Tests
    @Test
    @DisplayName("updateBasicFields - Should update title when provided")
    void updateBasicFields_ShouldUpdateTitleWhenProvided() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            "New Office Supplies",
            null, null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getTitle()).isEqualTo("New Office Supplies");
    }

    @Test
    @DisplayName("updateBasicFields - Should trim whitespace from title")
    void updateBasicFields_ShouldTrimWhitespaceFromTitle() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            "  Trimmed Title  ",
            null, null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getTitle()).isEqualTo("Trimmed Title");
    }

    @Test
    @DisplayName("updateBasicFields - Should update description when provided")
    void updateBasicFields_ShouldUpdateDescriptionWhenProvided() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null,
            "Updated description",
            null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getDescription()).isEqualTo("Updated description");
    }

    @Test
    @DisplayName("updateBasicFields - Should trim whitespace from description")
    void updateBasicFields_ShouldTrimWhitespaceFromDescription() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null,
            "  Trimmed Description  ",
            null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getDescription()).isEqualTo("Trimmed Description");
    }

    @Test
    @DisplayName("updateBasicFields - Should update vendor name")
    void updateBasicFields_ShouldUpdateVendorName() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null, null,
            "Acme Corp",
            null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getVendorName()).isEqualTo("Acme Corp");
    }

    @Test
    @DisplayName("updateBasicFields - Should update reference number")
    void updateBasicFields_ShouldUpdateReferenceNumber() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null, null, null,
            "REF-12345",
            null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getReferenceNumber()).isEqualTo("REF-12345");
    }

    @Test
    @DisplayName("updateBasicFields - Should update notes")
    void updateBasicFields_ShouldUpdateNotes() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null, null, null, null, null,
            "Additional notes",
            null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getNotes()).isEqualTo("Additional notes");
    }

    @Test
    @DisplayName("updateBasicFields - Should not update fields when null")
    void updateBasicFields_ShouldNotUpdateFieldsWhenNull() {
        // Given
        String originalTitle = expense.getTitle();
        String originalDescription = expense.getDescription();
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);

        // Then
        assertThat(expense.getTitle()).isEqualTo(originalTitle);
        assertThat(expense.getDescription()).isEqualTo(originalDescription);
    }

    // updateAmountAndDate Tests
    @Test
    @DisplayName("updateAmountAndDate - Should update amount when provided")
    void updateAmountAndDate_ShouldUpdateAmountWhenProvided() {
        // Given
        BigDecimal newAmount = BigDecimal.valueOf(250.00);
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null,
            newAmount,
            null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateAmountAndDate(expense, request);

        // Then
        assertThat(expense.getAmount()).isEqualByComparingTo(newAmount);
    }

    @Test
    @DisplayName("updateAmountAndDate - Should update expense date when provided")
    void updateAmountAndDate_ShouldUpdateExpenseDateWhenProvided() {
        // Given
        LocalDate newDate = LocalDate.now().minusDays(5);
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null,
            newDate,
            null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateAmountAndDate(expense, request);

        // Then
        assertThat(expense.getExpenseDate()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("updateAmountAndDate - Should update payment method when provided")
    void updateAmountAndDate_ShouldUpdatePaymentMethodWhenProvided() {
        // Given
        String paymentMethod = "CREDIT_CARD";
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null,
            paymentMethod,
            null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateAmountAndDate(expense, request);

        // Then
        assertThat(expense.getPaymentMethod()).isEqualTo(paymentMethod);
    }

    @Test
    @DisplayName("updateAmountAndDate - Should not update when all fields are null")
    void updateAmountAndDate_ShouldNotUpdateWhenAllFieldsAreNull() {
        // Given
        BigDecimal originalAmount = expense.getAmount();
        LocalDate originalDate = expense.getExpenseDate();
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            null, null, null, null, null, null, null, null, null, null, null, null
        );

        // When
        expenseFieldUpdater.updateAmountAndDate(expense, request);

        // Then
        assertThat(expense.getAmount()).isEqualByComparingTo(originalAmount);
        assertThat(expense.getExpenseDate()).isEqualTo(originalDate);
    }

    // updateCategory Tests
    @Test
    @DisplayName("updateCategory - Should update category when valid")
    void updateCategory_ShouldUpdateCategoryWhenValid() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        expenseFieldUpdater.updateCategory(expense, categoryId);

        // Then
        assertThat(expense.getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("updateCategory - Should not update when categoryId is null")
    void updateCategory_ShouldNotUpdateWhenCategoryIdIsNull() {
        // Given
        UUID originalCategoryId = expense.getCategoryId();

        // When
        expenseFieldUpdater.updateCategory(expense, null);

        // Then
        assertThat(expense.getCategoryId()).isEqualTo(originalCategoryId);
    }

    @Test
    @DisplayName("updateCategory - Should throw BusinessException when category not found")
    void updateCategory_ShouldThrowBusinessExceptionWhenCategoryNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> expenseFieldUpdater.updateCategory(expense, nonExistentId))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("updateCategory - Should throw AccessDeniedException when category belongs to different shop")
    void updateCategory_ShouldThrowAccessDeniedExceptionWhenCategoryBelongsToDifferentShop() {
        // Given
        ExpenseCategory differentShopCategory = ExpenseCategory.builder()
            .id(categoryId)
            .shopId("shop-2")
            .name("Different Shop Category")
            .isActive(true)
            .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(differentShopCategory));

        // When/Then
        assertThatThrownBy(() -> expenseFieldUpdater.updateCategory(expense, categoryId))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("updateCategory - Should throw BusinessRuleViolationException when category is inactive")
    void updateCategory_ShouldThrowBusinessRuleViolationExceptionWhenCategoryIsInactive() {
        // Given
        ExpenseCategory inactiveCategory = ExpenseCategory.builder()
            .id(categoryId)
            .shopId("shop-1")
            .name("Inactive Category")
            .isActive(false)
            .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(inactiveCategory));

        // When/Then
        assertThatThrownBy(() -> expenseFieldUpdater.updateCategory(expense, categoryId))
            .isInstanceOf(BusinessRuleViolationException.class)
            .hasMessageContaining("inactive");
    }

    // updateTags Tests
    @Test
    @DisplayName("updateTags - Should update tags when provided")
    void updateTags_ShouldUpdateTagsWhenProvided() {
        // Given
        Set<String> newTags = Set.of("urgent", "office", "supplies");

        // When
        expenseFieldUpdater.updateTags(expense, newTags);

        // Then
        assertThat(expense.getTags()).containsExactlyInAnyOrder("urgent", "office", "supplies");
    }

    @Test
    @DisplayName("updateTags - Should not update when tags are null")
    void updateTags_ShouldNotUpdateWhenTagsAreNull() {
        // Given
        Set<String> originalTags = Set.of("original");
        expense.setTags(originalTags);

        // When
        expenseFieldUpdater.updateTags(expense, null);

        // Then
        assertThat(expense.getTags()).isEqualTo(originalTags);
    }

    // cleanTags Tests
    @Test
    @DisplayName("cleanTags - Should trim whitespace from tags")
    void cleanTags_ShouldTrimWhitespaceFromTags() {
        // Given
        Set<String> tags = Set.of("  tag1  ", " tag2 ", "tag3");

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).containsExactlyInAnyOrder("tag1", "tag2", "tag3");
    }

    @Test
    @DisplayName("cleanTags - Should convert tags to lowercase")
    void cleanTags_ShouldConvertTagsToLowercase() {
        // Given
        Set<String> tags = Set.of("URGENT", "Office", "SuPpLiEs");

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).containsExactlyInAnyOrder("urgent", "office", "supplies");
    }

    @Test
    @DisplayName("cleanTags - Should remove empty tags")
    void cleanTags_ShouldRemoveEmptyTags() {
        // Given
        Set<String> tags = Set.of("tag1", "", "  ", "tag2");

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    @DisplayName("cleanTags - Should remove null tags")
    void cleanTags_ShouldRemoveNullTags() {
        // Given
        Set<String> tags = Set.of("tag1", "tag2");
        tags.add(null);

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).containsExactlyInAnyOrder("tag1", "tag2");
    }

    @Test
    @DisplayName("cleanTags - Should handle empty set")
    void cleanTags_ShouldHandleEmptySet() {
        // Given
        Set<String> tags = Set.of();

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).isEmpty();
    }

    @Test
    @DisplayName("cleanTags - Should apply all cleaning rules together")
    void cleanTags_ShouldApplyAllCleaningRulesTogether() {
        // Given
        Set<String> tags = Set.of("  URGENT  ", "Office", "", "  ", "SuPpLiEs");

        // When
        Set<String> cleaned = expenseFieldUpdater.cleanTags(tags);

        // Then
        assertThat(cleaned).containsExactlyInAnyOrder("urgent", "office", "supplies");
    }

    // Integration Test
    @Test
    @DisplayName("Full update - Should update all fields when all provided")
    void fullUpdate_ShouldUpdateAllFieldsWhenAllProvided() {
        // Given
        ExpenseUpdateRequest request = new ExpenseUpdateRequest(
            "Updated Title",
            "Updated Description",
            null,
            BigDecimal.valueOf(500.00),
            LocalDate.now().minusDays(3),
            "BANK_TRANSFER",
            "Updated Vendor",
            "REF-999",
            Set.of("tag1", "tag2"),
            "Updated Notes",
            null,
            null
        );

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // When
        expenseFieldUpdater.updateBasicFields(expense, request);
        expenseFieldUpdater.updateAmountAndDate(expense, request);
        expenseFieldUpdater.updateCategory(expense, categoryId);
        expenseFieldUpdater.updateTags(expense, request.tags());

        // Then
        assertThat(expense.getTitle()).isEqualTo("Updated Title");
        assertThat(expense.getDescription()).isEqualTo("Updated Description");
        assertThat(expense.getVendorName()).isEqualTo("Updated Vendor");
        assertThat(expense.getReferenceNumber()).isEqualTo("REF-999");
        assertThat(expense.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(expense.getExpenseDate()).isEqualTo(LocalDate.now().minusDays(3));
        assertThat(expense.getPaymentMethod()).isEqualTo("BANK_TRANSFER");
        assertThat(expense.getNotes()).isEqualTo("Updated Notes");
        assertThat(expense.getCategoryId()).isEqualTo(categoryId);
        assertThat(expense.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
    }
}
