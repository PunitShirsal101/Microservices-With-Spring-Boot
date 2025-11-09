package com.enterprise.ecommerce.common.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super(ERROR_CODE,
              String.format("%s not found with identifier: %s", resourceType, identifier),
              resourceType, identifier);
    }

    public ResourceNotFoundException(String resourceType, String field, Object value) {
        super(ERROR_CODE,
              String.format("%s not found with %s: %s", resourceType, field, value),
              resourceType, field, value);
    }

    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}