package com.reviewapp.application.exception;

/**
 * Base exception for all custom application errors in ReviewApp.
 * Extend this for all domain-specific, unchecked exceptions.
 */
public class ReviewAppException extends RuntimeException {
    public ReviewAppException(String message) {
        super(message);
    }
    public ReviewAppException(String message, Throwable cause) {
        super(message, cause);
    }
}
