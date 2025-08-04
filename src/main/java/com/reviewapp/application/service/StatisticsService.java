package com.reviewapp.application.service;

import com.reviewapp.domain.model.Statistics;
import com.reviewapp.domain.port.ReviewStatsPort;

import java.util.Map;
import java.util.Objects;

/**
 * Application service that exposes aggregate review statistics.
 * <p>
 * This service depends on the {@link ReviewStatsPort} (a read-only port) so the
 * underlying datastore (SQLite, in-memory, Postgres, REST, etc.) can be swapped
 * without changing this class.
 * <p>
 * By default, statistics are computed once at construction time and cached in
 * an immutable {@link Statistics} snapshot. If you need live/refreshable stats,
 * expose a {@code refresh()} method or recompute on each {@link #getReviewStatistics()} call.
 */
public class StatisticsService {

    private final ReviewStatsPort statsPort;
    private final Statistics statisticsSnapshot;

    /**
     * Builds a {@code StatisticsService} and computes an initial snapshot of statistics.
     *
     * @param statsPort the stats read port providing aggregate queries (non-null)
     */
    public StatisticsService(ReviewStatsPort statsPort) {
        this.statsPort = Objects.requireNonNull(statsPort, "statsPort must not be null");
        this.statisticsSnapshot = computeStatistics();
    }

    /**
     * Returns the precomputed, immutable statistics snapshot.
     * <p>
     * If you need up-to-date statistics each time, replace the body with
     * {@code return computeStatistics();}.
     */
    public Statistics getReviewStatistics() {
        return statisticsSnapshot;
    }

    // ---------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------

    private Statistics computeStatistics() {
        int totalReviews = statsPort.getTotalReviewCountStats();
        double averageRating = statsPort.getAverageRating();
        Map<Integer, Integer> ratingDistribution = statsPort.getRatingDistribution();
        Map<String, Double> monthlyRatingAverage = statsPort.getMonthlyRatingAverage();

        return new Statistics.Builder()
                .setTotalReviews(totalReviews)
                .setAverageRating(averageRating)
                .setRatingDistribution(ratingDistribution)
                .setMonthlyRatingAverage(monthlyRatingAverage)
                .build();
    }
}
