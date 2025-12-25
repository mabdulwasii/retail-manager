package com.princely.shopmanager.embedded.sync.service;

import com.princely.shopmanager.embedded.service.CloudSyncConfigurationService;
import com.princely.shopmanager.embedded.sync.domain.CloudSyncLog;
import com.princely.shopmanager.embedded.sync.dto.TransactionSyncDto;
import com.princely.shopmanager.embedded.sync.repository.CloudSyncLogRepository;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for syncing local data to cloud aggregator.
 * Uses tenant_id + shop_id for proper data hierarchy.
 */
@Slf4j
@Service
@Profile("embedded")
@RequiredArgsConstructor
public class CloudSyncService {

    private final CloudSyncConfigurationService cloudSyncConfigurationService;
    private final CloudSyncLogRepository syncLogRepository;
    private final SalesTransactionRepository salesTransactionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sync transactions to cloud for a specific tenant
     */
    @Transactional
    public CloudSyncLog syncTransactions(String tenantId, List<TransactionSyncDto> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            log.debug("No transactions to sync for tenant: {}", tenantId);
            return null;
        }

        // Get cloud sync config
        var cloudConfig = cloudSyncConfigurationService.getConfigByTenantId(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED));

        if (!cloudConfig.isActive()) {
            throw new BusinessException(ErrorCode.CLOUD_SYNC_NOT_CONFIGURED,
                    "Cloud sync is not active for this tenant");
        }

        String batchId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();
        String shopId = transactions.get(0).getShopId(); // Assume all from same shop

        CloudSyncLog syncLog = CloudSyncLog.builder()
                .storeId(shopId) // Store field for backward compatibility
                .syncBatchId(batchId)
                .syncType(CloudSyncLog.SyncType.SALES_TRANSACTIONS)
                .status(CloudSyncLog.SyncStatus.IN_PROGRESS)
                .syncStartTime(startTime)
                .recordsProcessed(transactions.size())
                .recordsSynced(0)
                .recordsFailed(0)
                .retryAttempt(0)
                .build();

        syncLog = syncLogRepository.save(syncLog);

        try {
            // Send to cloud endpoint
            int syncedCount = sendToCloud(cloudConfig, transactions);

            // Update sync log
            LocalDateTime endTime = LocalDateTime.now();
            syncLog.setStatus(CloudSyncLog.SyncStatus.COMPLETED);
            syncLog.setSyncEndTime(endTime);
            syncLog.setRecordsSynced(syncedCount);
            syncLog.setDurationMs(Duration.between(startTime, endTime).toMillis());

            log.info("Successfully synced {} transactions to cloud for tenant {} (batch: {})",
                    syncedCount, tenantId, batchId);

            // Mark transactions as synced
            markTransactionsAsSynced(transactions);

            // Mark sync as successful
            cloudSyncConfigurationService.markSyncSuccess(tenantId);

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            syncLog.setStatus(CloudSyncLog.SyncStatus.FAILED);
            syncLog.setSyncEndTime(endTime);
            syncLog.setRecordsFailed(transactions.size());
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setErrorDetails(getStackTraceAsString(e));
            syncLog.setDurationMs(Duration.between(startTime, endTime).toMillis());

            log.error("Failed to sync transactions to cloud for tenant {} (batch: {}): {}",
                    tenantId, batchId, e.getMessage(), e);

            // Mark transactions as failed (increment attempts)
            markTransactionsAsFailed(transactions, e.getMessage());

            // Mark sync as failed
            cloudSyncConfigurationService.markSyncFailed(tenantId, e.getMessage());
        }

        return syncLogRepository.save(syncLog);
    }

    /**
     * Send transactions to cloud endpoint
     */
    private int sendToCloud(com.princely.shopmanager.embedded.domain.CloudSyncConfig cloudConfig,
            List<TransactionSyncDto> transactions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", cloudConfig.getCloudApiKey());
        headers.set("X-Cloud-Tenant-Id", cloudConfig.getCloudTenantId());

        HttpEntity<List<TransactionSyncDto>> request = new HttpEntity<>(transactions, headers);

        String url = cloudConfig.getCloudApiUrl() + "/sync/transactions";

        try {
            ResponseEntity<SyncResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    SyncResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().syncedCount();
            }

            log.warn("Unexpected response from cloud: {}", response.getStatusCode());
            return 0;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("HTTP error during sync: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                    "Cloud sync failed with HTTP error: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Unexpected error during sync: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.CLOUD_SYNC_UNAVAILABLE,
                    "Cloud sync failed: " + e.getMessage());
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 4000) break; // Limit to 4000 chars
        }
        return sb.toString();
    }

    /**
     * Mark transactions as successfully synced
     */
    private void markTransactionsAsSynced(List<TransactionSyncDto> transactions) {
        List<String> transactionIds = transactions.stream()
                .map(TransactionSyncDto::getTransactionId)
                .toList();

        List<SalesTransaction> salesTransactions = salesTransactionRepository.findAllById(transactionIds);

        LocalDateTime now = LocalDateTime.now();
        for (SalesTransaction txn : salesTransactions) {
            txn.setSyncedToCloud(true);
            txn.setSyncedAt(now);
            txn.setLastSyncAttempt(now);
            txn.setLastSyncError(null); // Clear any previous errors
        }

        salesTransactionRepository.saveAll(salesTransactions);
        log.debug("Marked {} transactions as synced", salesTransactions.size());
    }

    /**
     * Mark transactions as failed (increment attempts)
     */
    private void markTransactionsAsFailed(List<TransactionSyncDto> transactions, String errorMessage) {
        List<String> transactionIds = transactions.stream()
                .map(TransactionSyncDto::getTransactionId)
                .toList();

        List<SalesTransaction> salesTransactions = salesTransactionRepository.findAllById(transactionIds);

        LocalDateTime now = LocalDateTime.now();
        for (SalesTransaction txn : salesTransactions) {
            txn.setSyncedToCloud(false);
            txn.setSyncAttempts(txn.getSyncAttempts() != null ? txn.getSyncAttempts() + 1 : 1);
            txn.setLastSyncAttempt(now);
            txn.setLastSyncError(errorMessage != null && errorMessage.length() > 500
                    ? errorMessage.substring(0, 500) : errorMessage);
        }

        salesTransactionRepository.saveAll(salesTransactions);
        log.debug("Marked {} transactions as failed (attempt incremented)", salesTransactions.size());
    }

    /**
     * Response DTO from cloud
     */
    public record SyncResponse(int syncedCount, String message) {
    }
}
