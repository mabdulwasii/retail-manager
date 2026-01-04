package com.princely.shopmanager.embedded.service;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionValidationService.
 * Tests transaction validation against cloud sync requirements.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionValidationService Unit Tests")
class TransactionValidationServiceTest {

    @Mock
    private CloudSyncConfigurationService cloudSyncConfigurationService;

    @InjectMocks
    private TransactionValidationService transactionValidationService;

    private static final String TEST_TENANT_ID = "test-tenant-123";

    // ============== validateCloudSyncForTransaction Tests ==============

    @Test
    @DisplayName("validateCloudSyncForTransaction should skip validation when sync not required")
    void validateCloudSyncForTransactionShouldSkipWhenNotRequired() {
        // Given
        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", false);

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .doesNotThrowAnyException();

        verify(cloudSyncConfigurationService, never()).getConfigByTenantId(any());
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should allow when config null and offline mode enabled")
    void validateCloudSyncForTransactionShouldAllowWhenConfigNullAndOfflineMode() {
        // Given
        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", true);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.empty());

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should throw when config null and offline mode disabled")
    void validateCloudSyncForTransactionShouldThrowWhenConfigNullAndNoOfflineMode() {
        // Given
        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_REQUIRED);
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should throw when config not fully configured")
    void validateCloudSyncForTransactionShouldThrowWhenNotConfigured() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .syncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then
        assertThatThrownBy(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_NOT_CONFIGURED);
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should allow when sync disabled and offline mode enabled")
    void validateCloudSyncForTransactionShouldAllowWhenSyncDisabledAndOfflineMode() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", true);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should throw when sync disabled and offline mode disabled")
    void validateCloudSyncForTransactionShouldThrowWhenSyncDisabledAndNoOfflineMode() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then
        assertThatThrownBy(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_REQUIRED);
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should allow when in ERROR state and offline mode enabled")
    void validateCloudSyncForTransactionShouldAllowWhenErrorStateAndOfflineMode() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.ERROR)
                .lastError("Connection failed")
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", true);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should throw when in ERROR state and offline mode disabled")
    void validateCloudSyncForTransactionShouldThrowWhenErrorStateAndNoOfflineMode() {
        // Given
        String errorMessage = "Connection timeout";
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.ERROR)
                .lastError(errorMessage)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then
        assertThatThrownBy(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CLOUD_SYNC_UNAVAILABLE);
    }

    @Test
    @DisplayName("validateCloudSyncForTransaction should pass when fully configured and enabled")
    void validateCloudSyncForTransactionShouldPassWhenValid() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForTransaction(TEST_TENANT_ID))
                .doesNotThrowAnyException();
    }

    // ============== validateCloudSyncForBatch Tests ==============

    @Test
    @DisplayName("validateCloudSyncForBatch should delegate to validateCloudSyncForTransaction")
    void validateCloudSyncForBatchShouldDelegate() {
        // Given
        int transactionCount = 100;
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        ReflectionTestUtils.setField(transactionValidationService, "cloudSyncRequired", true);
        ReflectionTestUtils.setField(transactionValidationService, "allowOfflineMode", false);
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When/Then - should not throw
        assertThatCode(() -> transactionValidationService.validateCloudSyncForBatch(TEST_TENANT_ID, transactionCount))
                .doesNotThrowAnyException();

        verify(cloudSyncConfigurationService).getConfigByTenantId(TEST_TENANT_ID);
    }

    // ============== isCloudSyncConfigured Tests ==============

    @Test
    @DisplayName("isCloudSyncConfigured should delegate to cloudSyncConfigurationService")
    void isCloudSyncConfiguredShouldDelegate() {
        // Given
        when(cloudSyncConfigurationService.isConfigured(TEST_TENANT_ID)).thenReturn(true);

        // When
        boolean result = transactionValidationService.isCloudSyncConfigured(TEST_TENANT_ID);

        // Then
        assertThat(result).isTrue();
        verify(cloudSyncConfigurationService).isConfigured(TEST_TENANT_ID);
    }

    // ============== isCloudSyncActive Tests ==============

    @Test
    @DisplayName("isCloudSyncActive should delegate to cloudSyncConfigurationService")
    void isCloudSyncActiveShouldDelegate() {
        // Given
        when(cloudSyncConfigurationService.isActive(TEST_TENANT_ID)).thenReturn(true);

        // When
        boolean result = transactionValidationService.isCloudSyncActive(TEST_TENANT_ID);

        // Then
        assertThat(result).isTrue();
        verify(cloudSyncConfigurationService).isActive(TEST_TENANT_ID);
    }

    // ============== getCloudSyncStatus Tests ==============

    @Test
    @DisplayName("getCloudSyncStatus should return NOT_CONFIGURED when config is null")
    void getCloudSyncStatusShouldReturnNotConfiguredWhenNull() {
        // Given
        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.empty());

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isFalse();
        assertThat(result.active()).isFalse();
        assertThat(result.status()).isEqualTo("NOT_CONFIGURED");
        assertThat(result.message()).contains("not been set up");
    }

    @Test
    @DisplayName("getCloudSyncStatus should return correct status for CONFIGURED and enabled")
    void getCloudSyncStatusShouldReturnConfiguredAndEnabled() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isTrue();
        assertThat(result.active()).isTrue();
        assertThat(result.status()).isEqualTo("CONFIGURED");
        assertThat(result.message()).contains("active and ready");
    }

    @Test
    @DisplayName("getCloudSyncStatus should return correct status for CONFIGURED but disabled")
    void getCloudSyncStatusShouldReturnConfiguredButDisabled() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(false)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isTrue();
        assertThat(result.active()).isFalse();
        assertThat(result.status()).isEqualTo("CONFIGURED");
        assertThat(result.message()).contains("configured but disabled");
    }

    @Test
    @DisplayName("getCloudSyncStatus should return ERROR status with error message")
    void getCloudSyncStatusShouldReturnErrorStatus() {
        // Given
        String errorMessage = "Network error";
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.ERROR)
                .lastError(errorMessage)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isTrue();
        assertThat(result.active()).isTrue(); // ERROR status still counts as configured
        assertThat(result.status()).isEqualTo("ERROR");
        assertThat(result.message()).contains("error").contains(errorMessage);
    }

    @Test
    @DisplayName("getCloudSyncStatus should return SYNCING status")
    void getCloudSyncStatusShouldReturnSyncingStatus() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .cloudTenantId("cloud-123")
                .cloudApiKey("key-123")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.SYNCING)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isTrue();
        assertThat(result.active()).isTrue();
        assertThat(result.status()).isEqualTo("SYNCING");
        assertThat(result.message()).contains("in progress");
    }

    @Test
    @DisplayName("getCloudSyncStatus should return NOT_CONFIGURED status message")
    void getCloudSyncStatusShouldReturnNotConfiguredMessage() {
        // Given
        CloudSyncConfig config = CloudSyncConfig.builder()
                .tenantId(TEST_TENANT_ID)
                .syncStatus(CloudSyncConfig.SyncStatus.NOT_CONFIGURED)
                .build();

        when(cloudSyncConfigurationService.getConfigByTenantId(TEST_TENANT_ID))
                .thenReturn(Optional.of(config));

        // When
        TransactionValidationService.CloudSyncStatus result =
                transactionValidationService.getCloudSyncStatus(TEST_TENANT_ID);

        // Then
        assertThat(result.configured()).isFalse();
        assertThat(result.active()).isFalse();
        assertThat(result.status()).isEqualTo("NOT_CONFIGURED");
        assertThat(result.message()).contains("setup is incomplete");
    }
}
