package com.reviewapp.application.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InvalidInputException}.
 * Verifies construction and message propagation.
 */
class InvalidInputExceptionTest {

    /**
     * Tests that the constructor sets the exception message correctly.
     */
    @Test
    void constructor_setsMessage() {
        // Arrange
        String message = "Invalid!";
        // Act
        InvalidInputException ex = new InvalidInputException(message);
        // Assert
        assertEquals("Invalid!", ex.getMessage());
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
        InvalidInputException ex = new InvalidInputException(message, cause);
        // Assert
        assertEquals("fail", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    /**
     * Tests that an instance of InvalidInputException is an instance of ReviewAppException.
     */
    @Test
    void instanceOf_givenInstance_isReviewAppException() {
        // Arrange
        InvalidInputException instance = new InvalidInputException("msg");
        // Act & Assert
        assertInstanceOf(ReviewAppException.class, instance);
    }

    /**
     * Tests that throwing an InvalidInputException is caught by assertThrows.
     */
    @Test
    void throwException_whenThrown_isCaughtByAssertThrows() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> { throw new InvalidInputException("fail"); });
    }
}
