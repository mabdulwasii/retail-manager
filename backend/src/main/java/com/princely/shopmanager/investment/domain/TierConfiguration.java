package com.princely.shopmanager.investment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embeddable configuration for tiered profit sharing.
 * Defines investment amount thresholds and corresponding bonus multipliers.
 *
 * <p>Example usage:
 * <ul>
 *   <li>Tier 1: ₦0 - ₦50,000 = 1.0x (no bonus)</li>
 *   <li>Tier 2: ₦50,000 - ₦100,000 = 1.1x (10% bonus)</li>
 *   <li>Tier 3: ₦100,000+ = 1.2x (20% bonus)</li>
 * </ul>
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierConfiguration {

    @Column(name = "tier1_threshold", precision = 12, scale = 2)
    private BigDecimal tier1Threshold;

    @Column(name = "tier1_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tier1Multiplier = BigDecimal.valueOf(1.0);

    @Column(name = "tier2_threshold", precision = 12, scale = 2)
    private BigDecimal tier2Threshold;

    @Column(name = "tier2_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tier2Multiplier = BigDecimal.valueOf(1.1);

    @Column(name = "tier3_threshold", precision = 12, scale = 2)
    private BigDecimal tier3Threshold;

    @Column(name = "tier3_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal tier3Multiplier = BigDecimal.valueOf(1.2);

    /**
     * Gets the appropriate multiplier for the given investment amount.
     *
     * @param amount Investment amount
     * @return Tier multiplier
     */
    public BigDecimal getMultiplierForAmount(BigDecimal amount) {
        if (tier3Threshold != null && amount.compareTo(tier3Threshold) >= 0) {
            return tier3Multiplier;
        } else if (tier2Threshold != null && amount.compareTo(tier2Threshold) >= 0) {
            return tier2Multiplier;
        } else {
            return tier1Multiplier;
        }
    }

    /**
     * Validates that thresholds are in ascending order.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (tier1Threshold == null || tier2Threshold == null || tier3Threshold == null) {
            return false;
        }
        return tier1Threshold.compareTo(tier2Threshold) < 0
            && tier2Threshold.compareTo(tier3Threshold) < 0;
    }
}
