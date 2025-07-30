package com.example.reviewapp.application.service;

import com.example.reviewapp.domain.model.Filters;
import com.example.reviewapp.domain.model.Review;
import com.example.reviewapp.domain.port.ReviewQueryPort;
import com.example.reviewapp.domain.port.ReviewWritePort;

import java.util.List;
import java.util.Objects;

/**
 * Application service for review read/write use cases.
 * <p>
 * This service depends on abstract ports (interfaces) rather than a concrete repository,
 * allowing the underlying datastore (SQLite, in-memory, Postgres, REST, etc.) to be
 * swapped without changing the service or callers.
 */
public class ReviewService {

    private final ReviewQueryPort queryPort;
    private final ReviewWritePort writePort; // may be a no-op if your use case is read-only

    /**
     * Constructs a service backed by the given ports.
     *
     * @param queryPort read/query port (required)
     * @param writePort write port (may be null if you don't need writes)
     */
    public ReviewService(ReviewQueryPort queryPort, ReviewWritePort writePort) {
        this.queryPort = Objects.requireNonNull(queryPort, "queryPort must not be null");
        this.writePort = writePort;
    }

    /** Fetch a single review by its identifier. */
    public Review getReviewById(long reviewId) {
        return queryPort.getReviewById(reviewId);
    }

    /** Keyword search across review text and titles. */
    public List<Review> getReviewsByKeywords(List<String> keywords) {
        return queryPort.getReviewsByKeywords(keywords);
    }

    /** Paged retrieval of reviews (1-based page index). */
    public List<Review> getReviewsPage(int page, int pageSize) {
        return queryPort.getReviewsPage(page, pageSize);
    }

    /** Total reviews in the datastore. */
    public int getTotalReviewCount() {
        return queryPort.getTotalReviewCount();
    }

    /** Filtered, paged retrieval of reviews. */
    public List<Review> getFilteredReviewsPage(Filters filters, int page, int pageSize) {
        return queryPort.getReviewsByFilters(filters, page, pageSize);
    }

    /** Count of reviews matching the filter. */
    public int getFilteredReviewCount(Filters filters) {
        return queryPort.getFilteredReviewCount(filters);
    }

    /**
     * Persist a batch of reviews via the write port.
     * If the service was constructed without a write port (read-only setup),
     * this method throws an IllegalStateException.
     */
    public void saveReviews(List<Review> reviews) {
        if (writePort == null) {
            throw new IllegalStateException("Write port not configured for ReviewService");
        }
        writePort.saveReviews(reviews);
    }
}
