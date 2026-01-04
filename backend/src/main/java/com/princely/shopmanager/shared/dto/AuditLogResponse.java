package com.princely.shopmanager.shared.dto;

import com.princely.shopmanager.shared.domain.AuditLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data Transfer Object for audit log response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log entry response")
public class AuditLogResponse {

    @Schema(description = "Unique audit log identifier", example = "audit-123e4567-e89b-12d3-a456-426614174000")
    private String id;

    @Schema(description = "Shop identifier", example = "shop-123")
    private String shopId;

    @Schema(description = "Shop name", example = "Downtown Electronics")
    private String shopName;

    @Schema(description = "User identifier who performed the action", example = "user-123")
    private String userId;

    @Schema(description = "Username who performed the action", example = "john.doe")
    private String username;

    @Schema(description = "Audit category", example = "DATA_MODIFICATION")
    private String category;

    @Schema(description = "Action type", example = "CREATE")
    private String actionType;

    @Schema(description = "Entity type", example = "PRODUCT")
    private String entityType;

    @Schema(description = "Entity identifier", example = "product-456")
    private String entityId;

    @Schema(description = "Action description", example = "Created new product")
    private String actionDescription;

    @Schema(description = "When the action occurred", example = "2024-01-15T14:30:00")
    private LocalDateTime actionDate;

    @Schema(description = "IP address", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "User agent string")
    private String userAgent;

    @Schema(description = "Session identifier")
    private String sessionId;

    @Schema(description = "Additional details")
    private Map<String, String> details;

    @Schema(description = "Old values (for updates)")
    private String oldValues;

    @Schema(description = "New values (for creates/updates)")
    private String newValues;

    @Schema(description = "Severity level", example = "INFO")
    private String severity;

    @Schema(description = "Whether the action was successful")
    private boolean success;

    @Schema(description = "Error message if action failed")
    private String errorMessage;

    @Schema(description = "When the log entry was created")
    private LocalDateTime createdAt;

    /**
     * Factory method to create AuditLogResponse from AuditLog entity.
     */
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return AuditLogResponse.builder()
            .id(auditLog.getId())
            .shopId(auditLog.getShop() != null ? auditLog.getShop().getId() : null)
            .shopName(auditLog.getShop() != null ? auditLog.getShop().getName() : null)
            .userId(auditLog.getUserId())
            .username(auditLog.getUsername())
            .category(auditLog.getCategory() != null ? auditLog.getCategory().name() : null)
            .actionType(auditLog.getActionType() != null ? auditLog.getActionType().name() : null)
            .entityType(auditLog.getEntityType())
            .entityId(auditLog.getEntityId())
            .actionDescription(auditLog.getActionDescription())
            .actionDate(auditLog.getActionDate())
            .ipAddress(auditLog.getIpAddress())
            .userAgent(auditLog.getUserAgent())
            .sessionId(auditLog.getSessionId())
            .details(auditLog.getDetails())
            .oldValues(auditLog.getOldValues())
            .newValues(auditLog.getNewValues())
            .severity(auditLog.getSeverity() != null ? auditLog.getSeverity().name() : null)
            .success(auditLog.isSuccess())
            .errorMessage(auditLog.getErrorMessage())
            .createdAt(auditLog.getCreatedAt())
            .build();
    }
}
