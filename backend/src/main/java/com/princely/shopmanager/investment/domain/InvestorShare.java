package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "investor_shares", indexes = {
    @Index(name = "idx_share_investment", columnList = "investment_id"),
    @Index(name = "idx_share_transaction", columnList = "transaction_id"),
    @Index(name = "idx_share_date", columnList = "calculation_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"investment", "transaction"})
@EqualsAndHashCode(callSuper = true, exclude = {"investment", "transaction"})
public class InvestorShare extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_id", nullable = false)
    private Investment investment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private SalesTransaction transaction;

    @Column(name = "transaction_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal transactionAmount;

    @Column(name = "profit_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal profitAmount;

    @Column(name = "share_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal shareAmount;

    @Column(name = "share_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal sharePercentage;

    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShareStatus status = ShareStatus.PENDING;

    @Column(name = "distribution_date")
    private LocalDateTime distributionDate;

    @Column(name = "notes")
    private String notes;

    @Getter
    public enum ShareStatus {
        PENDING("Pending"),
        CALCULATED("Calculated"),
        DISTRIBUTED("Distributed"),
        REINVESTED("Reinvested"),
        CANCELLED("Cancelled");

        private final String displayName;

        ShareStatus(String displayName) {
            this.displayName = displayName;
        }
    }
}