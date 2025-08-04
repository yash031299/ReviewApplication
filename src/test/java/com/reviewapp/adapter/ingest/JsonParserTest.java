package com.reviewapp.adapter.ingest;

import com.reviewapp.domain.model.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private ByteArrayOutputStream errStream;
    private PrintStream originalErr;

    @TempDir
    Path tempDir;

    /**
     * Sets up the test fixture before each test.
     * Redirects System.err to capture warnings for assertions.
     */
    @BeforeEach
    void setUp() {
        // Redirect System.err to capture warnings
        errStream = new ByteArrayOutputStream();
        originalErr = System.err;
        System.setErr(new PrintStream(errStream));
    }

    /**
     * Restores System.err after each test to avoid side effects.
     */
    @AfterEach
    void restoreSystemErr() {
        System.setErr(originalErr);
    }

    /**
     * Verifies that a valid JSON file with multiple reviews is parsed correctly.
     */
    @Test
    void parseReviewsFromFile_validJsonFile_parsesReviews() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("reviews.json");
        String json = "{\"id\":1,\"review\":\"Great product\",\"author\":\"John\",\"review_source\":\"Amazon\",\"title\":\"Awesome\",\"product_name\":\"Gadget\",\"reviewed_date\":\"2023-01-01\",\"rating\":5}\n" +
                "{\"id\":2,\"review\":\"Okay\",\"author\":\"Jane\",\"review_source\":\"Walmart\",\"title\":\"Decent\",\"product_name\":\"Gadget\",\"reviewed_date\":\"2023-02-01\",\"rating\":3}";
        Files.writeString(tempFile, json);

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertEquals(2, reviews.size());
        Review review1 = reviews.get(0);
        assertEquals(1L, review1.getReviewId());
        assertEquals("Great product", review1.getReviewText());
        assertEquals("John", review1.getAuthorName());
        assertEquals("Amazon", review1.getReviewSource());
        assertEquals("Awesome", review1.getReviewTitle());
        assertEquals("Gadget", review1.getProductName());
        assertEquals(LocalDate.of(2023, 1, 1), review1.getReviewedDate());
        assertEquals(5, review1.getProductRating());

        Review review2 = reviews.get(1);
        assertEquals(2L, review2.getReviewId());
        assertEquals("Okay", review2.getReviewText());
        assertEquals(3, review2.getProductRating());
    }

    /**
     * Verifies that invalid lines (malformed JSON) are skipped and a warning is printed.
     */
    @Test
    void parseReviewsFromFile_invalidLines_skipsInvalidLines() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("reviews.json");
        String json = "{\"id\":1,\"review\":\"Great product\",\"author\":\"John\",\"review_source\":\"Amazon\",\"title\":\"Awesome\",\"product_name\":\"Gadget\",\"reviewed_date\":\"2023-01-01\",\"rating\":5}\n" +
                "invalid_json\n" +
                "{\"id\":2,\"review\":\"Okay\",\"rating\":3,\"reviewed_date\":\"2023-02-01\"}";
        Files.writeString(tempFile, json);

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertEquals(2, reviews.size());
        assertEquals(1L, reviews.get(0).getReviewId());
        assertEquals(2L, reviews.get(1).getReviewId());
        String errOutput = errStream.toString();
        assertTrue(errOutput.contains("Skipping bad JSON at line 2"));
    }

    /**
     * Verifies that an empty JSON file returns an empty list.
     */
    @Test
    void parseReviewsFromFile_emptyJsonFile_returnsEmptyList() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("empty.json");
        Files.writeString(tempFile, "");

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertTrue(reviews.isEmpty());
    }

    /**
     * Verifies that a missing file throws a RuntimeException.
     */
    @Test
    void parseReviewsFromFile_fileNotFound_throwsRuntimeException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> JsonParser.parseReviewsFromFile("nonexistent.json"),
                "Expected RuntimeException for non-existent file");
    }

    /**
     * Verifies that missing optional fields in JSON result in null fields in Review.
     */
    @Test
    void parseReviewsFromFile_missingOptionalFields_parsesReviewWithNulls() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("reviews.json");
        String json = "{\"id\":1,\"rating\":5,\"reviewed_date\":\"2023-01-01\"}";
        Files.writeString(tempFile, json);

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertEquals(1, reviews.size());
        Review review = reviews.get(0);
        assertEquals(1L, review.getReviewId());
        assertNull(review.getReviewText());
        assertNull(review.getAuthorName());
        assertNull(review.getReviewSource());
        assertNull(review.getReviewTitle());
        assertNull(review.getProductName());
        assertEquals(LocalDate.of(2023, 1, 1), review.getReviewedDate());
        assertEquals(5, review.getProductRating());
    }

    /**
     * Verifies that a review with an invalid rating is skipped and an error is printed.
     */
    @Test
    void parseReviewsFromFile_invalidRating_printsErrorAndSkipsReview() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("reviews.json");
        String json = "{\"id\":1,\"rating\":6,\"reviewed_date\":\"2023-01-01\"}";
        Files.writeString(tempFile, json);

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertTrue(reviews.isEmpty());
        String errOutput = errStream.toString();
        assertTrue(errOutput.contains("Invalid rating; must be between 1 and 5"));
    }

    /**
     * Verifies that a missing ID in JSON results in a generated positive ID.
     */
    @Test
    void parseReviewsFromFile_missingId_generatesRandomId() throws IOException {
        // Arrange
        Path tempFile = tempDir.resolve("reviews.json");
        String json = "{\"rating\":5,\"reviewed_date\":\"2023-01-01\"}";
        Files.writeString(tempFile, json);

        // Act
        List<Review> reviews = JsonParser.parseReviewsFromFile(tempFile.toString());

        // Assert
        assertEquals(1, reviews.size());
        Review review = reviews.get(0);
        assertTrue(review.getReviewId() > 0, "Generated ID should be positive");
        assertEquals(5, review.getProductRating());
        assertEquals(LocalDate.of(2023, 1, 1), review.getReviewedDate());
    }

    /**
     * Verifies that a realistic mock Alexa JSON file is parsed with all reviews loaded.
     */
    @Test
    void parseReviewsFromFile_mockAlexaJsonFile_parsesAllReviews() throws IOException {
        // Arrange
        String filePath = "src/test/resources/mock_alexa.json";
        List<Review> reviews = JsonParser.parseReviewsFromFile(filePath);

        // Assert
        assertEquals(10, reviews.size(), "Should parse 10 reviews from mock_alexa.json");
        Review firstReview = reviews.get(0);
        assertEquals(1L, firstReview.getReviewId());
        assertEquals("Excellent product!", firstReview.getReviewText());
        assertEquals("Alice", firstReview.getAuthorName());
        assertEquals("Amazon", firstReview.getReviewSource());
        assertEquals("Great buy", firstReview.getReviewTitle());
        assertEquals("Echo Dot", firstReview.getProductName());
    }
}