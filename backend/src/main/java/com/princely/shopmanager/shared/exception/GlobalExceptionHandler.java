package com.princely.shopmanager.shared.exception;

import com.princely.shopmanager.auth.exception.KeycloakUserException;
import com.princely.shopmanager.core.exception.TenantRegistrationException;
import com.princely.shopmanager.shared.dto.ErrorResponse;
import com.princely.shopmanager.shared.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageService messageService;

    // Constraint detail map keys
    private static final String KEY_VIOLATION_TYPE = "violationType";
    private static final String KEY_CONSTRAINT_NAME = "constraintName";
    private static final String KEY_COLUMN = "column";
    private static final String KEY_ATTEMPTED_VALUE = "attemptedValue";
    private static final String KEY_TABLE = "table";

    // Constraint violation types
    private static final String VIOLATION_UNIQUE_CONSTRAINT = "UNIQUE_CONSTRAINT";
    private static final String VIOLATION_NOT_NULL = "NOT_NULL";
    private static final String VIOLATION_FOREIGN_KEY = "FOREIGN_KEY";

    // Regex patterns for extracting constraint details from PostgreSQL error messages
    private static final Pattern DUPLICATE_KEY_PATTERN = Pattern.compile(
        "duplicate key value violates unique constraint \"([^\"]+)\".*Key \\(([^)]+)\\)=\\(([^)]+)\\)",
        Pattern.DOTALL
    );
    private static final Pattern NOT_NULL_PATTERN = Pattern.compile(
        "null value in column \"([^\"]+)\" of relation \"([^\"]+)\"");
    private static final Pattern FOREIGN_KEY_PATTERN = Pattern.compile(
        "violates foreign key constraint \"([^\"]+)\".*Key \\(([^)]+)\\)=\\(([^)]+)\\)",
        Pattern.DOTALL
    );

    /**
     * Extracts detailed constraint violation information for debugging.
     * Logs table, column, constraint name, and violating value.
     */
    private Map<String, String> extractConstraintDetails(SQLException sqlEx) {
        Map<String, String> details = new HashMap<>();

        if (sqlEx == null || sqlEx.getMessage() == null) {
            return details;
        }

        String errorMessage = sqlEx.getMessage();

        // Try to extract duplicate key violation details
        Matcher duplicateMatcher = DUPLICATE_KEY_PATTERN.matcher(errorMessage);
        if (duplicateMatcher.find()) {
            details.put(KEY_VIOLATION_TYPE, VIOLATION_UNIQUE_CONSTRAINT);
            details.put(KEY_CONSTRAINT_NAME, duplicateMatcher.group(1));
            details.put(KEY_COLUMN, duplicateMatcher.group(2));
            details.put(KEY_ATTEMPTED_VALUE, duplicateMatcher.group(3));

            // Extract table name from constraint name (usually format: table_column_key)
            String constraintName = duplicateMatcher.group(1);
            if (constraintName.contains("_")) {
                details.put(KEY_TABLE, constraintName.split("_")[0]);
            }
            return details;
        }

        // Try to extract NOT NULL violation details
        Matcher notNullMatcher = NOT_NULL_PATTERN.matcher(errorMessage);
        if (notNullMatcher.find()) {
            details.put(KEY_VIOLATION_TYPE, VIOLATION_NOT_NULL);
            details.put(KEY_COLUMN, notNullMatcher.group(1));
            details.put(KEY_TABLE, notNullMatcher.group(2));
            return details;
        }

        // Try to extract foreign key violation details
        Matcher foreignKeyMatcher = FOREIGN_KEY_PATTERN.matcher(errorMessage);
        if (foreignKeyMatcher.find()) {
            details.put(KEY_VIOLATION_TYPE, VIOLATION_FOREIGN_KEY);
            details.put(KEY_CONSTRAINT_NAME, foreignKeyMatcher.group(1));
            details.put(KEY_COLUMN, foreignKeyMatcher.group(2));
            details.put(KEY_ATTEMPTED_VALUE, foreignKeyMatcher.group(3));
            return details;
        }

        return details;
    }

    /**
     * Logs detailed constraint violation information for debugging purposes.
     */
    private void logConstraintViolation(SQLException sqlEx, Map<String, String> details) {
        if (details.isEmpty()) {
            logger.error("Database constraint violation (unable to parse details): {}",
                sqlEx.getMessage());
            return;
        }

        String violationType = details.getOrDefault("violationType", "UNKNOWN");

        switch (violationType) {
            case "UNIQUE_CONSTRAINT":
                logger.error("UNIQUE CONSTRAINT VIOLATION - Table: {}, Column: {}, " +
                    "Constraint: {}, Attempted Value: {}, SQL State: {}",
                    details.get("table"),
                    details.get("column"),
                    details.get("constraintName"),
                    details.get("attemptedValue"),
                    sqlEx.getSQLState());
                break;

            case "NOT_NULL":
                logger.error("NOT NULL CONSTRAINT VIOLATION - Table: {}, Column: {}, SQL State: {}",
                    details.get("table"),
                    details.get("column"),
                    sqlEx.getSQLState());
                break;

            case "FOREIGN_KEY":
                logger.error("FOREIGN KEY CONSTRAINT VIOLATION - Constraint: {}, Column: {}, " +
                    "Attempted Value: {}, SQL State: {}",
                    details.get("constraintName"),
                    details.get("column"),
                    details.get("attemptedValue"),
                    sqlEx.getSQLState());
                break;

            default:
                logger.error("DATABASE CONSTRAINT VIOLATION - Type: {}, Details: {}",
                    violationType, details);
        }
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        String message;
        HttpStatus status;

        // If ErrorCode is present, use it to get localized message and HTTP status
        if (e.getErrorCode() != null) {
            message = messageService.getMessage(e.getErrorCode().getMessageKey(), e.getMessageParams());
            status = e.getErrorCode().getHttpStatus();
            logger.warn("Business exception occurred: {} - {}", e.getCode(), message);
        } else {
            // Fallback for deprecated constructor usage
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
            logger.warn("Business exception occurred (deprecated): {} - {}", e.getCode(), message);
        }

        return ResponseEntity.status(status)
            .body(new ErrorResponse(e.getCode(), message));
    }

    @ExceptionHandler(TenantAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleTenantAccessDeniedException(TenantAccessDeniedException e) {
        logger.warn("Tenant access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        logger.warn("Spring Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("FORBIDDEN", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        String message = "Validation failed for " + e.getBindingResult().getObjectName();
        logger.warn("Validation error: {}", fieldErrors);

        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", message, fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.joining(", "));

        logger.warn("Constraint violation: {}", message);
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("CONSTRAINT_VIOLATION", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Illegal argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_ARGUMENT", e.getMessage()));
    }

    @ExceptionHandler(ConversionFailedException.class)
    public ResponseEntity<ErrorResponse> handleConversionFailedException(ConversionFailedException e) {
        logger.warn("Conversion failed: {}", e.getMessage());
        String message = "Invalid parameter format";

        // Extract more specific error message for date/time parsing
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            String cause = e.getCause().getMessage();
            if (cause.contains("DateTimeParseException") || cause.contains("date")) {
                message = "Invalid date format. Expected format: yyyy-MM-dd or yyyy-MM-dd'T'HH:mm:ss";
            }
        }

        return ResponseEntity.badRequest()
            .body(new ErrorResponse("INVALID_PARAMETER_FORMAT", message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException e) {
        logger.warn("Illegal state: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("INVALID_STATE", e.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        logger.warn("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        logger.warn("Element not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("NOT_FOUND", "Requested resource not found"));
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException e) {
        logger.warn("Concurrent modification detected: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("CONCURRENT_UPDATE",
                "The record was modified by another user. Please refresh and try again."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = "Database constraint violation";
        String code = "DATA_INTEGRITY_ERROR";
        HttpStatus status = HttpStatus.CONFLICT;

        if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            org.hibernate.exception.ConstraintViolationException cve =
                (org.hibernate.exception.ConstraintViolationException) e.getCause();

            SQLException sqlEx = cve.getSQLException();

            // Extract and log detailed constraint information
            if (sqlEx != null) {
                Map<String, String> constraintDetails = extractConstraintDetails(sqlEx);
                logConstraintViolation(sqlEx, constraintDetails);

                // Build user-friendly message based on constraint details
                String violationType = constraintDetails.get("violationType");
                if ("UNIQUE_CONSTRAINT".equals(violationType)) {
                    code = "DUPLICATE_ENTRY";
                    String column = constraintDetails.getOrDefault("column", "field");
                    message = String.format("A record with this %s already exists",
                        column.replace("_", " "));
                    status = HttpStatus.CONFLICT;
                } else if ("NOT_NULL".equals(violationType)) {
                    code = "REQUIRED_FIELD_MISSING";
                    String column = constraintDetails.getOrDefault("column", "field");
                    message = String.format("Required field '%s' cannot be null",
                        column.replace("_", " "));
                    status = HttpStatus.BAD_REQUEST;
                } else if ("FOREIGN_KEY".equals(violationType)) {
                    code = "INVALID_REFERENCE";
                    message = "Referenced record does not exist or cannot be deleted due to dependencies";
                    status = HttpStatus.BAD_REQUEST;
                }
            } else {
                // Fallback: Log basic info
                logger.error("Data integrity violation without SQLException details: {}",
                    e.getMessage());
            }

        } else {
            // Not a Hibernate ConstraintViolationException - log basic info
            logger.error("Data integrity violation (non-Hibernate): {}", e.getMessage());
        }

        return ResponseEntity.status(status)
            .body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        // Log detailed error for debugging
        logger.error("DATABASE ACCESS ERROR - Type: {}, Message: {}, Root Cause: {}",
            e.getClass().getSimpleName(),
            e.getMessage(),
            e.getRootCause() != null ? e.getRootCause().getMessage() : "none",
            e);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("DATABASE_ERROR",
                "Database operation failed. Please try again later."));
    }

    @ExceptionHandler(KeycloakUserException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakUserException(KeycloakUserException e) {
        // Log detailed Keycloak error
        logger.error("KEYCLOAK OPERATION FAILED - Message: {}, Cause: {}",
            e.getMessage(),
            e.getCause() != null ? e.getCause().getMessage() : "none",
            e);

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse("EXTERNAL_SERVICE_ERROR",
                "User management operation failed. Please try again later."));
    }

    @ExceptionHandler(TenantRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleTenantRegistrationException(TenantRegistrationException e) {
        logger.warn("TENANT REGISTRATION FAILED - Message: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("REGISTRATION_ERROR", e.getMessage()));
    }

    @ExceptionHandler(jakarta.persistence.RollbackException.class)
    public ResponseEntity<ErrorResponse> handleRollbackException(jakarta.persistence.RollbackException e) {
        logger.warn("TRANSACTION ROLLBACK - Message: {}", e.getMessage());

        // Check if the cause is a ConstraintViolationException
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof ConstraintViolationException) {
                // Delegate to the existing ConstraintViolationException handler
                return handleConstraintViolationException((ConstraintViolationException) cause);
            }
            cause = cause.getCause();
        }

        // If no ConstraintViolationException found, return generic rollback error
        logger.error("Transaction rollback without ConstraintViolationException", e);
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("TRANSACTION_ERROR",
                "Transaction failed due to validation or constraint violation. Please check your input data."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e, WebRequest request) {
        // Log comprehensive error information for unexpected exceptions
        logger.error("UNEXPECTED ERROR - Type: {}, Message: {}, Request: {}, Stack Trace:",
            e.getClass().getName(),
            e.getMessage(),
            request.getDescription(false),
            e);

        // Log cause chain if present
        Throwable cause = e.getCause();
        int depth = 1;
        while (cause != null && depth <= 5) {
            logger.error("  Caused by [{}]: {} - {}",
                depth,
                cause.getClass().getName(),
                cause.getMessage());
            cause = cause.getCause();
            depth++;
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR",
                "An unexpected error occurred. Please contact support if the issue persists."));
    }
}