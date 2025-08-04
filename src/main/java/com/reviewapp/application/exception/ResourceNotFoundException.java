package com.reviewapp.application.exception;

/**
 * Thrown when a requested resource (such as a review or entity) is not found in the application.
 */
public class ResourceNotFoundException extends ReviewAppException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * 
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method)
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
