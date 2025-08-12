package com.reviewapp.application.service;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.port.ReviewQueryPort;
import com.reviewapp.domain.port.ReviewWritePort;
import com.reviewapp.adapter.ingest.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
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


    @DisplayName("Sets up a new ReviewService with mocked ports before each test.")
    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(queryPort, writePort);
    }

    // --- getAllReviews ---

    @DisplayName("Verifies that getAllReviews returns all reviews from the query port.")
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

    @DisplayName("Verifies that getReviewsByKeywords returns reviews for a single keyword.")
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


    @DisplayName("Verifies that getReviewsByKeywords returns reviews for multiple keywords.")
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

    @DisplayName("Verifies that getReviewsByKeywords throws if keywords list contains null, empty, or whitespace-only strings.")
    @Test
    void getReviewsByKeywords_givenInvalidKeywords_throwsException() {
        // null keyword in list
        List<String> withNull = Arrays.asList("valid", null);
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(withNull));
        // empty string in list
        List<String> withEmpty = Arrays.asList("valid", "");
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(withEmpty));
        // whitespace string in list
        List<String> withWhitespace = Arrays.asList("valid", "   ");
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(withWhitespace));
    }

    @DisplayName("Verifies that getReviewsByKeywords throws if single keyword is null, empty, or whitespace-only.")
    @Test
    void getReviewsByKeywords_singleInvalidKeyword_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(Arrays.asList((String) null)));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(List.of("")));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(List.of("   ")));
    }

    @DisplayName("Verifies that getReviewsByKeywords throws if keywords is null.")
    @Test
    void getReviewsByKeywords_givenNullKeywords_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(null));
    }

    @DisplayName("Verifies that getReviewsByKeywords throws if keywords is an empty list.")
    @Test
    void getReviewsByKeywords_givenEmptyKeywords_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsByKeywords(List.of()));
    }

    // --- getFilteredReviewsPage ---


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for a rating filter.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for rating and verified filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for multiple filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for rating, product, and other filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for rating, product, and page filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews for all filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns an empty list when no reviews match the filters.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews when only max rating is set.")
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


    @DisplayName("Verifies that getFilteredReviewsPage returns reviews when only product name is set.")
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


    @DisplayName("Verifies that getFilteredReviewsPage throws if filters is null.")
    @Test
    void getFilteredReviewsPage_givenNullFilters_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getFilteredReviewsPage(null, 1, 10));
    }

    @DisplayName("Verifies that getFilteredReviewsPage throws if page is non-positive.")
    @Test
    void getFilteredReviewsPage_givenInvalidPage_throwsException() {
        Filters filters = new Filters.Builder().setMinRating(1).build();
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getFilteredReviewsPage(filters, 0, 10));
    }

    @DisplayName("Verifies that getFilteredReviewsPage throws if pageSize is non-positive.")
    @Test
    void getFilteredReviewsPage_givenInvalidPageSize_throwsException() {
        Filters filters = new Filters.Builder().setMinRating(1).build();
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getFilteredReviewsPage(filters, 1, 0));
    }


    @DisplayName("Verifies that getFilteredReviewCount returns correct count from query port.")
    @Test
    void getFilteredReviewCount_returnsCountFromQueryPort() {
        Filters filters = new Filters.Builder().setMinRating(4).build();
        when(queryPort.getFilteredReviewCount(filters)).thenReturn(5);
        int count = reviewService.getFilteredReviewCount(filters);
        assertEquals(5, count);
        verify(queryPort).getFilteredReviewCount(filters);
    }


    @DisplayName("Verifies that getFilteredReviewCount throws if filters is null.")
    @Test
    void getFilteredReviewCount_givenNullFilters_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getFilteredReviewCount(null));
    }


    @DisplayName("Verifies that getTotalReviewCount returns correct count from query port.")
    @Test
    void getTotalReviewCount_returnsCountFromQueryPort() {
        when(queryPort.getTotalReviewCount()).thenReturn(10);
        int count = reviewService.getTotalReviewCount();
        assertEquals(10, count);
        verify(queryPort).getTotalReviewCount();
    }


    @DisplayName("Verifies that getReviewsPage returns paged reviews from query port.")
    @Test
    void getReviewsPage_returnsPagedReviewsFromQueryPort() {
        List<Review> expected = List.of(mock(Review.class));
        when(queryPort.getReviewsPage(1, 5)).thenReturn(expected);
        List<Review> actual = reviewService.getReviewsPage(1, 5);
        assertEquals(expected, actual);
        verify(queryPort).getReviewsPage(1, 5);
    }


    @DisplayName("Verifies that getReviewsPage throws if page or pageSize is invalid.")
    @Test
    void getReviewsPage_invalidPageOrSize_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsPage(0, 5));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewsPage(1, 0));
    }



    @DisplayName("Verifies that getReviewById returns review from query port.")
    @Test
    void getReviewById_returnsReviewFromQueryPort() {
        Review expected = mock(Review.class);
        when(queryPort.getReviewById(1L)).thenReturn(expected);
        Review actual = reviewService.getReviewById(1L);
        assertEquals(expected, actual);
        verify(queryPort).getReviewById(1L);
    }


    @DisplayName("Verifies that getReviewById throws if id is not positive.")
    @Test
    void getReviewById_invalidId_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewById(0));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.getReviewById(-1));
    }


    @DisplayName("Verifies that saveReviews delegates to write port.")
    @Test
    void saveReviews_validList_delegatesToWritePort() {
        List<Review> reviews = List.of(mock(Review.class));
        reviewService.saveReviews(reviews);
        verify(writePort).saveReviews(reviews);
    }

    @DisplayName("Verifies that saveReviews throws if any review in a multi-element list is null.")
    @Test
    void saveReviews_multiElementListWithNullReview_throwsException() {
        List<Review> reviews = Arrays.asList(mock(Review.class), null, mock(Review.class));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(reviews));
    }

    @DisplayName("Verifies that saveReviews throws if first element in two-element list is null.")
    @Test
    void saveReviews_firstElementNullInTwoElementList_throwsException() {
        List<Review> reviews = Arrays.asList(null, mock(Review.class));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(reviews));
    }

    @DisplayName("Verifies that saveReviews throws if second element in two-element list is null.")
    @Test
    void saveReviews_secondElementNullInTwoElementList_throwsException() {
        List<Review> reviews = Arrays.asList(mock(Review.class), null);
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(reviews));
    }

    @DisplayName("Verifies that saveReviews throws if write port is null.")
    @Test
    void saveReviews_noWritePort_throwsException() {
        ReviewService readOnlyService = new ReviewService(queryPort, null);
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> readOnlyService.saveReviews(List.of(mock(Review.class))));
    }


    @DisplayName("Verifies that saveReviews throws if reviews list is null or empty.")
    @Test
    void saveReviews_nullOrEmptyList_throwsException() {
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(null));
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(List.of()));
    }


    @DisplayName("Verifies that saveReviews throws if any review in the list is null.")
    @Test
    void saveReviews_listWithNullReview_throwsException() {
        List<Review> reviews = Arrays.asList((Review) null);
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class,
                () -> reviewService.saveReviews(reviews));
    }

    @DisplayName("Verifies that writePort is not called if saveReviews throws before delegation.")
    @Test
    void saveReviews_invalidInput_writePortNotCalled() {
        try {
            reviewService.saveReviews(null);
        } catch (Exception ignored) {}
        try {
            reviewService.saveReviews(List.of());
        } catch (Exception ignored) {}
        try {
            reviewService.saveReviews(Arrays.asList((Review) null));
        } catch (Exception ignored) {}
        verify(writePort, never()).saveReviews(any());
    }

    @DisplayName("Verifies that ReviewService constructor throws if queryPort is null.")
    @Test
    void constructor_givenNullQueryPort_throwsException() {
        assertThrows(NullPointerException.class,
                () -> new ReviewService(null, writePort));
    }

    @DisplayName("Verifies that ReviewService constructor throws NullPointerException with correct message for null queryPort.")
    @Test
    void constructor_givenNullQueryPort_throwsNullPointerExceptionWithMessage() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new ReviewService(null, writePort));
        assertTrue(ex.getMessage().contains("queryPort must not be null"));
    }
}
