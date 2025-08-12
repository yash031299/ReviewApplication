package com.reviewapp.application.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ValidationUtils} utility methods.
 * Covers parsing and validation of dates, times, and ratings.
 */
class ValidationUtilsTest {


    @DisplayName("Tests parsing of ISO date format (yyyy-MM-dd).")
    @Test
    void parseDate_givenIsoFormat_returnsLocalDate() {
        // Arrange
        String input = "2024-07-31";
        // Act
        LocalDate result = ValidationUtils.parseDate(input);
        // Assert
        assertEquals(LocalDate.of(2024, 7, 31), result);
    }


    @DisplayName("Tests parsing of slash date format (yyyy/MM/dd).")
    @Test
    void parseDate_givenSlashFormat_returnsLocalDate() {
        // Arrange
        String input = "2024/07/31";
        // Act
        LocalDate result = ValidationUtils.parseDate(input);
        // Assert
        assertEquals(LocalDate.of(2024, 7, 31), result);
    }


    @DisplayName("Tests parsing of US date format (MM/dd/yyyy).")
    @Test
    void parseDate_givenUsFormat_returnsLocalDate() {
        // Arrange
        String input = "07/31/2024";
        // Act
        LocalDate result = ValidationUtils.parseDate(input);
        // Assert
        assertEquals(LocalDate.of(2024, 7, 31), result);
    }


    @DisplayName("Tests that null or empty date input throws InvalidInputException.")
    @Test
    void parseDate_givenNullOrEmpty_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate(null));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate(""));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate("   "));
    }


    @DisplayName("Tests that invalid or unsupported date formats throw InvalidInputException.")
    @Test
    void parseDate_givenInvalidFormat_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate("notadate"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate("2024-13-01")); // invalid month
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseDate("31/07/2024")); // unsupported format
    }


    @DisplayName("Tests parsing of time in HH:mm format (should default seconds to zero).")
    @Test
    void parseTime_givenHourMinute_returnsLocalTimeWithZeroSeconds() {
        // Arrange
        String input = "14:30";
        // Act
        LocalTime result = ValidationUtils.parseTime(input);
        // Assert
        assertEquals(LocalTime.of(14, 30, 0), result);
    }


    @DisplayName("Tests parsing of time in HH:mm:ss format.")
    @Test
    void parseTime_givenHourMinuteSecond_returnsLocalTime() {
        // Arrange
        String input = "14:30:15";
        // Act
        LocalTime result = ValidationUtils.parseTime(input);
        // Assert
        assertEquals(LocalTime.of(14, 30, 15), result);
    }


    @DisplayName("Tests that null or empty time input throws InvalidInputException.")
    @Test
    void parseTime_givenNullOrEmpty_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime(null));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime(""));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime("   "));
    }


    @DisplayName("Tests that invalid or out-of-range time formats throw InvalidInputException.")
    @Test
    void parseTime_givenInvalidFormat_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime("notatime"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime("25:00")); // invalid hour
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime("12:60")); // invalid minute
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseTime("12:30:60")); // invalid second
    }


    @DisplayName("Tests parsing of valid ratings (1-5), including whitespace.")
    @Test
    void parseRating_givenValidRatings_returnsParsedInt() {
        // Arrange, Act & Assert
        assertEquals(1, ValidationUtils.parseRating("1"));
        assertEquals(5, ValidationUtils.parseRating("5"));
        assertEquals(3, ValidationUtils.parseRating(" 3 "));
    }


    @DisplayName("Tests that null or empty rating input throws InvalidInputException.")
    @Test
    void parseRating_givenNullOrEmpty_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating(null));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating(""));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("   "));
    }


    @DisplayName("Tests that non-integer rating input throws InvalidInputException.")
    @Test
    void parseRating_givenNonInteger_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("notanint"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("3.5"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("one"));
    }


    @DisplayName("Tests that out-of-range ratings throw InvalidInputException.")
    @Test
    void parseRating_givenOutOfRange_throwsInvalidInputException() {
        // Arrange, Act & Assert
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("0"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("6"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("-1"));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseRating("100"));
    }
}
