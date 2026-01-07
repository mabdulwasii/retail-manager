package com.princely.shopmanager.embedded.domain;

import com.princely.shopmanager.shared.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * System Settings Entity
 *
 * Flexible key-value store for system-wide configuration settings in embedded mode.
 * Settings are categorized and support different data types with validation.
 *
 * NOTE: This entity is ONLY used in embedded mode. In cloud mode, configuration
 * is managed via Kubernetes ConfigMaps and Secrets.
 *
 * @see com.princely.shopmanager.embedded.service.SystemSettingsService
 * @author Claude Code
 * @since 1.0.0
 */
@Entity
@Table(name = "system_settings", indexes = {
    @Index(name = "idx_system_settings_key", columnList = "setting_key"),
    @Index(name = "idx_system_settings_category", columnList = "setting_category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"createdAt", "updatedAt"})
@EqualsAndHashCode(callSuper = false)
public class SystemSettings extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Unique setting key identifier (e.g., "custom.domain", "cloud.sync.enabled")
     */
    @Column(name = "setting_key", unique = true, nullable = false, length = 100)
    private String key;

    /**
     * Current value of the setting (stored as text, converted based on dataType)
     */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String value;

    /**
     * Category grouping for settings (SYSTEM, DOMAIN, SYNC, STORAGE, SECURITY, DATABASE)
     */
    @Column(name = "setting_category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private SettingCategory category;

    /**
     * Data type for validation and conversion (STRING, NUMBER, BOOLEAN, JSON, ENCRYPTED)
     */
    @Column(name = "data_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SettingDataType dataType = SettingDataType.STRING;

    /**
     * Human-readable description of the setting's purpose
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Whether changing this setting requires application/container restart
     */
    @Column(name = "requires_restart", nullable = false)
    private Boolean requiresRestart = false;

    /**
     * Whether this setting contains sensitive data (API keys, passwords, etc.)
     * Sensitive settings are masked in API responses
     */
    @Column(name = "is_sensitive", nullable = false)
    private Boolean isSensitive = false;

    /**
     * Default value to use if setting is not configured or reset to default
     */
    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    /**
     * Optional regex pattern for validating setting value
     */
    @Column(name = "validation_regex", length = 255)
    private String validationRegex;

    /**
     * User ID who last updated this setting (for audit trail)
     */
    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(nullable = false)
    private Integer version = 0;

    /**
     * Setting categories for logical grouping
     */
    public enum SettingCategory {
        SYSTEM,      // General application settings
        DOMAIN,      // Domain and networking configuration
        SYNC,        // Cloud synchronization settings
        STORAGE,     // File storage configuration
        SECURITY,    // Security and authentication settings
        DATABASE     // Database backup and maintenance settings
    }

    /**
     * Supported data types for settings
     */
    public enum SettingDataType {
        STRING,      // Plain text value
        NUMBER,      // Numeric value (integer or decimal)
        BOOLEAN,     // True/false value
        JSON,        // JSON object or array
        ENCRYPTED    // Encrypted value (for passwords, API keys)
    }

    /**
     * Get the effective value (current value or default if null)
     */
    public String getEffectiveValue() {
        return value != null ? value : defaultValue;
    }

    /**
     * Check if setting has been modified from default
     */
    public boolean isModified() {
        return value != null && !value.equals(defaultValue);
    }

    /**
     * Reset setting to default value
     */
    public void resetToDefault() {
        this.value = this.defaultValue;
    }

    /**
     * Mask sensitive values in string representation
     */
    public String getMaskedValue() {
        if (isSensitive && value != null && !value.isEmpty()) {
            return "********";
        }
        return getEffectiveValue();
    }
}
