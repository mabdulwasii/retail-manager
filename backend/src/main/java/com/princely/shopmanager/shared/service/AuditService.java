package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.dto.AuditLogFilterRequest;
import com.princely.shopmanager.shared.repository.AuditLogRepository;
import com.princely.shopmanager.shared.specification.AuditLogSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    // Default user identifiers for system-level actions
    private static final String SYSTEM_USER_ID = "SYSTEM";
    private static final String SYSTEM_USERNAME = "system";

    // Entity type constants
    private static final String ENTITY_TYPE_EXPENSE = "EXPENSE";

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(Shop shop, String userId, String username,
                                AuditLog.ActionType actionType, String description,
                                String ipAddress, boolean success) {
        try {
            AuditLog auditLog = AuditLog.createSecurityEvent(
                shop, userId, username, actionType, description, ipAddress, success
            );
            auditLogRepository.save(auditLog);
            log.debug("Security event logged: {} for user {}", actionType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user {}", actionType, username, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDataModification(Shop shop, String userId, String username,
                                   AuditLog.ActionType actionType, String entityType,
                                   String entityId, String description,
                                   String oldValues, String newValues) {
        try {
            // Default to SYSTEM if userId is null
            String effectiveUserId = userId != null ? userId : SYSTEM_USER_ID;
            String effectiveUsername = username != null ? username : SYSTEM_USERNAME;

            AuditLog auditLog = AuditLog.createDataModification(
                shop, effectiveUserId, effectiveUsername, actionType, entityType, entityId,
                description, oldValues, newValues
            );
            auditLogRepository.save(auditLog);
            log.debug("Data modification logged: {} on {} {}", actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log data modification: {} on {} {}", actionType, entityType, entityId, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFinancialTransaction(Shop shop, String userId, String username,
                                       AuditLog.ActionType actionType, String entityId,
                                       String description, boolean success) {
        try {
            AuditLog auditLog = AuditLog.createFinancialTransaction(
                shop, userId, username, actionType, entityId, description, success
            );
            auditLogRepository.save(auditLog);
            log.debug("Financial transaction logged: {} for entity {}", actionType, entityId);
        } catch (Exception e) {
            log.error("Failed to log financial transaction: {} for entity {}", actionType, entityId, e);
        }
    }


    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCustomEvent(Shop shop, String userId, String username,
                              AuditLog.AuditCategory category, AuditLog.ActionType actionType,
                              String entityType, String entityId, String description,
                              Map<String, String> details, AuditLog.Severity severity) {
        try {
            // Default to SYSTEM if userId is null
            String effectiveUserId = userId != null ? userId : SYSTEM_USER_ID;
            String effectiveUsername = username != null ? username : SYSTEM_USERNAME;

            AuditLog auditLog = AuditLog.builder()
                .shop(shop)
                .userId(effectiveUserId)
                .username(effectiveUsername)
                .category(category)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .details(details != null ? details : Map.of())
                .severity(severity != null ? severity : AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Custom audit event logged: {} - {}", actionType, description);
        } catch (Exception e) {
            log.error("Failed to log custom audit event: {} - {}", actionType, description, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(Shop shop, String userId, String username,
                        AuditLog.ActionType actionType, String description,
                        String errorMessage) {
        try {
            // Default to SYSTEM if userId is null
            String effectiveUserId = userId != null ? userId : SYSTEM_USER_ID;
            String effectiveUsername = username != null ? username : SYSTEM_USERNAME;

            AuditLog auditLog = AuditLog.builder()
                .shop(shop)
                .userId(effectiveUserId)
                .username(effectiveUsername)
                .category(AuditLog.AuditCategory.SYSTEM_EVENT)
                .actionType(actionType)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.ERROR)
                .success(false)
                .errorMessage(errorMessage)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Error event logged: {} for user {}", actionType, effectiveUsername);
        } catch (Exception e) {
            log.error("Failed to log error event: {} for user {}", actionType, username, e);
        }
    }

    // Convenience methods for common audit operations
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityCreation(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .userId(SYSTEM_USER_ID)
                .username(SYSTEM_USERNAME)
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.CREATE)
                .entityType(entityType)
                .entityId(entityId)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Entity creation logged: {} {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log entity creation: {} {}", entityType, entityId, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityModification(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .userId(SYSTEM_USER_ID)
                .username(SYSTEM_USERNAME)
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.UPDATE)
                .entityType(entityType)
                .entityId(entityId)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Entity modification logged: {} {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log entity modification: {} {}", entityType, entityId, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityDeletion(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .userId(SYSTEM_USER_ID)
                .username(SYSTEM_USERNAME)
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.DELETE)
                .entityType(entityType)
                .entityId(entityId)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Entity deletion logged: {} {}", entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log entity deletion: {} {}", entityType, entityId, e);
        }
    }

    // Expense-specific audit methods
    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseCreation(java.util.UUID expenseId, String shopId, String userId, java.math.BigDecimal amount) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(null) // TODO: fetch Shop entity by shopId
                .userId(userId)
                .category(AuditLog.AuditCategory.FINANCIAL_TRANSACTION)
                .actionType(AuditLog.ActionType.CREATE)
                .entityType(ENTITY_TYPE_EXPENSE)
                .entityId(expenseId.toString())
                .actionDescription("Expense created with amount: " + amount)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Expense creation logged: {}", expenseId);
        } catch (Exception e) {
            log.error("Failed to log expense creation: {}", expenseId, e);
        }
    }

    @Async("auditTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseUpdate(java.util.UUID expenseId, String shopId, String userId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(null) // TODO: fetch Shop entity by shopId
                .userId(userId)
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.UPDATE)
                .entityType(ENTITY_TYPE_EXPENSE)
                .entityId(expenseId.toString())
                .actionDescription("Expense updated")
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Expense update logged: {}", expenseId);
        } catch (Exception e) {
            log.error("Failed to log expense update: {}", expenseId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseApproval(java.util.UUID expenseId, String shopId, String userId, boolean approved) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(null) // TODO: fetch Shop entity by shopId
                .userId(userId)
                .category(AuditLog.AuditCategory.BUSINESS_PROCESS)
                .actionType(approved ? AuditLog.ActionType.APPROVE : AuditLog.ActionType.REJECT)
                .entityType(ENTITY_TYPE_EXPENSE)
                .entityId(expenseId.toString())
                .actionDescription("Expense " + (approved ? "approved" : "rejected"))
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.INFO)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Expense approval logged: {} - {}", expenseId, approved ? "approved" : "rejected");
        } catch (Exception e) {
            log.error("Failed to log expense approval: {}", expenseId, e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseDeletion(java.util.UUID expenseId, String shopId, String userId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(null) // TODO: fetch Shop entity by shopId
                .userId(userId)
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.DELETE)
                .entityType(ENTITY_TYPE_EXPENSE)
                .entityId(expenseId.toString())
                .actionDescription("Expense deleted")
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.WARNING)
                .success(true)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Expense deletion logged: {}", expenseId);
        } catch (Exception e) {
            log.error("Failed to log expense deletion: {}", expenseId, e);
        }
    }

    /**
     * Log a general event with metadata
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEvent(String eventType, String description, Map<String, Object> metadata) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .category(AuditLog.AuditCategory.SYSTEM_EVENT)
                .actionType(AuditLog.ActionType.CREATE)
                .actionDescription(eventType + ": " + description)
                .actionDate(LocalDateTime.now())
                .userId(SYSTEM_USER_ID)
                .username(SYSTEM_USER_ID)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Event logged: {} - {}", eventType, description);
        } catch (Exception e) {
            log.error("Failed to log event: {} - {}", eventType, description, e);
        }
    }

    /**
     * Get audit logs for a shop with filters and pagination
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Shop shop, AuditLogFilterRequest filters, Pageable pageable) {
        log.debug("Fetching audit logs for shop: {} with filters", shop.getId());

        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(shop, filters);
        return auditLogRepository.findAll(spec, pageable);
    }

    /**
     * Export audit logs to CSV format
     */
    @Transactional(readOnly = true)
    public String exportAuditLogsToCsv(Shop shop, AuditLogFilterRequest filters) {
        log.debug("Exporting audit logs to CSV for shop: {}", shop.getId());

        Specification<AuditLog> spec = AuditLogSpecification.fromFilters(shop, filters);
        List<AuditLog> logs = auditLogRepository.findAll(spec);

        StringBuilder csv = new StringBuilder();

        // CSV header
        csv.append("Timestamp,Action,Category,Entity Type,Entity ID,User,Username,Description,IP Address,Severity,Success,Error Message\n");

        // CSV rows
        for (AuditLog log : logs) {
            csv.append(escapeCSV(log.getActionDate() != null ? log.getActionDate().toString() : "")).append(",");
            csv.append(escapeCSV(log.getActionType() != null ? log.getActionType().name() : "")).append(",");
            csv.append(escapeCSV(log.getCategory() != null ? log.getCategory().name() : "")).append(",");
            csv.append(escapeCSV(log.getEntityType() != null ? log.getEntityType() : "")).append(",");
            csv.append(escapeCSV(log.getEntityId() != null ? log.getEntityId() : "")).append(",");
            csv.append(escapeCSV(log.getUserId() != null ? log.getUserId() : "")).append(",");
            csv.append(escapeCSV(log.getUsername() != null ? log.getUsername() : "")).append(",");
            csv.append(escapeCSV(log.getActionDescription() != null ? log.getActionDescription() : "")).append(",");
            csv.append(escapeCSV(log.getIpAddress() != null ? log.getIpAddress() : "")).append(",");
            csv.append(escapeCSV(log.getSeverity() != null ? log.getSeverity().name() : "")).append(",");
            csv.append(log.isSuccess() ? "true" : "false").append(",");
            csv.append(escapeCSV(log.getErrorMessage() != null ? log.getErrorMessage() : "")).append("\n");
        }

        log.info("Exported {} audit logs to CSV for shop: {}", logs.size(), shop.getId());
        return csv.toString();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // If the value contains comma, quote, or newline, wrap in quotes and escape existing quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}