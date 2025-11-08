package com.princely.shopmanager.investment.controller;

import com.princely.shopmanager.investment.dto.InvestmentRoundCreateRequest;
import com.princely.shopmanager.investment.dto.InvestmentRoundResponse;
import com.princely.shopmanager.investment.service.InvestmentRoundService;
import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
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

/**
 * REST Controller for investment round operations.
 *
 * Investment rounds allow batch creation of investments with shared configuration.
 * Only SYSTEM_ADMIN, TENANT_ADMIN, and OWNER can manage rounds.
 * See docs/INVESTMENT_GUIDE.md for detailed usage.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Investment Rounds", description = "Investment round management operations")
@SecurityRequirement(name = "bearerAuth")
public class InvestmentRoundController {

    private final InvestmentRoundService investmentRoundService;

    @Operation(
        summary = "Create investment round",
        description = "Create a new investment round with multiple investors. " +
            "All investors in the round share the same profit sharing model, investment type, and maturity date."
    )
    @ApiResponse(responseCode = "201", description = "Investment round created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or validation failed")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_CREATE permission")
    @ApiResponse(responseCode = "404", description = "Shop or investor not found")
    @PostMapping("/shops/{shopId}/investment-rounds")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CREATE)")
    public ResponseEntity<InvestmentRoundResponse> createInvestmentRound(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Valid @RequestBody InvestmentRoundCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating investment round for shop {} with {} investors by user {}",
            shopId, request.getInvestors().size(), principal.getUsername());

        // Ensure shopId from path matches request
        request.setShopId(shopId);

        InvestmentRoundResponse response = investmentRoundService.createInvestmentRound(request, principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "List investment rounds",
        description = "Retrieve all investment rounds for a shop with pagination"
    )
    @ApiResponse(responseCode = "200", description = "Investment rounds retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_LIST permission")
    @GetMapping("/shops/{shopId}/investment-rounds")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_LIST)")
    public ResponseEntity<Page<InvestmentRoundResponse>> listInvestmentRounds(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InvestmentRoundResponse> response = investmentRoundService.listInvestmentRounds(shopId, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get investment round",
        description = "Retrieve details of a specific investment round including all investments"
    )
    @ApiResponse(responseCode = "200", description = "Investment round retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_READ permission")
    @ApiResponse(responseCode = "404", description = "Investment round not found")
    @GetMapping("/investment-rounds/{roundId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_READ)")
    public ResponseEntity<InvestmentRoundResponse> getInvestmentRound(
            @Parameter(description = "Investment round ID") @PathVariable String roundId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        InvestmentRoundResponse response = investmentRoundService.getInvestmentRound(roundId);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update investment round",
        description = "Update investment round configuration. Only notes and maturity date can be updated. " +
            "Cannot modify investors or amounts after creation."
    )
    @ApiResponse(responseCode = "200", description = "Investment round updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data or round is closed/completed")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_UPDATE permission")
    @ApiResponse(responseCode = "404", description = "Investment round not found")
    @PutMapping("/investment-rounds/{roundId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_UPDATE)")
    public ResponseEntity<InvestmentRoundResponse> updateInvestmentRound(
            @Parameter(description = "Investment round ID") @PathVariable String roundId,
            @Valid @RequestBody InvestmentRoundCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating investment round {} by user {}", roundId, principal.getUsername());

        InvestmentRoundResponse response = investmentRoundService.updateInvestmentRound(
            roundId, request, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete investment round",
        description = "Delete an investment round. Only allowed if no profit distributions have been made."
    )
    @ApiResponse(responseCode = "204", description = "Investment round deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete round with profit distributions")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_DELETE permission")
    @ApiResponse(responseCode = "404", description = "Investment round not found")
    @DeleteMapping("/investment-rounds/{roundId}")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_DELETE)")
    public ResponseEntity<Void> deleteInvestmentRound(
            @Parameter(description = "Investment round ID") @PathVariable String roundId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting investment round {} by user {}", roundId, principal.getUsername());

        investmentRoundService.deleteInvestmentRound(roundId, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Close investment round",
        description = "Close an investment round to new investors. Round status changes to CLOSED."
    )
    @ApiResponse(responseCode = "200", description = "Investment round closed successfully")
    @ApiResponse(responseCode = "400", description = "Round is not in OPEN status")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_CLOSE permission")
    @ApiResponse(responseCode = "404", description = "Investment round not found")
    @PostMapping("/investment-rounds/{roundId}/close")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CLOSE)")
    public ResponseEntity<InvestmentRoundResponse> closeInvestmentRound(
            @Parameter(description = "Investment round ID") @PathVariable String roundId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Closing investment round {} by user {}", roundId, principal.getUsername());

        InvestmentRoundResponse response = investmentRoundService.closeRound(roundId, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Add investor to round",
        description = "Add a new investor to an existing OPEN investment round"
    )
    @ApiResponse(responseCode = "200", description = "Investor added successfully")
    @ApiResponse(responseCode = "400", description = "Round is not OPEN or investor already in round")
    @ApiResponse(responseCode = "403", description = "Access denied - requires INVESTMENT_CREATE permission")
    @ApiResponse(responseCode = "404", description = "Investment round or investor not found")
    @PostMapping("/investment-rounds/{roundId}/investors")
    @PreAuthorize("hasPermission(null, T(com.princely.shopmanager.shared.constants.PermissionConstants).INVESTMENT_CREATE)")
    public ResponseEntity<InvestmentRoundResponse> addInvestorToRound(
            @Parameter(description = "Investment round ID") @PathVariable String roundId,
            @Valid @RequestBody InvestmentRoundCreateRequest.InvestorInput investorInput,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Adding investor {} to round {} by user {}",
            investorInput.getInvestorId(), roundId, principal.getUsername());

        InvestmentRoundResponse response = investmentRoundService.addInvestorToRound(
            roundId, investorInput, principal.getUsername());
        return ResponseEntity.ok(response);
    }
}
