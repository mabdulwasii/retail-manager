package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.User;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Investment entity representing an individual investor's participation in an InvestmentRound.
 *
 * <p>This entity tracks investor-specific details:
 * <ul>
 *   <li>Investment amount contributed by this investor</li>
 *   <li>Fixed shares (for FIXED_SHARES model)</li>
 *   <li>Total profit earned and withdrawn by this investor</li>
 *   <li>Investment status (ACTIVE, WITHDRAWN, etc.)</li>
 * </ul>
 *
 * <p>Shared configuration (investment type, profit sharing model, maturity date)
 * is stored in the InvestmentRound entity that this investment belongs to.
 *
 * @author Shop Manager Development Team
 * @version 2.0
 * @since 1.0
 */
@Entity
@Table(name = "investments", indexes = {
    @Index(name = "idx_investment_investor", columnList = "investor_id"),
    @Index(name = "idx_investment_shop", columnList = "shop_id"),
    @Index(name = "idx_investment_round", columnList = "investment_round_id"),
    @Index(name = "idx_investment_date", columnList = "investment_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"investor", "shop", "investmentRound", "shares"})
@EqualsAndHashCode(callSuper = true, exclude = {"investor", "shop", "investmentRound", "shares"})
public class Investment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "investment_number", unique = true, nullable = false)
    private String investmentNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private User investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investment_round_id", nullable = false)
    private InvestmentRound investmentRound;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "fixed_shares")
    private Integer fixedShares;

    @Column(name = "investment_date", nullable = false)
    private LocalDateTime investmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InvestmentStatus status = InvestmentStatus.ACTIVE;

    @Column(name = "total_profit_earned", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalProfitEarned = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Column(name = "last_profit_calculation")
    private LocalDateTime lastProfitCalculation;

    @OneToMany(mappedBy = "investment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<InvestorShare> shares = new HashSet<>();

    @Column(name = "notes", length = 1000)
    private String notes;

    @Getter
    public enum InvestmentType {
        SHOP_WIDE("Shop Wide"),
        PRODUCT_SPECIFIC("Product Specific"),
        CATEGORY_SPECIFIC("Category Specific");

        private final String displayName;

        InvestmentType(String displayName) {
            this.displayName = displayName;
        }
    }

    @Getter
    public enum ProfitSharingModel {
        PROPORTIONAL_BY_AMOUNT("Proportional by Amount"),
        FIXED_SHARES("Fixed Shares"),
        TIME_WEIGHTED("Time Weighted"),
        TIERED("Tiered");

        private final String displayName;

        ProfitSharingModel(String displayName) {
            this.displayName = displayName;
        }
    }

    @Getter
    public enum InvestmentStatus {
        PENDING("Pending"),
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        MATURED("Matured"),
        WITHDRAWN("Withdrawn"),
        CANCELLED("Cancelled");

        private final String displayName;

        InvestmentStatus(String displayName) {
            this.displayName = displayName;
        }
    }

    public BigDecimal getAvailableBalance() {
        return amount.add(totalProfitEarned).subtract(totalWithdrawn);
    }

    public boolean canWithdraw(BigDecimal withdrawAmount) {
        return getAvailableBalance().compareTo(withdrawAmount) >= 0;
    }

    /**
     * Gets the investment type from the associated round.
     *
     * @return Investment type
     */
    public InvestmentType getInvestmentType() {
        return investmentRound != null ? investmentRound.getInvestmentType() : null;
    }

    /**
     * Gets the profit sharing model from the associated round.
     *
     * @return Profit sharing model
     */
    public ProfitSharingModel getProfitSharingModel() {
        return investmentRound != null ? investmentRound.getProfitSharingModel() : null;
    }

    /**
     * Gets the maturity date from the associated round.
     *
     * @return Maturity date
     */
    public LocalDateTime getMaturityDate() {
        return investmentRound != null ? investmentRound.getMaturityDate() : null;
    }
}