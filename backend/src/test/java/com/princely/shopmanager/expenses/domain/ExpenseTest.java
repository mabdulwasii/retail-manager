package com.princely.shopmanager.expenses.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Expense Domain Tests")
class ExpenseTest {

    private Expense expense;
    private UUID testUserId;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testCategoryId = UUID.randomUUID();

        expense = Expense.builder()
            .shopId("shop-1")
            .title("Office Supplies")
            .categoryId(testCategoryId)
            .amount(BigDecimal.valueOf(500))
            .expenseDate(LocalDate.now())
            .expenseCreatedBy(testUserId)
            .build();
    }

    @Test
    @DisplayName("Should have default status as DRAFT")
    void shouldHaveDefaultStatusAsDraft() {
        // Given
        Expense newExpense = Expense.builder()
            .shopId("shop-1")
            .title("Test Expense")
            .categoryId(testCategoryId)
            .amount(BigDecimal.valueOf(100))
            .expenseDate(LocalDate.now())
            .expenseCreatedBy(testUserId)
            .build();

        // Then
        assertThat(newExpense.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
    }

    @Test
    @DisplayName("Should initialize tags as empty set")
    void shouldInitializeTagsAsEmptySet() {
        // Then
        assertThat(expense.getTags()).isNotNull();
        assertThat(expense.getTags()).isEmpty();
    }

    // Status checking methods
    @Test
    @DisplayName("isPending - Should return true when status is PENDING_APPROVAL")
    void isPending_shouldReturnTrueWhenPendingApproval() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);

        // When
        boolean result = expense.isPending();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isPending - Should return false when status is not PENDING_APPROVAL")
    void isPending_shouldReturnFalseWhenNotPendingApproval() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        boolean result = expense.isPending();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isApproved - Should return true when status is APPROVED")
    void isApproved_shouldReturnTrueWhenApproved() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);

        // When
        boolean result = expense.isApproved();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isApproved - Should return false when status is not APPROVED")
    void isApproved_shouldReturnFalseWhenNotApproved() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        boolean result = expense.isApproved();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isRejected - Should return true when status is REJECTED")
    void isRejected_shouldReturnTrueWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);

        // When
        boolean result = expense.isRejected();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isRejected - Should return false when status is not REJECTED")
    void isRejected_shouldReturnFalseWhenNotRejected() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        boolean result = expense.isRejected();

        // Then
        assertThat(result).isFalse();
    }

    // Edit permission methods
    @Test
    @DisplayName("canBeEdited - Should return true when status is DRAFT")
    void canBeEdited_shouldReturnTrueWhenDraft() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        boolean result = expense.canBeEdited();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canBeEdited - Should return true when status is REJECTED")
    void canBeEdited_shouldReturnTrueWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);

        // When
        boolean result = expense.canBeEdited();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canBeEdited - Should return false when status is PENDING_APPROVAL")
    void canBeEdited_shouldReturnFalseWhenPending() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);

        // When
        boolean result = expense.canBeEdited();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canBeEdited - Should return false when status is APPROVED")
    void canBeEdited_shouldReturnFalseWhenApproved() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);

        // When
        boolean result = expense.canBeEdited();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canBeEdited - Should return false when status is PAID")
    void canBeEdited_shouldReturnFalseWhenPaid() {
        // Given
        expense.setStatus(ExpenseStatus.PAID);

        // When
        boolean result = expense.canBeEdited();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("cannotBeApproved - Should return false when status is PENDING_APPROVAL")
    void cannotBeApproved_shouldReturnFalseWhenPending() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);

        // When
        boolean result = expense.cannotBeApproved();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("cannotBeApproved - Should return true when status is DRAFT")
    void cannotBeApproved_shouldReturnTrueWhenDraft() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        boolean result = expense.cannotBeApproved();

        // Then
        assertThat(result).isTrue();
    }

    // Workflow methods - submitForApproval
    @Test
    @DisplayName("submitForApproval - Should change status from DRAFT to PENDING_APPROVAL")
    void submitForApproval_shouldChangeStatusToPending() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When
        expense.submitForApproval();

        // Then
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("submitForApproval - Should throw exception when status is not DRAFT")
    void submitForApproval_shouldThrowExceptionWhenNotDraft() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);

        // When / Then
        assertThatThrownBy(() -> expense.submitForApproval())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only draft expenses can be submitted for approval");
    }

    // Workflow methods - approve
    @Test
    @DisplayName("approve - Should change status to APPROVED and set approval details")
    void approve_shouldChangeStatusAndSetApprovalDetails() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        UUID approverId = UUID.randomUUID();
        String approverName = "John Approver";
        String notes = "Approved for purchase";
        LocalDate beforeApproval = LocalDate.now().minusDays(1);

        // When
        expense.approve(approverId, approverName, notes);

        // Then
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(expense.getApprovedBy()).isEqualTo(approverId);
        assertThat(expense.getApprovedByName()).isEqualTo(approverName);
        assertThat(expense.getApprovalNotes()).isEqualTo(notes);
        assertThat(expense.getApprovalDate()).isNotNull();
        assertThat(expense.getApprovalDate()).isAfterOrEqualTo(beforeApproval);
    }

    @Test
    @DisplayName("approve - Should throw exception when status is not PENDING_APPROVAL")
    void approve_shouldThrowExceptionWhenNotPending() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);
        UUID approverId = UUID.randomUUID();

        // When / Then
        assertThatThrownBy(() -> expense.approve(approverId, "John", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be approved in current status");
    }

    @Test
    @DisplayName("approve - Should throw exception when status is APPROVED")
    void approve_shouldThrowExceptionWhenAlreadyApproved() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);
        UUID approverId = UUID.randomUUID();

        // When / Then
        assertThatThrownBy(() -> expense.approve(approverId, "John", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be approved in current status: APPROVED");
    }

    // Workflow methods - reject
    @Test
    @DisplayName("reject - Should change status to REJECTED and set rejection details")
    void reject_shouldChangeStatusAndSetRejectionDetails() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        UUID rejecterId = UUID.randomUUID();
        String rejecterName = "Jane Rejecter";
        String notes = "Missing receipt";
        LocalDate beforeRejection = LocalDate.now().minusDays(1);

        // When
        expense.reject(rejecterId, rejecterName, notes);

        // Then
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        assertThat(expense.getApprovedBy()).isEqualTo(rejecterId);
        assertThat(expense.getApprovedByName()).isEqualTo(rejecterName);
        assertThat(expense.getApprovalNotes()).isEqualTo(notes);
        assertThat(expense.getApprovalDate()).isNotNull();
        assertThat(expense.getApprovalDate()).isAfterOrEqualTo(beforeRejection);
    }

    @Test
    @DisplayName("reject - Should throw exception when status is not PENDING_APPROVAL")
    void reject_shouldThrowExceptionWhenNotPending() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);
        UUID rejecterId = UUID.randomUUID();

        // When / Then
        assertThatThrownBy(() -> expense.reject(rejecterId, "Jane", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be rejected in current status");
    }

    @Test
    @DisplayName("reject - Should throw exception when status is PAID")
    void reject_shouldThrowExceptionWhenPaid() {
        // Given
        expense.setStatus(ExpenseStatus.PAID);
        UUID rejecterId = UUID.randomUUID();

        // When / Then
        assertThatThrownBy(() -> expense.reject(rejecterId, "Jane", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be rejected in current status: PAID");
    }

    // Workflow methods - markAsPaid
    @Test
    @DisplayName("markAsPaid - Should change status to PAID when APPROVED")
    void markAsPaid_shouldChangeStatusToPaid() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);

        // When
        expense.markAsPaid();

        // Then
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.PAID);
    }

    @Test
    @DisplayName("markAsPaid - Should throw exception when status is not APPROVED")
    void markAsPaid_shouldThrowExceptionWhenNotApproved() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);

        // When / Then
        assertThatThrownBy(() -> expense.markAsPaid())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only approved expenses can be marked as paid");
    }

    // Tag management
    @Test
    @DisplayName("addTag - Should add tag in lowercase and trimmed")
    void addTag_shouldAddTagInLowercaseAndTrimmed() {
        // When
        expense.addTag("  URGENT  ");

        // Then
        assertThat(expense.getTags()).contains("urgent");
        assertThat(expense.getTags()).hasSize(1);
    }

    @Test
    @DisplayName("addTag - Should not add null tag")
    void addTag_shouldNotAddNullTag() {
        // When
        expense.addTag(null);

        // Then
        assertThat(expense.getTags()).isEmpty();
    }

    @Test
    @DisplayName("addTag - Should not add empty tag")
    void addTag_shouldNotAddEmptyTag() {
        // When
        expense.addTag("   ");

        // Then
        assertThat(expense.getTags()).isEmpty();
    }

    @Test
    @DisplayName("addTag - Should add multiple tags")
    void addTag_shouldAddMultipleTags() {
        // When
        expense.addTag("urgent");
        expense.addTag("office");
        expense.addTag("supplies");

        // Then
        assertThat(expense.getTags()).containsExactlyInAnyOrder("urgent", "office", "supplies");
        assertThat(expense.getTags()).hasSize(3);
    }

    @Test
    @DisplayName("addTag - Should not add duplicate tags")
    void addTag_shouldNotAddDuplicateTags() {
        // When
        expense.addTag("urgent");
        expense.addTag("URGENT");
        expense.addTag("  urgent  ");

        // Then
        assertThat(expense.getTags()).containsExactly("urgent");
        assertThat(expense.getTags()).hasSize(1);
    }

    @Test
    @DisplayName("removeTag - Should remove tag in lowercase")
    void removeTag_shouldRemoveTagInLowercase() {
        // Given
        expense.addTag("urgent");
        expense.addTag("office");

        // When
        expense.removeTag("URGENT");

        // Then
        assertThat(expense.getTags()).containsExactly("office");
        assertThat(expense.getTags()).hasSize(1);
    }

    @Test
    @DisplayName("removeTag - Should handle null tag gracefully")
    void removeTag_shouldHandleNullTagGracefully() {
        // Given
        expense.addTag("urgent");

        // When
        expense.removeTag(null);

        // Then
        assertThat(expense.getTags()).containsExactly("urgent");
    }

    @Test
    @DisplayName("removeTag - Should handle non-existent tag gracefully")
    void removeTag_shouldHandleNonExistentTagGracefully() {
        // Given
        expense.addTag("urgent");

        // When
        expense.removeTag("nonexistent");

        // Then
        assertThat(expense.getTags()).containsExactly("urgent");
    }

    // ShopAware implementation
    @Test
    @DisplayName("getShopId - Should return shop ID")
    void getShopId_shouldReturnShopId() {
        // Given
        expense.setShopId("shop-123");

        // When
        String shopId = expense.getShopId();

        // Then
        assertThat(shopId).isEqualTo("shop-123");
    }

    @Test
    @DisplayName("getShopId - Should return null when shop ID is null")
    void getShopId_shouldReturnNullWhenShopIdIsNull() {
        // Given
        expense.setShopId(null);

        // When
        String shopId = expense.getShopId();

        // Then
        assertThat(shopId).isNull();
    }
}
