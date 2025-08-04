package com.reviewapp.domain.port;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewWritePort} interface, verifying accessibility and method declarations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewWritePortTest {

    /**
     * Tests that ReviewWritePort is an interface.
     */
    @Test
    void interfaceIsAccessible() {
        // Act & Assert
        assertTrue(ReviewWritePort.class.isInterface());
    }

    /**
     * Tests that all expected methods are declared and accessible on ReviewWritePort.
     */
    @Test
    void allMethods_declaredAndAccessible() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> ReviewWritePort.class.getDeclaredMethod("saveReviews", java.util.List.class));
    }
}
