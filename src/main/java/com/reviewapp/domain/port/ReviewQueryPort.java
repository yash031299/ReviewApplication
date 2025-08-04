package com.reviewapp.domain.port;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import java.util.List;

/**
 * Port interface for querying reviews from the data store.
 */
public interface ReviewQueryPort {
    /**
     * Retrieves a paginated list of reviews.
     * @param page the page number (0-based)
     * @param pageSize the number of reviews per page
     * @return a list of reviews for the specified page
     */
    List<Review> getReviewsPage(int page, int pageSize);

    /**
     * Returns the total number of reviews in the data store.
     * @return the total review count
     */
    int getTotalReviewCount();

    /**
     * Fetches a review by its unique identifier.
     * @param id the review ID
     * @return the corresponding Review, or null if not found
     */
    Review getReviewById(Long id);

    /**
     * Retrieves a paginated list of reviews matching the provided filters.
     * @param filters the filter criteria
     * @param page the page number (0-based)
     * @param pageSize the number of reviews per page
     * @return a list of filtered reviews for the specified page
     */
    List<Review> getReviewsByFilters(Filters filters, int page, int pageSize);

    /**
     * Returns the total number of reviews matching the provided filters.
     * @param filters the filter criteria
     * @return the count of filtered reviews
     */
    int getFilteredReviewCount(Filters filters);

    /**
     * Retrieves reviews that match any of the provided keywords.
     * @param keywords the list of keywords to search for
     * @return a list of reviews containing the keywords
     */
    List<Review> getReviewsByKeywords(List<String> keywords);

    /**
     * Retrieves all reviews from the data store.
     * @deprecated Use {@link #getReviewsPage(int, int)} instead. Only kept for backwards command_line_interface compatibility.
     * @return a list of all reviews
     */
    @Deprecated
    public List<Review> getAllReviews();
}
