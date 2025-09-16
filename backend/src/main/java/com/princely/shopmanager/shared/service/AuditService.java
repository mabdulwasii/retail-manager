package com.princely.shopmanager.shared.service;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import com.princely.shopmanager.shared.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logDataModification(Shop shop, String userId, String username,
                                   AuditLog.ActionType actionType, String entityType,
                                   String entityId, String description,
                                   String oldValues, String newValues) {
        try {
            AuditLog auditLog = AuditLog.createDataModification(
                shop, userId, username, actionType, entityType, entityId,
                description, oldValues, newValues
            );
            auditLogRepository.save(auditLog);
            log.debug("Data modification logged: {} on {} {}", actionType, entityType, entityId);
        } catch (Exception e) {
            log.error("Failed to log data modification: {} on {} {}", actionType, entityType, entityId, e);
        }
    }

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


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logCustomEvent(Shop shop, String userId, String username,
                              AuditLog.AuditCategory category, AuditLog.ActionType actionType,
                              String entityType, String entityId, String description,
                              Map<String, String> details, AuditLog.Severity severity) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(shop)
                .userId(userId)
                .username(username)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(Shop shop, String userId, String username,
                        AuditLog.ActionType actionType, String description,
                        String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shop(shop)
                .userId(userId)
                .username(username)
                .category(AuditLog.AuditCategory.SYSTEM_EVENT)
                .actionType(actionType)
                .actionDescription(description)
                .actionDate(LocalDateTime.now())
                .severity(AuditLog.Severity.ERROR)
                .success(false)
                .errorMessage(errorMessage)
                .build();

            auditLogRepository.save(auditLog);
            log.debug("Error event logged: {} for user {}", actionType, username);
        } catch (Exception e) {
            log.error("Failed to log error event: {} for user {}", actionType, username, e);
        }
    }

    // Convenience methods for common audit operations
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityCreation(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityModification(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityDeletion(String entityType, String entityId, String description) {
        try {
            AuditLog auditLog = AuditLog.builder()
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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseCreation(java.util.UUID expenseId, java.util.UUID shopId, java.util.UUID userId, java.math.BigDecimal amount) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shopId(shopId)
                .userId(userId.toString())
                .category(AuditLog.AuditCategory.FINANCIAL_TRANSACTION)
                .actionType(AuditLog.ActionType.CREATE)
                .entityType("EXPENSE")
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logExpenseUpdate(java.util.UUID expenseId, java.util.UUID shopId, java.util.UUID userId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shopId(shopId)
                .userId(userId.toString())
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.UPDATE)
                .entityType("EXPENSE")
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
    public void logExpenseApproval(java.util.UUID expenseId, java.util.UUID shopId, java.util.UUID userId, boolean approved) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shopId(shopId)
                .userId(userId.toString())
                .category(AuditLog.AuditCategory.BUSINESS_PROCESS)
                .actionType(approved ? AuditLog.ActionType.APPROVE : AuditLog.ActionType.REJECT)
                .entityType("EXPENSE")
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
    public void logExpenseDeletion(java.util.UUID expenseId, java.util.UUID shopId, java.util.UUID userId) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .shopId(shopId)
                .userId(userId.toString())
                .category(AuditLog.AuditCategory.DATA_MODIFICATION)
                .actionType(AuditLog.ActionType.DELETE)
                .entityType("EXPENSE")
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
}