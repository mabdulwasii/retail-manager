package com.princely.shopmanager.embedded.sync.service;

import com.princely.shopmanager.embedded.config.CloudSyncConfig;
import com.princely.shopmanager.embedded.sync.dto.TransactionSyncDto;
import com.princely.shopmanager.sales.domain.LineItem;
import com.princely.shopmanager.sales.domain.SalesTransaction;
import com.princely.shopmanager.sales.repository.SalesTransactionRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduler for automatic cloud sync
 */
@Slf4j
@Service
@EnableScheduling
@Profile("embedded")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.sync.enabled", havingValue = "true")
public class CloudSyncScheduler {

    private final CloudSyncConfig config;
    private final CloudSyncService cloudSyncService;
    private final SalesTransactionRepository salesTransactionRepository;

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
     * Scheduled sync job (runs based on cron expression in config)
     */
    @Scheduled(cron = "${application.sync.schedule.cron}")
    public void syncTransactionsToCloud() {
        log.info("Starting scheduled cloud sync for store: {}", config.getStoreId());

        try {
            // Fetch transactions since last sync
            LocalDateTime syncCutoff = lastSyncTime;
            List<SalesTransaction> transactions = fetchTransactionsSinceLastSync(syncCutoff);

            if (transactions.isEmpty()) {
                log.info("No new transactions to sync");
                return;
            }

            // Process in batches
            int batchSize = config.getSchedule().getBatchSize();
            for (int i = 0; i < transactions.size(); i += batchSize) {
                int end = Math.min(i + batchSize, transactions.size());
                List<SalesTransaction> batch = transactions.subList(i, end);

                List<TransactionSyncDto> dtos = convertToSyncDtos(batch);
                cloudSyncService.syncTransactions(dtos);

                log.info("Synced batch {}/{} ({} transactions)",
                        (i / batchSize) + 1,
                        (transactions.size() + batchSize - 1) / batchSize,
                        batch.size());
            }

            // Update last sync time
            lastSyncTime = LocalDateTime.now();
            log.info("Completed cloud sync. Total transactions synced: {}", transactions.size());

        } catch (Exception e) {
            log.error("Error during scheduled cloud sync: {}", e.getMessage(), e);
        }
    }

    /**
     * Fetch transactions since last sync
     */
    private List<SalesTransaction> fetchTransactionsSinceLastSync(LocalDateTime since) {
        return salesTransactionRepository.findAll().stream()
                .filter(txn -> txn.getCreatedAt() != null && txn.getCreatedAt().isAfter(since))
                .filter(txn -> txn.getStatus() == SalesTransaction.TransactionStatus.COMPLETED)
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
     * Convert single transaction to DTO
     */
    private TransactionSyncDto toSyncDto(SalesTransaction txn) {
        return TransactionSyncDto.builder()
                .transactionId(txn.getId())
                .transactionNumber(txn.getTransactionNumber())
                .storeId(txn.getShop() != null ? txn.getShop().getId() : null)
                .tenantId(txn.getShop().getTenant().getId())
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
     * Manual sync trigger (can be called via API)
     */
    public void triggerManualSync() {
        log.info("Manual sync triggered for store: {}", config.getStoreId());
        syncTransactionsToCloud();
    }
}
