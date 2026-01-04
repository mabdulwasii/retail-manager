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
    private String testUserId;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID().toString();
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
        String approverId = UUID.randomUUID().toString();
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
        String approverId = UUID.randomUUID().toString();

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
        String approverId = UUID.randomUUID().toString();

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
        String rejecterId = UUID.randomUUID().toString();
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
        String rejecterId = UUID.randomUUID().toString();

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
        String rejecterId = UUID.randomUUID().toString();

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

    // ==================== Complete Workflow Tests ====================

    @Test
    @DisplayName("Complete workflow - DRAFT → PENDING → APPROVED → PAID")
    void completeWorkflow_draftToPendingToApprovedToPaid() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);
        String approverId = UUID.randomUUID().toString();

        // When & Then: DRAFT → PENDING_APPROVAL
        expense.submitForApproval();
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.PENDING_APPROVAL);
        assertThat(expense.isPending()).isTrue();

        // When & Then: PENDING_APPROVAL → APPROVED
        expense.approve(approverId, "Manager", "Approved");
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.APPROVED);
        assertThat(expense.isApproved()).isTrue();
        assertThat(expense.canBeEdited()).isFalse();

        // When & Then: APPROVED → PAID
        expense.markAsPaid();
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.PAID);
        assertThat(expense.canBeEdited()).isFalse();
    }

    @Test
    @DisplayName("Complete workflow - DRAFT → PENDING → REJECTED → editable")
    void completeWorkflow_draftToPendingToRejectedAndEditable() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);
        String rejecterId = UUID.randomUUID().toString();

        // When & Then: DRAFT → PENDING_APPROVAL
        expense.submitForApproval();
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.PENDING_APPROVAL);

        // When & Then: PENDING_APPROVAL → REJECTED
        expense.reject(rejecterId, "CFO", "Rejected");
        assertThat(expense.getStatus()).isEqualTo(ExpenseStatus.REJECTED);
        assertThat(expense.isRejected()).isTrue();
        assertThat(expense.canBeEdited()).isTrue(); // Can be edited after rejection
    }

    // ==================== Additional Edge Cases ====================

    @Test
    @DisplayName("approve - Should throw exception when status is REJECTED")
    void approve_shouldThrowExceptionWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);
        String approverId = UUID.randomUUID().toString();

        // When / Then
        assertThatThrownBy(() -> expense.approve(approverId, "John", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be approved in current status");
    }

    @Test
    @DisplayName("approve - Should throw exception when status is PAID")
    void approve_shouldThrowExceptionWhenPaid() {
        // Given
        expense.setStatus(ExpenseStatus.PAID);
        String approverId = UUID.randomUUID().toString();

        // When / Then
        assertThatThrownBy(() -> expense.approve(approverId, "John", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be approved in current status: PAID");
    }

    @Test
    @DisplayName("reject - Should throw exception when status is APPROVED")
    void reject_shouldThrowExceptionWhenApproved() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);
        String rejecterId = UUID.randomUUID().toString();

        // When / Then
        assertThatThrownBy(() -> expense.reject(rejecterId, "Jane", "Notes"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expense cannot be rejected in current status");
    }

    @Test
    @DisplayName("submitForApproval - Should throw exception when status is REJECTED")
    void submitForApproval_shouldThrowExceptionWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);

        // When / Then
        assertThatThrownBy(() -> expense.submitForApproval())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only draft expenses can be submitted for approval");
    }

    @Test
    @DisplayName("markAsPaid - Should throw exception when status is DRAFT")
    void markAsPaid_shouldThrowExceptionWhenDraft() {
        // Given
        expense.setStatus(ExpenseStatus.DRAFT);

        // When / Then
        assertThatThrownBy(() -> expense.markAsPaid())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only approved expenses can be marked as paid");
    }

    @Test
    @DisplayName("markAsPaid - Should throw exception when status is REJECTED")
    void markAsPaid_shouldThrowExceptionWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);

        // When / Then
        assertThatThrownBy(() -> expense.markAsPaid())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Only approved expenses can be marked as paid");
    }

    @Test
    @DisplayName("Builder - Should create expense with all optional fields")
    void builder_shouldCreateExpenseWithAllOptionalFields() {
        // Given
        String shopId = "shop-test";
        String title = "Test Expense";
        String description = "Test Description";
        UUID categoryId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(750.50);
        LocalDate expenseDate = LocalDate.now().minusDays(5);
        String paymentMethod = "CREDIT_CARD";
        String vendorName = "Test Vendor";
        String referenceNumber = "REF-TEST-001";
        String receiptUrl = "http://example.com/receipt.pdf";
        String notes = "Test notes";
        String creatorId = UUID.randomUUID().toString();
        String creatorName = "Test Creator";

        // When
        Expense newExpense = Expense.builder()
            .shopId(shopId)
            .title(title)
            .description(description)
            .categoryId(categoryId)
            .amount(amount)
            .expenseDate(expenseDate)
            .paymentMethod(paymentMethod)
            .vendorName(vendorName)
            .referenceNumber(referenceNumber)
            .receiptUrl(receiptUrl)
            .notes(notes)
            .expenseCreatedBy(creatorId)
            .createdByName(creatorName)
            .build();

        // Then
        assertThat(newExpense.getShopId()).isEqualTo(shopId);
        assertThat(newExpense.getTitle()).isEqualTo(title);
        assertThat(newExpense.getDescription()).isEqualTo(description);
        assertThat(newExpense.getCategoryId()).isEqualTo(categoryId);
        assertThat(newExpense.getAmount()).isEqualByComparingTo(amount);
        assertThat(newExpense.getExpenseDate()).isEqualTo(expenseDate);
        assertThat(newExpense.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(newExpense.getVendorName()).isEqualTo(vendorName);
        assertThat(newExpense.getReferenceNumber()).isEqualTo(referenceNumber);
        assertThat(newExpense.getReceiptUrl()).isEqualTo(receiptUrl);
        assertThat(newExpense.getNotes()).isEqualTo(notes);
        assertThat(newExpense.getExpenseCreatedBy()).isEqualTo(creatorId);
        assertThat(newExpense.getCreatedByName()).isEqualTo(creatorName);
        assertThat(newExpense.getStatus()).isEqualTo(ExpenseStatus.DRAFT);
        assertThat(newExpense.getTags()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("cannotBeApproved - Should return true when status is APPROVED")
    void cannotBeApproved_shouldReturnTrueWhenApproved() {
        // Given
        expense.setStatus(ExpenseStatus.APPROVED);

        // When
        boolean result = expense.cannotBeApproved();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("cannotBeApproved - Should return true when status is REJECTED")
    void cannotBeApproved_shouldReturnTrueWhenRejected() {
        // Given
        expense.setStatus(ExpenseStatus.REJECTED);

        // When
        boolean result = expense.cannotBeApproved();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("cannotBeApproved - Should return true when status is PAID")
    void cannotBeApproved_shouldReturnTrueWhenPaid() {
        // Given
        expense.setStatus(ExpenseStatus.PAID);

        // When
        boolean result = expense.cannotBeApproved();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Tag management - Should handle multiple tag operations")
    void tagManagement_shouldHandleMultipleTagOperations() {
        // When
        expense.addTag("urgent");
        expense.addTag("OFFICE");
        expense.addTag("  supplies  ");

        // Then
        assertThat(expense.getTags()).containsExactlyInAnyOrder("urgent", "office", "supplies");

        // When: Remove one tag
        expense.removeTag("office");

        // Then
        assertThat(expense.getTags()).containsExactlyInAnyOrder("urgent", "supplies");
        assertThat(expense.getTags()).hasSize(2);
    }

    @Test
    @DisplayName("approve - Should set approval date to current date")
    void approve_shouldSetApprovalDateToCurrentDate() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        String approverId = UUID.randomUUID().toString();
        LocalDate today = LocalDate.now();

        // When
        expense.approve(approverId, "Approver", "Notes");

        // Then
        assertThat(expense.getApprovalDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("reject - Should set rejection date to current date")
    void reject_shouldSetRejectionDateToCurrentDate() {
        // Given
        expense.setStatus(ExpenseStatus.PENDING_APPROVAL);
        String rejecterId = UUID.randomUUID().toString();
        LocalDate today = LocalDate.now();

        // When
        expense.reject(rejecterId, "Rejecter", "Notes");

        // Then
        assertThat(expense.getApprovalDate()).isEqualTo(today);
    }
}
