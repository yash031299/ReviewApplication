package com.reviewapp.application.exception;

/**
 * Thrown when invalid input is provided to the application (e.g., failed validation, domain constraint violations).
 * Used for argument errors and validation failures in both service and domain layers.
 */
public class InvalidInputException extends ReviewAppException {
    public InvalidInputException(String message) {
        super(message);
    }
    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
