package com.princely.shopmanager.returns.dto;

import com.princely.shopmanager.returns.domain.ProductReturn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnResponse {

    private String id;
    private String shopId;
    private String salesTransactionId;
    private String productId;
    private String productName;
    private Integer quantityReturned;
    private ProductReturn.ReturnReason returnReason;
    private ProductReturn.ReturnType returnType;
    private BigDecimal refundAmount;
    private ProductReturn.RefundType refundType;
    private ProductReturn.ReturnStatus status;
    private ProductReturn.FraudCheckStatus fraudCheckStatus;
    private LocalDateTime returnDate;
    private LocalDateTime processedDate;
    private String customerNotes;
    private String internalNotes;
}