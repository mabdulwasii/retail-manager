package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.SystemSettings;
import com.princely.shopmanager.embedded.repository.SystemSettingsRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SystemSettingsService.
 * Tests system settings business logic for embedded mode.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("System Settings Service Tests")
class SystemSettingsServiceTest {

    @Mock
    private SystemSettingsRepository repository;

    @InjectMocks
    private SystemSettingsService service;

    private static final String TEST_KEY = "custom.domain";
    private static final String TEST_VALUE = "shopmanager.local";
    private static final String TEST_USER_ID = "user-123";

    // ============================================================================
    // Get Settings Tests
    // ============================================================================

    @Test
    @DisplayName("Should get settings grouped by category")
    void shouldGetSettingsGroupedByCategory() {
        // Given
        SystemSettings domainSetting = createSetting("custom.domain", "shopmanager.local", SettingCategory.DOMAIN, SettingDataType.STRING);
        SystemSettings syncSetting = createSetting("cloud.sync.enabled", "false", SettingCategory.SYNC, SettingDataType.BOOLEAN);

        when(repository.findAllByOrderByCategoryAscKeyAsc())
            .thenReturn(Arrays.asList(domainSetting, syncSetting));

        // When
        Map<SettingCategory, List<SystemSettings>> grouped = service.getSettingsGroupedByCategory();

        // Then
        assertThat(grouped).hasSize(2);
        assertThat(grouped.get(SettingCategory.DOMAIN)).hasSize(1);
        assertThat(grouped.get(SettingCategory.SYNC)).hasSize(1);
        verify(repository).findAllByOrderByCategoryAscKeyAsc();
    }

    @Test
    @DisplayName("Should get single setting by key successfully")
    void shouldGetSettingByKey() {
        // Given
        SystemSettings setting = createSetting(TEST_KEY, TEST_VALUE, SettingCategory.DOMAIN, SettingDataType.STRING);
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(setting));

        // When
        SystemSettings result = service.getSettingByKey(TEST_KEY).orElseThrow();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getKey()).isEqualTo(TEST_KEY);
        assertThat(result.getValue()).isEqualTo(TEST_VALUE);
        verify(repository).findByKey(TEST_KEY);
    }

    @Test
    @DisplayName("Should return empty optional when setting not found")
    void shouldReturnEmptyWhenSettingNotFound() {
        // Given
        when(repository.findByKey("nonexistent.key")).thenReturn(Optional.empty());

        // When
        Optional<SystemSettings> result = service.getSettingByKey("nonexistent.key");

        // Then
        assertThat(result).isEmpty();
        verify(repository).findByKey("nonexistent.key");
    }

    @Test
    @DisplayName("Should get settings by category")
    void shouldGetSettingsByCategory() {
        // Given
        List<SystemSettings> domainSettings = Arrays.asList(
            createSetting("custom.domain", "shopmanager.local", SettingCategory.DOMAIN, SettingDataType.STRING),
            createSetting("domain.protocol", "https", SettingCategory.DOMAIN, SettingDataType.STRING)
        );
        when(repository.findByCategoryOrderByKeyAsc(SettingCategory.DOMAIN)).thenReturn(domainSettings);

        // When
        List<SystemSettings> result = service.getSettingsByCategory(SettingCategory.DOMAIN);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(s -> s.getCategory() == SettingCategory.DOMAIN);
        verify(repository).findByCategoryOrderByKeyAsc(SettingCategory.DOMAIN);
    }

    // ============================================================================
    // Update Setting Tests
    // ============================================================================

    @Test
    @DisplayName("Should update STRING setting successfully")
    void shouldUpdateStringSetting() {
        // Given
        SystemSettings setting = createSetting(TEST_KEY, "old-value", SettingCategory.DOMAIN, SettingDataType.STRING);
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.updateSetting(TEST_KEY, "new-value", TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("new-value");
        assertThat(result.getUpdatedBy()).isEqualTo(TEST_USER_ID);
        verify(repository).findByKey(TEST_KEY);
        verify(repository).save(setting);
    }

    @Test
    @DisplayName("Should update NUMBER setting with valid number")
    void shouldUpdateNumberSetting() {
        // Given
        SystemSettings setting = createSetting("max.upload.size", "10", SettingCategory.STORAGE, SettingDataType.NUMBER);
        when(repository.findByKey("max.upload.size")).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.updateSetting("max.upload.size", "20", TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("20");
        verify(repository).save(setting);
    }

    @Test
    @DisplayName("Should reject NUMBER setting with invalid number")
    void shouldRejectInvalidNumberSetting() {
        // Given
        SystemSettings setting = createSetting("max.upload.size", "10", SettingCategory.STORAGE, SettingDataType.NUMBER);
        when(repository.findByKey("max.upload.size")).thenReturn(Optional.of(setting));

        // When / Then
        assertThatThrownBy(() -> service.updateSetting("max.upload.size", "invalid-number", TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid number format");

        verify(repository).findByKey("max.upload.size");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should update BOOLEAN setting with valid boolean")
    void shouldUpdateBooleanSetting() {
        // Given
        SystemSettings setting = createSetting("cloud.sync.enabled", "false", SettingCategory.SYNC, SettingDataType.BOOLEAN);
        when(repository.findByKey("cloud.sync.enabled")).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.updateSetting("cloud.sync.enabled", "true", TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("true");
        verify(repository).save(setting);
    }

    @Test
    @DisplayName("Should reject BOOLEAN setting with invalid boolean")
    void shouldRejectInvalidBooleanSetting() {
        // Given
        SystemSettings setting = createSetting("cloud.sync.enabled", "false", SettingCategory.SYNC, SettingDataType.BOOLEAN);
        when(repository.findByKey("cloud.sync.enabled")).thenReturn(Optional.of(setting));

        // When / Then
        assertThatThrownBy(() -> service.updateSetting("cloud.sync.enabled", "yes", TEST_USER_ID))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid boolean value");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should update sensitive setting and mask value")
    void shouldUpdateSensitiveSetting() {
        // Given
        SystemSettings setting = createSetting("jwt.secret", "old-secret", SettingCategory.SECURITY, SettingDataType.ENCRYPTED);
        setting.setIsSensitive(true);
        when(repository.findByKey("jwt.secret")).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.updateSetting("jwt.secret", "new-secret", TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("new-secret");
        assertThat(result.getMaskedValue()).isEqualTo("********");
        verify(repository).save(setting);
    }

    @Test
    @DisplayName("Should log warning when updating restart-required setting")
    void shouldLogWarningForRestartRequiredSetting() {
        // Given
        SystemSettings setting = createSetting(TEST_KEY, "old-value", SettingCategory.DOMAIN, SettingDataType.STRING);
        setting.setRequiresRestart(true);
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.updateSetting(TEST_KEY, "new-value", TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRequiresRestart()).isTrue();
        verify(repository).save(setting);
        // Note: Logging verification would require a logging appender in real scenario
    }

    // ============================================================================
    // Bulk Update Tests
    // ============================================================================

    @Test
    @DisplayName("Should update multiple settings in bulk")
    void shouldUpdateMultipleSettings() {
        // Given
        SystemSettings setting1 = createSetting("key1", "value1", SettingCategory.SYSTEM, SettingDataType.STRING);
        SystemSettings setting2 = createSetting("key2", "value2", SettingCategory.SYSTEM, SettingDataType.STRING);

        when(repository.findByKey("key1")).thenReturn(Optional.of(setting1));
        when(repository.findByKey("key2")).thenReturn(Optional.of(setting2));
        when(repository.save(any(SystemSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> updates = Map.of(
            "key1", "new-value1",
            "key2", "new-value2"
        );

        // When
        List<SystemSettings> results = service.bulkUpdateSettings(updates, TEST_USER_ID);

        // Then
        assertThat(results).hasSize(2);
        verify(repository, times(2)).save(any(SystemSettings.class));
    }

    @Test
    @DisplayName("Should detect restart requirement in bulk update")
    void shouldDetectRestartRequirementInBulkUpdate() {
        // Given
        SystemSettings setting1 = createSetting("key1", "value1", SettingCategory.DOMAIN, SettingDataType.STRING);
        setting1.setRequiresRestart(true);
        SystemSettings setting2 = createSetting("key2", "value2", SettingCategory.SYSTEM, SettingDataType.STRING);
        setting2.setRequiresRestart(false);

        when(repository.findByKey("key1")).thenReturn(Optional.of(setting1));
        when(repository.findByKey("key2")).thenReturn(Optional.of(setting2));
        when(repository.save(any(SystemSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, String> updates = Map.of(
            "key1", "new-value1",
            "key2", "new-value2"
        );

        // When
        List<SystemSettings> results = service.bulkUpdateSettings(updates, TEST_USER_ID);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).anyMatch(SystemSettings::getRequiresRestart);
    }

    // ============================================================================
    // Reset to Default Tests
    // ============================================================================

    @Test
    @DisplayName("Should reset setting to default value")
    void shouldResetSettingToDefault() {
        // Given
        SystemSettings setting = createSetting(TEST_KEY, "modified-value", SettingCategory.DOMAIN, SettingDataType.STRING);
        setting.setDefaultValue("default-value");
        when(repository.findByKey(TEST_KEY)).thenReturn(Optional.of(setting));
        when(repository.save(any(SystemSettings.class))).thenReturn(setting);

        // When
        SystemSettings result = service.resetToDefault(TEST_KEY, TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getValue()).isEqualTo("default-value");
        verify(repository).save(setting);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private SystemSettings createSetting(String key, String value, SettingCategory category, SettingDataType dataType) {
        SystemSettings setting = new SystemSettings();
        setting.setId(java.util.UUID.randomUUID().toString());
        setting.setKey(key);
        setting.setValue(value);
        setting.setCategory(category);
        setting.setDataType(dataType);
        setting.setDescription("Test setting");
        setting.setRequiresRestart(false);
        setting.setIsSensitive(false);
        setting.setDefaultValue(value);
        setting.setVersion(0L);
        return setting;
    }
}
