package com.princely.shopmanager.expenses.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing an expense record.
 * Tracks procurement, expenditures, and operational costs for shops.
 */
@Entity
@Table(
    name = "expenses",
    indexes = {
        @Index(name = "idx_expenses_shop_date", columnList = "shopId, expenseDate"),
        @Index(name = "idx_expenses_category", columnList = "categoryId"),
        @Index(name = "idx_expenses_status", columnList = "status"),
        @Index(name = "idx_expenses_created_by", columnList = "createdBy"),
        @Index(name = "idx_expenses_approved_by", columnList = "approvedBy")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Expense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private String shopId;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "vendor_name", length = 255)
    private String vendorName;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ExpenseStatus status = ExpenseStatus.DRAFT;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "expense_tags",
        joinColumns = @JoinColumn(name = "expense_id")
    )
    @Column(name = "tag")
    @Builder.Default
    private Set<String> tags = new HashSet<>();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @Column(name = "expense_created_by", nullable = false)
    private UUID expenseCreatedBy;

    @Column(name = "created_by_name", length = 255)
    private String createdByName;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_by_name", length = 255)
    private String approvedByName;

    @Column(name = "approval_date")
    private LocalDate approvalDate;

    @Column(name = "approval_notes", columnDefinition = "TEXT")
    private String approvalNotes;

    // Helper methods
    public boolean isPending() {
        return status == ExpenseStatus.PENDING_APPROVAL;
    }

    public boolean isApproved() {
        return status == ExpenseStatus.APPROVED;
    }

    public boolean isRejected() {
        return status == ExpenseStatus.REJECTED;
    }

    public boolean canBeEdited() {
        return status == ExpenseStatus.DRAFT || status == ExpenseStatus.REJECTED;
    }

    public boolean canBeApproved() {
        return status == ExpenseStatus.PENDING_APPROVAL;
    }

    public void approve(UUID approvedBy, String approvedByName, String notes) {
        if (!canBeApproved()) {
            throw new IllegalStateException("Expense cannot be approved in current status: " + status);
        }
        this.status = ExpenseStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvedByName = approvedByName;
        this.approvalDate = LocalDate.now();
        this.approvalNotes = notes;
    }

    public void reject(UUID rejectedBy, String rejectedByName, String notes) {
        if (!canBeApproved()) {
            throw new IllegalStateException("Expense cannot be rejected in current status: " + status);
        }
        this.status = ExpenseStatus.REJECTED;
        this.approvedBy = rejectedBy;
        this.approvedByName = rejectedByName;
        this.approvalDate = LocalDate.now();
        this.approvalNotes = notes;
    }

    public void submitForApproval() {
        if (status != ExpenseStatus.DRAFT) {
            throw new IllegalStateException("Only draft expenses can be submitted for approval");
        }
        this.status = ExpenseStatus.PENDING_APPROVAL;
    }

    public void markAsPaid() {
        if (!isApproved()) {
            throw new IllegalStateException("Only approved expenses can be marked as paid");
        }
        this.status = ExpenseStatus.PAID;
    }

    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty()) {
            this.tags.add(tag.trim().toLowerCase());
        }
    }

    public void removeTag(String tag) {
        if (tag != null) {
            this.tags.remove(tag.trim().toLowerCase());
        }
    }

    @PrePersist
    @PreUpdate
    private void validateExpense() {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Expense amount must be greater than zero");
        }

        if (expenseDate != null && expenseDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Expense date cannot be in the future");
        }
    }
}