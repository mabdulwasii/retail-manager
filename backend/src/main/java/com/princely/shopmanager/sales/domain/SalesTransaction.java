package com.princely.shopmanager.sales.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_transactions", indexes = {
    @Index(name = "idx_transaction_shop", columnList = "shop_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_number", columnList = "transaction_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "cashier", "lineItems"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "cashier", "lineItems"})
public class SalesTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "transaction_number", unique = true, nullable = false)
    private String transactionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_email")
    private String customerEmail;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<LineItem> lineItems = new ArrayList<>();

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "notes")
    private String notes;

    @Builder.Default
    @Column(name = "is_voided")
    private boolean isVoided = false;

    @Column(name = "void_reason")
    private String voidReason;

    @Column(name = "voided_by")
    private String voidedBy;

    @Column(name = "voided_at")
    private LocalDateTime voidedAt;

    // Fraud detection fields
    @Column(name = "fraud_score", precision = 5, scale = 2)
    private BigDecimal fraudScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Builder.Default
    @Column(name = "requires_review")
    private boolean requiresReview = false;

    @Column(name = "fraud_flags")
    private String fraudFlags;

    public enum PaymentMethod {
        CASH,
        CARD,
        BANK_TRANSFER,
        MOBILE_MONEY,
        CREDIT,
        MIXED
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED
    }

    public void addLineItem(LineItem lineItem) {
        lineItems.add(lineItem);
        lineItem.setTransaction(this);
        recalculateTotals();
    }

    public void removeLineItem(LineItem lineItem) {
        lineItems.remove(lineItem);
        lineItem.setTransaction(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = lineItems.stream()
            .map(LineItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (this.taxAmount == null) {
            this.taxAmount = BigDecimal.ZERO;
        }
        if (this.discountAmount == null) {
            this.discountAmount = BigDecimal.ZERO;
        }

        this.totalAmount = this.subtotal
            .add(this.taxAmount)
            .subtract(this.discountAmount);
    }
}