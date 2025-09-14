package com.princely.shopmanager.returns.domain;

import com.princely.shopmanager.core.domain.Product;
import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "salesTransaction", "product", "processedBy"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "salesTransaction", "product", "processedBy"})
public class ProductReturn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_transaction_id", nullable = false)
    private SalesTransaction salesTransaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_returned", nullable = false)
    private Integer quantityReturned;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_reason", nullable = false)
    private ReturnReason returnReason;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "return_type", nullable = false)
    private ReturnType returnType = ReturnType.FULL;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type")
    private RefundType refundType = RefundType.CASH;

    @Column(name = "condition_assessment", length = 500)
    private String conditionAssessment;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "fraud_check_status")
    private FraudCheckStatus fraudCheckStatus = FraudCheckStatus.PENDING;

    @Column(name = "fraud_check_result", length = 1000)
    private String fraudCheckResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;

    @Builder.Default
    @Column(name = "return_date")
    private LocalDateTime returnDate = LocalDateTime.now();

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @Column(name = "internal_notes", length = 1000)
    private String internalNotes;

    @Column(name = "damage_assessment", length = 500)
    private String damageAssessment;

    @Builder.Default
    @Column(name = "is_restockable")
    private boolean isRestockable = true;

    public enum ReturnReason {
        DEFECTIVE,
        WRONG_ITEM,
        DAMAGED,
        EXPIRED,
        CUSTOMER_CHANGE_OF_MIND,
        WARRANTY_CLAIM,
        QUALITY_ISSUE,
        SIZE_ISSUE,
        OTHER
    }

    public enum ReturnType {
        FULL,
        PARTIAL,
        DAMAGED,
        EXPIRED
    }

    public enum RefundType {
        CASH,
        STORE_CREDIT,
        EXCHANGE,
        NO_REFUND
    }

    public enum ReturnStatus {
        PENDING,
        APPROVED,
        REJECTED,
        PROCESSING,
        COMPLETED,
        CANCELLED
    }

    public enum FraudCheckStatus {
        PENDING,
        PASSED,
        FAILED,
        REVIEW_REQUIRED,
        BYPASSED
    }

    public boolean isProcessable() {
        return status == ReturnStatus.PENDING ||
               status == ReturnStatus.APPROVED;
    }

    public boolean requiresFraudCheck() {
        return fraudCheckStatus == FraudCheckStatus.PENDING ||
               fraudCheckStatus == FraudCheckStatus.REVIEW_REQUIRED;
    }

    public boolean canProcess() {
        return isProcessable() &&
               (fraudCheckStatus == FraudCheckStatus.PASSED ||
                fraudCheckStatus == FraudCheckStatus.BYPASSED);
    }

    public void approve(User approver) {
        this.status = ReturnStatus.APPROVED;
        this.processedBy = approver;
        this.processedDate = LocalDateTime.now();
    }

    public void reject(User rejector, String reason) {
        this.status = ReturnStatus.REJECTED;
        this.processedBy = rejector;
        this.processedDate = LocalDateTime.now();
        this.internalNotes = reason;
    }

    public void complete() {
        this.status = ReturnStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }

    public BigDecimal calculateRefundAmount(BigDecimal originalUnitPrice) {
        if (refundAmount != null) {
            return refundAmount;
        }

        BigDecimal baseRefund = originalUnitPrice.multiply(BigDecimal.valueOf(quantityReturned));

        return switch (returnType) {
            case FULL -> baseRefund;
            case PARTIAL -> baseRefund.multiply(BigDecimal.valueOf(0.8));
            case DAMAGED -> baseRefund.multiply(BigDecimal.valueOf(0.5));
            case EXPIRED -> baseRefund.multiply(BigDecimal.valueOf(0.3));
        };
    }
}