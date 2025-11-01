package com.princely.shopmanager.expenses.controller;

import com.princely.shopmanager.shared.constants.PermissionConstants;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import com.princely.shopmanager.expenses.dto.*;
import com.princely.shopmanager.expenses.service.ExpenseService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller for expense management operations.
 * Uses granular permission-based authorization instead of role-based.
 * See docs/PERMISSION_MATRIX.md for complete permission matrix.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expenses", description = "Expense and procurement management operations")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
        summary = "Create new expense",
        description = "Create a new expense record for procurement or expenditure tracking"
    )
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @PostMapping("/shops/{shopId}/expenses")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_CREATE)")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Valid @RequestBody ExpenseCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Creating expense for shop: {}, user: {}", shopId, principal.getUsername());

        ExpenseResponse response = expenseService.createExpense(shopId, request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "Update expense",
        description = "Update an existing expense record"
    )
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @PutMapping("/expenses/{expenseId}")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_UPDATE)")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Updating expense: {}, user: {}", expenseId, principal.getUsername());

        ExpenseResponse response = expenseService.updateExpense(expenseId, request, principal);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get expense by ID",
        description = "Retrieve a specific expense by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Expense retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @GetMapping("/expenses/{expenseId}")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_READ)")
    public ResponseEntity<ExpenseResponse> getExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        ExpenseResponse response = expenseService.getExpenseById(expenseId, principal);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get expenses for shop",
        description = "Retrieve expenses for a shop with filtering and pagination"
    )
    @ApiResponse(responseCode = "200", description = "Expenses retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/expenses")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_LIST)")
    public ResponseEntity<Page<ExpenseResponse>> getExpenses(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Start date filter") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSS][.SS][.S]]") LocalDate startDate,
            @Parameter(description = "End date filter") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSS][.SS][.S]]") LocalDate endDate,
            @Parameter(description = "Status filter") @RequestParam(required = false) ExpenseStatus status,
            @Parameter(description = "Category ID filter") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Created by user filter") @RequestParam(required = false) UUID createdBy,
            @Parameter(description = "Minimum amount filter") @RequestParam(required = false) BigDecimal minAmount,
            @Parameter(description = "Maximum amount filter") @RequestParam(required = false) BigDecimal maxAmount,
            @Parameter(description = "Search query") @RequestParam(required = false) String search,
            @Parameter(description = "Payment method filter") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "Vendor name filter") @RequestParam(required = false) String vendorName,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "expenseDate") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal JwtPrincipal principal) {

        ExpenseFilterCriteria criteria = ExpenseFilterCriteria.builder()
            .startDate(startDate)
            .endDate(endDate)
            .status(status)
            .categoryId(categoryId)
            .createdBy(createdBy)
            .minAmount(minAmount)
            .maxAmount(maxAmount)
            .searchQuery(search)
            .paymentMethod(paymentMethod)
            .vendorName(vendorName)
            .build();

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ExpenseResponse> response = expenseService.getExpenses(shopId, criteria, pageable, principal);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Approve expense",
        description = "Approve a pending expense"
    )
    @ApiResponse(responseCode = "200", description = "Expense approved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or expense cannot be approved")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @PostMapping("/expenses/{expenseId}/approve")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_APPROVE)")
    public ResponseEntity<ExpenseResponse> approveExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Valid @RequestBody(required = false) ExpenseApprovalRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Approving expense: {}, user: {}", expenseId, principal.getUsername());

        if (request == null) {
            request = ExpenseApprovalRequest.builder().build();
        }

        ExpenseResponse response = expenseService.approveExpense(expenseId, request, principal);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Reject expense",
        description = "Reject a pending expense"
    )
    @ApiResponse(responseCode = "200", description = "Expense rejected successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or expense cannot be rejected")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @PostMapping("/expenses/{expenseId}/reject")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_APPROVE)")
    public ResponseEntity<ExpenseResponse> rejectExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @Valid @RequestBody(required = false) ExpenseApprovalRequest request,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Rejecting expense: {}, user: {}", expenseId, principal.getUsername());

        if (request == null) {
            request = ExpenseApprovalRequest.builder().build();
        }

        ExpenseResponse response = expenseService.rejectExpense(expenseId, request, principal);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete expense",
        description = "Delete an expense record"
    )
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @ApiResponse(responseCode = "400", description = "Expense cannot be deleted")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @DeleteMapping("/expenses/{expenseId}")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_DELETE)")
    public ResponseEntity<Void> deleteExpense(
            @Parameter(description = "Expense ID") @PathVariable UUID expenseId,
            @AuthenticationPrincipal JwtPrincipal principal) {

        log.info("Deleting expense: {}, user: {}", expenseId, principal.getUsername());

        expenseService.deleteExpense(expenseId, principal);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get expense summary",
        description = "Get expense summary and statistics for a shop"
    )
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @GetMapping("/shops/{shopId}/expenses/summary")
    @PreAuthorize("hasAuthority(T(com.princely.shopmanager.shared.constants.PermissionConstants).EXPENSE_LIST)")
    public ResponseEntity<ExpenseSummaryDto> getExpenseSummary(
            @Parameter(description = "Shop ID") @PathVariable String shopId,
            @Parameter(description = "Start date for summary") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSS][.SS][.S]]") LocalDate startDate,
            @Parameter(description = "End date for summary") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd['T'HH:mm:ss[.SSS][.SS][.S]]") LocalDate endDate,
            @AuthenticationPrincipal JwtPrincipal principal) {

        ExpenseSummaryDto response = expenseService.getExpenseSummary(shopId, startDate, endDate, principal);
        return ResponseEntity.ok(response);
    }
}