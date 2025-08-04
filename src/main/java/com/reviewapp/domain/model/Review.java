package com.reviewapp.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import com.reviewapp.application.exception.*;

/**
 * Immutable domain model representing a single product review.
 * <p>
 * Use {@link Builder} to construct validated instances. Instances are value objects with
 * stable equals/hashCode semantics based on all fields.
 * <p>
 * Required fields (validated on build):
 * <ul>
 *   <li>{@code reviewId} – must be non-null</li>
 *   <li>{@code productRating} – must be in the range 1..5 (inclusive)</li>
 *   <li>{@code reviewedDate} – must be non-null</li>
 * </ul>
 */
public final class Review {

    /** Unique identifier of the review. */
    private final Long reviewId;

    /** Free-form review text; may be null or empty. */
    private final String reviewText;

    /** Author's display name; may be null or empty. */
    private final String authorName;

    /** Review source/store/site name; may be null or empty. */
    private final String reviewSource;

    /** Review title/subject; may be null or empty. */
    private final String reviewTitle;

    /** Product name; may be null or empty. */
    private final String productName;

    /** The date on which the review was written/published (no time-of-day). */
    private final LocalDate reviewedDate;

    /** Star rating for the product (1..5 inclusive). */
    private final Integer productRating;

    private Review(Builder builder) {
        this.reviewId = builder.reviewId;
        this.reviewText = builder.reviewText;
        this.authorName = builder.authorName;
        this.reviewSource = builder.reviewSource;
        this.reviewTitle = builder.reviewTitle;
        this.productName = builder.productName;
        this.reviewedDate = builder.reviewedDate;
        this.productRating = builder.productRating;
    }

    // -------------------- Getters --------------------

    /** Unique identifier of the review. */
    public Long getReviewId() {
        return reviewId;
    }

    /** Free-form review text; may be null or empty. */
    public String getReviewText() {
        return reviewText;
    }

    /** Author's display name; may be null or empty. */
    public String getAuthorName() {
        return authorName;
    }

    /** Review source/store/site name; may be null or empty. */
    public String getReviewSource() {
        return reviewSource;
    }

    /** Review title/subject; may be null or empty. */
    public String getReviewTitle() {
        return reviewTitle;
    }

    /** Product name; may be null or empty. */
    public String getProductName() {
        return productName;
    }

    /** The date on which the review was written/published (no time-of-day). */
    public LocalDate getReviewedDate() {
        return reviewedDate;
    }

    /** Star rating for the product (1..5 inclusive). */
    public Integer getProductRating() {
        return productRating;
    }

    // -------------------- Object contract --------------------

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", reviewText='" + reviewText + '\'' +
                ", authorName='" + authorName + '\'' +
                ", reviewSource='" + reviewSource + '\'' +
                ", reviewTitle='" + reviewTitle + '\'' +
                ", productName='" + productName + '\'' +
                ", reviewedDate=" + reviewedDate +
                ", productRating=" + productRating +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Review)) return false;
        Review that = (Review) other;
        return Objects.equals(reviewId, that.reviewId)
                && Objects.equals(reviewText, that.reviewText)
                && Objects.equals(authorName, that.authorName)
                && Objects.equals(reviewSource, that.reviewSource)
                && Objects.equals(reviewTitle, that.reviewTitle)
                && Objects.equals(productName, that.productName)
                && Objects.equals(reviewedDate, that.reviewedDate)
                && Objects.equals(productRating, that.productRating);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                reviewId, reviewText, authorName, reviewSource,
                reviewTitle, productName, reviewedDate, productRating
        );
    }

    // -------------------- Builder --------------------

    /**
     * Builder for {@link Review}.
     * <p>
     * Enforces required fields and rating range on {@link #build()}.
     * After build, the builder cannot be reused.
     */
    public static final class Builder {

        private Long reviewId;
        private String reviewText;
        private String authorName;
        private String reviewSource;
        private String reviewTitle;
        private String productName;
        private LocalDate reviewedDate;
        private Integer productRating;

        // Guard to prevent reuse after build()
        private boolean built = false;

        /** Guard method to prevent using this builder after {@link #build()}. */
        private void assertNotBuilt() {
            if (built) {
                throw new InvalidInputException("Builder instance has already built a Review.");
            }
        }

        /** Sets the unique review identifier (required). */
        public Builder setReviewId(Long reviewId) {
            assertNotBuilt();
            this.reviewId = reviewId;
            return this;
        }

        /** Sets the free-form review text (optional). */
        public Builder setReviewText(String reviewText) {
            assertNotBuilt();
            this.reviewText = reviewText;
            return this;
        }

        /** Sets the author display name (optional). */
        public Builder setAuthorName(String authorName) {
            assertNotBuilt();
            this.authorName = authorName;
            return this;
        }

        /** Sets the review source/store/site (optional). */
        public Builder setReviewSource(String reviewSource) {
            assertNotBuilt();
            this.reviewSource = reviewSource;
            return this;
        }

        /** Sets the review title/subject (optional). */
        public Builder setReviewTitle(String reviewTitle) {
            assertNotBuilt();
            this.reviewTitle = reviewTitle;
            return this;
        }

        /** Sets the product name (optional). */
        public Builder setProductName(String productName) {
            assertNotBuilt();
            this.productName = productName;
            return this;
        }

        /** Sets the review date (required). */
        public Builder setReviewedDate(LocalDate reviewedDate) {
            assertNotBuilt();
            this.reviewedDate = reviewedDate;
            return this;
        }

        /** Sets the product star rating (required, 1..5 inclusive). */
        public Builder setProductRating(Integer productRating) {
            assertNotBuilt();
            this.productRating = productRating;
            return this;
        }

        /**
         * Builds an immutable {@link Review} instance after validation:
         * <ul>
         *   <li>{@code reviewId} must not be null</li>
         *   <li>{@code reviewedDate} must not be null</li>
         *   <li>{@code productRating} must be within 1..5 inclusive</li>
         * </ul>
         *
         * @return a validated {@link Review} instance
         * @throws InvalidInputException if the builder is reused or any required field is missing/invalid
         */
        public Review build() {
            assertNotBuilt();
            validateRequiredFields();
            validateRatingRange();
            built = true;
            return new Review(this);
        }

        // -------------------- Validation helpers --------------------

        private void validateRequiredFields() {
            if (reviewId == null) {
                throw new InvalidInputException("Review ID must not be null.");
            }
            if (reviewedDate == null) {
                throw new InvalidInputException("Reviewed date must not be null.");
            }
            if (productRating == null) {
                throw new InvalidInputException("Product rating must not be null.");
            }
        }

        private void validateRatingRange() {
            if (productRating < 1 || productRating > 5) {
                throw new InvalidInputException("Product rating must be between 1 and 5.");
            }
        }
    }
}
