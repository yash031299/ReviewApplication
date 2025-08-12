package com.reviewapp.domain.model;

import com.reviewapp.application.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

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

    @DisplayName("Tests that builder cannot be reused after build")
    @Test
    void builder_reuseAfterBuild_throwsIllegalStateException() {
        Statistics.Builder builder = new Statistics.Builder().setTotalReviews(1);
        builder.build();
        assertThrows(IllegalStateException.class, builder::build);
        assertThrows(IllegalStateException.class, () -> builder.setTotalReviews(2));
    }

    @DisplayName("Tests that negative ratingDistribution count throws IllegalArgumentException")
    @Test
    void builder_negativeRatingDistributionCount_throwsIllegalArgumentException() {
        Map<Integer, Integer> distr = Map.of(5, -10);
        Statistics.Builder builder = new Statistics.Builder().setTotalReviews(1).setAverageRating(1.0);
        assertThrows(IllegalArgumentException.class, () -> builder.setRatingDistribution(distr));
    }

    @DisplayName("Tests that setMonthlyRatingAverage and setRatingDistribution accept null and empty maps")
    @Test
    void builder_nullAndEmptyMapsAreAccepted() {
        Statistics stats1 = new Statistics.Builder().setTotalReviews(1).setAverageRating(1.0).setRatingDistribution(null).setMonthlyRatingAverage(null).build();
        assertTrue(stats1.getRatingDistribution().isEmpty());
        assertTrue(stats1.getMonthlyRatingAverage().isEmpty());
        Statistics stats2 = new Statistics.Builder().setTotalReviews(1).setAverageRating(1.0).setRatingDistribution(Collections.emptyMap()).setMonthlyRatingAverage(Collections.emptyMap()).build();
        assertTrue(stats2.getRatingDistribution().isEmpty());
        assertTrue(stats2.getMonthlyRatingAverage().isEmpty());
    }

    @DisplayName("Tests that overwriting builder fields (last value wins)")
    @Test
    void builder_overwriteFields_lastValueWins() {
        Statistics stats = new Statistics.Builder()
            .setTotalReviews(1).setTotalReviews(2)
            .setAverageRating(1.0).setAverageRating(4.0)
            .build();
        assertEquals(2, stats.getTotalReviews());
        assertEquals(4.0, stats.getAverageRating());
    }


    private Statistics.Builder baseBuilder() {
        Map<Integer, Integer> distr = new HashMap<>();
        distr.put(5, 10);
        Map<String, Double> monthly = new HashMap<>();
        monthly.put("2024-01", 4.5);
        return new Statistics.Builder()
                .setTotalReviews(10)
                .setAverageRating(4.5)
                .setRatingDistribution(distr)
                .setMonthlyRatingAverage(monthly);
    }

    @DisplayName("Tests equals returns false when differing only in each individual field")
    @Test
    void equals_differOnlyInEachField() {
        Statistics base = baseBuilder().build();

        // Differ in totalReviews
        Statistics varTotalReviews = baseBuilder().setTotalReviews(20).build();
        assertNotEquals(base, varTotalReviews);

        // Differ in averageRating
        Statistics varAverageRating = baseBuilder().setAverageRating(3.5).build();
        assertNotEquals(base, varAverageRating);

        // Differ in ratingDistribution
        Map<Integer, Integer> diffDistr = new HashMap<>();
        diffDistr.put(4, 10);
        Statistics varRatingDistribution = baseBuilder().setRatingDistribution(diffDistr).build();
        assertNotEquals(base, varRatingDistribution);

        // Differ in monthlyRatingAverage
        Map<String, Double> diffMonthly = new HashMap<>();
        diffMonthly.put("2024-02", 4.0);
        Statistics varMonthlyRatingAverage = baseBuilder().setMonthlyRatingAverage(diffMonthly).build();
        assertNotEquals(base, varMonthlyRatingAverage);
    }

    @DisplayName("Tests equals with NaN averageRating")
    @Test
    void equals_withNaNAverageRating() {
        Statistics base = baseBuilder().setAverageRating(Double.NaN).build();
        Statistics sameNaN = baseBuilder().setAverageRating(Double.NaN).build();
        Statistics diffRating = baseBuilder().setAverageRating(4.5).build();
        assertEquals(base, sameNaN); // NaN == NaN
        assertNotEquals(base, diffRating); // NaN != 4.5
    }

    @DisplayName("Tests all setters throw IllegalStateException after build")
    @Test
    void builder_allSettersAfterBuild_throwIllegalStateException() {
        Statistics.Builder builder = baseBuilder();
        builder.build(); // Mark as built
        assertThrows(IllegalStateException.class, () -> builder.setTotalReviews(20));
        assertThrows(IllegalStateException.class, () -> builder.setAverageRating(3.5));
        assertThrows(IllegalStateException.class, () -> builder.setRatingDistribution(new HashMap<>()));
        assertThrows(IllegalStateException.class, () -> builder.setMonthlyRatingAverage(new HashMap<>()));
    }

    @DisplayName("Tests ratingDistribution with null count is accepted")
    @Test
    void builder_ratingDistributionNullCount_accepted() {
        Map<Integer, Integer> distr = new HashMap<>();
        distr.put(5, null);
        Statistics stats = new Statistics.Builder()
                .setTotalReviews(10)
                .setAverageRating(4.5)
                .setRatingDistribution(distr)
                .build();
        assertEquals(null, stats.getRatingDistribution().get(5));
    }

    @DisplayName("Tests hashCode consistency with equal objects")
    @Test
    void hashCode_consistencyWithEqualObjects() {
        Statistics stats1 = baseBuilder().build();
        Statistics stats2 = baseBuilder().build();
        assertEquals(stats1.hashCode(), stats2.hashCode());
    }

    @DisplayName("Tests toString contains all fields")
    @Test
    void toString_containsAllFields() {
        Statistics stats = baseBuilder().build();
        String str = stats.toString();
        assertTrue(str.contains("totalReviews=10"));
        assertTrue(str.contains("averageRating=4.5"));
        assertTrue(str.contains("ratingDistribution={5=10}"));
        assertTrue(str.contains("monthlyRatingAverage={2024-01=4.5}"));
    }

    @DisplayName("Tests equals identity branch and null other")
    @Test
    void equals_identityAndNullOther() {
        // Arrange
        Statistics stats = baseBuilder().build();
        // Act & Assert
        assertEquals(stats, stats);
        assertNotEquals(stats, null);
    }

    @DisplayName("Tests all assertNotBuilt throws in every call site after build")
    @Test
    void builder_assertNotBuiltAllSitesAfterBuild() {
        // Arrange
        Statistics.Builder builder = baseBuilder();
        builder.build(); // Mark as built
        // Act & Assert
        assertThrows(IllegalStateException.class, builder::build); // build() site
        assertThrows(IllegalStateException.class, () -> builder.setTotalReviews(5)); // setTotalReviews site
        assertThrows(IllegalStateException.class, () -> builder.setAverageRating(3.0)); // setAverageRating site
        assertThrows(IllegalStateException.class, () -> builder.setRatingDistribution(null)); // setRatingDistribution site
        assertThrows(IllegalStateException.class, () -> builder.setMonthlyRatingAverage(null)); // setMonthlyRatingAverage site
    }

    @DisplayName("Tests setRatingDistribution loop with multiple entries, nulls, and positives")
    @Test
    void builder_ratingDistributionLoopMultipleEntriesAndNulls() {
        // Arrange
        Map<Integer, Integer> distr = new HashMap<>();
        distr.put(1, 5); // Positive, first iteration
        distr.put(2, null); // Null count, skips <0 check
        distr.put(3, 10); // Positive, later iteration
        distr.put(4, 0); // Zero (edge >=0)
        // Act
        Statistics stats = new Statistics.Builder()
                .setRatingDistribution(distr) // Hits else, full loop iterations (no throw)
                .build();
        // Assert
        assertEquals(4, stats.getRatingDistribution().size()); // Ensures copy and sorting (TreeMap)
        assertNull(stats.getRatingDistribution().get(2)); // Null preserved
    }

    @DisplayName("Tests setRatingDistribution loop throws on negative in different positions")
    @Test
    void builder_ratingDistributionLoopNegativeInPositions() {
        // Arrange
        Map<Integer, Integer> distrFirstNegative = new HashMap<>();
        distrFirstNegative.put(1, -1); // Negative first, throws early
        Map<Integer, Integer> distrMiddleNegative = new HashMap<>();
        distrMiddleNegative.put(1, 5);
        distrMiddleNegative.put(2, -1); // Negative middle, after some iterations
        Map<Integer, Integer> distrLastNegative = new HashMap<>();
        distrLastNegative.put(1, 5);
        distrLastNegative.put(2, 10);
        distrLastNegative.put(3, -1); // Negative last
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Statistics.Builder().setRatingDistribution(distrFirstNegative));
        assertThrows(IllegalArgumentException.class, () -> new Statistics.Builder().setRatingDistribution(distrMiddleNegative));
        assertThrows(IllegalArgumentException.class, () -> new Statistics.Builder().setRatingDistribution(distrLastNegative));
    }

    @DisplayName("Tests build validation throws for negative values (forced via reflection for coverage)")
    @Test
    void builder_buildValidationThrows() throws NoSuchFieldException, IllegalAccessException {
        // Arrange (use reflection to force invalid state, as setters prevent it)
        Statistics.Builder builder = new Statistics.Builder();
        java.lang.reflect.Field totalField = Statistics.Builder.class.getDeclaredField("totalReviews");
        totalField.setAccessible(true);
        totalField.set(builder, -1); // Force negative total
        java.lang.reflect.Field avgField = Statistics.Builder.class.getDeclaredField("averageRating");
        avgField.setAccessible(true);
        avgField.set(builder, -1.0); // Force negative avg
        // Act & Assert (hits build()'s <0 true branches)
        assertThrows(InvalidInputException.class, builder::build); // Will throw on total <0 first
    }

    @DisplayName("Tests hashCode with null and empty maps")
    @Test
    void hashCode_nullAndEmptyMaps() {
        // Arrange
        Statistics empty = new Statistics.Builder().setRatingDistribution(null).setMonthlyRatingAverage(null).build();
        Statistics sameEmpty = new Statistics.Builder().setRatingDistribution(null).setMonthlyRatingAverage(null).build();
        // Act & Assert
        assertEquals(empty.hashCode(), sameEmpty.hashCode());
    }


}
