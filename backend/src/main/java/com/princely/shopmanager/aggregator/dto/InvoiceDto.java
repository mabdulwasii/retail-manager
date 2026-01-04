package com.princely.shopmanager.aggregator.dto;

import com.princely.shopmanager.aggregator.domain.BillingInvoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for BillingInvoice.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {

    private String id;
    private String tenantId;
    private String subscriptionId;
    private String invoiceNumber;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal discountAmount;
    private BigDecimal total;
    private BigDecimal amountPaid;
    private String currency;
    private BillingInvoice.Status status;
    private String paymentMethod;
    private String paymentTransactionId;
    private LocalDateTime paymentDate;
    private String lineItems;
    private String notes;
    private String pdfUrl;
    private Integer retryCount;
    private LocalDateTime nextRetryDate;
    private LocalDateTime createdAt;
}
