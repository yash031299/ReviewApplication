package com.reviewapp.adapter.memory;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InMemoryReviewRepository.
 */
class InMemoryReviewRepositoryTest {

    private InMemoryReviewRepository repository;
    private List<Review> testReviews;


    @DisplayName("Sets up repository and test review data before each test.")
    @BeforeEach
    void setUp() {
        // Arrange
        repository = new InMemoryReviewRepository();
        testReviews = Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("Great product")
                        .setAuthorName("John Doe")
                        .setReviewSource("Amazon")
                        .setReviewTitle("Awesome")
                        .setProductName("Gadget")
                        .setReviewedDate(LocalDate.of(2023, 1, 1))
                        .setProductRating(5)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("Okay product")
                        .setAuthorName("Jane Smith")
                        .setReviewSource("Walmart")
                        .setReviewTitle("Decent")
                        .setProductName("Gadget")
                        .setReviewedDate(LocalDate.of(2023, 2, 1))
                        .setProductRating(3)
                        .build()
        );
        repository.saveReviews(testReviews);
    }


    @DisplayName("Verifies getAllReviews returns all reviews when repository is populated.")
    @Test
    void getAllReviews_returnsAllReviews_whenRepositoryPopulated() {
        // Act
        List<Review> reviews = repository.getAllReviews();

        // Assert
        assertEquals(2, reviews.size());
        assertTrue(reviews.stream().anyMatch(r -> r.getReviewId().equals(1L)));
        assertTrue(reviews.stream().anyMatch(r -> r.getReviewId().equals(2L)));
    }


    @DisplayName("Verifies getAllReviews returns an empty list when repository is empty.")
    @Test
    void getAllReviews_returnsEmptyList_whenRepositoryEmpty() {
        // Arrange
        repository = new InMemoryReviewRepository();

        // Act
        List<Review> reviews = repository.getAllReviews();

        // Assert
        assertTrue(reviews.isEmpty());
    }


    @DisplayName("Verifies getReviewById returns the review when the ID exists.")
    @Test
    void getReviewById_returnsReview_whenIdExists() {
        // Act
        Review review = repository.getReviewById(1L);

        // Assert
        assertNotNull(review);
        assertEquals("Great product", review.getReviewText());
        assertEquals(5, review.getProductRating());
    }


    @DisplayName("Verifies getReviewById returns null when the ID does not exist.")
    @Test
    void getReviewById_returnsNull_whenIdNotExists() {
        // Act
        Review review = repository.getReviewById(999L);

        // Assert
        assertNull(review);
    }


    @DisplayName("Verifies getReviewById returns null when the ID is null.")
    @Test
    void getReviewById_returnsNull_whenIdIsNull() {
        // Act
        Review review = repository.getReviewById(null);

        // Assert
        assertNull(review);
    }


    @DisplayName("Verifies getReviewsByKeywords returns reviews matching keywords in text or title.")
    @Test
    void getReviewsByKeywords_returnsReviews_whenKeywordsMatch() {
        // Arrange
        List<String> keywords = Arrays.asList("great", "product");

        // Act
        List<Review> reviews = repository.getReviewsByKeywords(keywords);

        // Assert
        assertTrue(reviews.size() >= 1 && reviews.size() <= 2);
        assertTrue(reviews.stream().anyMatch(r -> r.getReviewId().equals(1L)));
    }


    @DisplayName("Verifies getReviewsByKeywords returns an empty list when no keywords match.")
    @Test
    void getReviewsByKeywords_returnsEmptyList_whenNoKeywordsMatch() {
        // Arrange
        List<String> keywords = Arrays.asList("nonexistent");

        // Act
        List<Review> reviews = repository.getReviewsByKeywords(keywords);

        // Assert
        assertTrue(reviews.isEmpty());
    }


    @DisplayName("Verifies getReviewsByKeywords returns an empty list when keywords are null or empty.")
    @Test
    void getReviewsByKeywords_returnsEmptyList_whenNullOrEmptyKeywords() {
        // Act & Assert
        assertTrue(repository.getReviewsByKeywords(null).isEmpty());
        assertTrue(repository.getReviewsByKeywords(Collections.emptyList()).isEmpty());
    }


    @DisplayName("Verifies getReviewsByFilters returns filtered reviews when filters match.")
    @Test
    void getReviewsByFilters_returnsFilteredReviews_whenValidFilters() {
        // Arrange
        Filters filters = new Filters.Builder()
                .setRating(5)
                .setReviewDate(LocalDate.of(2023, 1, 1))
                .build();

        // Act
        List<Review> reviews = repository.getReviewsByFilters(filters, 1, 10);

        // Assert
        assertEquals(1, reviews.size());
        assertEquals(1L, reviews.get(0).getReviewId());
    }


    @DisplayName("Verifies getReviewsByFilters returns an empty list when filters do not match.")
    @Test
    void getReviewsByFilters_returnsEmptyList_whenFiltersDontMatch() {
        // Arrange
        Filters filters = new Filters.Builder().setRating(1).build();

        // Act
        List<Review> reviews = repository.getReviewsByFilters(filters, 1, 10);

        // Assert
        assertTrue(reviews.isEmpty());
    }


    @DisplayName("Verifies getReviewsByFilters returns all reviews when filters are null.")
    @Test
    void getReviewsByFilters_returnsAll_whenFiltersNull() {
        // Act
        List<Review> reviews = repository.getReviewsByFilters(null, 1, 10);

        // Assert
        assertEquals(2, reviews.size());
    }


    @DisplayName("Verifies getReviewsByFilters handles all filter fields and sorting logic.")
    @Test
    void getReviewsByFilters_handlesAllFieldsAndSorting() {
        // Arrange
        Review r3 = new Review.Builder()
                .setReviewId(3L)
                .setReviewText("Average product")
                .setAuthorName("Jane Smith")
                .setReviewSource("Walmart")
                .setReviewTitle("Average")
                .setProductName("Gadget")
                .setReviewedDate(LocalDate.of(2023, 3, 1))
                .setProductRating(4)
                .build();
        repository.saveReviews(List.of(r3));
        Filters filters = new Filters.Builder()
                .setAuthorName("Jane")
                .setProductName("Gadget")
                .setStoreName("Walmart")
                .setMinRating(3)
                .setMaxRating(4)
                .setStartDate(LocalDate.of(2023, 2, 1))
                .setEndDate(LocalDate.of(2023, 3, 1))
                .setSortByRating(true)
                .setSortByDate(true)
                .build();

        // Act
        List<Review> reviews = repository.getReviewsByFilters(filters, 1, 10);

        // Assert
        assertEquals(2, reviews.size());
        assertTrue(reviews.get(0).getProductRating() >= reviews.get(1).getProductRating());
    }


    @DisplayName("Verifies getReviewsByFilters returns an empty list when the page is out of bounds.")
    @Test
    void getReviewsByFilters_returnsEmptyList_whenPageOutOfBounds() {
        // Arrange
        Filters filters = new Filters.Builder().build();

        // Act
        List<Review> reviews = repository.getReviewsByFilters(filters, 100, 10);

        // Assert
        assertTrue(reviews.isEmpty());
    }


    @DisplayName("Verifies getReviewsByFilters returns an empty list when page size is zero or negative.")
    @Test
    void getReviewsByFilters_returnsEmptyList_whenPageSizeZeroOrNegative() {
        // Arrange
        Filters filters = new Filters.Builder().build();

        // Act & Assert
        assertTrue(repository.getReviewsByFilters(filters, 1, 0).isEmpty());
        assertTrue(repository.getReviewsByFilters(filters, 1, -1).isEmpty());
    }


    @DisplayName("Verifies getFilteredReviewCount returns the correct count when filters match.")
    @Test
    void getFilteredReviewCount_returnsCorrectCount_whenFiltersMatch() {
        // Arrange
        Filters filters = new Filters.Builder().setProductName("Gadget").build();

        // Act
        int count = repository.getFilteredReviewCount(filters);

        // Assert
        assertEquals(2, count);
    }


    @DisplayName("Verifies getFilteredReviewCount returns zero when there is no match.")
    @Test
    void getFilteredReviewCount_returnsZero_whenNoMatch() {
        // Arrange
        Filters filters = new Filters.Builder().setProductName("Widget").build();

        // Act
        int count = repository.getFilteredReviewCount(filters);

        // Assert
        assertEquals(0, count);
    }


    @DisplayName("Verifies getFilteredReviewCount returns all reviews when filters are null.")
    @Test
    void getFilteredReviewCount_returnsAll_whenFiltersNull() {
        // Act
        int count = repository.getFilteredReviewCount(null);

        // Assert
        assertEquals(2, count);
    }


    @DisplayName("Verifies getReviewsPage returns paged results for given page and size.")
    @Test
    void getReviewsPage_returnsPagedResults_whenPageAndSizeGiven() {
        // Arrange
        Review review3 = new Review.Builder()
                .setReviewId(3L)
                .setReviewText("Another review")
                .setAuthorName("Sam")
                .setReviewSource("eBay")
                .setReviewTitle("Solid")
                .setProductName("Widget")
                .setReviewedDate(LocalDate.of(2023, 3, 1))
                .setProductRating(4)
                .build();
        repository.saveReviews(List.of(review3));

        // Act
        List<Review> page2 = repository.getReviewsPage(2, 1);

        // Assert
        assertEquals(1, page2.size());
        assertEquals(2L, page2.get(0).getReviewId());
    }


    @DisplayName("Verifies getReviewsPage returns an empty list when there is no data.")
    @Test
    void getReviewsPage_returnsEmptyList_whenNoData() {
        // Arrange
        repository = new InMemoryReviewRepository();

        // Act
        List<Review> page = repository.getReviewsPage(1, 10);

        // Assert
        assertTrue(page.isEmpty());
    }


    @DisplayName("Verifies saveReviews saves a review when data is valid.")
    @Test
    void saveReviews_savesReview_whenValidData() {
        // Arrange
        Review newReview = new Review.Builder()
                .setReviewId(3L)
                .setReviewText("New review")
                .setAuthorName("Alice")
                .setReviewSource("BestBuy")
                .setReviewTitle("Good")
                .setProductName("Widget")
                .setReviewedDate(LocalDate.of(2023, 3, 1))
                .setProductRating(4)
                .build();

        // Act
        repository.saveReviews(List.of(newReview));

        // Assert
        Review saved = repository.getReviewById(3L);
        assertNotNull(saved);
        assertEquals("New review", saved.getReviewText());
        assertEquals("Alice", saved.getAuthorName());
        assertEquals(4, saved.getProductRating());
    }

}
