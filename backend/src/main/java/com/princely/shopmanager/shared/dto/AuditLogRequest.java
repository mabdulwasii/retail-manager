package com.princely.shopmanager.shared.dto;

import com.princely.shopmanager.core.domain.Shop;
import com.princely.shopmanager.shared.domain.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request object for audit log operations.
 *
 * This parameter object encapsulates all the necessary information
 * for creating audit log entries, reducing method parameter count
 * and improving code maintainability.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequest {

    private Shop shop;
    private String userId;
    private String username;
    private AuditLog.AuditCategory category;
    private AuditLog.ActionType actionType;
    private String entityType;
    private String entityId;
    private String actionDescription;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String oldValues;
    private String newValues;
    private AuditLog.Severity severity;
    private Boolean success;
    private String errorMessage;
    private Map<String, String> details;
}