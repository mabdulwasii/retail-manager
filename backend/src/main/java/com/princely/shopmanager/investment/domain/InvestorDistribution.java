package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_distributions", indexes = {
    @Index(name = "idx_distribution_investment", columnList = "investment_id"),
    @Index(name = "idx_distribution_date", columnList = "distribution_date"),
    @Index(name = "idx_distribution_period", columnList = "period_start, period_end")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"investment"})
@EqualsAndHashCode(callSuper = true, exclude = {"investment"})
public class InvestorDistribution extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "total_sales_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSalesRevenue;

    @Column(name = "total_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProfit;

    @Column(name = "investor_share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal investorSharePercentage;

    @Column(name = "investor_profit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal investorProfitAmount;

    @Column(name = "distribution_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal distributionAmount;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DistributionStatus status = DistributionStatus.CALCULATED;

    @Column(name = "distribution_date")
    private LocalDateTime distributionDate;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "calculation_details", length = 2000)
    private String calculationDetails;

    public enum DistributionStatus {
        CALCULATED,
        APPROVED,
        PAID,
        FAILED,
        CANCELLED
    }

    public void markAsPaid(String paymentReference) {
        this.status = DistributionStatus.PAID;
        this.distributionDate = LocalDateTime.now();
        this.paymentReference = paymentReference;
    }

    public void markAsFailed(String reason) {
        this.status = DistributionStatus.FAILED;
        this.notes = reason;
    }

    public boolean canBePaid() {
        return status == DistributionStatus.APPROVED;
    }

    public boolean isPaid() {
        return status == DistributionStatus.PAID;
    }
}