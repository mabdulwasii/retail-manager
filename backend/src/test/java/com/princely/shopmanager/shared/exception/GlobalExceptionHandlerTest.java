package com.princely.shopmanager.shared.exception;

import com.princely.shopmanager.auth.exception.KeycloakUserException;
import com.princely.shopmanager.core.exception.TenantRegistrationException;
import com.princely.shopmanager.shared.dto.ErrorResponse;
import com.princely.shopmanager.shared.service.MessageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.exception.ConstraintViolationException as HibernateConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        when(messageService.getMessage(anyString(), any())).thenReturn("Localized error message");
    }

    // BusinessException Tests
    @Test
    @DisplayName("Should handle BusinessException with ErrorCode")
    void shouldHandleBusinessExceptionWithErrorCode() {
        // Given
        ErrorCode errorCode = ErrorCode.SHOP_NOT_FOUND;
        BusinessException exception = new BusinessException(errorCode, "shop-123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("SHOP_NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle BusinessException without ErrorCode (deprecated)")
    void shouldHandleBusinessExceptionWithoutErrorCode() {
        // Given
        BusinessException exception = new BusinessException("CUSTOM_ERROR", "Custom error message");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CUSTOM_ERROR");
    }

    // Access Denial Tests
    @Test
    @DisplayName("Should handle TenantAccessDeniedException")
    void shouldHandleTenantAccessDeniedException() {
        // Given
        TenantAccessDeniedException exception = new TenantAccessDeniedException("tenant-123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTenantAccessDeniedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TENANT_ACCESS_DENIED");
    }

    @Test
    @DisplayName("Should handle Spring AccessDeniedException")
    void shouldHandleAccessDeniedException() {
        // Given
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAccessDeniedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("FORBIDDEN");
    }

    // Validation Tests
    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with field errors")
    void shouldHandleMethodArgumentNotValidException() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("product", "name", "Name is required");
        FieldError fieldError2 = new FieldError("product", "price", "Price must be positive");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(bindingResult.getObjectName()).thenReturn("product");

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getFieldErrors()).hasSize(2);
        assertThat(response.getBody().getFieldErrors()).containsKey("name");
        assertThat(response.getBody().getFieldErrors()).containsKey("price");
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException")
    void shouldHandleConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("must be a valid email");
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CONSTRAINT_VIOLATION");
    }

    // Argument and Conversion Tests
    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void shouldHandleIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid product ID");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_ARGUMENT");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid product ID");
    }

    @Test
    @DisplayName("Should handle ConversionFailedException for date parsing")
    void shouldHandleConversionFailedExceptionForDate() {
        // Given
        Exception cause = new IllegalArgumentException("DateTimeParseException: Invalid date format");
        ConversionFailedException exception = new ConversionFailedException(
            null, null, null, cause
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConversionFailedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_PARAMETER_FORMAT");
        assertThat(response.getBody().getMessage()).contains("date format");
    }

    @Test
    @DisplayName("Should handle ConversionFailedException for generic conversion")
    void shouldHandleConversionFailedExceptionGeneric() {
        // Given
        ConversionFailedException exception = new ConversionFailedException(
            null, null, null, new RuntimeException("Conversion error")
        );

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConversionFailedException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_PARAMETER_FORMAT");
    }

    // State and Entity Tests
    @Test
    @DisplayName("Should handle IllegalStateException")
    void shouldHandleIllegalStateException() {
        // Given
        IllegalStateException exception = new IllegalStateException("Product already sold");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalStateException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_STATE");
    }

    @Test
    @DisplayName("Should handle EntityNotFoundException")
    void shouldHandleEntityNotFoundException() {
        // Given
        EntityNotFoundException exception = new EntityNotFoundException("Product not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleEntityNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle NoSuchElementException")
    void shouldHandleNoSuchElementException() {
        // Given
        NoSuchElementException exception = new NoSuchElementException("Element not found");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleNoSuchElementException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("NOT_FOUND");
    }

    @Test
    @DisplayName("Should handle OptimisticLockException")
    void shouldHandleOptimisticLockException() {
        // Given
        OptimisticLockException exception = new OptimisticLockException("Concurrent modification");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOptimisticLockException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CONCURRENT_UPDATE");
    }

    // Database Constraint Tests
    @Test
    @DisplayName("Should handle DataIntegrityViolation - Unique Constraint")
    void shouldHandleDataIntegrityViolation_UniqueConstraint() {
        // Given
        SQLException sqlEx = new SQLException(
            "duplicate key value violates unique constraint \"products_barcode_key\" " +
            "Detail: Key (barcode)=(ABC123) already exists."
        );
        HibernateConstraintViolationException hibernateEx =
            new HibernateConstraintViolationException("Constraint violation", sqlEx, "products_barcode_key");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity", hibernateEx);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("DUPLICATE_ENTRY");
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolation - NOT NULL Constraint")
    void shouldHandleDataIntegrityViolation_NotNullConstraint() {
        // Given
        SQLException sqlEx = new SQLException(
            "null value in column \"name\" of relation \"products\" violates not-null constraint"
        );
        HibernateConstraintViolationException hibernateEx =
            new HibernateConstraintViolationException("Constraint violation", sqlEx, "products_name_notnull");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity", hibernateEx);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("REQUIRED_FIELD_MISSING");
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolation - Foreign Key Constraint")
    void shouldHandleDataIntegrityViolation_ForeignKeyConstraint() {
        // Given
        SQLException sqlEx = new SQLException(
            "insert or update on table \"products\" violates foreign key constraint \"fk_category\" " +
            "Detail: Key (category_id)=(cat-999) is not present in table \"categories\"."
        );
        HibernateConstraintViolationException hibernateEx =
            new HibernateConstraintViolationException("Constraint violation", sqlEx, "fk_category");
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity", hibernateEx);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INVALID_REFERENCE");
    }

    @Test
    @DisplayName("Should handle DataIntegrityViolation - No SQLException")
    void shouldHandleDataIntegrityViolation_NoSQLException() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data integrity violation");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("DATA_INTEGRITY_ERROR");
    }

    // Database Access Tests
    @Test
    @DisplayName("Should handle DataAccessException")
    void shouldHandleDataAccessException() {
        // Given
        DataAccessException exception = new DataAccessException("Database connection failed") {};

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("DATABASE_ERROR");
    }

    // External Service Tests
    @Test
    @DisplayName("Should handle KeycloakUserException")
    void shouldHandleKeycloakUserException() {
        // Given
        KeycloakUserException exception = new KeycloakUserException("Keycloak user creation failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleKeycloakUserException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("EXTERNAL_SERVICE_ERROR");
    }

    @Test
    @DisplayName("Should handle TenantRegistrationException")
    void shouldHandleTenantRegistrationException() {
        // Given
        TenantRegistrationException exception = new TenantRegistrationException("Registration failed");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleTenantRegistrationException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("REGISTRATION_ERROR");
    }

    // Transaction Tests
    @Test
    @DisplayName("Should handle RollbackException with ConstraintViolation cause")
    void shouldHandleRollbackException_WithConstraintViolation() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("email");
        when(violation.getMessage()).thenReturn("must be valid");
        violations.add(violation);

        ConstraintViolationException constraintEx = new ConstraintViolationException(violations);
        RollbackException exception = new RollbackException("Transaction rollback", constraintEx);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRollbackException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("CONSTRAINT_VIOLATION");
    }

    @Test
    @DisplayName("Should handle RollbackException without ConstraintViolation")
    void shouldHandleRollbackException_WithoutConstraintViolation() {
        // Given
        RollbackException exception = new RollbackException("Transaction rollback");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRollbackException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("TRANSACTION_ERROR");
    }

    // Generic Exception Test
    @Test
    @DisplayName("Should handle generic Exception with cause chain logging")
    void shouldHandleGenericException() {
        // Given
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/products");

        Exception rootCause = new IllegalArgumentException("Root cause");
        Exception cause = new RuntimeException("Intermediate cause", rootCause);
        Exception exception = new Exception("Unexpected error", cause);

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).contains("unexpected error");
    }
}
