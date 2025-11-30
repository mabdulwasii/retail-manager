package com.princely.shopmanager.investment.controller;

import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.investment.domain.Investment;
import com.princely.shopmanager.investment.dto.*;
import com.princely.shopmanager.investment.service.InvestmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for investment and profit sharing operations.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investment", description = "Investment and profit sharing management operations")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentController {

    private final InvestmentService investmentService;

    /**
     * @deprecated Use InvestmentRoundController to create investment rounds with multiple investors.
     * This endpoint is kept for backward compatibility but should not be used for new implementations.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    @Operation(
        summary = "[DEPRECATED] Create new investment",
        description = "DEPRECATED: Use POST /api/shops/{shopId}/investment-rounds instead. " +
            "This endpoint is maintained for backward compatibility only."
    )
    @ApiResponse(responseCode = "501", description = "Not implemented - use investment rounds instead")
    @PostMapping("/investments")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CREATE)")
    public ResponseEntity<String> createInvestment(
            @Valid @RequestBody InvestmentCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body("This endpoint is deprecated. Please use POST /api/shops/{shopId}/investment-rounds to create investment rounds.");
    }

    @Operation(
        summary = "Get investments for shop",
        description = "Retrieve all investments for a specific shop with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/investments")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_LIST)")
    public ResponseEntity<Page<InvestmentResponse>> getShopInvestments(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @PageableDefault(size = 20, sort = "investmentDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Page<InvestmentResponse> response = investmentService.getInvestments(shopId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get investor's investments",
        description = "Retrieve all investments made by the authenticated investor"
    )
    @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/my-investments")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_LIST)")
    public ResponseEntity<Page<InvestmentResponse>> getMyInvestments(
            @PageableDefault(size = 20, sort = "investmentDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Page<InvestmentResponse> response = investmentService.getInvestmentsByInvestor(principal.getUserId(), pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get investment by ID",
        description = "Retrieve a specific investment by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Investment retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @GetMapping("/investments/{investmentId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_READ)")
    public ResponseEntity<InvestmentResponse> getInvestment(
            @Parameter(description = "Investment ID") @PathVariable String investmentId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InvestmentResponse response = investmentService.getInvestmentById(investmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update investment status",
        description = "Update the status of an investment (activate, deactivate, mature, etc.)"
    )
    @ApiResponse(responseCode = "200", description = "Investment status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @PutMapping("/investments/{investmentId}/status")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_UPDATE)")
    public ResponseEntity<InvestmentResponse> updateInvestmentStatus(
            @Parameter(description = "Investment ID") @PathVariable String investmentId,
            @Parameter(description = "New status") @RequestParam Investment.InvestmentStatus status,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating investment status: {}, new status: {}, user: {}",
                investmentId, status, principal.getUsername());

        InvestmentResponse response = investmentService.updateInvestmentStatus(investmentId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update investment status (PATCH)",
        description = "Update the status of an investment (PATCH). Preferred over PUT for partial updates."
    )
    @ApiResponse(responseCode = "200", description = "Investment status updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid status")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @PatchMapping("/investments/{investmentId}/status")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_UPDATE)")
    public ResponseEntity<InvestmentResponse> patchInvestmentStatus(
            @Parameter(description = "Investment ID") @PathVariable String investmentId,
            @Parameter(description = "New status") @RequestParam Investment.InvestmentStatus status,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Patching investment status: {}, new status: {}, user: {}",
                investmentId, status, principal.getUsername());

        // Reuse the same update logic
        InvestmentResponse response = investmentService.updateInvestmentStatus(investmentId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Process withdrawal",
        description = "Process a withdrawal request from an investment"
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid withdrawal request or insufficient balance")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @PostMapping("/investments/{investmentId}/withdraw")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CLOSE)")
    public ResponseEntity<InvestmentResponse> processWithdrawal(
            @Parameter(description = "Investment ID") @PathVariable String investmentId,
            @Valid @RequestBody WithdrawalRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Processing withdrawal for investment: {}, amount: {}, user: {}",
                investmentId, request.getAmount(), principal.getUsername());

        InvestmentResponse response = investmentService.processWithdrawal(investmentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get investment distributions",
        description = "Retrieve profit distributions for a specific investment"
    )
    @ApiResponse(responseCode = "200", description = "Distributions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @GetMapping("/investments/{investmentId}/distributions")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_READ)")
    public ResponseEntity<List<InvestorDistributionResponse>> getInvestmentDistributions(
            @Parameter(description = "Investment ID") @PathVariable String investmentId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<InvestorDistributionResponse> response = investmentService.getDistributions(investmentId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get my distributions",
        description = "Retrieve all profit distributions for the authenticated investor"
    )
    @ApiResponse(responseCode = "200", description = "Distributions retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/my-distributions")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_READ)")
    public ResponseEntity<List<InvestorDistributionResponse>> getMyDistributions(
            @AuthenticationPrincipal JwtPrincipal principal) {

        List<InvestorDistributionResponse> response = investmentService.getDistributionsByInvestor(principal.getSubject());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Approve distribution",
        description = "Approve a calculated profit distribution for payment"
    )
    @ApiResponse(responseCode = "200", description = "Distribution approved successfully")
    @ApiResponse(responseCode = "400", description = "Distribution cannot be approved")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Distribution not found")
    @PostMapping("/distributions/{distributionId}/approve")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_PROFIT_DISTRIBUTE)")
    public ResponseEntity<InvestorDistributionResponse> approveDistribution(
            @Parameter(description = "Distribution ID") @PathVariable String distributionId,
            @Parameter(description = "Approval notes") @RequestParam(required = false) String notes,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Approving distribution: {}, user: {}", distributionId, principal.getUsername());

        InvestorDistributionResponse response = investmentService.approveDistribution(distributionId, notes);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Mark distribution as paid",
        description = "Mark an approved distribution as paid with payment reference"
    )
    @ApiResponse(responseCode = "200", description = "Distribution marked as paid successfully")
    @ApiResponse(responseCode = "400", description = "Distribution cannot be marked as paid")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Distribution not found")
    @PostMapping("/distributions/{distributionId}/mark-paid")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_PROFIT_DISTRIBUTE)")
    public ResponseEntity<InvestorDistributionResponse> markDistributionAsPaid(
            @Parameter(description = "Distribution ID") @PathVariable String distributionId,
            @Parameter(description = "Payment reference") @RequestParam String paymentReference,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Marking distribution as paid: {}, payment reference: {}, user: {}",
                distributionId, paymentReference, principal.getUsername());

        InvestorDistributionResponse response = investmentService.markDistributionAsPaid(distributionId, paymentReference);
        return ResponseEntity.ok(response);
    }
}