package com.princely.shopmanager.expenses.domain;

/**
 * Enumeration representing the different states an expense can be in.
 */
public enum ExpenseStatus {
    /**
     * Expense is being created/edited and not yet submitted
     */
    DRAFT,

    /**
     * Expense has been submitted and is awaiting approval
     */
    PENDING_APPROVAL,

    /**
     * Expense has been approved by an authorized user
     */
    APPROVED,

    /**
     * Expense has been rejected by an authorized user
     */
    REJECTED,

    /**
     * Approved expense has been paid/processed
     */
    PAID;

    public boolean canTransitionTo(ExpenseStatus target) {
        return switch (this) {
            case DRAFT -> target == PENDING_APPROVAL || target == APPROVED;
            case PENDING_APPROVAL -> target == APPROVED || target == REJECTED;
            case APPROVED -> target == PAID;
            case REJECTED -> target == DRAFT || target == PENDING_APPROVAL;
            case PAID -> false; // Terminal state
        };
    }

    public boolean isEditable() {
        return this == DRAFT || this == REJECTED;
    }

    public boolean requiresApproval() {
        return this == PENDING_APPROVAL;
    }

    public boolean isFinal() {
        return this == PAID;
    }
}