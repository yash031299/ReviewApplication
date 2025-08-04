package com.reviewapp.application.exception;

/**
 * Thrown when a persistence (DB) error occurs in the application.
 * Represents a persistence-related exception in the application.
 * This exception is thrown when an error occurs while interacting with the database or other persistence mechanisms.
 */
public class PersistenceException extends ReviewAppException {
    public PersistenceException(String message) {
        super(message);
    }
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
