package com.princely.shopmanager.investment.dto;

import com.princely.shopmanager.investment.domain.InvestorDistribution;
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
public class InvestorDistributionResponse {

    private String id;
    private String investmentId;
    private String investmentNumber;
    private String investorName;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal totalSalesRevenue;
    private BigDecimal totalProfit;
    private BigDecimal investorSharePercentage;
    private BigDecimal investorProfitAmount;
    private BigDecimal distributionAmount;
    private InvestorDistribution.DistributionStatus status;
    private LocalDateTime distributionDate;
    private String paymentReference;
    private String notes;
    private String calculationDetails;
    private LocalDateTime createdAt;
}