package com.princely.shopmanager.embedded.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for syncing transaction data to cloud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSyncDto {

    private String transactionId;
    private String transactionNumber;
    private String shopId; // Shop identifier (formerly storeId)
    private String tenantId; // Tenant identifier (organization)
    private LocalDateTime transactionDate;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private String paymentMethod;
    private String status;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private List<TransactionItemDto> items;
    private LocalDateTime syncedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionItemDto {
        private String productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
        private BigDecimal discountAmount;
        private BigDecimal taxAmount;
    }
}
