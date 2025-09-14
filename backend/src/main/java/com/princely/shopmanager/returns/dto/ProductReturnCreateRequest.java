package com.princely.shopmanager.returns.dto;

import com.princely.shopmanager.returns.domain.ProductReturn;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReturnCreateRequest {

    @NotNull(message = "Sales transaction ID is required")
    private String salesTransactionId;

    @NotNull(message = "Product ID is required")
    private String productId;

    @NotNull(message = "Quantity returned is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityReturned;

    @NotNull(message = "Return reason is required")
    private ProductReturn.ReturnReason returnReason;

    @Builder.Default
    private ProductReturn.ReturnType returnType = ProductReturn.ReturnType.FULL;

    private String customerNotes;
}