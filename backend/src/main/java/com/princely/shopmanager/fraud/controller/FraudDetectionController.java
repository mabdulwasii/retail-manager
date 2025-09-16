package com.princely.shopmanager.fraud.controller;

import com.princely.shopmanager.auth.domain.JwtPrincipal;
import com.princely.shopmanager.fraud.domain.FraudAlert;
import com.princely.shopmanager.fraud.domain.FraudRule;
import com.princely.shopmanager.fraud.domain.RiskAssessment;
import com.princely.shopmanager.fraud.dto.FraudAlertResponse;
import com.princely.shopmanager.fraud.dto.FraudRuleRequest;
import com.princely.shopmanager.fraud.dto.RiskAssessmentResponse;
import com.princely.shopmanager.fraud.service.FraudDetectionService;
import com.princely.shopmanager.fraud.service.FraudManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fraud Detection", description = "Fraud detection and risk management operations")
@SecurityRequirement(name = "bearerAuth")
public class FraudDetectionController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudManagementService fraudManagementService;

    @Operation(
        summary = "Get fraud alerts",
        description = "Retrieve fraud alerts with pagination and filtering"
    )
    @ApiResponse(responseCode = "200", description = "Fraud alerts retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/alerts")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Page<FraudAlertResponse>> getFraudAlerts(
            @Parameter(description = "Shop ID filter") @RequestParam(required = false) String shopId,
            @Parameter(description = "Alert status filter") @RequestParam(required = false) FraudAlert.AlertStatus status,
            @Parameter(description = "Alert severity filter") @RequestParam(required = false) FraudAlert.AlertSeverity severity,
            @Parameter(description = "Alert type filter") @RequestParam(required = false) FraudAlert.AlertType alertType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "detectionTimestamp") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FraudAlertResponse> alerts = fraudManagementService.getFraudAlerts(
            shopId, status, severity, alertType, pageable);

        return ResponseEntity.ok(alerts);
    }

    @Operation(
        summary = "Get fraud alert by ID",
        description = "Retrieve a specific fraud alert by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Fraud alert retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Fraud alert not found")
    @GetMapping("/alerts/{alertId}")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudAlertResponse> getFraudAlert(
            @Parameter(description = "Alert ID") @PathVariable String alertId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        FraudAlertResponse alert = fraudManagementService.getFraudAlertById(alertId);
        return ResponseEntity.ok(alert);
    }

    @Operation(
        summary = "Acknowledge fraud alert",
        description = "Acknowledge a fraud alert to indicate it's being reviewed"
    )
    @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully")
    @ApiResponse(responseCode = "400", description = "Alert cannot be acknowledged")
    @PostMapping("/alerts/{alertId}/acknowledge")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudAlertResponse> acknowledgeFraudAlert(
            @Parameter(description = "Alert ID") @PathVariable String alertId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Acknowledging fraud alert: {}, user: {}", alertId, principal.getUsername());

        fraudDetectionService.acknowledgeFraudAlert(alertId, principal.getSubject());
        FraudAlertResponse alert = fraudManagementService.getFraudAlertById(alertId);

        return ResponseEntity.ok(alert);
    }

    @Operation(
        summary = "Resolve fraud alert",
        description = "Resolve a fraud alert with resolution notes"
    )
    @ApiResponse(responseCode = "200", description = "Alert resolved successfully")
    @ApiResponse(responseCode = "400", description = "Alert cannot be resolved")
    @PostMapping("/alerts/{alertId}/resolve")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudAlertResponse> resolveFraudAlert(
            @Parameter(description = "Alert ID") @PathVariable String alertId,
            @Parameter(description = "Resolution notes") @RequestParam String resolutionNotes,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Resolving fraud alert: {}, user: {}", alertId, principal.getUsername());

        fraudDetectionService.resolveFraudAlert(alertId, principal.getSubject(), resolutionNotes);
        FraudAlertResponse alert = fraudManagementService.getFraudAlertById(alertId);

        return ResponseEntity.ok(alert);
    }

    @Operation(
        summary = "Mark alert as false positive",
        description = "Mark a fraud alert as a false positive"
    )
    @ApiResponse(responseCode = "200", description = "Alert marked as false positive successfully")
    @PostMapping("/alerts/{alertId}/false-positive")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudAlertResponse> markAsFalsePositive(
            @Parameter(description = "Alert ID") @PathVariable String alertId,
            @Parameter(description = "Reason for false positive") @RequestParam String reason,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Marking fraud alert as false positive: {}, user: {}", alertId, principal.getUsername());

        fraudManagementService.markAlertAsFalsePositive(alertId, principal.getSubject(), reason);
        FraudAlertResponse alert = fraudManagementService.getFraudAlertById(alertId);

        return ResponseEntity.ok(alert);
    }

    @Operation(
        summary = "Get risk assessments",
        description = "Retrieve risk assessments with pagination and filtering"
    )
    @ApiResponse(responseCode = "200", description = "Risk assessments retrieved successfully")
    @GetMapping("/risk-assessments")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Page<RiskAssessmentResponse>> getRiskAssessments(
            @Parameter(description = "Shop ID filter") @RequestParam(required = false) String shopId,
            @Parameter(description = "Risk level filter") @RequestParam(required = false) RiskAssessment.RiskLevel riskLevel,
            @Parameter(description = "Assessment status filter") @RequestParam(required = false) RiskAssessment.AssessmentStatus status,
            @Parameter(description = "Assessment type filter") @RequestParam(required = false) RiskAssessment.AssessmentType assessmentType,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "assessmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<RiskAssessmentResponse> assessments = fraudManagementService.getRiskAssessments(
            shopId, riskLevel, status, assessmentType, pageable);

        return ResponseEntity.ok(assessments);
    }

    @Operation(
        summary = "Approve risk assessment",
        description = "Approve a pending risk assessment"
    )
    @ApiResponse(responseCode = "200", description = "Risk assessment approved successfully")
    @PostMapping("/risk-assessments/{assessmentId}/approve")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<RiskAssessmentResponse> approveRiskAssessment(
            @Parameter(description = "Assessment ID") @PathVariable String assessmentId,
            @Parameter(description = "Review notes") @RequestParam(required = false) String reviewNotes,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Approving risk assessment: {}, user: {}", assessmentId, principal.getUsername());

        fraudDetectionService.approveRiskAssessment(assessmentId, principal.getSubject(), reviewNotes);
        RiskAssessmentResponse assessment = fraudManagementService.getRiskAssessmentById(assessmentId);

        return ResponseEntity.ok(assessment);
    }

    @Operation(
        summary = "Reject risk assessment",
        description = "Reject a pending risk assessment with resolution action"
    )
    @ApiResponse(responseCode = "200", description = "Risk assessment rejected successfully")
    @PostMapping("/risk-assessments/{assessmentId}/reject")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<RiskAssessmentResponse> rejectRiskAssessment(
            @Parameter(description = "Assessment ID") @PathVariable String assessmentId,
            @Parameter(description = "Review notes") @RequestParam String reviewNotes,
            @Parameter(description = "Resolution action") @RequestParam RiskAssessment.ResolutionAction action,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Rejecting risk assessment: {}, action: {}, user: {}",
                assessmentId, action, principal.getUsername());

        fraudDetectionService.rejectRiskAssessment(assessmentId, principal.getSubject(), reviewNotes, action);
        RiskAssessmentResponse assessment = fraudManagementService.getRiskAssessmentById(assessmentId);

        return ResponseEntity.ok(assessment);
    }

    @Operation(
        summary = "Get fraud rules",
        description = "Retrieve fraud detection rules with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Fraud rules retrieved successfully")
    @GetMapping("/rules")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Page<FraudRule>> getFraudRules(
            @Parameter(description = "Shop ID filter") @RequestParam(required = false) String shopId,
            @Parameter(description = "Rule type filter") @RequestParam(required = false) FraudRule.FraudRuleType ruleType,
            @Parameter(description = "Enabled filter") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "ruleName") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FraudRule> rules = fraudManagementService.getFraudRules(shopId, ruleType, enabled, pageable);
        return ResponseEntity.ok(rules);
    }

    @Operation(
        summary = "Create fraud rule",
        description = "Create a new fraud detection rule"
    )
    @ApiResponse(responseCode = "201", description = "Fraud rule created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping("/rules")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudRule> createFraudRule(
            @Valid @RequestBody FraudRuleRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating fraud rule: {}, user: {}", request.getRuleName(), principal.getUsername());

        FraudRule rule = fraudManagementService.createFraudRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(rule);
    }

    @Operation(
        summary = "Update fraud rule",
        description = "Update an existing fraud detection rule"
    )
    @ApiResponse(responseCode = "200", description = "Fraud rule updated successfully")
    @PutMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudRule> updateFraudRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId,
            @Valid @RequestBody FraudRuleRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating fraud rule: {}, user: {}", ruleId, principal.getUsername());

        FraudRule rule = fraudManagementService.updateFraudRule(ruleId, request);
        return ResponseEntity.ok(rule);
    }

    @Operation(
        summary = "Delete fraud rule",
        description = "Delete a fraud detection rule"
    )
    @ApiResponse(responseCode = "204", description = "Fraud rule deleted successfully")
    @DeleteMapping("/rules/{ruleId}")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deleteFraudRule(
            @Parameter(description = "Rule ID") @PathVariable String ruleId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting fraud rule: {}, user: {}", ruleId, principal.getUsername());

        fraudManagementService.deleteFraudRule(ruleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get fraud statistics",
        description = "Get fraud detection statistics and metrics"
    )
    @ApiResponse(responseCode = "200", description = "Fraud statistics retrieved successfully")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SHOP_MANAGER') or hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Map<String, Object>> getFraudStatistics(
            @Parameter(description = "Shop ID filter") @RequestParam(required = false) String shopId,
            @Parameter(description = "Start date") @RequestParam(required = false) LocalDateTime startDate,
            @Parameter(description = "End date") @RequestParam(required = false) LocalDateTime endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Map<String, Object> statistics = fraudManagementService.getFraudStatistics(shopId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
        summary = "Enable/Disable fraud rule",
        description = "Enable or disable a fraud detection rule"
    )
    @ApiResponse(responseCode = "200", description = "Fraud rule status updated successfully")
    @PutMapping("/rules/{ruleId}/status")
    @PreAuthorize("hasRole('SHOP_OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<FraudRule> updateRuleStatus(
            @Parameter(description = "Rule ID") @PathVariable String ruleId,
            @Parameter(description = "Enabled status") @RequestParam boolean enabled,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating fraud rule status: {}, enabled: {}, user: {}",
                ruleId, enabled, principal.getUsername());

        FraudRule rule = fraudManagementService.updateRuleStatus(ruleId, enabled);
        return ResponseEntity.ok(rule);
    }
}