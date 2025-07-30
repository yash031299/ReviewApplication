package com.example.reviewapp.adapter.ingest;

import com.example.reviewapp.domain.model.Review;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility to parse line-delimited JSON reviews from a local file into domain {@link Review} objects.
 *
 * <p><strong>Format:</strong> This parser expects one JSON object per line (a.k.a. NDJSON / JSONL).
 * Each JSON record is mapped to a {@link Review} via {@link #convertJsonNodeToReview(JsonNode)}.</p>
 *
 * <p><strong>Thread-safety:</strong> This class is stateless and thread-safe. Jackson's
 * {@link ObjectMapper} is thread-safe after configuration and can be shared.</p>
 *
 * <p><strong>Error handling:</strong> Malformed lines or records with invalid/insufficient data
 * are skipped; a warning is printed to {@code System.err}. The method continues parsing subsequent lines.</p>
 *
 * <p><strong>Typical usage:</strong> Call this during application bootstrap to ingest a local
 * JSON dump and then persist via a {@code ReviewWritePort}.</p>
 */
public final class JsonParser {

    /** Shared, thread-safe Jackson mapper (no custom modules required for this mapping). */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // --- JSON field names (centralized to avoid typos/drift) ---
    private static final String F_ID            = "id";
    private static final String F_REVIEW        = "review";
    private static final String F_AUTHOR        = "author";
    private static final String F_SOURCE        = "review_source";
    private static final String F_TITLE         = "title";
    private static final String F_PRODUCT_NAME  = "product_name";
    private static final String F_REVIEWED_DATE = "reviewed_date";
    private static final String F_RATING        = "rating";

    /** Non-instantiable utility class. */
    private JsonParser() {
        throw new AssertionError("Do not instantiate utility class JsonParser");
    }

    /**
     * Parses a line-delimited JSON file into a list of {@link Review} objects.
     *
     * <p>Each line is parsed independently. If a line cannot be parsed, it is skipped and a warning
     * is logged to {@code System.err}. The method returns all successfully parsed reviews.</p>
     *
     * @param filePath path to a local NDJSON/JSONL file (one JSON object per line)
     * @return list of parsed {@link Review} objects (never {@code null})
     * @throws RuntimeException if the file cannot be opened/read
     */
    public static List<Review> parseReviewsFromFile(String filePath) {
        Objects.requireNonNull(filePath, "filePath must not be null");

        List<Review> reviews = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = bufferedReader.readLine()) != null) {
                lineNumber++;

                // Skip empty/whitespace-only lines gracefully
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    JsonNode jsonNode = MAPPER.readTree(line);
                    Review review = convertJsonNodeToReview(jsonNode);
                    reviews.add(review);
                } catch (Exception exception) {
                    // In production, replace with a logger (e.g., SLF4J) at WARN level
                    System.err.println("[WARN] Skipping bad JSON at line " + lineNumber + ": " + exception.getMessage());
                }
            }
        } catch (IOException ioException) {
            // Escalate as unchecked since callers typically cannot recover here.
            throw new RuntimeException("Failed to read JSON file: " + filePath, ioException);
        }
        return reviews;
    }

    /**
     * Maps a single JSON object to a {@link Review}.
     *
     * <p>Field handling:</p>
     * <ul>
     *   <li><b>id</b>: if absent/null, a random positive long is generated as a fallback.</li>
     *   <li><b>rating</b>: must be in [1..5]; invalid/missing ratings cause an {@link IllegalArgumentException}.</li>
     *   <li><b>reviewed_date</b>: accepts {@code yyyy-MM-dd} or an ISO-like timestamp; only the date portion is used.</li>
     *   <li>All string fields are read as-is; missing fields map to {@code null}.</li>
     * </ul>
     *
     * <p><strong>Note:</strong> {@link Review.Builder} enforces required fields and will also throw
     * for invalid data. We proactively validate rating here to produce a clearer error per record.</p>
     *
     * @param node a non-null JSON object node
     * @return an immutable {@link Review}
     * @throws IllegalArgumentException if validation fails (e.g., rating out of range)
     */
    private static Review convertJsonNodeToReview(JsonNode node) {
        // Resolve ID; generate a positive fallback if missing
        long reviewId = node.hasNonNull(F_ID) ? node.get(F_ID).asLong(-1L)
                : Math.abs(UUID.randomUUID().getLeastSignificantBits());

        // Resolve and validate rating early to produce a clear message
        int rating = node.hasNonNull(F_RATING) ? node.get(F_RATING).asInt(-1) : -1;
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid rating; must be between 1 and 5");
        }

        return new Review.Builder()
                .setReviewId(reviewId)
                .setReviewText(node.hasNonNull(F_REVIEW) ? node.get(F_REVIEW).asText() : null)
                .setAuthorName(node.hasNonNull(F_AUTHOR) ? node.get(F_AUTHOR).asText() : null)
                .setReviewSource(node.hasNonNull(F_SOURCE) ? node.get(F_SOURCE).asText() : null)
                .setReviewTitle(node.hasNonNull(F_TITLE) ? node.get(F_TITLE).asText() : null)
                .setProductName(node.hasNonNull(F_PRODUCT_NAME) ? node.get(F_PRODUCT_NAME).asText() : null)
                .setReviewedDate(node.hasNonNull(F_REVIEWED_DATE)
                        ? parseFlexibleDate(node.get(F_REVIEWED_DATE).asText())
                        : null)
                .setProductRating(rating)
                .build();
    }

    /**
     * Parses date-like strings by taking the first 10 characters (YYYY-MM-DD).
     *
     * <p>Accepts:
     * <ul>
     *   <li>{@code yyyy-MM-dd}</li>
     *   <li>ISO-like timestamps where the first 10 chars represent the date
     *       (e.g., {@code yyyy-MM-ddTHH:mm:ss.SSSZ})</li>
     * </ul>
     * If parsing fails, {@code null} is returned.
     *
     * @param dateStr input date string (may be {@code null})
     * @return {@link LocalDate} or {@code null} if not parsable
     */
    private static LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        // Guard against very short strings to avoid StringIndexOutOfBounds
        if (dateStr.length() < 10) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.substring(0, 10));
        } catch (Exception ignore) {
            return null;
        }
    }
}
