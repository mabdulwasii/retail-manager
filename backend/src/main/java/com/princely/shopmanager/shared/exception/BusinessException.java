package com.princely.shopmanager.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final String code;
    private final ErrorCode errorCode;
    private final HttpStatus httpStatus;
    private final Object[] messageParams;

    /**
     * @deprecated Use {@link #BusinessException(ErrorCode, Object...)} instead
     */
    @Deprecated
    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.messageParams = new Object[0];
    }

    /**
     * @deprecated Use {@link #BusinessException(ErrorCode, Throwable, Object...)} instead
     */
    @Deprecated
    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.errorCode = null;
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.messageParams = new Object[0];
    }

    /**
     * Create a business exception with an error code and optional message parameters
     *
     * @param errorCode the error code
     * @param messageParams optional parameters for the error message
     */
    public BusinessException(ErrorCode errorCode, Object... messageParams) {
        super(errorCode.getMessageKey());
        this.code = errorCode.name();
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.messageParams = messageParams;
    }

    /**
     * Create a business exception with an error code, cause, and optional message parameters
     *
     * @param errorCode the error code
     * @param cause the cause of the exception
     * @param messageParams optional parameters for the error message
     */
    public BusinessException(ErrorCode errorCode, Throwable cause, Object... messageParams) {
        super(errorCode.getMessageKey(), cause);
        this.code = errorCode.name();
        this.errorCode = errorCode;
        this.httpStatus = errorCode.getHttpStatus();
        this.messageParams = messageParams;
    }

    public String getCode() {
        return code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public Object[] getMessageParams() {
        return messageParams;
    }
}