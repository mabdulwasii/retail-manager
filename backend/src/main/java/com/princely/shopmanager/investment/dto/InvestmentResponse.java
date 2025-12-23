package com.princely.shopmanager.investment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.princely.shopmanager.investment.domain.Investment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvestmentResponse {

    private String id;
    private String investmentNumber;
    private String investorId;
    private String investorName;
    private String investorEmail;
    private String shopId;
    private String shopName;
    private Investment.InvestmentType investmentType;
    private BigDecimal amount;
    private Investment.ProfitSharingModel profitSharingModel;
    private BigDecimal profitPercentage;
    private Integer fixedShares;
    private LocalDateTime investmentDate;
    private LocalDateTime maturityDate;
    private Investment.InvestmentStatus status;
    private BigDecimal totalProfitEarned;
    private BigDecimal totalWithdrawn;
    private BigDecimal availableBalance;
    private LocalDateTime lastProfitCalculation;
    private Set<ProductInfo> products;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private String category;
        private BigDecimal price;
    }
}