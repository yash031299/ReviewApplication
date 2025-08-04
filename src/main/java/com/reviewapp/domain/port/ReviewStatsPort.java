package com.reviewapp.domain.port;

import java.util.Map;

/**
 * Port interface for retrieving review statistics from the data store.
 */
public interface ReviewStatsPort {
    /**
     * Returns the total number of reviews for statistics purposes.
     * @return the total review count
     */
    int getTotalReviewCountStats();

    /**
     * Calculates the average rating across all reviews.
     * @return the average rating value
     */
    double getAverageRating();

    /**
     * Retrieves the distribution of ratings as a map where the key is the rating value and the value is the count.
     * @return a map of rating values to their counts
     */
    Map<Integer, Integer> getRatingDistribution();

    /**
     * Retrieves the average rating per month as a map where the key is the month and the value is the average rating.
     * @return a map of month strings to average ratings
     */
    Map<String, Double> getMonthlyRatingAverage();
}
