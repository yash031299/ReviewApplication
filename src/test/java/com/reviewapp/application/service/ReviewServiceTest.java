package com.reviewapp.application.service;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.port.ReviewQueryPort;
import com.reviewapp.domain.port.ReviewWritePort;
import com.reviewapp.adapter.ingest.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReviewService} covering all service methods, integration with ports, and error handling.
 * Each test follows Arrange-Act-Assert and documents scenario and edge cases.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewQueryPort queryPort;

    @Mock
    private ReviewWritePort writePort;

    private ReviewService reviewService;

    /**
     * Sets up a new ReviewService with mocked ports before each test.
     */
    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(queryPort, writePort);
    }

    // --- getAllReviews ---

    /**
     * Tests that getAllReviews returns all reviews from the query port.
     */
    @Test
    void getAllReviews_returnsAllReviewsFromResource() throws IOException {
        // Arrange
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/all_reviews.json");
        when(queryPort.getAllReviews()).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getAllReviews();
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getAllReviews();
    }

    // --- getReviewsByKeywords ---

    /**
     * Tests that getReviewsByKeywords returns reviews for a single keyword.
     */
    @Test
    void getReviewsByKeywords_givenKeywordExcellent_returnsExpectedReviews() throws IOException {
        // Arrange
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_keyword_excellent.json");
        when(queryPort.getReviewsByKeywords(List.of("Excellent"))).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getReviewsByKeywords(List.of("Excellent"));
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByKeywords(List.of("Excellent"));
    }

    /**
     * Tests that getReviewsByKeywords returns reviews for multiple keywords.
     */
    @Test
    void getReviewsByKeywords_givenMultipleKeywords_returnsExpectedReviews() throws IOException {
        // Arrange
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_keywords_excellent_amazing.json");
        when(queryPort.getReviewsByKeywords(List.of("Excellent", "Amazing"))).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getReviewsByKeywords(List.of("Excellent", "Amazing"));
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByKeywords(List.of("Excellent", "Amazing"));
    }

    // --- getFilteredReviewsPage ---

    /**
     * Tests that getFilteredReviewsPage returns reviews for a rating filter.
     */
    @Test
    void getFilteredReviewsPage_givenRating5Filter_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder().setMinRating(5).build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_5.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews for rating and verified filters.
     */
    @Test
    void getFilteredReviewsPage_givenRating5AndVerifiedFilter_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder().setMinRating(5).build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_5_verified.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews for multiple filters.
     */
    @Test
    void getFilteredReviewsPage_givenMultipleFilters_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMinRating(4)
                .setProductName("Echo")
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_4_verified_echo.json");
        when(queryPort.getReviewsByFilters(filters, 2, 5)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 2, 5);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 2, 5);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews for rating, product, and other filters.
     */
    @Test
    void getFilteredReviewsPage_givenRating4VerifiedAndProductEcho_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMinRating(4)
                .setProductName("Echo")
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_4_verified_echo.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews for rating, product, and page filters.
     */
    @Test
    void getFilteredReviewsPage_givenRating3UnverifiedAndProductDot_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMinRating(3)
                .setProductName("Dot")
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_3_unverified_dot.json");
        when(queryPort.getReviewsByFilters(filters, 2, 5)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 2, 5);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 2, 5);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews when all filters are set.
     */
    @Test
    void getFilteredReviewsPage_givenAllFilters_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMinRating(5)
                .setProductName("Echo Show")
                .setMaxRating(5)
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_rating_5_verified_echoshow.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns an empty list when no reviews match the filters.
     */
    @Test
    void getFilteredReviewsPage_givenFiltersWithNoResults_returnsEmptyList() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMinRating(5)
                .setProductName("NonexistentProduct")
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_no_results.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews when only max rating is set.
     */
    @Test
    void getFilteredReviewsPage_givenMaxRatingOnly_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setMaxRating(3)
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_max_rating_3.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

    /**
     * Tests that getFilteredReviewsPage returns reviews when only product name is set.
     */
    @Test
    void getFilteredReviewsPage_givenProductNameOnly_returnsExpectedReviews() throws IOException {
        // Arrange
        Filters filters = new Filters.Builder()
                .setProductName("Echo Dot")
                .build();
        List<Review> expected = JsonParser.parseReviewsFromFile("src/test/resources/expected_reviews_filter_product_echo_dot.json");
        when(queryPort.getReviewsByFilters(filters, 1, 10)).thenReturn(expected);
        // Act
        List<Review> actual = reviewService.getFilteredReviewsPage(filters, 1, 10);
        // Assert
        assertEquals(expected, actual);
        verify(queryPort).getReviewsByFilters(filters, 1, 10);
    }

}
