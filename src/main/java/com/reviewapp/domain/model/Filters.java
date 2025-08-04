package com.reviewapp.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import com.reviewapp.application.exception.*;

/**
 * Immutable filter criteria for querying review data.
 * <p>
 * Use the nested {@link Builder} to construct instances with validation.
 * All dates and times use the {@code java.time} API to ensure type safety.
 * <p>
 * Typical usage:
 * <pre>{@code
 * Filters filters = new Filters.Builder()
 *         .setMinRating(3)
 *         .setAuthorName("John")
 *         .setStartDate(LocalDate.parse("2023-01-01"))
 *         .setEndDate(LocalDate.parse("2023-12-31"))
 *         .setSortByRating(true)
 *         .build();
 * }</pre>
 */
public final class Filters {

    // --- Rating constraints ---
    private final Integer rating;
    private final Integer minRating;
    private final Integer maxRating;

    // --- Review metadata ---
    private final String authorName;
    private final String reviewTitle;
    private final String productName;
    private final LocalDate reviewDate; // exact date match (if set)
    private final String storeName;

    // --- Range criteria ---
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    // --- Sorting preferences ---
    private final boolean sortByDate;
    private final boolean sortByRating;

    private Filters(Builder builder) {
        this.rating = builder.rating;
        this.minRating = builder.minRating;
        this.maxRating = builder.maxRating;

        this.authorName = builder.authorName;
        this.reviewTitle = builder.reviewTitle;
        this.productName = builder.productName;
        this.reviewDate = builder.reviewDate;
        this.storeName = builder.storeName;

        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;

        this.sortByDate = builder.sortByDate;
        this.sortByRating = builder.sortByRating;
    }

    // -------------------- Getters --------------------

    /** Exact rating to match (1..5), if set. */
    public Integer getRating() {
        return rating;
    }

    /** Minimum rating inclusive (1..5), if set. */
    public Integer getMinRating() {
        return minRating;
    }

    /** Maximum rating inclusive (1..5), if set. */
    public Integer getMaxRating() {
        return maxRating;
    }

    /** Case-insensitive substring match on author name, if set. */
    public String getAuthorName() {
        return authorName;
    }

    /** Case-insensitive substring match on review title, if set. */
    public String getReviewTitle() {
        return reviewTitle;
    }

    /** Exact product name (case-insensitive), if set. */
    public String getProductName() {
        return productName;
    }

    /** Exact review date to match, if set. */
    public LocalDate getReviewDate() {
        return reviewDate;
    }

    /** Case-insensitive substring match on review source/store, if set. */
    public String getStoreName() {
        return storeName;
    }

    /** Start date inclusive for date-range filtering, if set. */
    public LocalDate getStartDate() {
        return startDate;
    }

    /** End date inclusive for date-range filtering, if set. */
    public LocalDate getEndDate() {
        return endDate;
    }

    /** Start time inclusive for time-range filtering, if set. */
    public LocalTime getStartTime() {
        return startTime;
    }

    /** End time inclusive for time-range filtering, if set. */
    public LocalTime getEndTime() {
        return endTime;
    }

    /** Whether to sort results by date (ascending/implementation-defined). */
    public boolean isSortByDate() {
        return sortByDate;
    }

    /** Whether to sort results by rating (descending/implementation-defined). */
    public boolean isSortByRating() {
        return sortByRating;
    }

    // -------------------- Object contract --------------------

    @Override
    public String toString() {
        return "Filters{" +
                "rating=" + rating +
                ", minRating=" + minRating +
                ", maxRating=" + maxRating +
                ", authorName='" + authorName + '\'' +
                ", reviewTitle='" + reviewTitle + '\'' +
                ", productName='" + productName + '\'' +
                ", reviewDate=" + reviewDate +
                ", storeName='" + storeName + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", sortByDate=" + sortByDate +
                ", sortByRating=" + sortByRating +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Filters)) return false;
        Filters that = (Filters) other;
        return sortByDate == that.sortByDate
                && sortByRating == that.sortByRating
                && Objects.equals(rating, that.rating)
                && Objects.equals(minRating, that.minRating)
                && Objects.equals(maxRating, that.maxRating)
                && Objects.equals(authorName, that.authorName)
                && Objects.equals(reviewTitle, that.reviewTitle)
                && Objects.equals(productName, that.productName)
                && Objects.equals(reviewDate, that.reviewDate)
                && Objects.equals(storeName, that.storeName)
                && Objects.equals(startDate, that.startDate)
                && Objects.equals(endDate, that.endDate)
                && Objects.equals(startTime, that.startTime)
                && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                rating, minRating, maxRating,
                authorName, reviewTitle, productName, reviewDate, storeName,
                startDate, endDate, startTime, endTime,
                sortByDate, sortByRating
        );
    }

    // -------------------- Builder --------------------

    /**
     * Builder for {@link Filters}.
     * <p>
     * Validates ranges and logical relationships (e.g., min ≤ max, start ≤ end).
     * Once {@link #build()} is invoked, the builder cannot be reused.
     */
    public static final class Builder {

        // Ratings
        private Integer rating;
        private Integer minRating;
        private Integer maxRating;

        // Review metadata
        private String authorName;
        private String reviewTitle;
        private String productName;
        private LocalDate reviewDate;
        private String storeName;

        // Date & time ranges
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;

        // Sorting
        private boolean sortByDate;
        private boolean sortByRating;

        // Reuse guard
        private boolean built = false;

        /** Guard method to prevent use after {@link #build()}. */
        private void assertNotBuilt() {
            if (built) {
                throw new InvalidInputException(
                        "This builder instance has already been used to build a Filters object.");
            }
        }

        /** Sets exact rating (1..5). */
        public Builder setRating(Integer rating) {
            assertNotBuilt();
            this.rating = rating;
            return this;
        }

        /** Sets minimum rating inclusive (1..5). */
        public Builder setMinRating(Integer minRating) {
            assertNotBuilt();
            this.minRating = minRating;
            return this;
        }

        /** Sets maximum rating inclusive (1..5). */
        public Builder setMaxRating(Integer maxRating) {
            assertNotBuilt();
            this.maxRating = maxRating;
            return this;
        }

        /** Sets author substring (case-insensitive). */
        public Builder setAuthorName(String authorName) {
            assertNotBuilt();
            this.authorName = authorName;
            return this;
        }

        /** Sets title substring (case-insensitive). */
        public Builder setReviewTitle(String reviewTitle) {
            assertNotBuilt();
            this.reviewTitle = reviewTitle;
            return this;
        }

        /** Sets exact product name (case-insensitive). */
        public Builder setProductName(String productName) {
            assertNotBuilt();
            this.productName = productName;
            return this;
        }

        /** Sets exact review date to match. */
        public Builder setReviewDate(LocalDate reviewDate) {
            assertNotBuilt();
            this.reviewDate = reviewDate;
            return this;
        }

        /** Sets review source/store substring (case-insensitive). */
        public Builder setStoreName(String storeName) {
            assertNotBuilt();
            this.storeName = storeName;
            return this;
        }

        /** Sets start date inclusive for range filtering. */
        public Builder setStartDate(LocalDate startDate) {
            assertNotBuilt();
            this.startDate = startDate;
            return this;
        }

        /** Sets end date inclusive for range filtering. */
        public Builder setEndDate(LocalDate endDate) {
            assertNotBuilt();
            this.endDate = endDate;
            return this;
        }

        /** Sets start time inclusive for time range filtering. */
        public Builder setStartTime(LocalTime startTime) {
            assertNotBuilt();
            this.startTime = startTime;
            return this;
        }

        /** Sets end time inclusive for time range filtering. */
        public Builder setEndTime(LocalTime endTime) {
            assertNotBuilt();
            this.endTime = endTime;
            return this;
        }

        /** Enables/disables sort by date. */
        public Builder setSortByDate(boolean sortByDate) {
            assertNotBuilt();
            this.sortByDate = sortByDate;
            return this;
        }

        /** Enables/disables sort by rating. */
        public Builder setSortByRating(boolean sortByRating) {
            assertNotBuilt();
            this.sortByRating = sortByRating;
            return this;
        }

        /**
         * Builds an immutable {@link Filters} instance after validating:
         * <ul>
         *     <li>Rating values within 1..5 (if provided).</li>
         *     <li>{@code minRating ≤ maxRating} (if both provided).</li>
         *     <li>{@code startDate ≤ endDate} (if both provided).</li>
         *     <li>{@code startTime ≤ endTime} (if both provided).</li>
         * </ul>
         *
         * @return a validated {@link Filters} instance
         * @throws InvalidInputException if any validation fails
         */
        public Filters build() {
            assertNotBuilt();
            validate();
            built = true;
            return new Filters(this);
        }

        // -------------------- Validation helpers --------------------

        private void validate() {
            validateRatingInRange("Rating", rating);
            validateRatingInRange("Min rating", minRating);
            validateRatingInRange("Max rating", maxRating);

            if (minRating != null && maxRating != null && minRating > maxRating) {
                throw new InvalidInputException("Min rating cannot be greater than max rating.");
            }

            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                throw new InvalidInputException("Start date cannot be after end date.");
            }

            if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
                throw new InvalidInputException("Start time cannot be after end time.");
            }
        }

        private void validateRatingInRange(String fieldName, Integer value) {
            if (value != null && (value < 1 || value > 5)) {
                throw new InvalidInputException(fieldName + " must be between 1 and 5.");
            }
        }
    }
}
