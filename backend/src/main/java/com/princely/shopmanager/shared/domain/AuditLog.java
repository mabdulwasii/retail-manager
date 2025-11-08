package com.princely.shopmanager.shared.domain;

import com.princely.shopmanager.core.domain.Shop;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_shop", columnList = "shop_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_action", columnList = "action_type"),
    @Index(name = "idx_audit_date", columnList = "action_date"),
    @Index(name = "idx_audit_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"shop"})
@EqualsAndHashCode(callSuper = true, exclude = {"shop"})
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Builder.Default
    @Column(name = "user_id", nullable = false)
    private String userId = "SYSTEM";

    @Column(name = "username")
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private AuditCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "action_description", nullable = false, length = 500)
    private String actionDescription;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @ElementCollection
    @CollectionTable(
        name = "audit_log_details",
        joinColumns = @JoinColumn(name = "audit_log_id")
    )
    @MapKeyColumn(name = "detail_key")
    @Column(name = "detail_value", length = 1000)
    @Builder.Default
    private Map<String, String> details = new HashMap<>();

    @Column(name = "old_values", columnDefinition = "TEXT")
    private String oldValues;

    @Column(name = "new_values", columnDefinition = "TEXT")
    private String newValues;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity = Severity.INFO;

    @Builder.Default
    @Column(name = "success", nullable = false)
    private boolean success = true;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    public enum AuditCategory {
        AUTHENTICATION,
        AUTHORIZATION,
        DATA_ACCESS,
        DATA_MODIFICATION,
        FINANCIAL_TRANSACTION,
        SYSTEM_EVENT,
        SECURITY_EVENT,
        CONFIGURATION_CHANGE,
        REPORT_ACCESS,
        ADMIN_ACTION,
        BUSINESS_PROCESS
    }

    public enum ActionType {
        LOGIN,
        LOGOUT,
        LOGIN_FAILED,
        CREATE,
        READ,
        UPDATE,
        DELETE,
        APPROVE,
        REJECT,
        CANCEL,
        VOID,
        EXPORT,
        IMPORT,
        BACKUP,
        RESTORE,
        PERMISSION_GRANTED,
        PERMISSION_DENIED,
        PASSWORD_CHANGE,
        ACCOUNT_LOCKED,
        ACCOUNT_UNLOCKED,
        // Investment and Financial specific actions
        INVESTMENT_CREATED,
        INVESTMENT_UPDATED,
        INVESTMENT_DELETED,
        PROFIT_CALCULATED,
        DISTRIBUTION_APPROVED,
        DISTRIBUTION_PAID
    }

    public enum Severity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    public void addDetail(String key, String value) {
        details.put(key, value);
    }

    public void addDetails(Map<String, String> additionalDetails) {
        details.putAll(additionalDetails);
    }

    public String getDetail(String key) {
        return details.get(key);
    }

    public boolean hasDetail(String key) {
        return details.containsKey(key);
    }

    public static AuditLog createSecurityEvent(Shop shop, String userId, String username,
                                               ActionType actionType, String description,
                                               String ipAddress, boolean success) {
        return AuditLog.builder()
            .shop(shop)
            .userId(userId)
            .username(username)
            .category(AuditCategory.SECURITY_EVENT)
            .actionType(actionType)
            .actionDescription(description)
            .actionDate(LocalDateTime.now())
            .ipAddress(ipAddress)
            .severity(success ? Severity.INFO : Severity.WARNING)
            .success(success)
            .build();
    }

    public static AuditLog createDataModification(Shop shop, String userId, String username,
                                                  ActionType actionType, String entityType,
                                                  String entityId, String description,
                                                  String oldValues, String newValues) {
        return AuditLog.builder()
            .shop(shop)
            .userId(userId)
            .username(username)
            .category(AuditCategory.DATA_MODIFICATION)
            .actionType(actionType)
            .entityType(entityType)
            .entityId(entityId)
            .actionDescription(description)
            .actionDate(LocalDateTime.now())
            .oldValues(oldValues)
            .newValues(newValues)
            .severity(Severity.INFO)
            .success(true)
            .build();
    }

    public static AuditLog createFinancialTransaction(Shop shop, String userId, String username,
                                                      ActionType actionType, String entityId,
                                                      String description, boolean success) {
        return AuditLog.builder()
            .shop(shop)
            .userId(userId)
            .username(username)
            .category(AuditCategory.FINANCIAL_TRANSACTION)
            .actionType(actionType)
            .entityType("FINANCIAL_TRANSACTION")
            .entityId(entityId)
            .actionDescription(description)
            .actionDate(LocalDateTime.now())
            .severity(success ? Severity.INFO : Severity.ERROR)
            .success(success)
            .build();
    }
}