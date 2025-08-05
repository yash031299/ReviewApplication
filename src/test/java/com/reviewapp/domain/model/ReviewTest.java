package com.reviewapp.domain.model;

import com.reviewapp.application.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Review} covering builder logic, validation, equals/hashCode, and toString.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewTest {

    @DisplayName("Tests that building a review with all fields sets all fields correctly")
    @Test
    void builder_buildWithAllFields_setsFieldsCorrectly() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewText("Great product!")
                .setAuthorName("Alice")
                .setReviewSource("Amazon")
                .setReviewTitle("Excellent")
                .setProductName("Echo Dot")
                .setReviewedDate(date)
                .setProductRating(5)
                .build();
        // Assert
        assertEquals(1L, review.getReviewId());
        assertEquals("Great product!", review.getReviewText());
        assertEquals("Alice", review.getAuthorName());
        assertEquals("Amazon", review.getReviewSource());
        assertEquals("Excellent", review.getReviewTitle());
        assertEquals("Echo Dot", review.getProductName());
        assertEquals(date, review.getReviewedDate());
        assertEquals(5, review.getProductRating());
    }


    @DisplayName("Tests that building a review with only required fields sets optionals to null")
    @Test
    void builder_buildWithOnlyRequiredFields_setsOptionalsToNull() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act
        Review review = new Review.Builder()
                .setReviewId(2L)
                .setReviewedDate(date)
                .setProductRating(3)
                .build();
        // Assert
        assertEquals(2L, review.getReviewId());
        assertNull(review.getReviewText());
        assertNull(review.getAuthorName());
        assertNull(review.getReviewSource());
        assertNull(review.getReviewTitle());
        assertNull(review.getProductName());
        assertEquals(date, review.getReviewedDate());
        assertEquals(3, review.getProductRating());
    }


    @DisplayName("Tests that missing reviewId throws InvalidInputException")
    @Test
    void builder_missingReviewId_throwsInvalidInputException() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Review.Builder()
                .setReviewedDate(date)
                .setProductRating(4)
                .build());
    }


    @DisplayName("Tests that missing reviewedDate throws InvalidInputException")
    @Test
    void builder_missingReviewedDate_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Review.Builder()
                .setReviewId(100L)
                .setProductRating(4)
                .build());
    }


    @DisplayName("Tests that missing productRating throws InvalidInputException")
    @Test
    void builder_missingProductRating_throwsInvalidInputException() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Review.Builder()
                .setReviewId(100L)
                .setReviewedDate(date)
                .build());
    }


    @DisplayName("Tests that productRating out of range throws InvalidInputException")
    @Test
    void builder_productRatingOutOfRange_throwsInvalidInputException() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Review.Builder()
                .setReviewId(100L)
                .setReviewedDate(date)
                .setProductRating(0)
                .build());
        assertThrows(InvalidInputException.class, () -> new Review.Builder()
                .setReviewId(100L)
                .setReviewedDate(date)
                .setProductRating(6)
                .build());
    }


    @DisplayName("Tests that reusing a builder after build throws InvalidInputException")
    @Test
    void builder_reuseAfterBuild_throwsInvalidInputException() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review.Builder builder = new Review.Builder()
                .setReviewId(100L)
                .setReviewedDate(date)
                .setProductRating(5);
        builder.build();
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> builder.setReviewId(200L));
        assertThrows(InvalidInputException.class, builder::build);
    }


    @DisplayName("Tests equals and hashCode for identical and different reviews")
    @Test
    void equals_andHashCode_workForIdenticalAndDifferentReviews() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review r1 = new Review.Builder().setReviewId(1L).setReviewedDate(date).setProductRating(4).build();
        Review r2 = new Review.Builder().setReviewId(1L).setReviewedDate(date).setProductRating(4).build();
        Review r3 = new Review.Builder().setReviewId(2L).setReviewedDate(date).setProductRating(4).build();
        // Act & Assert
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }


    @DisplayName("Tests toString contains all fields")
    @Test
    void toString_containsAllFields() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewText("Great product!")
                .setAuthorName("Alice")
                .setReviewSource("Amazon")
                .setReviewTitle("Excellent")
                .setProductName("Echo Dot")
                .setReviewedDate(date)
                .setProductRating(5)
                .build();
        // Act
        String str = review.toString();
        // Assert
        assertTrue(str.contains("reviewId=1"));
        assertTrue(str.contains("Great product!"));
        assertTrue(str.contains("Alice"));
        assertTrue(str.contains("Amazon"));
        assertTrue(str.contains("Excellent"));
        assertTrue(str.contains("Echo Dot"));
        assertTrue(str.contains("2024-08-04"));
        assertTrue(str.contains("productRating=5"));
    }


    @DisplayName("Tests that null and empty optional fields are accepted by the builder")
    @Test
    void builder_nullAndEmptyOptionals_areAccepted() {
        // Arrange
        LocalDate date = LocalDate.of(2024, 8, 4);
        // Act
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewText("")
                .setAuthorName(null)
                .setReviewSource("")
                .setReviewTitle(null)
                .setProductName("")
                .setReviewedDate(date)
                .setProductRating(1)
                .build();
        // Assert
        assertEquals("", review.getReviewText());
        assertNull(review.getAuthorName());
        assertEquals("", review.getReviewSource());
        assertNull(review.getReviewTitle());
        assertEquals("", review.getProductName());
    }
}
