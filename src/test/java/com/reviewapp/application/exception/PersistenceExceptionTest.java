package com.reviewapp.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PersistenceException}.
 * Verifies construction and message propagation.
 */
class PersistenceExceptionTest {

    /**
     * Tests that the constructor sets the exception message correctly.
     */
    @Test
    void constructor_setsMessage() {
        // Arrange
        String message = "DB error";
        // Act
        PersistenceException ex = new PersistenceException(message);
        // Assert
        assertEquals("DB error", ex.getMessage());
    }

    /**
     * Tests that the constructor sets both the message and cause correctly.
     */
    @Test
    void constructor_withCause_setsMessageAndCause() {
        // Arrange
        String message = "fail";
        Throwable cause = new RuntimeException("root");
        // Act
        PersistenceException ex = new PersistenceException(message, cause);
        // Assert
        assertEquals("fail", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    /**
     * Tests that the exception is caught by assertThrows when thrown.
     */
    @Test
    void throwException_whenThrown_isCaughtByAssertThrows() {
        // Arrange, Act & Assert
        assertThrows(PersistenceException.class, () -> { throw new PersistenceException("fail"); });
    }
}
