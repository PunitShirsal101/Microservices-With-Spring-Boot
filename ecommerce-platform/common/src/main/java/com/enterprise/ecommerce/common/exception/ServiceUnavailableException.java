package com.enterprise.ecommerce.common.exception;

/**
 * Exception thrown when a service is unavailable
 */
public class ServiceUnavailableException extends BusinessException {

    private static final String ERROR_CODE = "SERVICE_UNAVAILABLE";

    public ServiceUnavailableException(String serviceName) {
        super(ERROR_CODE,
              String.format("Service %s is currently unavailable", serviceName),
              serviceName);
    }

    public ServiceUnavailableException(String serviceName, String message) {
        super(ERROR_CODE, message, serviceName);
    }

    public ServiceUnavailableException(String serviceName, Throwable cause) {
        super(ERROR_CODE,
              String.format("Service %s is currently unavailable", serviceName),
              cause, serviceName);
    }
}