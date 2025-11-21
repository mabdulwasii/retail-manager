package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Investment Round entity representing a grouped set of investments with shared configuration.
 *
 * <p>An Investment Round captures the business concept of opening a fundraising round
 * where multiple investors can participate under the same terms and profit-sharing rules.
 *
 * <p>Key features:
 * <ul>
 *   <li>All investments in a round share the same profit-sharing model</li>
 *   <li>Configurable tier bonuses for TIERED model</li>
 *   <li>Configurable time weighting for TIME_WEIGHTED model</li>
 *   <li>Round can be OPEN (accepting new investors) or CLOSED</li>
 *   <li>Automatic calculation of total investment amount and investor count</li>
 * </ul>
 *
 * @author Shop Manager Development Team
 * @version 1.0
 * @since 1.0
 */
@Entity
@Table(name = "investment_rounds", indexes = {
    @Index(name = "idx_investment_round_shop", columnList = "shop_id"),
    @Index(name = "idx_investment_round_number", columnList = "round_number"),
    @Index(name = "idx_investment_round_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop", "investments"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop", "investments"})
public class InvestmentRound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "round_number", nullable = false, length = 50)
    private String roundNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(name = "investment_type", nullable = false, length = 50)
    private Investment.InvestmentType investmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "profit_sharing_model", nullable = false, length = 50)
    private Investment.ProfitSharingModel profitSharingModel;

    @Column(name = "maturity_date")
    private LocalDateTime maturityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private RoundStatus status = RoundStatus.OPEN;

    /**
     * Tier configuration for TIERED profit sharing model.
     * Only used when profitSharingModel = TIERED.
     */
    @Embedded
    private TierConfiguration tierConfiguration;

    /**
     * Time weighting rules for TIME_WEIGHTED profit sharing model.
     * Only used when profitSharingModel = TIME_WEIGHTED.
     */
    @Embedded
    private TimeWeightingRules timeWeightingRules;

    @Column(name = "notes", length = 1000)
    private String notes;

    @OneToMany(mappedBy = "investmentRound", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Investment> investments = new HashSet<>();

    /**
     * Investment Round status enumeration.
     */
    @Getter
    public enum RoundStatus {
        OPEN("Open"),           // Accepting new investors
        CLOSED("Closed"),       // No longer accepting new investors
        COMPLETED("Completed"), // Round has matured and been settled
        CANCELLED("Cancelled"); // Round was cancelled

        private final String displayName;

        RoundStatus(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Adds an investment to this round.
     *
     * @param investment Investment to add
     */
    public void addInvestment(Investment investment) {
        investments.add(investment);
        investment.setInvestmentRound(this);
    }

    /**
     * Removes an investment from this round.
     *
     * @param investment Investment to remove
     */
    public void removeInvestment(Investment investment) {
        investments.remove(investment);
        investment.setInvestmentRound(null);
    }

    /**
     * Checks if the round can accept new investors.
     *
     * @return true if status is OPEN, false otherwise
     */
    public boolean canAcceptInvestors() {
        return status == RoundStatus.OPEN;
    }

    /**
     * Closes the round to new investors.
     */
    public void close() {
        this.status = RoundStatus.CLOSED;
    }

    /**
     * Marks the round as completed.
     */
    public void complete() {
        this.status = RoundStatus.COMPLETED;
    }

    /**
     * Cancels the round.
     */
    public void cancel() {
        this.status = RoundStatus.CANCELLED;
    }
}
