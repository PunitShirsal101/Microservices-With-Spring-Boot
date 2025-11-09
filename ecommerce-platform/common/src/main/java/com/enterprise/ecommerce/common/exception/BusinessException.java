package com.enterprise.ecommerce.common.exception;

/**
 * Base exception for all business logic exceptions
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;
    private final transient Object[] parameters;

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = new Object[0];
    }

    protected BusinessException(String errorCode, String message, Object... parameters) {
        super(message);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = new Object[0];
    }

    protected BusinessException(String errorCode, String message, Throwable cause, Object... parameters) {
        super(message, cause);
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Object[] getParameters() {
        return parameters;
    }
}