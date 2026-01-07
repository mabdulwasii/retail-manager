package com.princely.shopmanager.embedded.repository;

import com.princely.shopmanager.embedded.domain.SystemSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingCategory;
import static com.princely.shopmanager.embedded.domain.SystemSettings.SettingDataType;
import static org.assertj.core.api.Assertions.*;

/**
 * Repository tests for SystemSettingsRepository.
 * Tests data access layer for system settings.
 */
@DataJpaTest
@ActiveProfiles("embedded")
@DisplayName("System Settings Repository Tests")
class SystemSettingsRepositoryTest {

    @Autowired
    private SystemSettingsRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should find all settings ordered by category and key")
    void shouldFindAllOrderedByCategoryAndKey() {
        // Given
        SystemSettings setting1 = createSetting("z.key", "value1", SettingCategory.SYNC, SettingDataType.STRING);
        SystemSettings setting2 = createSetting("a.key", "value2", SettingCategory.DOMAIN, SettingDataType.STRING);
        SystemSettings setting3 = createSetting("m.key", "value3", SettingCategory.DOMAIN, SettingDataType.STRING);

        repository.save(setting1);
        repository.save(setting2);
        repository.save(setting3);

        // When
        List<SystemSettings> results = repository.findAllByOrderByCategoryAscKeyAsc();

        // Then
        assertThat(results).hasSize(3);
        // First should be DOMAIN (comes before SYNC alphabetically)
        assertThat(results.get(0).getCategory()).isEqualTo(SettingCategory.DOMAIN);
        assertThat(results.get(0).getKey()).isEqualTo("a.key");
        // Second should be DOMAIN with key "m.key"
        assertThat(results.get(1).getCategory()).isEqualTo(SettingCategory.DOMAIN);
        assertThat(results.get(1).getKey()).isEqualTo("m.key");
        // Third should be SYNC
        assertThat(results.get(2).getCategory()).isEqualTo(SettingCategory.SYNC);
    }

    @Test
    @DisplayName("Should find setting by key")
    void shouldFindByKey() {
        // Given
        SystemSettings setting = createSetting("test.key", "test.value", SettingCategory.SYSTEM, SettingDataType.STRING);
        repository.save(setting);

        // When
        Optional<SystemSettings> result = repository.findByKey("test.key");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getKey()).isEqualTo("test.key");
        assertThat(result.get().getValue()).isEqualTo("test.value");
    }

    @Test
    @DisplayName("Should return empty when key not found")
    void shouldReturnEmptyWhenKeyNotFound() {
        // When
        Optional<SystemSettings> result = repository.findByKey("nonexistent.key");

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find settings by category")
    void shouldFindByCategory() {
        // Given
        SystemSettings domainSetting1 = createSetting("domain.key1", "value1", SettingCategory.DOMAIN, SettingDataType.STRING);
        SystemSettings domainSetting2 = createSetting("domain.key2", "value2", SettingCategory.DOMAIN, SettingDataType.STRING);
        SystemSettings syncSetting = createSetting("sync.key", "value3", SettingCategory.SYNC, SettingDataType.STRING);

        repository.save(domainSetting1);
        repository.save(domainSetting2);
        repository.save(syncSetting);

        // When
        List<SystemSettings> domainResults = repository.findByCategory(SettingCategory.DOMAIN);
        List<SystemSettings> syncResults = repository.findByCategory(SettingCategory.SYNC);

        // Then
        assertThat(domainResults).hasSize(2);
        assertThat(domainResults).allMatch(s -> s.getCategory() == SettingCategory.DOMAIN);

        assertThat(syncResults).hasSize(1);
        assertThat(syncResults.get(0).getCategory()).isEqualTo(SettingCategory.SYNC);
    }

    @Test
    @DisplayName("Should find settings that require restart")
    void shouldFindSettingsThatRequireRestart() {
        // Given
        SystemSettings restartSetting1 = createSetting("restart.key1", "value1", SettingCategory.DOMAIN, SettingDataType.STRING);
        restartSetting1.setRequiresRestart(true);

        SystemSettings restartSetting2 = createSetting("restart.key2", "value2", SettingCategory.SECURITY, SettingDataType.STRING);
        restartSetting2.setRequiresRestart(true);

        SystemSettings normalSetting = createSetting("normal.key", "value3", SettingCategory.SYSTEM, SettingDataType.STRING);
        normalSetting.setRequiresRestart(false);

        repository.save(restartSetting1);
        repository.save(restartSetting2);
        repository.save(normalSetting);

        // When
        List<SystemSettings> results = repository.findByRequiresRestart(true);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(SystemSettings::getRequiresRestart);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private SystemSettings createSetting(String key, String value, SettingCategory category, SettingDataType dataType) {
        SystemSettings setting = new SystemSettings();
        setting.setKey(key);
        setting.setValue(value);
        setting.setCategory(category);
        setting.setDataType(dataType);
        setting.setDescription("Test setting");
        setting.setRequiresRestart(false);
        setting.setIsSensitive(false);
        setting.setDefaultValue(value);
        setting.setVersion(0);
        return setting;
    }
}
