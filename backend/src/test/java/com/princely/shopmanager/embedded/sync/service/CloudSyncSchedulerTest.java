package com.princely.shopmanager.embedded.sync.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.core.domain.Tenant;
import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.service.CloudSyncConfigurationService;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CloudSyncScheduler.
 * Tests scheduled sync logic without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CloudSyncScheduler Unit Tests")
class CloudSyncSchedulerTest {

    @Mock
    private CloudSyncConfigurationService cloudSyncConfigurationService;

    @Mock
    private CloudSyncService cloudSyncService;

    @Mock
    private SalesTransactionRepository salesTransactionRepository;

    @InjectMocks
    private CloudSyncScheduler cloudSyncScheduler;

    private static final String TEST_TENANT_ID = "test-tenant-123";
    private static final String TEST_SHOP_ID = "test-shop-456";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(cloudSyncScheduler, "syncBatchSize", 1000);
    }

    // ============== syncTransactionsToCloud Tests ==============

    @Test
    @DisplayName("syncTransactionsToCloud should skip when no active configs found")
    void syncTransactionsToCloudShouldSkipWhenNoActiveConfigs() {
        // Given
        when(cloudSyncConfigurationService.getAllActiveConfigurations()).thenReturn(List.of());

        // When
        cloudSyncScheduler.syncTransactionsToCloud();

        // Then
        verify(cloudSyncConfigurationService).getAllActiveConfigurations();
        verify(salesTransactionRepository, never()).findAll();
    }

    @Test
    @DisplayName("syncTransactionsToCloud should sync for all active tenants")
    void syncTransactionsToCloudShouldSyncAllActiveTenants() {
        // Given
        List<CloudSyncConfig> activeConfigs = List.of(
                createCloudSyncConfig("tenant-1"),
                createCloudSyncConfig("tenant-2")
        );
        when(cloudSyncConfigurationService.getAllActiveConfigurations()).thenReturn(activeConfigs);
        when(salesTransactionRepository.findAll()).thenReturn(List.of());

        // When
        cloudSyncScheduler.syncTransactionsToCloud();

        // Then
        verify(cloudSyncConfigurationService).getAllActiveConfigurations();
        verify(salesTransactionRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("syncTransactionsToCloud should handle exceptions gracefully")
    void syncTransactionsToCloudShouldHandleExceptions() {
        // Given
        when(cloudSyncConfigurationService.getAllActiveConfigurations())
                .thenThrow(new RuntimeException("Database error"));

        // When/Then - should not throw
        assertThatCode(() -> cloudSyncScheduler.syncTransactionsToCloud())
                .doesNotThrowAnyException();
    }

    // ============== syncTenantTransactions Tests ==============

    @Test
    @DisplayName("syncTenantTransactions should skip when no transactions found")
    void syncTenantTransactionsShouldSkipWhenNoTransactions() {
        // Given
        when(salesTransactionRepository.findAll()).thenReturn(List.of());

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        verify(salesTransactionRepository).findAll();
        verify(cloudSyncService, never()).syncTransactions(anyString(), anyList());
    }

    @Test
    @DisplayName("syncTenantTransactions should sync unsynced completed transactions")
    void syncTenantTransactionsShouldSyncUnsyncedTransactions() {
        // Given
        List<SalesTransaction> transactions = List.of(
                createCompletedTransaction("txn-1", false, 0),
                createCompletedTransaction("txn-2", false, 0)
        );
        when(salesTransactionRepository.findAll()).thenReturn(transactions);

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        verify(salesTransactionRepository).findAll();
        verify(cloudSyncService).syncTransactions(eq(TEST_TENANT_ID), anyList());
    }

    @Test
    @DisplayName("syncTenantTransactions should skip already synced transactions")
    void syncTenantTransactionsShouldSkipSyncedTransactions() {
        // Given
        List<SalesTransaction> transactions = List.of(
                createCompletedTransaction("txn-1", true, 0), // Already synced
                createCompletedTransaction("txn-2", false, 0) // Not synced
        );
        when(salesTransactionRepository.findAll()).thenReturn(transactions);

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        ArgumentCaptor<List> dtoCaptor = ArgumentCaptor.forClass(List.class);
        verify(cloudSyncService).syncTransactions(eq(TEST_TENANT_ID), dtoCaptor.capture());
        assertThat(dtoCaptor.getValue()).hasSize(1); // Only unsynced transaction
    }

    @Test
    @DisplayName("syncTenantTransactions should skip transactions with too many retry attempts")
    void syncTenantTransactionsShouldSkipHighRetryAttempts() {
        // Given
        List<SalesTransaction> transactions = List.of(
                createCompletedTransaction("txn-1", false, 5),  // 5 attempts - should sync
                createCompletedTransaction("txn-2", false, 10), // 10 attempts - should skip
                createCompletedTransaction("txn-3", false, 15)  // 15 attempts - should skip
        );
        when(salesTransactionRepository.findAll()).thenReturn(transactions);

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        ArgumentCaptor<List> dtoCaptor = ArgumentCaptor.forClass(List.class);
        verify(cloudSyncService).syncTransactions(eq(TEST_TENANT_ID), dtoCaptor.capture());
        assertThat(dtoCaptor.getValue()).hasSize(1); // Only txn with < 10 attempts
    }

    @Test
    @DisplayName("syncTenantTransactions should skip non-completed transactions")
    void syncTenantTransactionsShouldSkipNonCompletedTransactions() {
        // Given
        SalesTransaction pendingTxn = createCompletedTransaction("txn-1", false, 0);
        pendingTxn.setStatus(SalesTransaction.TransactionStatus.PENDING);

        SalesTransaction completedTxn = createCompletedTransaction("txn-2", false, 0);

        when(salesTransactionRepository.findAll()).thenReturn(List.of(pendingTxn, completedTxn));

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        ArgumentCaptor<List> dtoCaptor = ArgumentCaptor.forClass(List.class);
        verify(cloudSyncService).syncTransactions(eq(TEST_TENANT_ID), dtoCaptor.capture());
        assertThat(dtoCaptor.getValue()).hasSize(1); // Only completed transaction
    }

    @Test
    @DisplayName("syncTenantTransactions should chunk large transaction batches")
    void syncTenantTransactionsShouldChunkLargeBatches() {
        // Given
        ReflectionTestUtils.setField(cloudSyncScheduler, "syncBatchSize", 5);
        List<SalesTransaction> transactions = createMultipleTransactions(12); // 12 transactions
        when(salesTransactionRepository.findAll()).thenReturn(transactions);

        // When
        cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID);

        // Then
        verify(cloudSyncService, times(3)).syncTransactions(eq(TEST_TENANT_ID), anyList());
        // 3 batches: 5 + 5 + 2
    }

    @Test
    @DisplayName("syncTenantTransactions should continue on batch failure")
    void syncTenantTransactionsShouldContinueOnBatchFailure() {
        // Given
        ReflectionTestUtils.setField(cloudSyncScheduler, "syncBatchSize", 5);
        List<SalesTransaction> transactions = createMultipleTransactions(10); // 2 batches of 5
        when(salesTransactionRepository.findAll()).thenReturn(transactions);

        // First batch fails, second should still be attempted
        when(cloudSyncService.syncTransactions(eq(TEST_TENANT_ID), anyList()))
                .thenThrow(new RuntimeException("Sync error"))
                .thenReturn(null);

        // When/Then - should not throw
        assertThatCode(() -> cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID))
                .doesNotThrowAnyException();

        verify(cloudSyncService, times(2)).syncTransactions(eq(TEST_TENANT_ID), anyList());
    }

    @Test
    @DisplayName("syncTenantTransactions should handle exceptions gracefully")
    void syncTenantTransactionsShouldHandleExceptions() {
        // Given
        when(salesTransactionRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When/Then - should not throw
        assertThatCode(() -> cloudSyncScheduler.syncTenantTransactions(TEST_TENANT_ID))
                .doesNotThrowAnyException();
    }

    // ============== Manual Trigger Tests ==============

    @Test
    @DisplayName("triggerManualSync should delegate to syncTransactionsToCloud")
    void triggerManualSyncShouldDelegate() {
        // Given
        when(cloudSyncConfigurationService.getAllActiveConfigurations()).thenReturn(List.of());

        // When
        cloudSyncScheduler.triggerManualSync();

        // Then
        verify(cloudSyncConfigurationService).getAllActiveConfigurations();
    }

    @Test
    @DisplayName("triggerManualSyncForTenant should sync specific tenant")
    void triggerManualSyncForTenantShouldSyncTenant() {
        // Given
        when(salesTransactionRepository.findAll()).thenReturn(List.of());

        // When
        cloudSyncScheduler.triggerManualSyncForTenant(TEST_TENANT_ID);

        // Then
        verify(salesTransactionRepository).findAll();
    }

    // Helper methods

    private CloudSyncConfig createCloudSyncConfig(String tenantId) {
        return CloudSyncConfig.builder()
                .tenantId(tenantId)
                .cloudTenantId("cloud-" + tenantId)
                .cloudApiKey("api-key")
                .syncEnabled(true)
                .syncStatus(CloudSyncConfig.SyncStatus.CONFIGURED)
                .build();
    }

    private SalesTransaction createCompletedTransaction(String id, boolean synced, int syncAttempts) {
        Tenant tenant = Tenant.builder()
                .id(TEST_TENANT_ID)
                .name("Test Tenant")
                .build();

        Shop shop = Shop.builder()
                .id(TEST_SHOP_ID)
                .name("Test Shop")
                .tenant(tenant)
                .build();

        return SalesTransaction.builder()
                .id(id)
                .transactionNumber("TXN-" + id)
                .shop(shop)
                .status(SalesTransaction.TransactionStatus.COMPLETED)
                .totalAmount(BigDecimal.valueOf(100.00))
                .transactionDate(LocalDateTime.now().minusHours(1))
                .syncedToCloud(synced)
                .syncAttempts(syncAttempts)
                .lineItems(new ArrayList<>())
                .build();
    }

    private List<SalesTransaction> createMultipleTransactions(int count) {
        List<SalesTransaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(createCompletedTransaction("txn-" + i, false, 0));
        }
        return transactions;
    }
}
