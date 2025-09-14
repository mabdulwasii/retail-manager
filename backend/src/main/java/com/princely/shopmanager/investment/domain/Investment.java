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

    public enum InvestmentType {
        SHOP_WIDE,
        PRODUCT_SPECIFIC,
        CATEGORY_SPECIFIC
    }

    public enum ProfitSharingModel {
        PROPORTIONAL_BY_AMOUNT,
        FIXED_SHARES,
        TIME_WEIGHTED,
        TIERED
    }

    public enum InvestmentStatus {
        PENDING,
        ACTIVE,
        INACTIVE,
        MATURED,
        WITHDRAWN,
        CANCELLED
    }

    public BigDecimal getAvailableBalance() {
        return amount.add(totalProfitEarned).subtract(totalWithdrawn);
    }

    public boolean canWithdraw(BigDecimal withdrawAmount) {
        return getAvailableBalance().compareTo(withdrawAmount) >= 0;
    }
}