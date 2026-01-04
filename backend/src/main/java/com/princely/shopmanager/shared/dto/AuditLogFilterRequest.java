package com.princely.shopmanager.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for audit log filter parameters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Audit log filter parameters")
public class AuditLogFilterRequest {

    @Schema(description = "Search query for description, username, or entity", example = "product")
    private String search;

    @Schema(description = "Filter by action type", example = "CREATE")
    private String actionType;

    @Schema(description = "Filter by entity type", example = "PRODUCT")
    private String entityType;

    @Schema(description = "Filter by category", example = "DATA_MODIFICATION")
    private String category;

    @Schema(description = "Filter by user ID", example = "user-123")
    private String userId;

    @Schema(description = "Filter logs from this date onwards", example = "2024-01-01T00:00:00")
    private LocalDateTime dateFrom;

    @Schema(description = "Filter logs until this date", example = "2024-01-31T23:59:59")
    private LocalDateTime dateTo;

    @Schema(description = "Filter by severity", example = "WARNING")
    private String severity;

    @Schema(description = "Filter by success status", example = "true")
    private Boolean success;
}
