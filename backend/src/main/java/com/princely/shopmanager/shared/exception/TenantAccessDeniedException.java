package com.princely.shopmanager.shared.exception;

public class TenantAccessDeniedException extends SecurityException {
    private final String code;

    public TenantAccessDeniedException(String resource) {
        super("Access denied to resource: " + resource);
        this.code = "ACCESS_DENIED";
    }

    public String getCode() {
        return code;
    }
}