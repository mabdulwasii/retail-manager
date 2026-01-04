package com.princely.shopmanager.aggregator.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Billing Invoice entity.
 * Tracks invoices, payments, and billing history for cloud subscriptions.
 */
@Entity
@Table(name = "billing_invoices", indexes = {
        @Index(name = "idx_billing_invoice_tenant", columnList = "tenant_id"),
        @Index(name = "idx_billing_invoice_subscription", columnList = "subscription_id"),
        @Index(name = "idx_billing_invoice_status", columnList = "status"),
        @Index(name = "idx_billing_invoice_due_date", columnList = "due_date"),
        @Index(name = "idx_billing_invoice_number", columnList = "invoice_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(callSuper = true)
public class BillingInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotEmpty(message = "Tenant ID is required")
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @NotEmpty(message = "Subscription ID is required")
    @Column(name = "subscription_id", nullable = false)
    private String subscriptionId;

    /**
     * Unique invoice number (e.g., INV-2026-001234).
     */
    @NotEmpty(message = "Invoice number is required")
    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    /**
     * Billing period start date.
     */
    @NotNull(message = "Period start date is required")
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    /**
     * Billing period end date.
     */
    @NotNull(message = "Period end date is required")
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    /**
     * Invoice issue date.
     */
    @NotNull(message = "Issue date is required")
    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    /**
     * Payment due date.
     */
    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    /**
     * Subtotal amount (before tax).
     */
    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    /**
     * Tax amount.
     */
    @Builder.Default
    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * Tax rate (percentage).
     */
    @Builder.Default
    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;

    /**
     * Discount amount (if any).
     */
    @Builder.Default
    @Column(name = "discount_amount", precision = 19, scale = 4)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Total amount (subtotal + tax - discount).
     */
    @NotNull(message = "Total is required")
    @Positive(message = "Total must be positive")
    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    /**
     * Amount paid.
     */
    @Builder.Default
    @Column(name = "amount_paid", precision = 19, scale = 4)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    /**
     * Currency code (USD, EUR, GBP, NGN, etc.).
     */
    @NotEmpty(message = "Currency is required")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * Invoice status.
     */
    @Builder.Default
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status = Status.PENDING;

    /**
     * Payment method used.
     */
    @Column(name = "payment_method", length = 100)
    private String paymentMethod;

    /**
     * Payment transaction ID (from payment gateway).
     */
    @Column(name = "payment_transaction_id", length = 255)
    private String paymentTransactionId;

    /**
     * Payment date.
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * Line items (description of charges).
     */
    @Column(name = "line_items", columnDefinition = "TEXT")
    private String lineItems;

    /**
     * Notes or comments.
     */
    @Column(name = "notes", length = 1000)
    private String notes;

    /**
     * URL to download invoice PDF.
     */
    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    /**
     * Number of payment retry attempts.
     */
    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    /**
     * Next retry date (for failed payments).
     */
    @Column(name = "next_retry_date")
    private LocalDateTime nextRetryDate;

    /**
     * Invoice status enum.
     */
    public enum Status {
        DRAFT,          // Invoice created but not sent
        PENDING,        // Sent to customer, awaiting payment
        PAID,           // Payment received
        OVERDUE,        // Past due date, not paid
        FAILED,         // Payment failed
        CANCELLED,      // Invoice cancelled
        REFUNDED        // Payment refunded
    }

    /**
     * Check if invoice is paid.
     */
    public boolean isPaid() {
        return Status.PAID.equals(this.status);
    }

    /**
     * Check if invoice is overdue.
     */
    public boolean isOverdue() {
        return Status.OVERDUE.equals(this.status) ||
                (Status.PENDING.equals(this.status) && LocalDateTime.now().isAfter(dueDate));
    }

    /**
     * Check if payment can be retried.
     */
    public boolean canRetry() {
        return (Status.FAILED.equals(this.status) || Status.OVERDUE.equals(this.status)) &&
                retryCount < 3;
    }

    /**
     * Mark invoice as paid.
     */
    public void markPaid(String paymentMethod, String transactionId) {
        this.status = Status.PAID;
        this.paymentMethod = paymentMethod;
        this.paymentTransactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
        this.amountPaid = this.total;
    }

    /**
     * Mark invoice as failed.
     */
    public void markFailed() {
        this.status = Status.FAILED;
        this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;

        // Schedule next retry (3 days, 7 days, 14 days)
        int daysToAdd = switch (this.retryCount) {
            case 1 -> 3;
            case 2 -> 7;
            case 3 -> 14;
            default -> 0;
        };

        if (daysToAdd > 0) {
            this.nextRetryDate = LocalDateTime.now().plusDays(daysToAdd);
        }
    }

    /**
     * Mark invoice as overdue.
     */
    public void markOverdue() {
        if (Status.PENDING.equals(this.status)) {
            this.status = Status.OVERDUE;
        }
    }

    /**
     * Cancel invoice.
     */
    public void cancel(String reason) {
        this.status = Status.CANCELLED;
        this.notes = (this.notes == null ? "" : this.notes + "\n") + "Cancelled: " + reason;
    }

    /**
     * Refund invoice.
     */
    public void refund(String reason) {
        if (!Status.PAID.equals(this.status)) {
            throw new IllegalStateException("Only paid invoices can be refunded");
        }
        this.status = Status.REFUNDED;
        this.amountPaid = BigDecimal.ZERO;
        this.notes = (this.notes == null ? "" : this.notes + "\n") + "Refunded: " + reason;
    }

    /**
     * Calculate total from subtotal, tax, and discount.
     */
    public void calculateTotal() {
        BigDecimal subtotalValue = this.subtotal != null ? this.subtotal : BigDecimal.ZERO;
        BigDecimal taxValue = this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO;
        BigDecimal discountValue = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;

        this.total = subtotalValue.add(taxValue).subtract(discountValue);
    }

    /**
     * Calculate tax amount from subtotal and tax rate.
     */
    public void calculateTax() {
        if (this.subtotal != null && this.taxRate != null) {
            this.taxAmount = this.subtotal.multiply(this.taxRate).divide(new BigDecimal("100"));
        }
    }
}
