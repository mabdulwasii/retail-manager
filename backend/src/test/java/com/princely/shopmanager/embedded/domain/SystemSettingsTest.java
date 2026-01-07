package com.princely.shopmanager.embedded.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import static org.assertj.core.api.Assertions.*;

/**
 * Entity tests for SystemSettings.
 * Tests entity methods and value masking logic.
 */
@DisplayName("System Settings Entity Tests")
class SystemSettingsTest {

    @Test
    @DisplayName("Should mask sensitive values with getMaskedValue()")
    void shouldMaskSensitiveValues() {
        // Given
        SystemSettings setting = createSetting("jwt.secret", "my-secret-key-12345", true);

        // When
        String maskedValue = setting.getMaskedValue();

        // Then
        assertThat(maskedValue).isEqualTo("********");
        assertThat(setting.getValue()).isEqualTo("my-secret-key-12345"); // Original value unchanged
    }

    @Test
    @DisplayName("Should return plain value for non-sensitive settings with getMaskedValue()")
    void shouldReturnPlainValueForNonSensitiveSettings() {
        // Given
        SystemSettings setting = createSetting("custom.domain", "shopmanager.local", false);

        // When
        String maskedValue = setting.getMaskedValue();

        // Then
        assertThat(maskedValue).isEqualTo("shopmanager.local");
        assertThat(setting.getValue()).isEqualTo("shopmanager.local");
    }

    @Test
    @DisplayName("Should return effective value or default with getEffectiveValue()")
    void shouldReturnEffectiveValueOrDefault() {
        // Given - Setting with value
        SystemSettings settingWithValue = createSetting("key1", "actual-value", false);
        settingWithValue.setDefaultValue("default-value");

        // Given - Setting with null value
        SystemSettings settingWithNullValue = createSetting("key2", null, false);
        settingWithNullValue.setDefaultValue("default-value");

        // Given - Setting with empty value
        SystemSettings settingWithEmptyValue = createSetting("key3", "", false);
        settingWithEmptyValue.setDefaultValue("default-value");

        // When / Then
        assertThat(settingWithValue.getEffectiveValue()).isEqualTo("actual-value");
        assertThat(settingWithNullValue.getEffectiveValue()).isEqualTo("default-value");
        assertThat(settingWithEmptyValue.getEffectiveValue()).isEqualTo("default-value");
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private SystemSettings createSetting(String key, String value, boolean isSensitive) {
        SystemSettings setting = new SystemSettings();
        setting.setKey(key);
        setting.setValue(value);
        setting.setCategory(SettingCategory.SYSTEM);
        setting.setDataType(SettingDataType.STRING);
        setting.setDescription("Test setting");
        setting.setRequiresRestart(false);
        setting.setIsSensitive(isSensitive);
        setting.setDefaultValue("default-value");
        setting.setVersion(0);
        return setting;
    }
}
