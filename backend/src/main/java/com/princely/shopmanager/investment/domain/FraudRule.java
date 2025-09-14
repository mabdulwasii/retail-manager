package com.princely.shopmanager.investment.domain;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_rules", indexes = {
    @Index(name = "idx_fraud_rule_shop", columnList = "shop_id"),
    @Index(name = "idx_fraud_rule_type", columnList = "rule_type"),
    @Index(name = "idx_fraud_rule_enabled", columnList = "enabled")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop"})
public class FraudRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop; // null for global rules

    @Column(name = "rule_name", nullable = false)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private FraudRuleType ruleType;

    @Column(name = "description", length = 500)
    private String description;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "threshold_amount", precision = 12, scale = 2)
    private BigDecimal thresholdAmount;

    @Column(name = "threshold_count")
    private Integer thresholdCount;

    @Column(name = "time_window_minutes")
    private Integer timeWindowMinutes;

    @Builder.Default
    @Column(name = "risk_score_weight", precision = 3, scale = 2)
    private BigDecimal riskScoreWeight = BigDecimal.valueOf(1.0);

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private RiskAssessment.RiskLevel severity = RiskAssessment.RiskLevel.MEDIUM;

    @Builder.Default
    @Column(name = "auto_block", nullable = false)
    private boolean autoBlock = false;

    @Builder.Default
    @Column(name = "requires_manual_review", nullable = false)
    private boolean requiresManualReview = true;

    @Column(name = "rule_configuration", length = 2000)
    private String ruleConfiguration; // JSON configuration for complex rules

    public enum FraudRuleType {
        HIGH_AMOUNT_TRANSACTION,
        HIGH_FREQUENCY_TRANSACTIONS,
        UNUSUAL_TIME_TRANSACTION,
        RAPID_SUCCESSIVE_TRANSACTIONS,
        UNUSUAL_PAYMENT_METHOD,
        SUSPICIOUS_CUSTOMER_PATTERN,
        INVENTORY_MISMATCH,
        GEOGRAPHIC_ANOMALY,
        VELOCITY_CHECK,
        BLACKLIST_CHECK,
        CUSTOM_RULE
    }

    public boolean isGlobal() {
        return shop == null;
    }

    public boolean shouldTrigger(BigDecimal amount, Integer count, Integer timeWindow) {
        if (!enabled) {
            return false;
        }

        return switch (ruleType) {
            case HIGH_AMOUNT_TRANSACTION -> amount != null && thresholdAmount != null &&
                amount.compareTo(thresholdAmount) > 0;
            case HIGH_FREQUENCY_TRANSACTIONS -> count != null && thresholdCount != null &&
                count > thresholdCount;
            case RAPID_SUCCESSIVE_TRANSACTIONS -> count != null && thresholdCount != null &&
                timeWindow != null && timeWindowMinutes != null &&
                count > thresholdCount && timeWindow <= timeWindowMinutes;
            default -> false; // Other rules require more complex logic
        };
    }

    public String getFlag() {
        return ruleType.name() + "_VIOLATION";
    }
}