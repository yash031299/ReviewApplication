package com.reviewapp.domain.port;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewStatsPort} interface, verifying accessibility and method declarations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewStatsPortTest {


    @DisplayName("ReviewStatsPort is an interface")
    @Test
    void interfaceIsAccessible() {
        // Act & Assert
        assertTrue(ReviewStatsPort.class.isInterface());
    }


    @DisplayName("All expected methods are declared and accessible on ReviewStatsPort")
    @Test
    void allMethods_declaredAndAccessible() {
        // Arrange & Act & Assert
        assertDoesNotThrow(() -> ReviewStatsPort.class.getDeclaredMethod("getTotalReviewCountStats"));
        assertDoesNotThrow(() -> ReviewStatsPort.class.getDeclaredMethod("getAverageRating"));
        assertDoesNotThrow(() -> ReviewStatsPort.class.getDeclaredMethod("getRatingDistribution"));
        assertDoesNotThrow(() -> ReviewStatsPort.class.getDeclaredMethod("getMonthlyRatingAverage"));
    }
}
