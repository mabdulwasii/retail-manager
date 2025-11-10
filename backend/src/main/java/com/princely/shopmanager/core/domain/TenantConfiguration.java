package com.princely.shopmanager.core.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Entity representing tenant-wide configuration settings.
 *
 * Supports flexible key-value based configuration with:
 * - Type safety (STRING, NUMBER, BOOLEAN, JSON)
 * - Category grouping for organization
 * - Description and default values
 * - Tenant isolation
 *
 * Example configurations:
 * - BUSINESS: tax_rate, currency, return_policy_days
 * - DISPLAY: date_format, timezone, language
 * - NOTIFICATION: email_enabled, sms_enabled
 * - INTEGRATION: payment_gateway, shipping_provider
 * - OPERATIONAL: business_hours, max_discount_percent
 */
@Entity
@Table(
    name = "tenant_configurations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id", "config_key"}),
    indexes = {
        @Index(name = "idx_tenant_config", columnList = "tenant_id, config_key"),
        @Index(name = "idx_config_category", columnList = "category")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "tenant")
@EqualsAndHashCode(callSuper = true, exclude = "tenant")
public class TenantConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    @NotNull(message = "Tenant is required")
    private Tenant tenant;

    @NotEmpty(message = "Configuration key is required")
    @Column(name = "config_key", nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    @NotNull(message = "Value type is required")
    @Builder.Default
    private ValueType valueType = ValueType.STRING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull(message = "Category is required")
    @Builder.Default
    private ConfigCategory category = ConfigCategory.BUSINESS;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean editable = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Configuration value types for type safety and validation.
     */
    public enum ValueType {
        STRING,
        NUMBER,
        BOOLEAN,
        JSON
    }

    /**
     * Configuration categories for logical grouping.
     */
    public enum ConfigCategory {
        BUSINESS,       // Tax rates, currency, return policies
        DISPLAY,        // UI preferences, formats, language
        NOTIFICATION,   // Email, SMS, push settings
        INTEGRATION,    // External service configurations
        OPERATIONAL,    // Business hours, limits, thresholds
        SECURITY,       // Security-related settings
        FEATURE         // Feature flags and toggles
    }

    /**
     * Gets the effective value (actual value or default if null).
     */
    public String getEffectiveValue() {
        return value != null ? value : defaultValue;
    }
}
