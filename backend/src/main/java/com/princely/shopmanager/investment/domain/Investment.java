package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Product;
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
 * Investment entity representing an investment made by a user in a shop or specific products.
 *
 * <p>This entity tracks investment details including:
 * <ul>
 *   <li>Investment amount and type (shop-wide or product-specific)</li>
 *   <li>Expected and actual returns</li>
 *   <li>Investment duration and maturity dates</li>
 *   <li>Profit distribution preferences</li>
 *   <li>Risk assessment and status tracking</li>
 * </ul>
 *
 * <p>Investments can be either:
 * <ul>
 *   <li><b>SHOP_WIDE:</b> Investment in the entire shop's performance</li>
 *   <li><b>PRODUCT_SPECIFIC:</b> Investment targeting specific products</li>
 *   <li><b>CATEGORY_BASED:</b> Investment in product categories</li>
 * </ul>
 *
 * @author Shop Manager Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "investments", indexes = {
    @Index(name = "idx_investment_investor", columnList = "investor_id"),
    @Index(name = "idx_investment_shop", columnList = "shop_id"),
    @Index(name = "idx_investment_date", columnList = "investment_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"investor", "shop", "products", "shares"})
@EqualsAndHashCode(callSuper = true, exclude = {"investor", "shop", "products", "shares"})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false)
    private InvestmentType investmentType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "investment_products",
        joinColumns = @JoinColumn(name = "investment_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private Set<Product> products = new HashSet<>();

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "profit_sharing_model", nullable = false)
    private ProfitSharingModel profitSharingModel;

    @Column(name = "profit_percentage", precision = 5, scale = 2)
    private BigDecimal profitPercentage;

    @Column(name = "fixed_shares")
    private Integer fixedShares;

    @Column(name = "investment_date", nullable = false)
    private LocalDateTime investmentDate;

    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;

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
}