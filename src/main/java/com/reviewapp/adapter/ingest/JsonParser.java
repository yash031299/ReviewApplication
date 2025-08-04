package com.reviewapp.adapter.ingest;

import com.reviewapp.domain.model.Review;
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
    private static final String ID            = "id";
    private static final String REVIEW        = "review";
    private static final String AUTHOR        = "author";
    private static final String SOURCE        = "review_source";
    private static final String TITLE         = "title";
    private static final String PRODUCT_NAME  = "product_name";
    private static final String REVIEWED_DATE = "reviewed_date";
    private static final String RATING        = "rating";

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
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String content = sb.toString().trim();
            if (content.startsWith("[")) {
                // JSON array
                JsonNode arrayNode = MAPPER.readTree(content);
                if (arrayNode.isArray()) {
                    for (JsonNode node : arrayNode) {
                        try {
                            reviews.add(convertJsonNodeToReview(node));
                        } catch (Exception e) {
                            System.err.println("[WARN] Skipping bad JSON array element: " + e.getMessage());
                        }
                    }
                }
            } else {
                // NDJSON/JSONL
                bufferedReader.close(); // Re-open to read line by line
                try (BufferedReader br2 = new BufferedReader(new FileReader(filePath))) {
                    int lineNumber = 0;
                    while ((line = br2.readLine()) != null) {
                        lineNumber++;
                        if (line.trim().isEmpty()) continue;
                        try {
                            JsonNode jsonNode = MAPPER.readTree(line);
                            reviews.add(convertJsonNodeToReview(jsonNode));
                        } catch (Exception exception) {
                            System.err.println("[WARN] Skipping bad JSON at line " + lineNumber + ": " + exception.getMessage());
                        }
                    }
                }
            }
        } catch (IOException ioException) {
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
        long reviewId = node.hasNonNull(ID) ? node.get(ID).asLong(-1L)
                : Math.abs(UUID.randomUUID().getLeastSignificantBits());

        // Resolve and validate rating early to produce a clear message
        int rating = node.hasNonNull(RATING) ? node.get(RATING).asInt(-1) : -1;
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Invalid rating; must be between 1 and 5");
        }

        return new Review.Builder()
                .setReviewId(reviewId)
                .setReviewText(node.hasNonNull(REVIEW) ? node.get(REVIEW).asText() : null)
                .setAuthorName(node.hasNonNull(AUTHOR) ? node.get(AUTHOR).asText() : null)
                .setReviewSource(node.hasNonNull(SOURCE) ? node.get(SOURCE).asText() : null)
                .setReviewTitle(node.hasNonNull(TITLE) ? node.get(TITLE).asText() : null)
                .setProductName(node.hasNonNull(PRODUCT_NAME) ? node.get(PRODUCT_NAME).asText() : null)
                .setReviewedDate(node.hasNonNull(REVIEWED_DATE)
                        ? parseFlexibleDate(node.get(REVIEWED_DATE).asText())
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
     *       (e.g., {@code yyyy-MM-ddTHH:mm:ssZ})</li>
     * </ul>
     * If parsing fails, {@code null} is returned.
     *
     * @param dateStr input date string (maybe {@code null})
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
