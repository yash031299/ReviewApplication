package com.reviewapp.domain.model;

import com.reviewapp.application.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Statistics} covering builder logic, validation, immutability, and edge cases.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class StatisticsTest {

    @DisplayName("Tests that building with all fields sets all fields correctly")
    @Test
    void builder_buildWithAllFields_setsFieldsCorrectly() {
        // Arrange
        Map<Integer, Integer> distr = new HashMap<>();
        distr.put(5, 10);
        Map<String, Double> monthly = new HashMap<>();
        monthly.put("2024-01", 4.5);
        // Act
        Statistics stats = new Statistics.Builder()
                .setTotalReviews(10)
                .setAverageRating(4.5)
                .setRatingDistribution(distr)
                .setMonthlyRatingAverage(monthly)
                .build();
        // Assert
        assertEquals(10, stats.getTotalReviews());
        assertEquals(4.5, stats.getAverageRating());
        assertEquals(1, stats.getRatingDistribution().size());
        assertEquals(1, stats.getMonthlyRatingAverage().size());
    }


    @DisplayName("Tests that building with no fields sets defaults and empty collections")
    @Test
    void builder_buildWithNoFields_setsDefaultsAndEmptyCollections() {
        // Arrange
        // Act
        Statistics stats = new Statistics.Builder().build();
        // Assert
        assertEquals(0, stats.getTotalReviews());
        assertEquals(0.0, stats.getAverageRating());
        assertTrue(stats.getRatingDistribution().isEmpty());
        assertTrue(stats.getMonthlyRatingAverage().isEmpty());
    }


    @DisplayName("Tests that building with different values results in distinct objects")
    @Test
    void builder_buildWithDifferentValues_resultsInDistinctObjects() {
        // Arrange
        // Act
        Statistics stats1 = new Statistics.Builder().setTotalReviews(1).build();
        Statistics stats2 = new Statistics.Builder().setTotalReviews(2).build();
        // Assert
        assertNotEquals(stats1, stats2);
    }


    @DisplayName("Tests that setting negative totalReviews throws InvalidInputException.")
    @Test
    void builder_setNegativeTotalReviews_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Statistics.Builder().setTotalReviews(-1).build());
    }


    @DisplayName("Tests that setting negative averageRating throws InvalidInputException.")
    @Test
    void builder_setNegativeAverageRating_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Statistics.Builder().setAverageRating(-1.0).build());
    }


    @DisplayName("Tests that Statistics is immutable and collections are unmodifiable and defensively copied.")
    @Test
    void builder_immutabilityEnforced_collectionsAreUnmodifiableAndDefensiveCopied() {
        // Arrange
        Map<Integer, Integer> distr = new HashMap<>();
        distr.put(5, 10);
        Statistics stats = new Statistics.Builder()
                .setTotalReviews(1)
                .setAverageRating(5.0)
                .setRatingDistribution(distr)
                .build();
        // Act
        distr.put(1, 99); // Should not affect stats
        // Assert
        assertFalse(stats.getRatingDistribution().containsKey(1));
        assertThrows(UnsupportedOperationException.class, () -> stats.getRatingDistribution().put(2, 2));
    }
}
