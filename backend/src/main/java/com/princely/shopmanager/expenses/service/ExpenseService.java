package com.princely.shopmanager.expenses.service;

import com.princely.shopmanager.auth.security.ShopAccessValidator;
import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.domain.ExpenseCategory;
import com.princely.shopmanager.expenses.domain.ExpenseStatus;
import com.princely.shopmanager.expenses.dto.ExpenseApprovalRequest;
import com.princely.shopmanager.expenses.dto.ExpenseCategoryResponse;
import com.princely.shopmanager.expenses.dto.ExpenseCreateRequest;
import com.princely.shopmanager.expenses.dto.ExpenseFilterCriteria;
import com.princely.shopmanager.expenses.dto.ExpenseResponse;
import com.princely.shopmanager.expenses.dto.ExpenseSummaryDto;
import com.princely.shopmanager.expenses.dto.ExpenseUpdateRequest;
import com.princely.shopmanager.expenses.repository.ExpenseCategoryRepository;
import com.princely.shopmanager.expenses.repository.ExpenseRepository;
import com.princely.shopmanager.shared.domain.JwtPrincipal;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.BusinessRuleViolationException;
import com.princely.shopmanager.shared.exception.ErrorCode;
import com.princely.shopmanager.shared.exception.ShopNotFoundException;
import com.princely.shopmanager.shared.service.AuditService;
import com.princely.shopmanager.shared.service.ShopAwareService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing expenses and expenditures
 */
@Service
@Slf4j
@Transactional(readOnly = true)
public class ExpenseService extends ShopAwareService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final AuditService auditService;
    private final ExpenseFieldUpdater fieldUpdater;
    private final ExpenseSpecificationBuilder specificationBuilder;

    public ExpenseService(
            ShopAccessValidator shopAccessValidator,
            com.princely.shopmanager.core.repository.ShopRepository shopRepository,
            ExpenseRepository expenseRepository,
            ExpenseCategoryRepository categoryRepository,
            AuditService auditService,
            ExpenseFieldUpdater fieldUpdater,
            ExpenseSpecificationBuilder specificationBuilder
    ) {
        super(shopAccessValidator, shopRepository);
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
        this.auditService = auditService;
        this.fieldUpdater = fieldUpdater;
        this.specificationBuilder = specificationBuilder;
    }

    /**
     * Create a new expense
     */
    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ACCOUNTANT')")
    public ExpenseResponse createExpense(String shopId, ExpenseCreateRequest request, JwtPrincipal principal) {
        log.info("Creating expense for shop: {}, title: {}", shopId, request.title());

        validateShopAccess(shopId, principal);

        // Validate category exists and belongs to the shop
        ExpenseCategory category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, request.categoryId()));

        // Validate category belongs to the shop
        if (!category.getShopId().equals(shopId)) {
            throw new AccessDeniedException("You don't have permission to use this expense category");
        }

        if (Boolean.FALSE.equals(category.getIsActive())) {
            throw new BusinessRuleViolationException("Cannot create expense for inactive category");
        }

        // Determine initial status based on auto-approval
        ExpenseStatus initialStatus = ExpenseStatus.DRAFT;
        if (category.canAutoApprove(request.amount())) {
            initialStatus = ExpenseStatus.APPROVED;
        } else if (Boolean.TRUE.equals(request.submitForApproval())) {
            initialStatus = ExpenseStatus.PENDING_APPROVAL;
        }

        // Create expense entity
        Expense.ExpenseBuilder expenseBuilder = Expense.builder()
            .shopId(shopId)
            .title(request.title().trim())
            .description(request.description() != null ? request.description().trim() : null)
            .categoryId(request.categoryId())
            .amount(request.amount())
            .expenseDate(request.expenseDate())
            .paymentMethod(request.paymentMethod())
            .vendorName(request.vendorName())
            .referenceNumber(request.referenceNumber())
            .tags(request.tags() != null ? fieldUpdater.cleanTags(request.tags()) : new HashSet<>())
            .notes(request.notes())
            .expenseCreatedBy(principal.getUserId())
            .createdByName(principal.getFullName())
            .status(initialStatus);

        // Add approval details if auto-approved
        if (category.canAutoApprove(request.amount())) {
            expenseBuilder
                .approvedBy(principal.getUserId())
                .approvedByName(principal.getFullName())
                .approvalDate(LocalDate.now())
                .approvalNotes("Auto-approved based on category settings");
        }

        Expense expense = expenseBuilder.build();

        expense = expenseRepository.save(expense);

        // Audit log
        auditService.logExpenseCreation(expense.getId(), shopId, principal.getUserId(), expense.getAmount());

        log.info("Expense created successfully: {}", expense.getId());
        return mapToResponse(expense, category);
    }

    /**
     * Update an existing expense
     */
    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER', 'ACCOUNTANT')")
    public ExpenseResponse updateExpense(UUID expenseId, ExpenseUpdateRequest request, JwtPrincipal principal) {
        log.info("Updating expense: {}", expenseId);

        Expense expense = findExpenseForUser(expenseId, principal);

        if (!expense.canBeEdited()) {
            throw new BusinessRuleViolationException("Expense cannot be edited in current status: " + expense.getStatus());
        }

        // Delegate field updates to specialized service
        fieldUpdater.updateBasicFields(expense, request);
        fieldUpdater.updateAmountAndDate(expense, request);
        fieldUpdater.updateCategory(expense, request.categoryId());
        fieldUpdater.updateTags(expense, request.tags());

        // Handle status change or submission for approval
        if (Boolean.TRUE.equals(request.submitForApproval()) && expense.getStatus() == ExpenseStatus.DRAFT) {
            expense.submitForApproval();
        }

        expense = expenseRepository.save(expense);

        // Audit log
        auditService.logExpenseUpdate(expense.getId(), expense.getShopId(), principal.getUserId());

        log.info("Expense updated successfully: {}", expense.getId());

        UUID categoryId = expense.getCategoryId();
        ExpenseCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, categoryId));

        return mapToResponse(expense, category);
    }

    /**
     * Approve an expense
     */
    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ExpenseResponse approveExpense(UUID expenseId, ExpenseApprovalRequest request, JwtPrincipal principal) {
        log.info("Approving expense: {}", expenseId);

        Expense expense = findExpenseForUser(expenseId, principal);

        if (expense.cannotBeApproved()) {
            throw new BusinessRuleViolationException("Expense cannot be approved in current status: " + expense.getStatus());
        }

        expense.approve(principal.getUserId(), principal.getFullName(), request.notes());
        expense = expenseRepository.save(expense);

        // Audit log
        auditService.logExpenseApproval(expense.getId(), expense.getShopId(), principal.getUserId(), true);

        log.info("Expense approved successfully: {}", expense.getId());

        UUID categoryId = expense.getCategoryId();
        ExpenseCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, categoryId));

        return mapToResponse(expense, category);
    }

    /**
     * Reject an expense
     */
    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ExpenseResponse rejectExpense(UUID expenseId, ExpenseApprovalRequest request, JwtPrincipal principal) {
        log.info("Rejecting expense: {}", expenseId);

        Expense expense = findExpenseForUser(expenseId, principal);

        if (expense.cannotBeApproved()) {
            throw new BusinessRuleViolationException("Expense cannot be rejected in current status: " + expense.getStatus());
        }

        expense.reject(principal.getUserId(), principal.getFullName(), request.notes());
        expense = expenseRepository.save(expense);

        // Audit log
        auditService.logExpenseApproval(expense.getId(), expense.getShopId(), principal.getUserId(), false);

        log.info("Expense rejected successfully: {}", expense.getId());

        UUID categoryId = expense.getCategoryId();
        ExpenseCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, categoryId));

        return mapToResponse(expense, category);
    }

    /**
     * Get expense by ID
     */
    public ExpenseResponse getExpenseById(UUID expenseId, JwtPrincipal principal) {
        Expense expense = findExpenseForUser(expenseId, principal);

        UUID categoryId = expense.getCategoryId();
        ExpenseCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, categoryId));

        return mapToResponse(expense, category);
    }

    /**
     * Get expenses for a shop with filtering and pagination
     */
    public Page<ExpenseResponse> getExpenses(String shopId, ExpenseFilterCriteria criteria, Pageable pageable, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);

        Specification<Expense> spec = createExpenseSpecification(shopId, criteria);
        Page<Expense> expenses = expenseRepository.findAll(spec, pageable);

        return expenses.map(this::mapToResponseWithCategory);
    }

    /**
     * Delete an expense
     */
    @Transactional
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public void deleteExpense(UUID expenseId, JwtPrincipal principal) {
        log.info("Deleting expense: {}", expenseId);

        Expense expense = findExpenseForUser(expenseId, principal);

        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new BusinessRuleViolationException("Cannot delete paid expenses");
        }

        if (expense.getStatus() == ExpenseStatus.APPROVED) {
            throw new BusinessRuleViolationException("Cannot delete approved expenses. Please reject first if necessary.");
        }

        expenseRepository.delete(expense);

        // Audit log
        auditService.logExpenseDeletion(expense.getId(), expense.getShopId(), principal.getUserId());

        log.info("Expense deleted successfully: {}", expenseId);
    }

    /**
     * Get expense summary for a shop
     */
    public ExpenseSummaryDto getExpenseSummary(String shopId, LocalDate startDate, LocalDate endDate, JwtPrincipal principal) {
        validateShopAccess(shopId, principal);

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // Get basic counts
        List<Object[]> statusCounts = expenseRepository.countExpensesByStatus(shopId);
        Map<ExpenseStatus, Long> statusCountMap = statusCounts.stream()
            .collect(Collectors.toMap(
                row -> ExpenseStatus.valueOf((String) row[0]),
                row -> (Long) row[1]
            ));

        // Get total amounts
        BigDecimal totalAmount = expenseRepository.calculateTotalExpensesByShopAndDateRange(shopId, startDate, endDate);
        BigDecimal monthlyTotal = expenseRepository.calculateTotalExpensesByShopAndDateRange(
            shopId, LocalDate.now().withDayOfMonth(1), LocalDate.now()
        );

        // Get category breakdown
        List<Object[]> categoryBreakdown = expenseRepository.calculateExpensesByCategoryAndDateRange(shopId, startDate, endDate);

        return ExpenseSummaryDto.builder()
            .totalExpenses(statusCountMap.values().stream().mapToLong(Long::longValue).sum())
            .pendingApproval(statusCountMap.getOrDefault(ExpenseStatus.PENDING_APPROVAL, 0L))
            .approvedExpenses(statusCountMap.getOrDefault(ExpenseStatus.APPROVED, 0L) + statusCountMap.getOrDefault(ExpenseStatus.PAID, 0L))
            .totalAmount(totalAmount != null ? totalAmount : BigDecimal.ZERO)
            .monthlyTotal(monthlyTotal != null ? monthlyTotal : BigDecimal.ZERO)
            .categoryBreakdown(mapCategoryBreakdown(categoryBreakdown))
            .build();
    }

    // Private helper methods

    private Expense findExpenseForUser(UUID expenseId, JwtPrincipal principal) {
        // Step 1: Check if expense exists
        Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_NOT_FOUND, expenseId));

        // Step 2: Check permissions using ShopAccessValidator
        if (shopAccessValidator.hasNoAccessToShop(expense.getShopId(), principal)) {
            throw new AccessDeniedException("You don't have permission to access this expense");
        }

        return expense;
    }

    // Note: validateShopAccess() is inherited from ShopAwareService parent class

    private Specification<Expense> createExpenseSpecification(String shopId, ExpenseFilterCriteria criteria) {
        return specificationBuilder.buildSpecification(shopId, criteria);
    }

    private ExpenseResponse mapToResponseWithCategory(Expense expense) {
        ExpenseCategory category = categoryRepository.findById(expense.getCategoryId())
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, expense.getCategoryId()));
        return mapToResponse(expense, category);
    }

    private ExpenseResponse mapToResponse(Expense expense, ExpenseCategory category) {
        return ExpenseResponse.builder()
            .id(expense.getId())
            .shopId(expense.getShopId())
            .title(expense.getTitle())
            .description(expense.getDescription())
            .category(mapCategoryToResponse(category))
            .amount(expense.getAmount())
            .expenseDate(expense.getExpenseDate())
            .paymentMethod(expense.getPaymentMethod())
            .vendorName(expense.getVendorName())
            .referenceNumber(expense.getReferenceNumber())
            .receiptUrl(expense.getReceiptUrl())
            .status(expense.getStatus())
            .tags(expense.getTags())
            .notes(expense.getNotes())
            .createdBy(expense.getExpenseCreatedBy())
            .createdByName(expense.getCreatedByName())
            .approvedBy(expense.getApprovedBy())
            .approvedByName(expense.getApprovedByName())
            .approvalDate(expense.getApprovalDate())
            .approvalNotes(expense.getApprovalNotes())
            .createdAt(expense.getCreatedAt())
            .updatedAt(expense.getUpdatedAt())
            .version(expense.getVersion())
            .build();
    }

    private ExpenseCategoryResponse mapCategoryToResponse(ExpenseCategory category) {
        return ExpenseCategoryResponse.builder()
            .id(category.getId())
            .name(category.getName())
            .description(category.getDescription())
            .requiresApproval(category.getRequiresApproval())
            .approvalLimit(category.getApprovalLimit())
            .defaultPaymentMethod(category.getDefaultPaymentMethod())
            .taxDeductible(category.getTaxDeductible())
            .build();
    }

    private List<ExpenseSummaryDto.CategoryBreakdown> mapCategoryBreakdown(List<Object[]> breakdown) {
        return breakdown.stream()
            .map(row -> {
                UUID categoryId = (UUID) row[0];
                BigDecimal amount = (BigDecimal) row[1];

                ExpenseCategory category = categoryRepository.findById(categoryId)
                    .orElse(null);

                return ExpenseSummaryDto.CategoryBreakdown.builder()
                    .categoryId(categoryId)
                    .categoryName(category != null ? category.getName() : "Unknown")
                    .amount(amount)
                    .count(1L) // We'd need a separate query for count
                    .build();
            })
            .collect(Collectors.toList());
    }
}