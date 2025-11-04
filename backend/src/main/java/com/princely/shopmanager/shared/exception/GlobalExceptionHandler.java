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

import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final MessageService messageService;

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
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        logger.warn("Validation error: {}", message);
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("VALIDATION_ERROR", message));
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
        logger.error("Data integrity violation: {}", e.getMessage(), e);

        String message = "Database constraint violation";
        String code = "DATA_INTEGRITY_ERROR";
        HttpStatus status = HttpStatus.CONFLICT;

        if (e.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
            org.hibernate.exception.ConstraintViolationException cve =
                (org.hibernate.exception.ConstraintViolationException) e.getCause();

            // Check for NOT NULL constraint violations first
            if (cve.getSQLException() != null && cve.getSQLException().getMessage() != null) {
                String sqlMsg = cve.getSQLException().getMessage();
                if (sqlMsg.contains("null value") && sqlMsg.contains("violates not-null constraint")) {
                    code = "REQUIRED_FIELD_MISSING";
                    status = HttpStatus.BAD_REQUEST; // 400 for validation errors

                    // Try to extract the column name from error message
                    // Format: null value in column "column_name" of relation "table_name"
                    try {
                        int columnStart = sqlMsg.indexOf("column \"") + 8;
                        int columnEnd = sqlMsg.indexOf("\"", columnStart);
                        if (columnStart > 7 && columnEnd > columnStart) {
                            String columnName = sqlMsg.substring(columnStart, columnEnd);
                            message = "Required field '" + columnName + "' is missing or null";
                        } else {
                            message = "Required field is missing or null";
                        }
                    } catch (Exception ex) {
                        message = "Required field is missing or null";
                    }

                    return ResponseEntity.status(status)
                        .body(new ErrorResponse(code, message));
                }
            }

            // Check constraint name for other violations
            if (cve.getConstraintName() != null) {
                if (cve.getConstraintName().toLowerCase().contains("unique") ||
                    cve.getConstraintName().toLowerCase().contains("uq_")) {
                    code = "DUPLICATE_ENTRY";
                    message = "A record with this information already exists";
                    status = HttpStatus.CONFLICT; // 409 for conflicts
                } else if (cve.getConstraintName().toLowerCase().contains("foreign") ||
                           cve.getConstraintName().toLowerCase().contains("fk_")) {
                    code = "INVALID_REFERENCE";
                    message = "Referenced record does not exist";
                    status = HttpStatus.BAD_REQUEST; // 400 for invalid references
                }
            }
        } else if (e.getMessage() != null) {
            // Try to extract constraint type from error message
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("null value") && errorMsg.contains("not-null constraint")) {
                code = "REQUIRED_FIELD_MISSING";
                message = "Required field is missing or null";
                status = HttpStatus.BAD_REQUEST; // 400 for validation errors
            } else if (errorMsg.contains("unique") || errorMsg.contains("duplicate")) {
                code = "DUPLICATE_ENTRY";
                message = "A record with this information already exists";
                status = HttpStatus.CONFLICT; // 409 for conflicts
            } else if (errorMsg.contains("foreign key") || errorMsg.contains("violates")) {
                code = "INVALID_REFERENCE";
                message = "Referenced record does not exist";
                status = HttpStatus.BAD_REQUEST; // 400 for invalid references
            }
        }

        return ResponseEntity.status(status)
            .body(new ErrorResponse(code, message));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        logger.error("Database access error", e);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(new ErrorResponse("DATABASE_ERROR",
                "Database operation failed. Please try again later."));
    }

    @ExceptionHandler(KeycloakUserException.class)
    public ResponseEntity<ErrorResponse> handleKeycloakUserException(KeycloakUserException e) {
        logger.error("Keycloak user operation failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(new ErrorResponse("EXTERNAL_SERVICE_ERROR",
                "User management operation failed. Please try again later."));
    }

    @ExceptionHandler(TenantRegistrationException.class)
    public ResponseEntity<ErrorResponse> handleTenantRegistrationException(TenantRegistrationException e) {
        logger.warn("Tenant registration failed: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(new ErrorResponse("REGISTRATION_ERROR", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}