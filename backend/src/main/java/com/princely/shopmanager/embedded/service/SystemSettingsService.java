package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.SystemSettings;
import com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import com.princely.shopmanager.embedded.repository.SystemSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * System Settings Service (Embedded Mode Only)
 *
 * Provides centralized management of system-wide configuration settings
 * stored in the database for Docker Lite deployments.
 *
 * NOTE: This service is ONLY active in embedded mode (@Profile("embedded")).
 * In cloud mode, configuration is managed via Kubernetes ConfigMaps and Secrets.
 *
 * Features:
 * - Category-based settings retrieval
 * - Data type validation
 * - Sensitive data masking
 * - Regex validation
 * - Restart requirement tracking
 *
 * @see com.princely.shopmanager.embedded.domain.SystemSettings
 * @see com.princely.shopmanager.embedded.controller.SystemSettingsController
 * @author Claude Code
 * @since 1.0.0
 */
@Service
@Profile("embedded")  // ← CRITICAL: Only loads in embedded mode
@RequiredArgsConstructor
@Slf4j
public class SystemSettingsService {

    private final SystemSettingsRepository repository;

    /**
     * Get all system settings
     *
     * @return List of all settings
     */
    @Transactional(readOnly = true)
    public List<SystemSettings> getAllSettings() {
        log.debug("Fetching all system settings");
        return repository.findAllByOrderByCategoryAscKeyAsc();
    }

    /**
     * Get all settings grouped by category
     *
     * @return Map of category to list of settings
     */
    @Transactional(readOnly = true)
    public Map<SettingCategory, List<SystemSettings>> getSettingsGroupedByCategory() {
        log.debug("Fetching settings grouped by category");
        return repository.findAllByOrderByCategoryAscKeyAsc()
            .stream()
            .collect(Collectors.groupingBy(SystemSettings::getCategory));
    }

    /**
     * Get settings by category
     *
     * @param category Setting category
     * @return List of settings in the category
     */
    @Transactional(readOnly = true)
    public List<SystemSettings> getSettingsByCategory(SettingCategory category) {
        log.debug("Fetching settings for category: {}", category);
        return repository.findByCategoryOrderByKeyAsc(category);
    }

    /**
     * Get a single setting by key
     *
     * @param key Setting key
     * @return Optional containing the setting if found
     */
    @Transactional(readOnly = true)
    public Optional<SystemSettings> getSettingByKey(String key) {
        log.debug("Fetching setting by key: {}", key);
        return repository.findByKey(key);
    }

    /**
     * Get setting value by key (returns effective value: current or default)
     *
     * @param key Setting key
     * @return Setting value or null if not found
     */
    @Transactional(readOnly = true)
    public String getSettingValue(String key) {
        return repository.findByKey(key)
            .map(SystemSettings::getEffectiveValue)
            .orElse(null);
    }

    /**
     * Get setting value by key with fallback default
     *
     * @param key          Setting key
     * @param defaultValue Fallback default value if setting not found
     * @return Setting value or provided default
     */
    @Transactional(readOnly = true)
    public String getSettingValue(String key, String defaultValue) {
        return repository.findByKey(key)
            .map(SystemSettings::getEffectiveValue)
            .orElse(defaultValue);
    }

    /**
     * Get setting value as Boolean
     *
     * @param key Setting key
     * @return Boolean value or null if not found or not boolean
     */
    @Transactional(readOnly = true)
    public Boolean getBooleanValue(String key) {
        return repository.findByKey(key)
            .filter(s -> s.getDataType() == SettingDataType.BOOLEAN)
            .map(s -> Boolean.parseBoolean(s.getEffectiveValue()))
            .orElse(null);
    }

    /**
     * Get setting value as Integer
     *
     * @param key Setting key
     * @return Integer value or null if not found or not numeric
     */
    @Transactional(readOnly = true)
    public Integer getIntegerValue(String key) {
        return repository.findByKey(key)
            .filter(s -> s.getDataType() == SettingDataType.NUMBER)
            .map(s -> {
                try {
                    return Integer.parseInt(s.getEffectiveValue());
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse setting {} as integer: {}", key, s.getEffectiveValue());
                    return null;
                }
            })
            .orElse(null);
    }

    /**
     * Get setting value as Long
     *
     * @param key Setting key
     * @return Long value or null if not found or not numeric
     */
    @Transactional(readOnly = true)
    public Long getLongValue(String key) {
        return repository.findByKey(key)
            .filter(s -> s.getDataType() == SettingDataType.NUMBER)
            .map(s -> {
                try {
                    return Long.parseLong(s.getEffectiveValue());
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse setting {} as long: {}", key, s.getEffectiveValue());
                    return null;
                }
            })
            .orElse(null);
    }

    /**
     * Update a setting value
     *
     * @param key      Setting key
     * @param value    New value
     * @param updatedBy User ID who is updating the setting
     * @return Updated setting
     * @throws IllegalArgumentException if setting not found or validation fails
     */
    @Transactional
    public SystemSettings updateSetting(String key, String value, String updatedBy) {
        log.info("Updating setting {} by user {}", key, updatedBy);

        SystemSettings setting = repository.findByKey(key)
            .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        // Validate value
        validateSettingValue(setting, value);

        // Update value and metadata
        setting.setValue(value);
        setting.setUpdatedBy(updatedBy);

        SystemSettings saved = repository.save(setting);

        // Log warning if restart required
        if (setting.getRequiresRestart()) {
            log.warn("Setting {} updated. Application restart required for changes to take effect.", key);
        }

        return saved;
    }

    /**
     * Bulk update multiple settings
     *
     * @param updates   Map of setting key to new value
     * @param updatedBy User ID who is updating the settings
     * @return List of updated settings
     */
    @Transactional
    public List<SystemSettings> bulkUpdateSettings(Map<String, String> updates, String updatedBy) {
        log.info("Bulk updating {} settings by user {}", updates.size(), updatedBy);

        return updates.entrySet().stream()
            .map(entry -> updateSetting(entry.getKey(), entry.getValue(), updatedBy))
            .collect(Collectors.toList());
    }

    /**
     * Reset a setting to its default value
     *
     * @param key      Setting key
     * @param updatedBy User ID who is resetting the setting
     * @return Reset setting
     */
    @Transactional
    public SystemSettings resetToDefault(String key, String updatedBy) {
        log.info("Resetting setting {} to default by user {}", key, updatedBy);

        SystemSettings setting = repository.findByKey(key)
            .orElseThrow(() -> new IllegalArgumentException("Setting not found: " + key));

        setting.resetToDefault();
        setting.setUpdatedBy(updatedBy);

        return repository.save(setting);
    }

    /**
     * Get all settings that require restart
     *
     * @return List of settings requiring restart
     */
    @Transactional(readOnly = true)
    public List<SystemSettings> getSettingsRequiringRestart() {
        return repository.findByRequiresRestart(true);
    }

    /**
     * Get all modified settings (different from default)
     *
     * @return List of modified settings
     */
    @Transactional(readOnly = true)
    public List<SystemSettings> getModifiedSettings() {
        return repository.findAllModifiedSettings();
    }

    /**
     * Search settings by keyword
     *
     * @param searchTerm Search term
     * @return List of matching settings
     */
    @Transactional(readOnly = true)
    public List<SystemSettings> searchSettings(String searchTerm) {
        log.debug("Searching settings with term: {}", searchTerm);
        return repository.searchSettings(searchTerm);
    }

    /**
     * Check if a setting exists
     *
     * @param key Setting key
     * @return true if setting exists
     */
    @Transactional(readOnly = true)
    public boolean settingExists(String key) {
        return repository.existsByKey(key);
    }

    /**
     * Validate setting value against data type and regex
     *
     * @param setting Setting to validate
     * @param value   Value to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateSettingValue(SystemSettings setting, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Setting value cannot be null");
        }

        // Validate against data type
        switch (setting.getDataType()) {
            case NUMBER:
                try {
                    Long.parseLong(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number format for setting: " + setting.getKey());
                }
                break;
            case BOOLEAN:
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Invalid boolean value for setting: " + setting.getKey());
                }
                break;
            case JSON:
                // TODO: Add JSON validation if needed
                break;
            case STRING:
            case ENCRYPTED:
            default:
                // No special validation for strings
                break;
        }

        // Validate against regex if provided
        if (setting.getValidationRegex() != null && !setting.getValidationRegex().isEmpty()) {
            Pattern pattern = Pattern.compile(setting.getValidationRegex());
            if (!pattern.matcher(value).matches()) {
                throw new IllegalArgumentException(
                    String.format("Value does not match validation pattern for setting %s: %s",
                        setting.getKey(), setting.getValidationRegex())
                );
            }
        }
    }

    /**
     * Check if embedded mode is active (always true since this service is @Profile("embedded"))
     *
     * @return true
     */
    public boolean isEmbeddedMode() {
        return true;
    }
}
