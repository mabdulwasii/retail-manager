package com.princely.shopmanager.expenses.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ExpenseStatus Enum Tests")
class ExpenseStatusTest {

    // canTransitionTo tests - DRAFT
    @Test
    @DisplayName("DRAFT - Can transition to PENDING_APPROVAL")
    void draft_canTransitionToPendingApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PENDING_APPROVAL);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("DRAFT - Can transition to APPROVED")
    void draft_canTransitionToApproved() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.APPROVED);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("DRAFT - Cannot transition to REJECTED")
    void draft_cannotTransitionToRejected() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.REJECTED);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("DRAFT - Cannot transition to PAID")
    void draft_cannotTransitionToPaid() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PAID);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("DRAFT - Cannot transition to DRAFT")
    void draft_cannotTransitionToDraft() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.DRAFT);

        // Then
        assertThat(canTransition).isFalse();
    }

    // canTransitionTo tests - PENDING_APPROVAL
    @Test
    @DisplayName("PENDING_APPROVAL - Can transition to APPROVED")
    void pendingApproval_canTransitionToApproved() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.APPROVED);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("PENDING_APPROVAL - Can transition to REJECTED")
    void pendingApproval_canTransitionToRejected() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.REJECTED);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("PENDING_APPROVAL - Cannot transition to DRAFT")
    void pendingApproval_cannotTransitionToDraft() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.DRAFT);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("PENDING_APPROVAL - Cannot transition to PAID")
    void pendingApproval_cannotTransitionToPaid() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PAID);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("PENDING_APPROVAL - Cannot transition to PENDING_APPROVAL")
    void pendingApproval_cannotTransitionToPendingApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PENDING_APPROVAL);

        // Then
        assertThat(canTransition).isFalse();
    }

    // canTransitionTo tests - APPROVED
    @Test
    @DisplayName("APPROVED - Can transition to PAID")
    void approved_canTransitionToPaid() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PAID);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("APPROVED - Cannot transition to DRAFT")
    void approved_cannotTransitionToDraft() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.DRAFT);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("APPROVED - Cannot transition to PENDING_APPROVAL")
    void approved_cannotTransitionToPendingApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PENDING_APPROVAL);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("APPROVED - Cannot transition to REJECTED")
    void approved_cannotTransitionToRejected() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.REJECTED);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("APPROVED - Cannot transition to APPROVED")
    void approved_cannotTransitionToApproved() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.APPROVED);

        // Then
        assertThat(canTransition).isFalse();
    }

    // canTransitionTo tests - REJECTED
    @Test
    @DisplayName("REJECTED - Can transition to DRAFT")
    void rejected_canTransitionToDraft() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.DRAFT);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("REJECTED - Can transition to PENDING_APPROVAL")
    void rejected_canTransitionToPendingApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PENDING_APPROVAL);

        // Then
        assertThat(canTransition).isTrue();
    }

    @Test
    @DisplayName("REJECTED - Cannot transition to APPROVED")
    void rejected_cannotTransitionToApproved() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.APPROVED);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("REJECTED - Cannot transition to PAID")
    void rejected_cannotTransitionToPaid() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.PAID);

        // Then
        assertThat(canTransition).isFalse();
    }

    @Test
    @DisplayName("REJECTED - Cannot transition to REJECTED")
    void rejected_cannotTransitionToRejected() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean canTransition = status.canTransitionTo(ExpenseStatus.REJECTED);

        // Then
        assertThat(canTransition).isFalse();
    }

    // canTransitionTo tests - PAID (terminal state)
    @Test
    @DisplayName("PAID - Cannot transition to any state (terminal)")
    void paid_cannotTransitionToAnyState() {
        // Given
        ExpenseStatus status = ExpenseStatus.PAID;

        // When / Then
        assertThat(status.canTransitionTo(ExpenseStatus.DRAFT)).isFalse();
        assertThat(status.canTransitionTo(ExpenseStatus.PENDING_APPROVAL)).isFalse();
        assertThat(status.canTransitionTo(ExpenseStatus.APPROVED)).isFalse();
        assertThat(status.canTransitionTo(ExpenseStatus.REJECTED)).isFalse();
        assertThat(status.canTransitionTo(ExpenseStatus.PAID)).isFalse();
    }

    // isEditable tests
    @Test
    @DisplayName("isEditable - DRAFT is editable")
    void isEditable_draftIsEditable() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean isEditable = status.isEditable();

        // Then
        assertThat(isEditable).isTrue();
    }

    @Test
    @DisplayName("isEditable - REJECTED is editable")
    void isEditable_rejectedIsEditable() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean isEditable = status.isEditable();

        // Then
        assertThat(isEditable).isTrue();
    }

    @Test
    @DisplayName("isEditable - PENDING_APPROVAL is not editable")
    void isEditable_pendingApprovalIsNotEditable() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean isEditable = status.isEditable();

        // Then
        assertThat(isEditable).isFalse();
    }

    @Test
    @DisplayName("isEditable - APPROVED is not editable")
    void isEditable_approvedIsNotEditable() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean isEditable = status.isEditable();

        // Then
        assertThat(isEditable).isFalse();
    }

    @Test
    @DisplayName("isEditable - PAID is not editable")
    void isEditable_paidIsNotEditable() {
        // Given
        ExpenseStatus status = ExpenseStatus.PAID;

        // When
        boolean isEditable = status.isEditable();

        // Then
        assertThat(isEditable).isFalse();
    }

    // requiresApproval tests
    @Test
    @DisplayName("requiresApproval - PENDING_APPROVAL requires approval")
    void requiresApproval_pendingApprovalRequiresApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean requiresApproval = status.requiresApproval();

        // Then
        assertThat(requiresApproval).isTrue();
    }

    @Test
    @DisplayName("requiresApproval - DRAFT does not require approval")
    void requiresApproval_draftDoesNotRequireApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean requiresApproval = status.requiresApproval();

        // Then
        assertThat(requiresApproval).isFalse();
    }

    @Test
    @DisplayName("requiresApproval - APPROVED does not require approval")
    void requiresApproval_approvedDoesNotRequireApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean requiresApproval = status.requiresApproval();

        // Then
        assertThat(requiresApproval).isFalse();
    }

    @Test
    @DisplayName("requiresApproval - REJECTED does not require approval")
    void requiresApproval_rejectedDoesNotRequireApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean requiresApproval = status.requiresApproval();

        // Then
        assertThat(requiresApproval).isFalse();
    }

    @Test
    @DisplayName("requiresApproval - PAID does not require approval")
    void requiresApproval_paidDoesNotRequireApproval() {
        // Given
        ExpenseStatus status = ExpenseStatus.PAID;

        // When
        boolean requiresApproval = status.requiresApproval();

        // Then
        assertThat(requiresApproval).isFalse();
    }

    // isFinal tests
    @Test
    @DisplayName("isFinal - PAID is final")
    void isFinal_paidIsFinal() {
        // Given
        ExpenseStatus status = ExpenseStatus.PAID;

        // When
        boolean isFinal = status.isFinal();

        // Then
        assertThat(isFinal).isTrue();
    }

    @Test
    @DisplayName("isFinal - DRAFT is not final")
    void isFinal_draftIsNotFinal() {
        // Given
        ExpenseStatus status = ExpenseStatus.DRAFT;

        // When
        boolean isFinal = status.isFinal();

        // Then
        assertThat(isFinal).isFalse();
    }

    @Test
    @DisplayName("isFinal - PENDING_APPROVAL is not final")
    void isFinal_pendingApprovalIsNotFinal() {
        // Given
        ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

        // When
        boolean isFinal = status.isFinal();

        // Then
        assertThat(isFinal).isFalse();
    }

    @Test
    @DisplayName("isFinal - APPROVED is not final")
    void isFinal_approvedIsNotFinal() {
        // Given
        ExpenseStatus status = ExpenseStatus.APPROVED;

        // When
        boolean isFinal = status.isFinal();

        // Then
        assertThat(isFinal).isFalse();
    }

    @Test
    @DisplayName("isFinal - REJECTED is not final")
    void isFinal_rejectedIsNotFinal() {
        // Given
        ExpenseStatus status = ExpenseStatus.REJECTED;

        // When
        boolean isFinal = status.isFinal();

        // Then
        assertThat(isFinal).isFalse();
    }

    // State machine workflow validation
    @Test
    @DisplayName("Workflow - Valid transitions from DRAFT to PAID")
    void workflow_validTransitionsFromDraftToPaid() {
        // Workflow: DRAFT -> PENDING_APPROVAL -> APPROVED -> PAID
        ExpenseStatus draft = ExpenseStatus.DRAFT;
        ExpenseStatus pending = ExpenseStatus.PENDING_APPROVAL;
        ExpenseStatus approved = ExpenseStatus.APPROVED;
        ExpenseStatus paid = ExpenseStatus.PAID;

        assertThat(draft.canTransitionTo(pending)).isTrue();
        assertThat(pending.canTransitionTo(approved)).isTrue();
        assertThat(approved.canTransitionTo(paid)).isTrue();
        assertThat(paid.canTransitionTo(draft)).isFalse(); // Terminal
    }

    @Test
    @DisplayName("Workflow - Valid rejection and resubmission flow")
    void workflow_validRejectionAndResubmissionFlow() {
        // Workflow: DRAFT -> PENDING_APPROVAL -> REJECTED -> DRAFT -> PENDING_APPROVAL
        ExpenseStatus draft = ExpenseStatus.DRAFT;
        ExpenseStatus pending = ExpenseStatus.PENDING_APPROVAL;
        ExpenseStatus rejected = ExpenseStatus.REJECTED;

        assertThat(draft.canTransitionTo(pending)).isTrue();
        assertThat(pending.canTransitionTo(rejected)).isTrue();
        assertThat(rejected.canTransitionTo(draft)).isTrue();
        assertThat(draft.canTransitionTo(pending)).isTrue();
    }

    @Test
    @DisplayName("Workflow - Direct approval from DRAFT")
    void workflow_directApprovalFromDraft() {
        // Workflow: DRAFT -> APPROVED (for auto-approved or small amounts)
        ExpenseStatus draft = ExpenseStatus.DRAFT;
        ExpenseStatus approved = ExpenseStatus.APPROVED;

        assertThat(draft.canTransitionTo(approved)).isTrue();
        assertThat(approved.canTransitionTo(ExpenseStatus.PAID)).isTrue();
    }

    @Test
    @DisplayName("Workflow - All enum values exist")
    void workflow_allEnumValuesExist() {
        ExpenseStatus[] allStatuses = ExpenseStatus.values();

        assertThat(allStatuses).hasSize(5);
        assertThat(allStatuses).contains(
            ExpenseStatus.DRAFT,
            ExpenseStatus.PENDING_APPROVAL,
            ExpenseStatus.APPROVED,
            ExpenseStatus.REJECTED,
            ExpenseStatus.PAID
        );
    }
}
