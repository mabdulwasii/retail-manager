package com.princely.shopmanager.embedded.sync.service;

import com.princely.shopmanager.embedded.config.CloudSyncConfig;
import com.princely.shopmanager.embedded.sync.domain.CloudSyncLog;
import com.princely.shopmanager.embedded.sync.dto.TransactionSyncDto;
import com.princely.shopmanager.embedded.sync.repository.CloudSyncLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for syncing local data to cloud
 */
@Slf4j
@Service
@Profile("embedded")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.sync.enabled", havingValue = "true")
public class CloudSyncService {

    private final CloudSyncConfig config;
    private final CloudSyncLogRepository syncLogRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sync transactions to cloud
     */
    public CloudSyncLog syncTransactions(List<TransactionSyncDto> transactions) {
        String batchId = UUID.randomUUID().toString();
        LocalDateTime startTime = LocalDateTime.now();

        CloudSyncLog syncLog = CloudSyncLog.builder()
                .storeId(config.getStoreId())
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
            // Anonymize PII if configured
            if (config.getPrivacy().isAnonymizePii()) {
                transactions = anonymizeTransactions(transactions);
            }

            // Send to cloud endpoint
            int syncedCount = sendToCloud(transactions);

            // Update sync log
            LocalDateTime endTime = LocalDateTime.now();
            syncLog.setStatus(CloudSyncLog.SyncStatus.COMPLETED);
            syncLog.setSyncEndTime(endTime);
            syncLog.setRecordsSynced(syncedCount);
            syncLog.setDurationMs(Duration.between(startTime, endTime).toMillis());

            log.info("Successfully synced {} transactions to cloud (batch: {})", syncedCount, batchId);

        } catch (Exception e) {
            LocalDateTime endTime = LocalDateTime.now();
            syncLog.setStatus(CloudSyncLog.SyncStatus.FAILED);
            syncLog.setSyncEndTime(endTime);
            syncLog.setRecordsFailed(transactions.size());
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setErrorDetails(getStackTraceAsString(e));
            syncLog.setDurationMs(Duration.between(startTime, endTime).toMillis());

            log.error("Failed to sync transactions to cloud (batch: {}): {}", batchId, e.getMessage(), e);
        }

        return syncLogRepository.save(syncLog);
    }

    /**
     * Send transactions to cloud endpoint
     */
    private int sendToCloud(List<TransactionSyncDto> transactions) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", config.getApiKey());
        headers.set("X-Store-Id", config.getStoreId());

        HttpEntity<List<TransactionSyncDto>> request = new HttpEntity<>(transactions, headers);

        String url = config.getCloudEndpoint() + "/api/sync/transactions";

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
            throw new RuntimeException("Cloud sync failed with HTTP error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Unexpected error during sync: {}", e.getMessage(), e);
            throw new RuntimeException("Cloud sync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Anonymize PII in transactions
     */
    private List<TransactionSyncDto> anonymizeTransactions(List<TransactionSyncDto> transactions) {
        List<String> fieldsToAnonymize = config.getPrivacy().getFieldsToAnonymize();

        return transactions.stream().map(txn -> {
            if (fieldsToAnonymize.contains("customerName")) {
                txn.setCustomerName(anonymize(txn.getCustomerName()));
            }
            if (fieldsToAnonymize.contains("customerPhone")) {
                txn.setCustomerPhone(anonymize(txn.getCustomerPhone()));
            }
            if (fieldsToAnonymize.contains("customerEmail")) {
                txn.setCustomerEmail(anonymize(txn.getCustomerEmail()));
            }
            return txn;
        }).toList();
    }

    /**
     * Anonymize a field value
     */
    private String anonymize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        // Simple anonymization - replace with asterisks except first character
        return value.charAt(0) + "*".repeat(Math.min(value.length() - 1, 10));
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
     * Response DTO from cloud
     */
    public record SyncResponse(int syncedCount, String message) {
    }
}
