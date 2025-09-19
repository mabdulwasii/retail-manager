package com.princely.shopmanager.auth.service;

/**
 * Exception thrown when Keycloak user operations fail
 */
public class KeycloakUserException extends RuntimeException {

    public KeycloakUserException(String message) {
        super(message);
    }

    public KeycloakUserException(String message, Throwable cause) {
        super(message, cause);
    }
}