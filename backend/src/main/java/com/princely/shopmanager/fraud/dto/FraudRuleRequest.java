package com.princely.shopmanager.fraud.dto;

import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FraudRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(max = 100, message = "Rule name must not exceed 100 characters")
    private String ruleName;

    @NotNull(message = "Rule type is required")
    private FraudRule.FraudRuleType ruleType;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String shopId; // null for global rules

    private Boolean enabled = true;

    @DecimalMin(value = "0.0", message = "Threshold amount must be positive")
    private BigDecimal thresholdAmount;

    @Min(value = 1, message = "Threshold count must be at least 1")
    private Integer thresholdCount;

    @Min(value = 1, message = "Time window must be at least 1 minute")
    @Max(value = 1440, message = "Time window must not exceed 1440 minutes (24 hours)")
    private Integer timeWindowMinutes;

    @DecimalMin(value = "0.1", message = "Risk score weight must be at least 0.1")
    @DecimalMax(value = "10.0", message = "Risk score weight must not exceed 10.0")
    private BigDecimal riskScoreWeight = BigDecimal.valueOf(1.0);

    @NotNull(message = "Severity is required")
    private RiskAssessment.RiskLevel severity = RiskAssessment.RiskLevel.MEDIUM;

    private Boolean autoBlock = false;

    private Boolean requiresManualReview = true;

    @Size(max = 2000, message = "Rule configuration must not exceed 2000 characters")
    private String ruleConfiguration;
}