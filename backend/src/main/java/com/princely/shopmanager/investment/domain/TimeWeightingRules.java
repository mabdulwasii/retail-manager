package com.princely.shopmanager.investment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embeddable configuration for time-weighted profit sharing.
 * Defines how profit share multipliers increase based on investment duration.
 *
 * <p>Example usage:
 * <ul>
 *   <li>1 year = 1.0x base multiplier</li>
 *   <li>2 years = 1.2x (20% bonus for loyalty)</li>
 *   <li>3+ years = 1.5x (50% bonus, capped)</li>
 * </ul>
 *
 * <p>The multiplier is applied to the investor's proportional share:
 * <pre>
 * effectiveShare = (investorAmount / totalRoundAmount) * timeMultiplier
 * </pre>
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeWeightingRules {

    /**
     * Base investment duration in years (typically 1.0).
     * Investors with this duration get the base multiplier.
     */
    @Column(name = "base_years", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal baseYears = BigDecimal.valueOf(1.0);

    /**
     * Multiplier for base duration (typically 1.0 = no bonus).
     */
    @Column(name = "base_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal baseMultiplier = BigDecimal.valueOf(1.0);

    /**
     * Years for second tier (e.g., 2.0 years).
     */
    @Column(name = "year2_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal year2Threshold = BigDecimal.valueOf(2.0);

    /**
     * Multiplier for year 2+ (e.g., 1.2 = 20% bonus).
     */
    @Column(name = "year2_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal year2Multiplier = BigDecimal.valueOf(1.2);

    /**
     * Years for third tier (e.g., 3.0 years).
     */
    @Column(name = "year3_threshold", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal year3Threshold = BigDecimal.valueOf(3.0);

    /**
     * Multiplier for year 3+ (e.g., 1.5 = 50% bonus).
     */
    @Column(name = "year3_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal year3Multiplier = BigDecimal.valueOf(1.5);

    /**
     * Maximum multiplier cap to prevent excessive rewards.
     */
    @Column(name = "max_multiplier", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal maxMultiplier = BigDecimal.valueOf(2.0);

    /**
     * Gets the appropriate time multiplier for the given years invested.
     *
     * @param yearsInvested Number of years the investor has been invested
     * @return Time-based multiplier (capped at maxMultiplier)
     */
    public BigDecimal getMultiplierForYears(BigDecimal yearsInvested) {
        BigDecimal multiplier;

        if (yearsInvested.compareTo(year3Threshold) >= 0) {
            multiplier = year3Multiplier;
        } else if (yearsInvested.compareTo(year2Threshold) >= 0) {
            multiplier = year2Multiplier;
        } else {
            // Linear interpolation between base and year2 for partial years
            if (yearsInvested.compareTo(baseYears) >= 0) {
                BigDecimal progressToYear2 = yearsInvested.subtract(baseYears)
                    .divide(year2Threshold.subtract(baseYears), 4, java.math.RoundingMode.HALF_UP);
                BigDecimal bonusIncrease = year2Multiplier.subtract(baseMultiplier).multiply(progressToYear2);
                multiplier = baseMultiplier.add(bonusIncrease);
            } else {
                multiplier = baseMultiplier;
            }
        }

        // Apply max cap
        return multiplier.min(maxMultiplier);
    }

    /**
     * Validates that thresholds and multipliers are in ascending order.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return baseYears != null && year2Threshold != null && year3Threshold != null
            && baseYears.compareTo(year2Threshold) < 0
            && year2Threshold.compareTo(year3Threshold) < 0
            && baseMultiplier.compareTo(year2Multiplier) <= 0
            && year2Multiplier.compareTo(year3Multiplier) <= 0;
    }
}
