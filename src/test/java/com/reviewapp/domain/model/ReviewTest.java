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

    @DisplayName("Tests equals with null and different type, and all fields different")
    @Test
    void equals_withNullAndDifferentType_andAllFieldsDifferent() {
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review r1 = new Review.Builder().setReviewId(1L).setReviewedDate(date).setProductRating(4).build();
        assertNotEquals(r1, null);
        assertNotEquals(r1, "not a review");
        Review r2 = new Review.Builder().setReviewId(2L).setReviewedDate(date.plusDays(1)).setProductRating(5)
            .setReviewText("t").setAuthorName("a").setReviewSource("s").setReviewTitle("tt").setProductName("p").build();
        assertNotEquals(r1, r2);
    }

    @DisplayName("Tests toString with null/empty fields")
    @Test
    void toString_withNullAndEmptyFields() {
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review review = new Review.Builder()
            .setReviewId(1L)
            .setReviewedDate(date)
            .setProductRating(2)
            .build();
        String str = review.toString();
        assertTrue(str.contains("reviewId=1"));
        assertTrue(str.contains("reviewText='null'"));
        assertTrue(str.contains("authorName='null'"));
        assertTrue(str.contains("reviewSource='null'"));
        assertTrue(str.contains("reviewTitle='null'"));
        assertTrue(str.contains("productName='null'"));
    }

    @DisplayName("Tests builder allows overwriting fields (last value wins)")
    @Test
    void builder_overwriteFields() {
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review review = new Review.Builder()
            .setReviewId(1L).setReviewId(2L)
            .setProductRating(1).setProductRating(5)
            .setReviewedDate(date).setReviewedDate(date.plusDays(1))
            .setReviewText("A").setReviewText(null)
            .build();
        assertEquals(2L, review.getReviewId());
        assertEquals(5, review.getProductRating());
        assertEquals(date.plusDays(1), review.getReviewedDate());
        assertNull(review.getReviewText());
    }

    @DisplayName("Tests only one optional set at a time")
    @Test
    void builder_onlyOneOptionalAtATime() {
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review t = new Review.Builder().setReviewId(1L).setReviewedDate(date).setProductRating(1).setReviewText("T").build();
        assertEquals("T", t.getReviewText());
        Review a = new Review.Builder().setReviewId(2L).setReviewedDate(date).setProductRating(2).setAuthorName("A").build();
        assertEquals("A", a.getAuthorName());
        Review s = new Review.Builder().setReviewId(3L).setReviewedDate(date).setProductRating(3).setReviewSource("S").build();
        assertEquals("S", s.getReviewSource());
        Review tt = new Review.Builder().setReviewId(4L).setReviewedDate(date).setProductRating(4).setReviewTitle("TT").build();
        assertEquals("TT", tt.getReviewTitle());
        Review p = new Review.Builder().setReviewId(5L).setReviewedDate(date).setProductRating(5).setProductName("P").build();
        assertEquals("P", p.getProductName());
    }

    @DisplayName("Tests hashCode with all fields null except required")
    @Test
    void hashCode_withNullFieldsExceptRequired() {
        LocalDate date = LocalDate.of(2024, 8, 4);
        Review review = new Review.Builder().setReviewId(1L).setReviewedDate(date).setProductRating(1).build();
        assertDoesNotThrow(review::hashCode);
    }

    // Add to ReviewTest.java

    private Review.Builder baseBuilder() {
        return new Review.Builder()
                .setReviewId(1L)
                .setReviewText("Great product!")
                .setAuthorName("Alice")
                .setReviewSource("Amazon")
                .setReviewTitle("Excellent")
                .setProductName("Echo Dot")
                .setReviewedDate(LocalDate.of(2024, 8, 4))
                .setProductRating(5);
    }

    @DisplayName("Tests equals returns false when differing only in each individual field")
    @Test
    void equals_differOnlyInEachField() {
        Review base = baseBuilder().build();

        // Differ in reviewId
        Review varReviewId = baseBuilder().setReviewId(2L).build();
        assertNotEquals(base, varReviewId);

        // Differ in reviewText
        Review varReviewText = baseBuilder().setReviewText("Different text").build();
        assertNotEquals(base, varReviewText);

        // Differ in authorName
        Review varAuthorName = baseBuilder().setAuthorName("Bob").build();
        assertNotEquals(base, varAuthorName);

        // Differ in reviewSource
        Review varReviewSource = baseBuilder().setReviewSource("eBay").build();
        assertNotEquals(base, varReviewSource);

        // Differ in reviewTitle
        Review varReviewTitle = baseBuilder().setReviewTitle("Good").build();
        assertNotEquals(base, varReviewTitle);

        // Differ in productName
        Review varProductName = baseBuilder().setProductName("Fire TV").build();
        assertNotEquals(base, varProductName);

        // Differ in reviewedDate
        Review varReviewedDate = baseBuilder().setReviewedDate(LocalDate.of(2024, 8, 5)).build();
        assertNotEquals(base, varReviewedDate);

        // Differ in productRating
        Review varProductRating = baseBuilder().setProductRating(4).build();
        assertNotEquals(base, varProductRating);
    }

    @DisplayName("Tests all setters throw InvalidInputException after build")
    @Test
    void builder_allSettersAfterBuild_throwInvalidInputException() {
        // Arrange
        Review.Builder builder = baseBuilder();
        builder.build(); // Mark as built
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> builder.setReviewId(2L));
        assertThrows(InvalidInputException.class, () -> builder.setReviewText("Text"));
        assertThrows(InvalidInputException.class, () -> builder.setAuthorName("Bob"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewSource("eBay"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewTitle("Title"));
        assertThrows(InvalidInputException.class, () -> builder.setProductName("Product"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewedDate(LocalDate.of(2024, 8, 5)));
        assertThrows(InvalidInputException.class, () -> builder.setProductRating(4));
    }

    @DisplayName("Tests rating validation short-circuit for less than 1")
    @Test
    void builder_ratingValidationShortCircuit_lessThanOne() {
        // Arrange
        Review.Builder builder = baseBuilder().setProductRating(0);
        // Act & Assert
        assertThrows(InvalidInputException.class, builder::build); // Tests productRating < 1 true, short-circuits > 5
    }
}
