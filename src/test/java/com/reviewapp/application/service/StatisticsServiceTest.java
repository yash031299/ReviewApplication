package com.reviewapp.application.service;

import com.reviewapp.domain.model.Statistics;
import com.reviewapp.domain.port.ReviewStatsPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StatisticsService} covering statistics aggregation, edge cases, and error handling.
 * Each test follows the Arrange-Act-Assert pattern and documents scenario and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private ReviewStatsPort reviewStatsPort;

    private StatisticsService statisticsService;

    /**
     * Sets up the StatisticsService with mocked ReviewStatsPort before each test.
     */
    @BeforeEach
    void setUp() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(100);
        when(reviewStatsPort.getAverageRating()).thenReturn(4.5);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(Map.of(5, 50, 4, 50));
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Map.of("2024-01", 4.5));
        statisticsService = new StatisticsService(reviewStatsPort);
    }

    /**
     * Tests that getReviewStatistics returns correct statistics for typical data.
     */
    @Test
    void getReviewStatistics_shouldReturnComputedStatistics() {
        // Arrange
        // (Mocks set up in setUp)
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Assert
        assertNotNull(statistics);
        assertEquals(100, statistics.getTotalReviews());
        assertEquals(4.5, statistics.getAverageRating());
        assertEquals(Map.of(5, 50, 4, 50), statistics.getRatingDistribution());
        assertEquals(Map.of("2024-01", 4.5), statistics.getMonthlyRatingAverage());
    }

    /**
     * Tests that getReviewStatistics returns empty/zero statistics when there are no reviews.
     */
    @Test
    void getReviewStatistics_whenNoReviews_shouldReturnEmptyStatistics() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(0);
        when(reviewStatsPort.getAverageRating()).thenReturn(0.0);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(Collections.emptyMap());
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Collections.emptyMap());
        statisticsService = new StatisticsService(reviewStatsPort);
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Assert
        assertNotNull(statistics);
        assertEquals(0, statistics.getTotalReviews());
        assertEquals(0.0, statistics.getAverageRating());
        assertEquals(Collections.emptyMap(), statistics.getRatingDistribution());
        assertEquals(Collections.emptyMap(), statistics.getMonthlyRatingAverage());
    }

    /**
     * Tests that getReviewStatistics handles large numbers correctly.
     */
    @Test
    void getReviewStatistics_withLargeNumbers_shouldReturnCorrectStatistics() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(Integer.MAX_VALUE);
        when(reviewStatsPort.getAverageRating()).thenReturn(5.0);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(Map.of(5, Integer.MAX_VALUE));
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Map.of("2099-12", 5.0));
        statisticsService = new StatisticsService(reviewStatsPort);
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Assert
        assertNotNull(statistics);
        assertEquals(Integer.MAX_VALUE, statistics.getTotalReviews());
        assertEquals(5.0, statistics.getAverageRating());
        assertEquals(Map.of(5, Integer.MAX_VALUE), statistics.getRatingDistribution());
        assertEquals(Map.of("2099-12", 5.0), statistics.getMonthlyRatingAverage());
    }

    /**
     * Tests that getReviewStatistics throws InvalidInputException for negative/invalid values.
     */
    @Test
    void getReviewStatistics_withNegativeAndInvalidValues_shouldHandleOrThrow() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(-1);
        when(reviewStatsPort.getAverageRating()).thenReturn(-3.0);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(Map.of(-1, -100));
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Map.of("bad-month", -5.0));
        // Act & Assert
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class, () -> new StatisticsService(reviewStatsPort).getReviewStatistics());
    }

    /**
     * Tests that getReviewStatistics returns empty maps if port returns null maps.
     */
    @Test
    void getReviewStatistics_withNullMaps_shouldReturnEmptyMaps() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(10);
        when(reviewStatsPort.getAverageRating()).thenReturn(3.0);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(null);
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(null);
        statisticsService = new StatisticsService(reviewStatsPort);
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Assert
        assertNotNull(statistics);
        assertEquals(10, statistics.getTotalReviews());
        assertEquals(3.0, statistics.getAverageRating());
        assertEquals(Collections.emptyMap(), statistics.getRatingDistribution());
        assertEquals(Collections.emptyMap(), statistics.getMonthlyRatingAverage());
    }

    /**
     * Tests that getReviewStatistics covers full rating range and missing values.
     */
    @Test
    void getReviewStatistics_withFullRatingRangeAndMissingValues() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(5);
        when(reviewStatsPort.getAverageRating()).thenReturn(3.0);
        when(reviewStatsPort.getRatingDistribution()).thenReturn(Map.of(1, 1, 2, 1, 3, 1, 5, 2));
        when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Map.of("2025-01", 3.0));
        statisticsService = new StatisticsService(reviewStatsPort);
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Assert
        assertNotNull(statistics);
        assertEquals(5, statistics.getTotalReviews());
        assertEquals(3.0, statistics.getAverageRating());
        assertEquals(Map.of(1, 1, 2, 1, 3, 1, 5, 2), statistics.getRatingDistribution());
        assertEquals(Map.of("2025-01", 3.0), statistics.getMonthlyRatingAverage());
    }

    /**
     * Tests that getReviewStatistics propagates exceptions from the port.
     */
    @Test
    void getReviewStatistics_whenPortThrows_shouldPropagateException() {
        // Arrange
        when(reviewStatsPort.getTotalReviewCountStats()).thenThrow(new RuntimeException("DB error"));
        // Act & Assert
        assertThrows(RuntimeException.class, () -> new StatisticsService(reviewStatsPort));
    }

    /**
     * Tests that the statistics snapshot is immutable even if the port changes after construction.
     */
    @Test
    void getReviewStatistics_snapshotShouldBeImmutableEvenIfPortChanges() {
        // Arrange
        lenient().when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(10);
        lenient().when(reviewStatsPort.getAverageRating()).thenReturn(4.0);
        lenient().when(reviewStatsPort.getRatingDistribution()).thenReturn(Map.of(5, 10));
        lenient().when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Map.of("2025-01", 4.0));
        statisticsService = new StatisticsService(reviewStatsPort);
        // Act
        Statistics statistics = statisticsService.getReviewStatistics();
        // Simulate port data change
        lenient().when(reviewStatsPort.getTotalReviewCountStats()).thenReturn(0);
        lenient().when(reviewStatsPort.getAverageRating()).thenReturn(0.0);
        lenient().when(reviewStatsPort.getRatingDistribution()).thenReturn(Collections.emptyMap());
        lenient().when(reviewStatsPort.getMonthlyRatingAverage()).thenReturn(Collections.emptyMap());
        // Assert
        assertEquals(10, statistics.getTotalReviews());
        assertEquals(4.0, statistics.getAverageRating());
        assertEquals(Map.of(5, 10), statistics.getRatingDistribution());
        assertEquals(Map.of("2025-01", 4.0), statistics.getMonthlyRatingAverage());
    }
}