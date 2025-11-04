package com.princely.shopmanager.expenses.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Entity representing an expense category.
 * Categories help organize and control expense approvals.
 */
@Entity
@Table(
    name = "expense_categories",
    indexes = {
        @Index(name = "idx_expense_categories_shop", columnList = "shopId"),
        @Index(name = "idx_expense_categories_active", columnList = "isActive"),
        @Index(name = "idx_expense_categories_name", columnList = "name")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_expense_category_shop_name", columnNames = {"shopId", "name"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ExpenseCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "shop_id", nullable = false)
    private String shopId;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @NotNull
    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = true;

    @DecimalMin(value = "0.0")
    @Column(name = "approval_limit", precision = 10, scale = 2)
    private BigDecimal approvalLimit;

    @Column(name = "default_payment_method", length = 50)
    private String defaultPaymentMethod;

    @Column(name = "gl_account_code", length = 50)
    private String glAccountCode;

    @Column(name = "tax_deductible", nullable = false)
    @Builder.Default
    private Boolean taxDeductible = true;

    @Column(name = "auto_approval_enabled", nullable = false)
    @Builder.Default
    private Boolean autoApprovalEnabled = false;

    // Business logic methods
    public boolean requiresApprovalForAmount(BigDecimal amount) {
        if (!requiresApproval) {
            return false;
        }

        if (approvalLimit == null) {
            return true;
        }

        return amount.compareTo(approvalLimit) > 0;
    }

    public boolean canAutoApprove(BigDecimal amount) {
        return autoApprovalEnabled &&
               !requiresApprovalForAmount(amount) &&
               isActive;
    }

    public void activate() {
        this.isActive = true;
    }

    public void setApprovalLimit(BigDecimal limit) {
        if (limit != null && limit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Approval limit cannot be negative");
        }
        this.approvalLimit = limit;
    }

    @PrePersist
    @PreUpdate
    private void validateCategory() {
        if (name != null) {
            name = name.trim();
        }

        if (approvalLimit != null && approvalLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Approval limit cannot be negative");
        }
    }
}