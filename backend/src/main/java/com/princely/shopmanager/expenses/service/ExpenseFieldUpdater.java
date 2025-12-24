package com.princely.shopmanager.expenses.service;

import com.princely.shopmanager.expenses.domain.Expense;
import com.princely.shopmanager.expenses.domain.ExpenseCategory;
import com.princely.shopmanager.expenses.dto.ExpenseUpdateRequest;
import com.princely.shopmanager.expenses.repository.ExpenseCategoryRepository;
import com.princely.shopmanager.shared.exception.BusinessException;
import com.princely.shopmanager.shared.exception.BusinessRuleViolationException;
import com.princely.shopmanager.shared.exception.ErrorCode;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for updating expense fields.
 * Handles validation and field assignment logic for expense updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseFieldUpdater {

    private final ExpenseCategoryRepository categoryRepository;

    /**
     * Updates basic expense fields (title, description, vendor, etc).
     *
     * @param expense Expense to update
     * @param request Update request
     */
    public void updateBasicFields(Expense expense, ExpenseUpdateRequest request) {
        if (request.title() != null) {
            expense.setTitle(request.title().trim());
        }

        if (request.description() != null) {
            expense.setDescription(request.description().trim());
        }

        if (request.vendorName() != null) {
            expense.setVendorName(request.vendorName());
        }

        if (request.referenceNumber() != null) {
            expense.setReferenceNumber(request.referenceNumber());
        }

        if (request.notes() != null) {
            expense.setNotes(request.notes());
        }
    }

    /**
     * Updates expense amount and date fields.
     *
     * @param expense Expense to update
     * @param request Update request
     */
    public void updateAmountAndDate(Expense expense, ExpenseUpdateRequest request) {
        if (request.amount() != null) {
            expense.setAmount(request.amount());
        }

        if (request.expenseDate() != null) {
            expense.setExpenseDate(request.expenseDate());
        }

        if (request.paymentMethod() != null) {
            expense.setPaymentMethod(request.paymentMethod());
        }
    }

    /**
     * Updates expense category with validation.
     * Validates that category exists, belongs to same shop, and is active.
     *
     * @param expense Expense to update
     * @param categoryId Category ID to set
     * @throws BusinessException if category not found
     * @throws AccessDeniedException if category belongs to different shop
     * @throws BusinessRuleViolationException if category is inactive
     */
    public void updateCategory(Expense expense, java.util.UUID categoryId) {
        if (categoryId == null) {
            return;
        }

        ExpenseCategory category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.EXPENSE_CATEGORY_NOT_FOUND, categoryId));

        // Validate category belongs to the same shop
        if (!category.getShopId().equals(expense.getShopId())) {
            throw new AccessDeniedException("You don't have permission to use this expense category");
        }

        if (Boolean.FALSE.equals(category.getIsActive())) {
            throw new BusinessRuleViolationException("Cannot assign expense to inactive category");
        }

        expense.setCategoryId(categoryId);
    }

    /**
     * Updates expense tags, cleaning and normalizing them.
     *
     * @param expense Expense to update
     * @param tags Tags to set
     */
    public void updateTags(Expense expense, Set<String> tags) {
        if (tags != null) {
            expense.setTags(cleanTags(tags));
        }
    }

    /**
     * Cleans tags by trimming whitespace, converting to lowercase, and removing empty entries.
     *
     * @param tags Raw tags
     * @return Cleaned tags
     */
    public Set<String> cleanTags(Set<String> tags) {
        return tags.stream()
            .filter(java.util.Objects::nonNull)
            .map(String::trim)
            .map(String::toLowerCase)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.toSet());
    }
}
