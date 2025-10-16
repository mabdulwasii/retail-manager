package com.princely.shopmanager.investment.controller;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for investment and profit sharing operations
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investment", description = "Investment and profit sharing management operations")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentController {

    private final InvestmentService investmentService;

    @Operation(
        summary = "Create new investment",
        description = "Create a new investment in a shop or specific products"
    )
    @ApiResponse(responseCode = "201", description = "Investment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Shop or products not found")
    @PostMapping("/investments")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<InvestmentResponse> createInvestment(
            @Valid @RequestBody InvestmentCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating investment for shop: {}, user: {}", request.getShopId(), principal.getUsername());

        InvestmentResponse response = investmentService.createInvestment(request, principal.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Get investments for shop",
        description = "Retrieve all investments for a specific shop with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Investments retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/investments")
    @PreAuthorize("hasRole('MANAGER') or hasRole('OWNER') or hasRole('TENANT_ADMIN')")
    public ResponseEntity<Page<InvestmentResponse>> getShopInvestments(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "investmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

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
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<Page<InvestmentResponse>> getMyInvestments(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "investmentDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InvestmentResponse> response = investmentService.getInvestmentsByInvestor(principal.getSubject(), pageable);
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
    @PreAuthorize("hasRole('INVESTOR') or hasRole('MANAGER') or hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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
    @PreAuthorize("hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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
        summary = "Process withdrawal",
        description = "Process a withdrawal request from an investment"
    )
    @ApiResponse(responseCode = "200", description = "Withdrawal processed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid withdrawal request or insufficient balance")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Investment not found")
    @PostMapping("/investments/{investmentId}/withdraw")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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
    @PreAuthorize("hasRole('INVESTOR') or hasRole('MANAGER') or hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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
    @PreAuthorize("hasRole('INVESTOR')")
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
    @PreAuthorize("hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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
    @PreAuthorize("hasRole('OWNER') or hasRole('TENANT_ADMIN')")
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