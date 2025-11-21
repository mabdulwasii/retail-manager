package com.princely.shopmanager.core.exception;

/**
 * Exception thrown during tenant registration process
 */
public class TenantRegistrationException extends RuntimeException {

    public TenantRegistrationException(String message) {
        super(message);
    }

    public TenantRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}