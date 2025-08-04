package com.reviewapp.domain.port;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewQueryPort} interface, verifying accessibility and method declarations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewQueryPortTest {

    /**
     * Tests that ReviewQueryPort is an interface.
     */
    @Test
    void interfaceIsAccessible() {
        // Act & Assert
        assertTrue(ReviewQueryPort.class.isInterface());
    }

    /**
     * Tests that all expected methods are declared and accessible on ReviewQueryPort.
     */
    @Test
    void allMethods_declaredAndAccessible() throws ClassNotFoundException {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getReviewsPage", int.class, int.class));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getTotalReviewCount"));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getReviewById", Long.class));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getReviewsByFilters", Class.forName("com.reviewapp.domain.model.Filters"), int.class, int.class));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getFilteredReviewCount", Class.forName("com.reviewapp.domain.model.Filters")));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getReviewsByKeywords", java.util.List.class));
        assertDoesNotThrow(() -> ReviewQueryPort.class.getDeclaredMethod("getAllReviews"));
    }
}
