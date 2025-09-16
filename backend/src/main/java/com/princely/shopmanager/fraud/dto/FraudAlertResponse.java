package com.princely.shopmanager.fraud.dto;

import com.princely.shopmanager.fraud.domain.FraudAlert;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class FraudAlertResponse {
    private String id;
    private String alertNumber;
    private FraudAlert.AlertType alertType;
    private FraudAlert.AlertSeverity severity;
    private FraudAlert.AlertStatus status;
    private String title;
    private String description;
    private String shopId;
    private String shopName;
    private String userId;
    private String userName;
    private String transactionId;
    private String investmentId;
    private BigDecimal riskScore;
    private BigDecimal confidenceLevel;
    private Map<String, String> evidence;
    private String detectionRule;
    private LocalDateTime detectionTimestamp;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String resolutionNotes;
    private Boolean falsePositive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}