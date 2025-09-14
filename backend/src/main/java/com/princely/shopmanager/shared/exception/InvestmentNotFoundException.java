package com.princely.shopmanager.shared.exception;

public class InvestmentNotFoundException extends BusinessException {
    public InvestmentNotFoundException(String investmentId) {
        super("INVESTMENT_NOT_FOUND", "Investment not found: " + investmentId);
    }
}