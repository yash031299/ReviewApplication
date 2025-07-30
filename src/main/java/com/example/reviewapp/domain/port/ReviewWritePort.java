package com.example.reviewapp.domain.port;

import com.example.reviewapp.domain.model.Review;
import java.util.List;

/**
 * Port interface for writing reviews to the data store.
 */
public interface ReviewWritePort {
    /**
     * Saves a list of reviews to the data store.
     * @param reviews the list of reviews to save
     */
    void saveReviews(List<Review> reviews);
}
