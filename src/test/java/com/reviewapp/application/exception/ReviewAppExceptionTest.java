package com.reviewapp.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewAppException}.
 * Verifies construction and message propagation.
 */
class ReviewAppExceptionTest {

    /**
     * Tests that the constructor sets the exception message correctly.
     */
    @Test
    void constructor_setsMessage() {
        // Arrange
        String message = "App error";
        // Act
        ReviewAppException ex = new ReviewAppException(message);
        // Assert
        assertEquals("App error", ex.getMessage());
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
        ReviewAppException ex = new ReviewAppException(message, cause);
        // Assert
        assertEquals("fail", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    /**
     * Tests that ReviewAppException is an instance of RuntimeException.
     */
    @Test
    void instanceOf_givenInstance_isRuntimeException() {
        // Arrange
        ReviewAppException ex = new ReviewAppException("msg");
        // Act
        // Assert
        assertTrue(ex instanceof RuntimeException);
    }
}
