package com.enterprise.ecommerce.common.exception;

/**
 * Exception thrown when an invalid request is made
 */
public class InvalidRequestException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_REQUEST";

    public InvalidRequestException(String message) {
        super(ERROR_CODE, message);
    }

    public InvalidRequestException(String message, Object... parameters) {
        super(ERROR_CODE, message, parameters);
    }

    public InvalidRequestException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}