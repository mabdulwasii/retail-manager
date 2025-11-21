package com.princely.shopmanager.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Centralized error codes for the application.
 * Each error code includes a description and HTTP status code.
 */
public enum ErrorCode {
    // General errors (1000-1999)
    INTERNAL_SERVER_ERROR("errors.internal", HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR("errors.validation", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("errors.unauthorized", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("errors.access.denied", HttpStatus.FORBIDDEN),
    NOT_FOUND("errors.not.found", HttpStatus.NOT_FOUND),

    // Tenant & Shop errors (2000-2999)
    TENANT_NOT_FOUND("errors.tenant.not.found", HttpStatus.NOT_FOUND),
    TENANT_ACCESS_DENIED("errors.tenant.access.denied", HttpStatus.FORBIDDEN),
    SHOP_NOT_FOUND("errors.shop.not.found", HttpStatus.NOT_FOUND),
    SHOP_ACCESS_DENIED("errors.shop.access.denied", HttpStatus.FORBIDDEN),
    SHOP_STATUS_INVALID("errors.shop.status.invalid", HttpStatus.BAD_REQUEST),
    SHOP_CANNOT_CLOSE_WITH_ACTIVE_INVESTMENTS("errors.shop.cannot.close.active.investments", HttpStatus.CONFLICT),
    SHOP_CANNOT_SUSPEND_WITH_PENDING_ORDERS("errors.shop.cannot.suspend.pending.orders", HttpStatus.CONFLICT),

    // Expense errors (3000-3999)
    EXPENSE_NOT_FOUND("errors.expense.not.found", HttpStatus.NOT_FOUND),
    EXPENSE_CATEGORY_NOT_FOUND("errors.expense.category.not.found", HttpStatus.NOT_FOUND),
    EXPENSE_CATEGORY_ACCESS_DENIED("errors.expense.category.access.denied", HttpStatus.FORBIDDEN),
    EXPENSE_CATEGORY_INACTIVE("errors.expense.category.inactive", HttpStatus.BAD_REQUEST),
    EXPENSE_CANNOT_EDIT("errors.expense.cannot.edit", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_APPROVE("errors.expense.cannot.approve", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_REJECT("errors.expense.cannot.reject", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_DELETE_PAID("errors.expense.cannot.delete.paid", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_DELETE_APPROVED("errors.expense.cannot.delete.approved", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_SUBMIT("errors.expense.cannot.submit", HttpStatus.CONFLICT),
    EXPENSE_CANNOT_MARK_PAID("errors.expense.cannot.mark.paid", HttpStatus.CONFLICT),
    EXPENSE_AMOUNT_INVALID("errors.expense.amount.invalid", HttpStatus.BAD_REQUEST),
    EXPENSE_DATE_FUTURE("errors.expense.date.future", HttpStatus.BAD_REQUEST),

    // Inventory errors (4000-4999)
    INVENTORY_NOT_FOUND("errors.inventory.not.found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND("errors.product.not.found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("errors.inventory.insufficient.stock", HttpStatus.CONFLICT),
    CANNOT_RESERVE_STOCK("errors.inventory.cannot.reserve", HttpStatus.CONFLICT),

    // Investment errors (5000-5999)
    INVESTMENT_NOT_FOUND("errors.investment.not.found", HttpStatus.NOT_FOUND),
    INVESTOR_NOT_FOUND("errors.investor.not.found", HttpStatus.NOT_FOUND),
    INVESTMENT_INSUFFICIENT_BALANCE("errors.investment.insufficient.balance", HttpStatus.CONFLICT),
    DISTRIBUTION_NOT_FOUND("errors.distribution.not.found", HttpStatus.NOT_FOUND),
    DISTRIBUTION_INVALID_STATUS("errors.distribution.invalid.status", HttpStatus.CONFLICT),
    DISTRIBUTION_CANNOT_APPROVE("errors.distribution.cannot.approve", HttpStatus.CONFLICT),
    DISTRIBUTION_CANNOT_PAY("errors.distribution.cannot.pay", HttpStatus.CONFLICT),
    PROFIT_SHARING_MODEL_UNSUPPORTED("errors.profit.sharing.model.unsupported", HttpStatus.BAD_REQUEST),

    // Sales & Receipt errors (6000-6999)
    RECEIPT_NOT_FOUND("errors.receipt.not.found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_FOUND("errors.transaction.not.found", HttpStatus.NOT_FOUND),

    // Product Return errors (7000-7999)
    RETURN_NOT_FOUND("errors.return.not.found", HttpStatus.NOT_FOUND),
    RETURN_CANNOT_PROCESS("errors.return.cannot.process", HttpStatus.CONFLICT),

    // User & Authentication errors (8000-8999)
    USER_NOT_FOUND("errors.user.not.found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("errors.credentials.invalid", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("errors.token.expired", HttpStatus.UNAUTHORIZED),

    // Business Rule Violations (9000-9999)
    BUSINESS_RULE_VIOLATION("errors.business.rule.violation", HttpStatus.CONFLICT),
    INVALID_STATE_TRANSITION("errors.state.transition.invalid", HttpStatus.CONFLICT);

    private final String messageKey;
    private final HttpStatus httpStatus;

    ErrorCode(String messageKey, HttpStatus httpStatus) {
        this.messageKey = messageKey;
        this.httpStatus = httpStatus;
    }

    /**
     * Get the message key for internationalization
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Get the HTTP status code
     */
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * Get the HTTP status code value
     */
    public int getStatusCode() {
        return httpStatus.value();
    }
}
