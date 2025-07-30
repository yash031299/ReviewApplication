package com.example.reviewapp.domain.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Immutable snapshot of review statistics.
 * <p>
 * Encapsulates:
 * <ul>
 *   <li>Total review count</li>
 *   <li>Average rating across reviews</li>
 *   <li>Rating distribution (rating value → count)</li>
 *   <li>Monthly average ratings (month label → average rating)</li>
 * </ul>
 * Instances are immutable and safe to share across threads.
 * Use {@link Statistics.Builder} to construct validated instances.
 */
public final class Statistics {

    /** Total number of reviews considered for these statistics (non-negative). */
    private final int totalReviews;

    /** Average rating across all reviews (commonly 0..5). */
    private final double averageRating;

    /** Map of rating value → number of reviews with that rating. Immutable copy. */
    private final Map<Integer, Integer> ratingDistribution;

    /** Map of month label → average rating for that month. Immutable copy. */
    private final Map<String, Double> monthlyRatingAverage;

    private Statistics(Builder builder) {
        this.totalReviews = builder.totalReviews;
        this.averageRating = builder.averageRating;

        // Make defensive, unmodifiable copies to guarantee immutability of internal state
        Map<Integer, Integer> rd =
                (builder.ratingDistribution == null) ? Collections.emptyMap()
                        : new TreeMap<>(builder.ratingDistribution);
        Map<String, Double> ma =
                (builder.monthlyRatingAverage == null) ? Collections.emptyMap()
                        : new TreeMap<>(builder.monthlyRatingAverage);

        this.ratingDistribution = Collections.unmodifiableMap(rd);
        this.monthlyRatingAverage = Collections.unmodifiableMap(ma);
    }

    // -------------------- Accessors --------------------

    /** @return total review count (non-negative) */
    public int getTotalReviews() {
        return totalReviews;
    }

    /** @return average rating (typ. 0..5 depending on your scale) */
    public double getAverageRating() {
        return averageRating;
    }

    /**
     * @return immutable map of rating value → count.
     * The returned map is unmodifiable and safe to expose publicly.
     */
    public Map<Integer, Integer> getRatingDistribution() {
        return ratingDistribution;
    }

    /**
     * @return immutable map of month label → average rating.
     * The returned map is unmodifiable and safe to expose publicly.
     */
    public Map<String, Double> getMonthlyRatingAverage() {
        return monthlyRatingAverage;
    }

    // -------------------- Object contract --------------------

    @Override
    public String toString() {
        return "Statistics{" +
                "totalReviews=" + totalReviews +
                ", averageRating=" + averageRating +
                ", ratingDistribution=" + ratingDistribution +
                ", monthlyRatingAverage=" + monthlyRatingAverage +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Statistics)) return false;
        Statistics that = (Statistics) other;
        return totalReviews == that.totalReviews
                && Double.compare(that.averageRating, averageRating) == 0
                && Objects.equals(ratingDistribution, that.ratingDistribution)
                && Objects.equals(monthlyRatingAverage, that.monthlyRatingAverage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalReviews, averageRating, ratingDistribution, monthlyRatingAverage);
    }

    // -------------------- Builder --------------------

    /**
     * Builder for {@link Statistics}.
     * <p>
     * Validates basic invariants on {@link #build()}:
     * <ul>
     *   <li>{@code totalReviews ≥ 0}</li>
     *   <li>(Optional) {@code averageRating} within expected bounds</li>
     *   <li>Maps are copied defensively and never null</li>
     * </ul>
     * After build, the builder cannot be reused.
     */
    public static final class Builder {

        // Total reviews and average rating
        private int totalReviews = 0;
        private double averageRating = 0.0;

        // Maps of rating to count and month to average rating
        private Map<Integer, Integer> ratingDistribution = new TreeMap<>();
        private Map<String, Double> monthlyRatingAverage = new TreeMap<>();

        // Builder reuse guard
        private boolean built = false;

        /** Guard method to prevent reuse after build. */
        private void assertNotBuilt() {
            if (built) {
                throw new IllegalStateException("Builder instance has already built a Statistics object.");
            }
        }

        /**
         * Sets the total number of reviews represented by this snapshot.
         *
         * @param totalReviews non-negative number of reviews
         * @return this builder
         * @throws IllegalArgumentException if {@code totalReviews} is negative
         */
        public Builder setTotalReviews(int totalReviews) {
            assertNotBuilt();
            if (totalReviews < 0) {
                throw new IllegalArgumentException("Total reviews cannot be negative.");
            }
            this.totalReviews = totalReviews;
            return this;
        }

        /**
         * Sets the average rating across all reviews.
         * <p>
         * If your rating scale is 0..5 or 1..5, you may want this to be in that range.
         * This builder does not hard-enforce the range; adjust if your domain requires it.
         *
         * @param averageRating average rating value
         * @return this builder
         */
        public Builder setAverageRating(double averageRating) {
            assertNotBuilt();
            this.averageRating = averageRating;
            return this;
        }

        /**
         * Sets the rating distribution as a copy of the provided map.
         * Null is treated as an empty map.
         *
         * @param ratingDistribution map of rating value → count
         * @return this builder
         * @throws IllegalArgumentException if any count is negative
         */
        public Builder setRatingDistribution(Map<Integer, Integer> ratingDistribution) {
            assertNotBuilt();
            if (ratingDistribution == null) {
                this.ratingDistribution = new TreeMap<>();
            } else {
                // validate counts non-negative
                for (Map.Entry<Integer, Integer> entry : ratingDistribution.entrySet()) {
                    Integer count = entry.getValue();
                    if (count != null && count < 0) {
                        throw new IllegalArgumentException("Rating count cannot be negative for rating: " + entry.getKey());
                    }
                }
                this.ratingDistribution = new TreeMap<>(ratingDistribution);
            }
            return this;
        }

        /**
         * Sets the monthly average ratings as a copy of the provided map.
         * Null is treated as an empty map.
         *
         * @param monthlyRatingAverage map of month label → average rating
         * @return this builder
         */
        public Builder setMonthlyRatingAverage(Map<String, Double> monthlyRatingAverage) {
            assertNotBuilt();
            if (monthlyRatingAverage == null) {
                this.monthlyRatingAverage = new TreeMap<>();
            } else {
                this.monthlyRatingAverage = new TreeMap<>(monthlyRatingAverage);
            }
            return this;
        }

        /**
         * Builds an immutable {@link Statistics} instance after validation.
         * <p>
         * Validations performed:
         * <ul>
         *   <li>{@code totalReviews ≥ 0}</li>
         * </ul>
         *
         * @return a validated immutable {@link Statistics} instance
         * @throws IllegalStateException if the builder is reused
         * @throws IllegalArgumentException if validation fails
         */
        public Statistics build() {
            assertNotBuilt();

            // Basic validations
            if (totalReviews < 0) {
                throw new IllegalArgumentException("Total reviews cannot be negative.");
            }

            built = true;
            return new Statistics(this);
        }
    }
}
