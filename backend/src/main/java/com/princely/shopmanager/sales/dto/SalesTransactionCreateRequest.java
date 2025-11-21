package com.princely.shopmanager.sales.dto;

import com.princely.shopmanager.sales.domain.SalesTransaction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesTransactionCreateRequest {

    @NotNull(message = "Shop ID is required")
    private String shopId;

    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @NotEmpty(message = "At least one line item is required")
    @Valid
    private List<LineItemRequest> lineItems;

    private BigDecimal taxAmount;
    private BigDecimal discountAmount;

    @NotNull(message = "Payment method is required")
    private SalesTransaction.PaymentMethod paymentMethod;

    private String paymentReference;
    private String notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineItemRequest {
        @NotNull(message = "Product ID is required")
        private String productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Unit price is required")
        private BigDecimal unitPrice;

        private BigDecimal discount;
    }
}
