package com.princely.shopmanager.fraud.dto;

import com.princely.shopmanager.fraud.domain.RiskAssessment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RiskAssessmentResponse {
    private String id;
    private String shopId;
    private String shopName;
    private String transactionId;
    private String transactionNumber;
    private RiskAssessment.AssessmentType assessmentType;
    private RiskAssessment.RiskLevel riskLevel;
    private BigDecimal riskScore;
    private LocalDateTime assessmentDate;
    private List<String> flags;
    private String details;
    private RiskAssessment.AssessmentStatus status;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String reviewNotes;
    private RiskAssessment.ResolutionAction resolutionAction;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}