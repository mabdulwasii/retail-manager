package com.princely.shopmanager.shared.domain;

import com.princely.shopmanager.core.domain.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "feature_flags", indexes = {
    @Index(name = "idx_feature_shop", columnList = "shop_id"),
    @Index(name = "idx_feature_name", columnList = "feature_name"),
    @Index(name = "idx_feature_enabled", columnList = "enabled")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_feature_shop", columnNames = {"shop_id", "feature_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop"})
public class FeatureFlag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop; // null for global features

    @Column(name = "feature_name", nullable = false)
    private String featureName;

    @Builder.Default
    @Column(name = "enabled", nullable = false)
    private boolean enabled = false;

    @Column(name = "description", length = 500)
    private String description;

    @ElementCollection
    @CollectionTable(
        name = "feature_flag_config",
        joinColumns = @JoinColumn(name = "feature_flag_id")
    )
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value", length = 1000)
    @Builder.Default
    private Map<String, String> configuration = new HashMap<>();

    @Column(name = "effective_from")
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_until")
    private LocalDateTime effectiveUntil;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_modified_by")
    private String lastModifiedBy;

    public boolean isEffective() {
        LocalDateTime now = LocalDateTime.now();

        if (effectiveFrom != null && now.isBefore(effectiveFrom)) {
            return false;
        }

        if (effectiveUntil != null && now.isAfter(effectiveUntil)) {
            return false;
        }

        return enabled;
    }

    public boolean isGlobal() {
        return shop == null;
    }

    public void addConfig(String key, String value) {
        configuration.put(key, value);
    }

    public String getConfig(String key) {
        return configuration.get(key);
    }

    public String getConfig(String key, String defaultValue) {
        return configuration.getOrDefault(key, defaultValue);
    }

    public boolean hasConfig(String key) {
        return configuration.containsKey(key);
    }

    public void enable(String modifiedBy) {
        this.enabled = true;
        this.lastModifiedBy = modifiedBy;
    }

    public void disable(String modifiedBy) {
        this.enabled = false;
        this.lastModifiedBy = modifiedBy;
    }

    public void setEffectivePeriod(LocalDateTime from, LocalDateTime until, String modifiedBy) {
        this.effectiveFrom = from;
        this.effectiveUntil = until;
        this.lastModifiedBy = modifiedBy;
    }

    // Common feature flag names
    public static final String INVESTMENT_ENABLED = "investment.enabled";
    public static final String ANALYTICS_ENABLED = "analytics.enabled";
    public static final String FRAUD_ENABLED = "fraud.enabled";
    public static final String ADVANCED_REPORTING = "reporting.advanced";
    public static final String MULTI_CURRENCY = "currency.multi";
    public static final String MOBILE_PAYMENTS = "payments.mobile";
    public static final String INVENTORY_TRACKING = "inventory.tracking";
    public static final String CUSTOMER_LOYALTY = "loyalty.enabled";
}