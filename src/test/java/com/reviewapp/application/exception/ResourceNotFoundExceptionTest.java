package com.reviewapp.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResourceNotFoundException}.
 * Verifies construction and message propagation.
 */
class ResourceNotFoundExceptionTest {

    @DisplayName("Verifies that the constructor sets the exception message correctly.")
    @Test
    void constructor_setsMessage() {
        // Arrange
        String message = "Not found";
        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(message);
        // Assert
        assertEquals("Not found", ex.getMessage());
    }


    @DisplayName("Verifies that the constructor sets both the message and cause correctly.")
    @Test
    void constructor_withCause_setsMessageAndCause() {
        // Arrange
        String message = "fail";
        Throwable cause = new RuntimeException("root");
        // Act
        ResourceNotFoundException ex = new ResourceNotFoundException(message, cause);
        // Assert
        assertEquals("fail", ex.getMessage());
        assertSame(cause, ex.getCause());
    }


    @DisplayName("Verifies that the exception is caught by assertThrows when thrown.")
    @Test
    void throwException_whenThrown_isCaughtByAssertThrows() {
        // Arrange, Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> { throw new ResourceNotFoundException("fail"); });
    }
}
