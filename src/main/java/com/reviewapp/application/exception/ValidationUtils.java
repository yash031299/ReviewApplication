package com.reviewapp.application.exception;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Utility class for input validation and parsing in the application.
 * <p>
 * Provides static methods to parse and validate dates, times, and ratings from string input.
 * Throws {@link InvalidInputException} for invalid or unrecognized input formats.
 * </p>
 */
public final class ValidationUtils {
    /**
     * Private constructor to prevent instantiation.
     */
    private ValidationUtils() {}

    /**
     * Parses a string into a {@link LocalDate}.
     * Accepts ISO (yyyy-MM-dd), yyyy/MM/dd, and MM/dd/yyyy formats.
     *
     * @param input the date string to parse
     * @return the parsed {@link LocalDate}
     * @throws InvalidInputException if the input is null, empty, or unrecognized
     */
    public static LocalDate parseDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException("Date must not be empty");
        }
        String v = input.trim();
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            // Try yyyy/MM/dd
            try { return LocalDate.parse(v.replace('/', '-')); } catch (DateTimeParseException ignore) {}
            // Try MM/dd/yyyy
            try {
                String[] parts = v.split("[/-]");
                if (parts.length == 3 && parts[0].length() == 2) {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    return LocalDate.of(year, month, day);
                }
            } catch (Exception ignore) {}
            throw new InvalidInputException("Unrecognized date format: " + input);
        }
    }

    /**
     * Parses a string into a {@link LocalTime}.
     * Accepts HH:mm or HH:mm:ss formats.
     *
     * @param input the time string to parse
     * @return the parsed {@link LocalTime}
     * @throws InvalidInputException if the input is null, empty, or unrecognized
     */
    public static LocalTime parseTime(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException("Time must not be empty");
        }
        String v = input.trim();
        long colons = v.chars().filter(ch -> ch == ':').count();
        if (colons == 1) v += ":00"; // HH:mm -> HH:mm:ss
        try {
            return LocalTime.parse(v);
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Unrecognized time format: " + input);
        }
    }

    /**
     * Parses a string into a product rating (1-5).
     *
     * @param input the rating string to parse
     * @return the parsed rating as an integer
     * @throws InvalidInputException if the input is null, empty, non-numeric, or out of range
     */
    public static int parseRating(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidInputException("Rating must not be empty");
        }
        try {
            int rating = Integer.parseInt(input.trim());
            if (rating < 1 || rating > 5) {
                throw new InvalidInputException("Rating must be between 1 and 5");
            }
            return rating;
        } catch (NumberFormatException e) {
            throw new InvalidInputException("Invalid rating format: " + input);
        }
    }
}
