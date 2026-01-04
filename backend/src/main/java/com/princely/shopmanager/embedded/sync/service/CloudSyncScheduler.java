package com.princely.shopmanager.embedded.sync.service;

import com.princely.shopmanager.embedded.domain.CloudSyncConfig;
import com.princely.shopmanager.embedded.service.CloudSyncConfigurationService;
import com.princely.shopmanager.embedded.sync.dto.TransactionSyncDto;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler for automatic cloud sync.
 * Syncs transactions for all active tenants on a schedule.
 */
@Slf4j
@Service
@EnableScheduling
@Profile("embedded")
@RequiredArgsConstructor
public class CloudSyncScheduler {

    private final CloudSyncConfigurationService cloudSyncConfigurationService;
    private final CloudSyncService cloudSyncService;
    private final SalesTransactionRepository salesTransactionRepository;

    @Value("${application.cloud.sync-batch-size:1000}")
    private int syncBatchSize;

    private LocalDateTime lastSyncTime = LocalDateTime.now().minusDays(1);

    private static TransactionSyncDto.TransactionItemDto apply(LineItem item) {
        return TransactionSyncDto.TransactionItemDto.builder()
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getLineTotal())
                .discountAmount(item.getDiscountAmount())
                .taxAmount(item.getTaxAmount())
                .build();
    }

    /**
     * Scheduled sync job (runs hourly for all active tenants)
     */
    @Scheduled(cron = "${application.cloud.sync-cron:0 0 * * * ?}")
    public void syncTransactionsToCloud() {
        log.info("Starting scheduled cloud sync for all active tenants");

        try {
            // Get all active cloud sync configurations
            List<CloudSyncConfig> activeConfigs = cloudSyncConfigurationService.getAllActiveConfigurations();

            if (activeConfigs.isEmpty()) {
                log.info("No active cloud sync configurations found");
                return;
            }

            log.info("Found {} active tenant(s) for cloud sync", activeConfigs.size());

            // Sync transactions for each tenant
            for (CloudSyncConfig config : activeConfigs) {
                syncTenantTransactions(config.getTenantId());
            }

            // Update last sync time
            lastSyncTime = LocalDateTime.now();
            log.info("Completed scheduled cloud sync for all tenants");

        } catch (Exception e) {
            log.error("Error during scheduled cloud sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Sync transactions for a specific tenant
     */
    public void syncTenantTransactions(String tenantId) {
        log.info("Starting cloud sync for tenant: {}", tenantId);

        try {
            // Fetch transactions since last sync
            LocalDateTime syncCutoff = lastSyncTime;
            List<SalesTransaction> transactions = fetchTransactionsByTenant(tenantId, syncCutoff);

            if (transactions.isEmpty()) {
                log.info("No new transactions to sync for tenant: {}", tenantId);
                return;
            }

            log.info("Found {} transactions to sync for tenant: {}", transactions.size(), tenantId);

            // Group transactions by shop
            Map<String, List<SalesTransaction>> transactionsByShop = transactions.stream()
                    .filter(txn -> txn.getShop() != null)
                    .collect(Collectors.groupingBy(txn -> txn.getShop().getId()));

            // Sync each shop's transactions with chunking
            int totalSynced = 0;
            for (Map.Entry<String, List<SalesTransaction>> entry : transactionsByShop.entrySet()) {
                List<SalesTransaction> shopTransactions = entry.getValue();
                List<TransactionSyncDto> allDtos = convertToSyncDtos(shopTransactions);

                // Chunk into batches to prevent overwhelming cloud API
                List<List<TransactionSyncDto>> batches = chunkTransactions(allDtos, syncBatchSize);

                log.info("Syncing {} transactions in {} batch(es) for shop {} (tenant: {})",
                        allDtos.size(), batches.size(), entry.getKey(), tenantId);

                // Sync each batch
                for (int i = 0; i < batches.size(); i++) {
                    List<TransactionSyncDto> batch = batches.get(i);
                    try {
                        cloudSyncService.syncTransactions(tenantId, batch);
                        totalSynced += batch.size();
                        log.info("Synced batch {}/{} ({} transactions) for shop {} (tenant: {})",
                                i + 1, batches.size(), batch.size(), entry.getKey(), tenantId);
                    } catch (Exception e) {
                        log.error("Failed to sync batch {}/{} for shop {} (tenant: {}): {}",
                                i + 1, batches.size(), entry.getKey(), tenantId, e.getMessage(), e);
                        // Continue with next batch even if this one fails
                    }
                }
            }

            log.info("Completed cloud sync for tenant: {}. Total transactions synced: {}/{}",
                    tenantId, totalSynced, transactions.size());

        } catch (Exception e) {
            log.error("Error syncing tenant {}: {}", tenantId, e.getMessage(), e);
        }
    }

    /**
     * Fetch UNSYNCED transactions for a specific tenant.
     * This ensures offline resilience - transactions are synced even after weeks offline.
     */
    private List<SalesTransaction> fetchTransactionsByTenant(String tenantId, LocalDateTime since) {
        return salesTransactionRepository.findAll().stream()
                .filter(txn -> txn.getShop() != null)
                .filter(txn -> txn.getShop().getTenant() != null)
                .filter(txn -> txn.getShop().getTenant().getId().equals(tenantId))
                .filter(txn -> txn.getStatus() == SalesTransaction.TransactionStatus.COMPLETED)
                // CRITICAL: Only sync unsynced transactions (persistent tracking)
                .filter(txn -> !Boolean.TRUE.equals(txn.getSyncedToCloud()))
                // Optional: Limit retry attempts to avoid infinite loops
                .filter(txn -> txn.getSyncAttempts() == null || txn.getSyncAttempts() < 10)
                .toList();
    }

    /**
     * Convert transactions to sync DTOs
     */
    private List<TransactionSyncDto> convertToSyncDtos(List<SalesTransaction> transactions) {
        return transactions.stream()
                .map(this::toSyncDto)
                .toList();
    }

    /**
     * Convert a single transaction to DTO
     */
    private TransactionSyncDto toSyncDto(SalesTransaction txn) {
        return TransactionSyncDto.builder()
                .transactionId(txn.getId())
                .transactionNumber(txn.getTransactionNumber())
                .shopId(txn.getShop() != null ? txn.getShop().getId() : null)
                .tenantId(txn.getShop() != null && txn.getShop().getTenant() != null
                        ? txn.getShop().getTenant().getId() : null)
                .transactionDate(txn.getTransactionDate())
                .totalAmount(txn.getTotalAmount())
                .taxAmount(txn.getTaxAmount())
                .discountAmount(txn.getDiscountAmount())
                .paymentMethod(txn.getPaymentMethod() != null ? txn.getPaymentMethod().name() : null)
                .status(txn.getStatus() != null ? txn.getStatus().name() : null)
                .customerName(txn.getCustomerName())
                .customerPhone(txn.getCustomerPhone())
                .customerEmail(txn.getCustomerEmail())
                .items(txn.getLineItems() != null ? txn.getLineItems().stream()
                        .map(CloudSyncScheduler::apply)
                        .toList() : List.of())
                .syncedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Manual sync trigger for all active tenants
     */
    public void triggerManualSync() {
        log.info("Manual sync triggered for all active tenants");
        syncTransactionsToCloud();
    }

    /**
     * Manual sync trigger for a specific tenant
     */
    public void triggerManualSyncForTenant(String tenantId) {
        log.info("Manual sync triggered for tenant: {}", tenantId);
        syncTenantTransactions(tenantId);
    }

    /**
     * Chunk a list of transactions into batches of specified size.
     * This prevents overwhelming the cloud API with thousands of transactions at once.
     */
    private List<List<TransactionSyncDto>> chunkTransactions(List<TransactionSyncDto> transactions, int chunkSize) {
        List<List<TransactionSyncDto>> chunks = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, transactions.size());
            chunks.add(new ArrayList<>(transactions.subList(i, end)));
        }
        return chunks;
    }
}
