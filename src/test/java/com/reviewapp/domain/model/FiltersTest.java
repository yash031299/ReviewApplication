package com.reviewapp.domain.model;

import com.reviewapp.application.exception.InvalidInputException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Filters} covering builder logic, validation, and edge cases.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class FiltersTest {

    @DisplayName("Tests that building with all fields sets all fields correctly")
    @Test
    void builder_buildWithAllFields_setsFieldsCorrectly() {
        // Arrange
        // Act
        Filters filters = new Filters.Builder()
                .setMinRating(3)
                .setAuthorName("John")
                .setProductName("Echo Dot")
                .setStartDate(LocalDate.of(2023, 1, 1))
                .setEndDate(LocalDate.of(2023, 12, 31))
                .setSortByRating(true)
                .build();
        // Assert
        assertEquals(3, filters.getMinRating());
        assertEquals("John", filters.getAuthorName());
        assertEquals("Echo Dot", filters.getProductName());
        assertEquals(LocalDate.of(2023, 1, 1), filters.getStartDate());
        assertEquals(LocalDate.of(2023, 12, 31), filters.getEndDate());
        assertTrue(filters.isSortByRating());
    }


    @DisplayName("Tests that building with no fields sets defaults and nulls")
    @Test
    void builder_buildWithNoFields_setsDefaultsAndNulls() {
        // Arrange
        // Act
        Filters filters = new Filters.Builder().build();
        // Assert
        assertNull(filters.getMinRating());
        assertNull(filters.getAuthorName());
        assertNull(filters.getProductName());
        assertNull(filters.getStartDate());
        assertNull(filters.getEndDate());
        assertFalse(filters.isSortByRating());
    }


    @DisplayName("Tests that different values in builder result in distinct filter objects")
    @Test
    void builder_buildWithDifferentValues_resultsInDistinctObjects() {
        // Arrange
        // Act
        Filters filters1 = new Filters.Builder().setAuthorName("A").build();
        Filters filters2 = new Filters.Builder().setAuthorName("B").build();
        // Assert
        assertNotEquals(filters1, filters2);
    }


    @DisplayName("Tests that invalid ratings in builder throw InvalidInputException.")
    @Test
    void builder_setInvalidRatings_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(0).build());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(6).build());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setMinRating(-1).build());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setMaxRating(10).build());
    }


    @DisplayName("Tests that minRating greater than maxRating throws InvalidInputException.")
    @Test
    void builder_setMinGreaterThanMax_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setMinRating(4).setMaxRating(2).build());
    }


    @DisplayName("Tests that startDate after endDate throws InvalidInputException.")
    @Test
    void builder_setStartDateAfterEndDate_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Filters.Builder()
                .setStartDate(LocalDate.of(2023, 12, 31))
                .setEndDate(LocalDate.of(2023, 1, 1))
                .build());
    }


    @DisplayName("Tests that startTime after endTime throws InvalidInputException.")
    @Test
    void builder_setStartTimeAfterEndTime_throwsInvalidInputException() {
        // Arrange
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> new Filters.Builder()
                .setStartTime(LocalTime.of(15, 0))
                .setEndTime(LocalTime.of(14, 0))
                .build());
    }

    @DisplayName("Tests all builder fields and their getters")
    @Test
    void builder_allFieldsAndGetters() {
        Filters filters = new Filters.Builder()
                .setRating(4)
                .setMinRating(2)
                .setMaxRating(5)
                .setAuthorName("Alice")
                .setReviewTitle("Great!")
                .setProductName("Echo Show")
                .setReviewDate(LocalDate.of(2024, 2, 2))
                .setStoreName("BestBuy")
                .setStartDate(LocalDate.of(2024, 1, 1))
                .setEndDate(LocalDate.of(2024, 12, 31))
                .setStartTime(LocalTime.of(9, 0))
                .setEndTime(LocalTime.of(18, 0))
                .setSortByDate(true)
                .setSortByRating(false)
                .build();
        assertEquals(4, filters.getRating());
        assertEquals(2, filters.getMinRating());
        assertEquals(5, filters.getMaxRating());
        assertEquals("Alice", filters.getAuthorName());
        assertEquals("Great!", filters.getReviewTitle());
        assertEquals("Echo Show", filters.getProductName());
        assertEquals(LocalDate.of(2024, 2, 2), filters.getReviewDate());
        assertEquals("BestBuy", filters.getStoreName());
        assertEquals(LocalDate.of(2024, 1, 1), filters.getStartDate());
        assertEquals(LocalDate.of(2024, 12, 31), filters.getEndDate());
        assertEquals(LocalTime.of(9, 0), filters.getStartTime());
        assertEquals(LocalTime.of(18, 0), filters.getEndTime());
        assertTrue(filters.isSortByDate());
        assertFalse(filters.isSortByRating());
    }

    @DisplayName("Tests builder double build throws InvalidInputException")
    @Test
    void builder_doubleBuild_throwsInvalidInputException() {
        Filters.Builder builder = new Filters.Builder();
        builder.build();
        assertThrows(InvalidInputException.class, builder::build);
    }

    @DisplayName("Tests equals, hashCode, and toString edge cases")
    @Test
    void equals_hashCode_toString_edgeCases() {
        Filters f1 = new Filters.Builder().setAuthorName("A").build();
        Filters f2 = new Filters.Builder().setAuthorName("A").build();
        Filters f3 = new Filters.Builder().setAuthorName("B").build();
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
        assertNotEquals(f1, f3);
        assertNotEquals(f1, null);
        assertNotEquals(f1, "not a filter");
        assertTrue(f1.toString().contains("authorName='A'"));
    }

    @DisplayName("Tests that blank string fields are accepted and distinguishable from null")
    @Test
    void builder_blankStrings_vs_null() {
        Filters blank = new Filters.Builder().setAuthorName("").setReviewTitle("").setProductName("").setStoreName("").build();
        Filters allNull = new Filters.Builder().build();
        assertNotEquals(blank, allNull);
        assertEquals("", blank.getAuthorName());
        assertNull(allNull.getAuthorName());
    }

    @DisplayName("Tests only minRating set, in and out of range")
    @Test
    void builder_onlyMinRating() {
        Filters filters = new Filters.Builder().setMinRating(1).build();
        assertEquals(1, filters.getMinRating());
        assertNull(filters.getMaxRating());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setMinRating(0).build());
    }

    @DisplayName("Tests only maxRating set, in and out of range")
    @Test
    void builder_onlyMaxRating() {
        Filters filters = new Filters.Builder().setMaxRating(5).build();
        assertEquals(5, filters.getMaxRating());
        assertNull(filters.getMinRating());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setMaxRating(6).build());
    }

    @DisplayName("Tests minRating equals maxRating is valid")
    @Test
    void builder_minEqualsMaxRating() {
        Filters filters = new Filters.Builder().setMinRating(3).setMaxRating(3).build();
        assertEquals(3, filters.getMinRating());
        assertEquals(3, filters.getMaxRating());
    }

    @DisplayName("Tests only startDate or only endDate set")
    @Test
    void builder_onlyStartOrEndDate() {
        Filters s = new Filters.Builder().setStartDate(LocalDate.of(2024,1,1)).build();
        assertEquals(LocalDate.of(2024,1,1), s.getStartDate());
        assertNull(s.getEndDate());
        Filters e = new Filters.Builder().setEndDate(LocalDate.of(2024,12,31)).build();
        assertEquals(LocalDate.of(2024,12,31), e.getEndDate());
        assertNull(e.getStartDate());
    }

    @DisplayName("Tests startDate equals endDate is valid")
    @Test
    void builder_startEqualsEndDate() {
        Filters filters = new Filters.Builder().setStartDate(LocalDate.of(2024,5,5)).setEndDate(LocalDate.of(2024,5,5)).build();
        assertEquals(LocalDate.of(2024,5,5), filters.getStartDate());
        assertEquals(LocalDate.of(2024,5,5), filters.getEndDate());
    }

    @DisplayName("Tests only startTime or only endTime set")
    @Test
    void builder_onlyStartOrEndTime() {
        Filters s = new Filters.Builder().setStartTime(LocalTime.of(8,0)).build();
        assertEquals(LocalTime.of(8,0), s.getStartTime());
        assertNull(s.getEndTime());
        Filters e = new Filters.Builder().setEndTime(LocalTime.of(20,0)).build();
        assertEquals(LocalTime.of(20,0), e.getEndTime());
        assertNull(e.getStartTime());
    }

    @DisplayName("Tests startTime equals endTime is valid")
    @Test
    void builder_startEqualsEndTime() {
        Filters filters = new Filters.Builder().setStartTime(LocalTime.of(12,0)).setEndTime(LocalTime.of(12,0)).build();
        assertEquals(LocalTime.of(12,0), filters.getStartTime());
        assertEquals(LocalTime.of(12,0), filters.getEndTime());
    }

    @DisplayName("Tests all sort flag combinations in equals/hashCode")
    @Test
    void equals_sortFlagCombinations() {
        Filters a = new Filters.Builder().setSortByDate(true).setSortByRating(false).build();
        Filters b = new Filters.Builder().setSortByDate(true).setSortByRating(false).build();
        Filters c = new Filters.Builder().setSortByDate(false).setSortByRating(true).build();
        Filters d = new Filters.Builder().setSortByDate(false).setSortByRating(false).build();
        assertEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(c, d);
    }

    @DisplayName("Tests builder allows overwriting fields (last value wins)")
    @Test
    void builder_overwriteFields() {
        Filters filters = new Filters.Builder()
                .setAuthorName("A").setAuthorName("B")
                .setMinRating(1).setMinRating(2)
                .setStartDate(LocalDate.of(2023,1,1)).setStartDate(null)
                .build();
        assertEquals("B", filters.getAuthorName());
        assertEquals(2, filters.getMinRating());
        assertNull(filters.getStartDate());
    }

    @DisplayName("Tests equals identity check (same object reference)")
    @Test
    void equals_identityCheck() {
        // Arrange
        Filters filters = new Filters.Builder().setAuthorName("Test").build();
        // Act & Assert
        assertEquals(filters, filters); // Hits the 'this == other' branch
    }

    @DisplayName("Tests setting fields to null explicitly skips validations")
    @Test
    void builder_setFieldsToNull_skipsValidations() {
        // Arrange & Act
        Filters filters = new Filters.Builder()
                .setRating(null)
                .setMinRating(null)
                .setMaxRating(null)
                .setStartDate(null)
                .setEndDate(null)
                .setStartTime(null)
                .setEndTime(null)
                .build();
        // Assert
        assertNull(filters.getRating());
        assertNull(filters.getMinRating());
        assertNull(filters.getMaxRating());
        assertNull(filters.getStartDate());
        assertNull(filters.getEndDate());
        assertNull(filters.getStartTime());
        assertNull(filters.getEndTime());
    }

    @DisplayName("Tests validation short-circuiting with one range field null")
    @Test
    void builder_partialRangesWithNull_skipsValidation() {
        // Arrange & Act
        Filters datePartial = new Filters.Builder().setStartDate(LocalDate.of(2023, 1, 1)).setEndDate(null).build();
        Filters timePartial = new Filters.Builder().setStartTime(LocalTime.of(10, 0)).setEndTime(null).build();
        Filters minPartial = new Filters.Builder().setMinRating(2).setMaxRating(null).build();
        // Assert
        assertNull(datePartial.getEndDate()); // Ensures null skip didn't throw
        assertNull(timePartial.getEndTime());
        assertNull(minPartial.getMaxRating());
    }

    @DisplayName("Tests rating validation edge values without throwing")
    @Test
    void builder_ratingEdges_validNoThrow() {
        // Arrange & Act
        Filters filters = new Filters.Builder()
                .setRating(1) // Hits <1 false, >5 false
                .setMinRating(1) // Edge low
                .setMaxRating(5) // Edge high, valid since min <= max
                .build();
        // Assert
        assertEquals(1, filters.getRating());
        assertEquals(1, filters.getMinRating());
        assertEquals(5, filters.getMaxRating());
    }


    @DisplayName("Tests attempting setters after build throws InvalidInputException")
    @Test
    void builder_settersAfterBuild_throwInvalidInputException() {
        // Arrange
        Filters.Builder builder = new Filters.Builder();
        builder.build();
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> builder.setRating(3));
        assertThrows(InvalidInputException.class, () -> builder.setMinRating(2));
        assertThrows(InvalidInputException.class, () -> builder.setMaxRating(4));
        assertThrows(InvalidInputException.class, () -> builder.setAuthorName("Test"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewTitle("Title"));
        assertThrows(InvalidInputException.class, () -> builder.setProductName("Product"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewDate(LocalDate.now()));
        assertThrows(InvalidInputException.class, () -> builder.setStoreName("Store"));
        assertThrows(InvalidInputException.class, () -> builder.setStartDate(LocalDate.now()));
        assertThrows(InvalidInputException.class, () -> builder.setEndDate(LocalDate.now()));
        assertThrows(InvalidInputException.class, () -> builder.setStartTime(LocalTime.now()));
        assertThrows(InvalidInputException.class, () -> builder.setEndTime(LocalTime.now()));
        assertThrows(InvalidInputException.class, () -> builder.setSortByDate(true));
        assertThrows(InvalidInputException.class, () -> builder.setSortByRating(true));
    }

    @DisplayName("Tests hashCode with all null fields")
    @Test
    void hashCode_allNullFields() {
        // Arrange
        Filters filters = new Filters.Builder().build(); // All defaults null/false
        // Act
        int hash = filters.hashCode();
        // Assert
        assertEquals(filters.hashCode(), hash); // Consistent, exercises null paths in Objects.hash
    }

    @DisplayName("Tests equals with mixed null and non-null fields")
    @Test
    void equals_mixedNullNonNull() {
        // Arrange
        Filters f1 = new Filters.Builder().setRating(3).setMinRating(null).build();
        Filters f2 = new Filters.Builder().setRating(3).setMinRating(null).build();
        Filters f3 = new Filters.Builder().setRating(3).setMinRating(4).build(); // Differs in null vs value
        // Act & Assert
        assertEquals(f1, f2); // Both have null in same place
        assertNotEquals(f1, f3); // Null vs non-null difference
    }

    @DisplayName("Tests validation for rating exactly at boundaries without min/max interaction")
    @Test
    void builder_ratingBoundaries_isolated() {
        // Arrange & Act
        Filters low = new Filters.Builder().setRating(1).build(); // <1 false, >5 false
        Filters high = new Filters.Builder().setRating(5).build();
        // Assert
        assertEquals(1, low.getRating());
        assertEquals(5, high.getRating());
        // Additional invalid to hit isolated || branches
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(0).build()); // <1 true
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(6).build()); // >5 true
    }

    @DisplayName("Tests equals method identity branch explicitly")
    @Test
    void equals_identityBranch() {
        // Arrange
        Filters filters = new Filters.Builder().build();
        // Act & Assert
        assertTrue(filters.equals(filters));
    }

    @DisplayName("Tests assertNotBuilt throw in every setter after build")
    @Test
    void builder_allSettersAfterBuild_throwForEach() {
        // Arrange
        Filters.Builder builder = new Filters.Builder();
        builder.build();
        // Act & Assert
        assertThrows(InvalidInputException.class, () -> builder.setRating(3));
        assertThrows(InvalidInputException.class, () -> builder.setMinRating(2));
        assertThrows(InvalidInputException.class, () -> builder.setMaxRating(4));
        assertThrows(InvalidInputException.class, () -> builder.setAuthorName("Test"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewTitle("Title"));
        assertThrows(InvalidInputException.class, () -> builder.setProductName("Product"));
        assertThrows(InvalidInputException.class, () -> builder.setReviewDate(LocalDate.of(2023, 1, 1)));
        assertThrows(InvalidInputException.class, () -> builder.setStoreName("Store"));
        assertThrows(InvalidInputException.class, () -> builder.setStartDate(LocalDate.of(2023, 1, 1)));
        assertThrows(InvalidInputException.class, () -> builder.setEndDate(LocalDate.of(2023, 12, 31)));
        assertThrows(InvalidInputException.class, () -> builder.setStartTime(LocalTime.of(10, 0)));
        assertThrows(InvalidInputException.class, () -> builder.setEndTime(LocalTime.of(18, 0)));
        assertThrows(InvalidInputException.class, () -> builder.setSortByDate(true));
        assertThrows(InvalidInputException.class, () -> builder.setSortByRating(true));
    }

    @DisplayName("Tests validation short-circuit skips for each range with one side null")
    @Test
    void builder_rangeValidationNullSkips() {
        // Arrange & Act
        Filters minMaxSkip = new Filters.Builder().setMinRating(5).setMaxRating(null).build(); // min not null, max null -> skip
        Filters dateSkip = new Filters.Builder().setStartDate(LocalDate.of(2023, 1, 1)).setEndDate(null).build(); // start not null, end null
        Filters timeSkip = new Filters.Builder().setStartTime(LocalTime.of(10, 0)).setEndTime(null).build(); // start not null, end null
        // Assert
        assertNull(minMaxSkip.getMaxRating());
        assertNull(dateSkip.getEndDate());
        assertNull(timeSkip.getEndTime());
        // Reverse for completeness
        new Filters.Builder().setMinRating(null).setMaxRating(1).build();
        new Filters.Builder().setStartDate(null).setEndDate(LocalDate.of(2023, 12, 31)).build();
        new Filters.Builder().setStartTime(null).setEndTime(LocalTime.of(18, 0)).build();
    }

    @DisplayName("Tests rating validation null skips for each field")
    @Test
    void builder_ratingValidationNullSkips() {
        // Arrange & Act
        Filters filters = new Filters.Builder()
                .setRating(null) // Skip for rating
                .setMinRating(null) // Skip for min
                .setMaxRating(null) // Skip for max
                .build();
        // Assert
        assertNull(filters.getRating());
        assertNull(filters.getMinRating());
        assertNull(filters.getMaxRating());
    }

    @DisplayName("Tests isolated || branches in rating validation")
    @Test
    void builder_ratingValidationIsolatedOrBranches() {
        // Arrange & Act & Assert
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(0).build());
        assertThrows(InvalidInputException.class, () -> new Filters.Builder().setRating(6).build());

        new Filters.Builder().setRating(1).build();
        new Filters.Builder().setRating(5).build();
    }


    private Filters.Builder baseBuilder() {
        return new Filters.Builder()
                .setRating(3)
                .setMinRating(1)
                .setMaxRating(5)
                .setAuthorName("Alice")
                .setReviewTitle("Great")
                .setProductName("Widget")
                .setReviewDate(LocalDate.of(2023, 6, 15))
                .setStoreName("Amazon")
                .setStartDate(LocalDate.of(2023, 1, 1))
                .setEndDate(LocalDate.of(2023, 12, 31))
                .setStartTime(LocalTime.of(9, 0))
                .setEndTime(LocalTime.of(17, 0))
                .setSortByDate(true)
                .setSortByRating(true);
    }

    @DisplayName("Tests equals returns false when differing only in each individual field (covers short-circuit branches)")
    @Test
    void equals_differOnlyInEachField() {
        Filters base = baseBuilder().build();

        // Differ in sortByDate
        Filters varSortByDate = baseBuilder().setSortByDate(false).build();
        assertNotEquals(base, varSortByDate);

        // Differ in sortByRating
        Filters varSortByRating = baseBuilder().setSortByRating(false).build();
        assertNotEquals(base, varSortByRating);

        // Differ in rating
        Filters varRating = baseBuilder().setRating(4).build();
        assertNotEquals(base, varRating);

        // Differ in minRating
        Filters varMinRating = baseBuilder().setMinRating(2).build();
        assertNotEquals(base, varMinRating);

        // Differ in maxRating
        Filters varMaxRating = baseBuilder().setMaxRating(4).build();
        assertNotEquals(base, varMaxRating);

        // Differ in authorName
        Filters varAuthorName = baseBuilder().setAuthorName("Bob").build();
        assertNotEquals(base, varAuthorName);

        // Differ in reviewTitle
        Filters varReviewTitle = baseBuilder().setReviewTitle("Bad").build();
        assertNotEquals(base, varReviewTitle);

        // Differ in productName
        Filters varProductName = baseBuilder().setProductName("Gadget").build();
        assertNotEquals(base, varProductName);

        // Differ in reviewDate
        Filters varReviewDate = baseBuilder().setReviewDate(LocalDate.of(2023, 7, 1)).build();
        assertNotEquals(base, varReviewDate);

        // Differ in storeName
        Filters varStoreName = baseBuilder().setStoreName("eBay").build();
        assertNotEquals(base, varStoreName);

        // Differ in startDate
        Filters varStartDate = baseBuilder().setStartDate(LocalDate.of(2023, 2, 1)).build();
        assertNotEquals(base, varStartDate);

        // Differ in endDate
        Filters varEndDate = baseBuilder().setEndDate(LocalDate.of(2023, 11, 30)).build();
        assertNotEquals(base, varEndDate);

        // Differ in startTime
        Filters varStartTime = baseBuilder().setStartTime(LocalTime.of(10, 0)).build();
        assertNotEquals(base, varStartTime);

        // Differ in endTime
        Filters varEndTime = baseBuilder().setEndTime(LocalTime.of(16, 0)).build();
        assertNotEquals(base, varEndTime);
    }

}
