package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.repository.CloudSyncConfigRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CloudSyncConfigurationService.
 * Tests cloud sync configuration management without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudSyncConfigurationService Unit Tests")
class CloudSyncConfigurationServiceTest {

    @Mock
    private CloudSyncConfigRepository cloudSyncConfigRepository;

    @Mock
    private TextEncryptor textEncryptor;

    @InjectMocks
    private CloudSyncConfigurationService cloudSyncConfigurationService;

    private static final String TEST_TENANT_ID = "test-tenant-123";
    private static final String TEST_CLOUD_TENANT_ID = "cloud-tenant-456";
    private static final String TEST_API_KEY = "test-api-key-789";
    private static final String ENCRYPTED_API_KEY = "encrypted-api-key-xyz";
    private static final String TEST_API_URL = "https://cloud.test.com/api";

    // ============== getConfigByTenantId Tests ==============

    @Test
    @DisplayName("getConfigByTenantId should return config with decrypted API key when found")
    void getConfigByTenantIdShouldReturnConfigWhenFound() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);

        // When
        Optional<CloudSyncConfig> result = cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCloudApiKey()).isEqualTo(TEST_API_KEY);
        verify(cloudSyncConfigRepository).findByTenantId(TEST_TENANT_ID);
        verify(textEncryptor).decrypt(ENCRYPTED_API_KEY);
    }

    @Test
    @DisplayName("getConfigByTenantId should return empty when config not found")
    void getConfigByTenantIdShouldReturnEmptyWhenNotFound() {
        // Given
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When
        Optional<CloudSyncConfig> result = cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID);

        // Then
        assertThat(result).isEmpty();
        verify(cloudSyncConfigRepository).findByTenantId(TEST_TENANT_ID);
        verify(textEncryptor, never()).decrypt(any());
    }

    @Test
    @DisplayName("getConfigByTenantId should handle decryption failure gracefully")
    void getConfigByTenantIdShouldHandleDecryptionFailure() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenThrow(new RuntimeException("Decryption failed"));

        // When
        Optional<CloudSyncConfig> result = cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCloudApiKey()).isNull(); // Key cleared on decryption failure
    }

    // ============== getConfigByTenantIdOrThrow Tests ==============

    @Test
    @DisplayName("getConfigByTenantIdOrThrow should return config when found")
    void getConfigByTenantIdOrThrowShouldReturnConfigWhenFound() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCloudApiKey()).isEqualTo(TEST_API_KEY);
    }

    @Test
    @DisplayName("getConfigByTenantIdOrThrow should throw exception when config not found")
    void getConfigByTenantIdOrThrowShouldThrowWhenNotFound() {
        // Given
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> cloudSyncConfigurationService.getConfigByTenantIdOrThrow(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);
    }

    // ============== isConfigured Tests ==============

    @Test
    @DisplayName("isConfigured should return true when config is configured")
    void isConfiguredShouldReturnTrueWhenConfigured() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));

        // When
        boolean result = cloudSyncConfigurationService.isConfigured(TEST_TENANT_ID);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isConfigured should return false when config not found")
    void isConfiguredShouldReturnFalseWhenNotFound() {
        // Given
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When
        boolean result = cloudSyncConfigurationService.isConfigured(TEST_TENANT_ID);

        // Then
        assertThat(result).isFalse();
    }

    // ============== isActive Tests ==============

    @Test
    @DisplayName("isActive should return true when sync is enabled and configured")
    void isActiveShouldReturnTrueWhenEnabledAndConfigured() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncEnabled(true);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));

        // When
        boolean result = cloudSyncConfigurationService.isActive(TEST_TENANT_ID);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isActive should return false when sync is disabled")
    void isActiveShouldReturnFalseWhenDisabled() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncEnabled(false);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));

        // When
        boolean result = cloudSyncConfigurationService.isActive(TEST_TENANT_ID);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isActive should return false when config not found")
    void isActiveShouldReturnFalseWhenNotFound() {
        // Given
        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.empty());

        // When
        boolean result = cloudSyncConfigurationService.isActive(TEST_TENANT_ID);

        // Then
        assertThat(result).isFalse();
    }

    // ============== saveConfiguration Tests ==============

    @Test
    @DisplayName("saveConfiguration should encrypt API key before saving")
    void saveConfigurationShouldEncryptApiKey() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(TEST_API_KEY);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED);

        when(textEncryptor.encrypt(TEST_API_KEY)).thenReturn(ENCRYPTED_API_KEY);
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.saveConfiguration(config);

        // Then
        assertThat(config.getSyncStatus()).isEqualTo(CloudSyncConfig.SyncStatus.CONFIGURED);
        assertThat(result.getCloudApiKey()).isEqualTo(TEST_API_KEY); // Returned decrypted
        verify(textEncryptor).encrypt(TEST_API_KEY);
        verify(textEncryptor).decrypt(ENCRYPTED_API_KEY);
    }

    @Test
    @DisplayName("saveConfiguration should set status to CONFIGURED when credentials provided")
    void saveConfigurationShouldSetConfiguredStatus() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId(TEST_CLOUD_TENANT_ID)
                .cloudApiKey(TEST_API_KEY)
                .cloudApiUrl(TEST_API_URL)
                .syncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED)
                .build();

        when(textEncryptor.encrypt(TEST_API_KEY)).thenReturn(ENCRYPTED_API_KEY);
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        cloudSyncConfigurationService.saveConfiguration(config);

        // Then
        assertThat(config.getSyncStatus()).isEqualTo(CloudSyncConfig.SyncStatus.CONFIGURED);
    }

    // ============== enableSync Tests ==============

    @Test
    @DisplayName("enableSync should enable sync when config is configured")
    void enableSyncShouldEnableSyncWhenConfigured() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncEnabled(false);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.CONFIGURED);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.enableSync(TEST_TENANT_ID);

        // Then
        assertThat(result.getSyncEnabled()).isTrue();
        verify(cloudSyncConfigRepository).save(config);
    }

    @Test
    @DisplayName("enableSync should throw exception when config not configured")
    void enableSyncShouldThrowWhenNotConfigured() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .syncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED)
                .build();

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));

        // When/Then
        assertThatThrownBy(() -> cloudSyncConfigurationService.enableSync(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);
    }

    // ============== disableSync Tests ==============

    @Test
    @DisplayName("disableSync should disable sync")
    void disableSyncShouldDisableSync() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncEnabled(true);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.disableSync(TEST_TENANT_ID);

        // Then
        assertThat(result.getSyncEnabled()).isFalse();
        verify(cloudSyncConfigRepository).save(config);
    }

    // ============== updateApiKey Tests ==============

    @Test
    @DisplayName("updateApiKey should update and encrypt new API key")
    void updateApiKeyShouldUpdateAndEncrypt() {
        // Given
        String newApiKey = "new-api-key";
        String newEncryptedKey = "new-encrypted-key";
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.encrypt(newApiKey)).thenReturn(newEncryptedKey);
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(textEncryptor.decrypt(newEncryptedKey)).thenReturn(newApiKey);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.updateApiKey(TEST_TENANT_ID, newApiKey);

        // Then
        assertThat(result.getCloudApiKey()).isEqualTo(newApiKey); // Returned decrypted
        assertThat(result.getSyncStatus()).isEqualTo(CloudSyncConfig.SyncStatus.CONFIGURED);
        verify(textEncryptor).encrypt(newApiKey);
    }

    // ============== updateApiUrl Tests ==============

    @Test
    @DisplayName("updateApiUrl should update cloud API URL")
    void updateApiUrlShouldUpdateUrl() {
        // Given
        String newApiUrl = "https://new-cloud.test.com/api";
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        CloudSyncConfig result = cloudSyncConfigurationService.updateApiUrl(TEST_TENANT_ID, newApiUrl);

        // Then
        assertThat(result.getCloudApiUrl()).isEqualTo(newApiUrl);
        verify(cloudSyncConfigRepository).save(config);
    }

    // ============== markSyncSuccess Tests ==============

    @Test
    @DisplayName("markSyncSuccess should update sync status and clear errors")
    void markSyncSuccessShouldUpdateStatus() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.ERROR);
        config.setLastError("Previous error");

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        cloudSyncConfigurationService.markSyncSuccess(TEST_TENANT_ID);

        // Then
        assertThat(config.getSyncStatus()).isEqualTo(CloudSyncConfig.SyncStatus.CONFIGURED);
        assertThat(config.getLastError()).isNull();
        assertThat(config.getLastSyncAt()).isNotNull();
        verify(cloudSyncConfigRepository).save(config);
    }

    // ============== markSyncFailed Tests ==============

    @Test
    @DisplayName("markSyncFailed should update status and record error")
    void markSyncFailedShouldUpdateStatus() {
        // Given
        String errorMessage = "Connection timeout";
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);
        config.setSyncStatus(CloudSyncConfig.SyncStatus.SYNCING);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);
        when(cloudSyncConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // When
        cloudSyncConfigurationService.markSyncFailed(TEST_TENANT_ID, errorMessage);

        // Then
        assertThat(config.getSyncStatus()).isEqualTo(CloudSyncConfig.SyncStatus.ERROR);
        assertThat(config.getLastError()).isEqualTo(errorMessage);
        verify(cloudSyncConfigRepository).save(config);
    }

    // ============== getAllActiveConfigurations Tests ==============

    @Test
    @DisplayName("getAllActiveConfigurations should return decrypted configs")
    void getAllActiveConfigurationsShouldReturnDecryptedConfigs() {
        // Given
        CloudSyncConfig config1 = createCloudSyncConfig("encrypted-1");
        config1.setSyncEnabled(true);
        CloudSyncConfig config2 = createCloudSyncConfig("encrypted-2");
        config2.setSyncEnabled(true);

        when(cloudSyncConfigRepository.findAllActive()).thenReturn(List.of(config1, config2));
        when(textEncryptor.decrypt("encrypted-1")).thenReturn("decrypted-1");
        when(textEncryptor.decrypt("encrypted-2")).thenReturn("decrypted-2");

        // When
        List<CloudSyncConfig> result = cloudSyncConfigurationService.getAllActiveConfigurations();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCloudApiKey()).isEqualTo("decrypted-1");
        assertThat(result.get(1).getCloudApiKey()).isEqualTo("decrypted-2");
        verify(cloudSyncConfigRepository).findAllActive();
    }

    // ============== deleteConfiguration Tests ==============

    @Test
    @DisplayName("deleteConfiguration should delete config")
    void deleteConfigurationShouldDeleteConfig() {
        // Given
        CloudSyncConfig config = createCloudSyncConfig(ENCRYPTED_API_KEY);

        when(cloudSyncConfigRepository.findByTenantId(TEST_TENANT_ID)).thenReturn(Optional.of(config));
        when(textEncryptor.decrypt(ENCRYPTED_API_KEY)).thenReturn(TEST_API_KEY);

        // When
        cloudSyncConfigurationService.deleteConfiguration(TEST_TENANT_ID);

        // Then
        verify(cloudSyncConfigRepository).delete(config);
    }

    // Helper methods

    private CloudSyncConfig createCloudSyncConfig(String apiKey) {
        return CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId(TEST_CLOUD_TENANT_ID)
                .cloudApiKey(apiKey)
                .cloudApiUrl(TEST_API_URL)
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .lastSyncAt(LocalDateTime.now().minusHours(1))
                .build();
    }
}
