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
}
