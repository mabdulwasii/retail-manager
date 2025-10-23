package com.princely.shopmanager.sales.dto;

import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTransactionResponse {

    private String id;
    private String transactionNumber;
    private String shopId;
    private String shopName;
    private String cashierId;
    private String cashierName;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private List<LineItemResponse> lineItems;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private SalesTransaction.PaymentMethod paymentMethod;
    private String paymentReference;
    private SalesTransaction.TransactionStatus status;
    private LocalDateTime transactionDate;
    private String notes;
    private boolean isVoided;
    private String voidReason;
    private String voidedBy;
    private LocalDateTime voidedAt;
    private BigDecimal fraudScore;
    private String riskLevel;
    private boolean requiresReview;
    private String fraudFlags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItemResponse {
        private String id;
        private String productId;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal discount;
        private BigDecimal lineTotal;
    }

    public static SalesTransactionResponse fromEntity(SalesTransaction transaction) {
        return SalesTransactionResponse.builder()
            .id(transaction.getId())
            .transactionNumber(transaction.getTransactionNumber())
            .shopId(transaction.getShop() != null ? transaction.getShop().getId() : null)
            .shopName(transaction.getShop() != null ? transaction.getShop().getName() : null)
            .cashierId(transaction.getCashier() != null ? transaction.getCashier().getId() : null)
            .cashierName(transaction.getCashier() != null ? transaction.getCashier().getFullName() : null)
            .customerName(transaction.getCustomerName())
            .customerPhone(transaction.getCustomerPhone())
            .customerEmail(transaction.getCustomerEmail())
            .lineItems(transaction.getLineItems() != null ?
                transaction.getLineItems().stream()
                    .map(SalesTransactionResponse::toLineItemResponse)
                    .collect(Collectors.toList()) : List.of())
            .subtotal(transaction.getSubtotal())
            .taxAmount(transaction.getTaxAmount())
            .discountAmount(transaction.getDiscountAmount())
            .totalAmount(transaction.getTotalAmount())
            .paymentMethod(transaction.getPaymentMethod())
            .paymentReference(transaction.getPaymentReference())
            .status(transaction.getStatus())
            .transactionDate(transaction.getTransactionDate())
            .notes(transaction.getNotes())
            .isVoided(transaction.isVoided())
            .voidReason(transaction.getVoidReason())
            .voidedBy(transaction.getVoidedBy())
            .voidedAt(transaction.getVoidedAt())
            .fraudScore(transaction.getFraudScore())
            .riskLevel(transaction.getRiskLevel())
            .requiresReview(transaction.isRequiresReview())
            .fraudFlags(transaction.getFraudFlags())
            .build();
    }

    private static LineItemResponse toLineItemResponse(LineItem lineItem) {
        return LineItemResponse.builder()
            .id(lineItem.getId())
            .productId(lineItem.getProduct() != null ? lineItem.getProduct().getId() : null)
            .productName(lineItem.getProduct() != null ? lineItem.getProduct().getName() : null)
            .quantity(lineItem.getQuantity())
            .unitPrice(lineItem.getUnitPrice())
            .discount(lineItem.getDiscountAmount())
            .lineTotal(lineItem.getLineTotal())
            .build();
    }
}
