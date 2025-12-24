package com.princely.shopmanager.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException Unit Tests")
class BusinessExceptionTest {

    @Test
    @DisplayName("Should create exception with code and message")
    void shouldCreateExceptionWithCodeAndMessage() {
        // Given
        String code = "TEST_ERROR";
        String message = "Test error message";

        // When
        BusinessException exception = new BusinessException(code, message);

        // Then
        assertThat(exception)
            .extracting("code", "message", "errorCode", "httpStatus")
            .containsExactly(code, message, null, HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessageParams()).isEmpty();
    }

    @Test
    @DisplayName("Should create exception with ErrorCode and no params")
    void shouldCreateExceptionWithErrorCodeNoParams() {
        // Given
        ErrorCode errorCode = ErrorCode.TENANT_NOT_FOUND;

        // When
        BusinessException exception = new BusinessException(errorCode);

        // Then
        assertThat(exception)
            .extracting("code", "message", "errorCode", "httpStatus")
            .containsExactly("TENANT_NOT_FOUND", errorCode.getMessageKey(), errorCode, HttpStatus.NOT_FOUND);
        assertThat(exception.getMessageParams()).isEmpty();
    }

    @Test
    @DisplayName("Should create exception with ErrorCode and message params")
    void shouldCreateExceptionWithErrorCodeAndParams() {
        // Given
        ErrorCode errorCode = ErrorCode.PRODUCT_NOT_FOUND;
        Object[] params = {"product-123", "Shop A"};

        // When
        BusinessException exception = new BusinessException(errorCode, params);

        // Then
        assertThat(exception)
            .extracting("code", "message", "errorCode", "httpStatus")
            .containsExactly("PRODUCT_NOT_FOUND", errorCode.getMessageKey(), errorCode, HttpStatus.NOT_FOUND);
        assertThat(exception.getMessageParams())
            .hasSize(2)
            .containsExactly("product-123", "Shop A");
    }

    @Test
    @DisplayName("Should create exception with ErrorCode, cause, and no params")
    void shouldCreateExceptionWithErrorCodeAndCauseNoParams() {
        // Given
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Throwable cause = new RuntimeException("Database connection failed");

        // When
        BusinessException exception = new BusinessException(errorCode, cause);

        // Then
        assertThat(exception.getCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessageKey());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessageParams()).isEmpty();
    }

    @Test
    @DisplayName("Should create exception with ErrorCode, cause, and params")
    void shouldCreateExceptionWithErrorCodeCauseAndParams() {
        // Given
        ErrorCode errorCode = ErrorCode.EXPENSE_CANNOT_APPROVE;
        Throwable cause = new IllegalStateException("Invalid expense state");
        Object[] params = {"EXP-001", "DRAFT"};

        // When
        BusinessException exception = new BusinessException(errorCode, cause, params);

        // Then
        assertThat(exception.getCode()).isEqualTo("EXPENSE_CANNOT_APPROVE");
        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessageKey());
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getErrorCode()).isEqualTo(errorCode);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getMessageParams()).hasSize(2);
        assertThat(exception.getMessageParams()[0]).isEqualTo("EXP-001");
        assertThat(exception.getMessageParams()[1]).isEqualTo("DRAFT");
    }

    @Test
    @DisplayName("Should handle different HTTP status codes from ErrorCode")
    void shouldHandleDifferentHttpStatusCodes() {
        // When/Then - NOT_FOUND
        BusinessException notFoundEx = new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        assertThat(notFoundEx.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        // When/Then - FORBIDDEN
        BusinessException forbiddenEx = new BusinessException(ErrorCode.ACCESS_DENIED);
        assertThat(forbiddenEx.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        // When/Then - BAD_REQUEST
        BusinessException badRequestEx = new BusinessException(ErrorCode.VALIDATION_ERROR);
        assertThat(badRequestEx.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);

        // When/Then - CONFLICT
        BusinessException conflictEx = new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        assertThat(conflictEx.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);

        // When/Then - UNAUTHORIZED
        BusinessException unauthorizedEx = new BusinessException(ErrorCode.UNAUTHORIZED);
        assertThat(unauthorizedEx.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Should preserve exception inheritance chain")
    void shouldPreserveExceptionInheritanceChain() {
        // Given
        ErrorCode errorCode = ErrorCode.TENANT_ACCESS_DENIED;

        // When
        BusinessException exception = new BusinessException(errorCode);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle empty message params array")
    void shouldHandleEmptyMessageParams() {
        // Given
        ErrorCode errorCode = ErrorCode.SHOP_STATUS_INVALID;
        Object[] emptyParams = {};

        // When
        BusinessException exception = new BusinessException(errorCode, emptyParams);

        // Then
        assertThat(exception.getMessageParams()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null cause with ErrorCode constructor")
    void shouldHandleNullCause() {
        // Given
        ErrorCode errorCode = ErrorCode.EXPENSE_AMOUNT_INVALID;
        Throwable nullCause = null;
        Object[] params = {"100.00"};

        // When
        BusinessException exception = new BusinessException(errorCode, nullCause, params);

        // Then
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getCode()).isEqualTo("EXPENSE_AMOUNT_INVALID");
        assertThat(exception.getMessageParams()).hasSize(1);
    }
}
