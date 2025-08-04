package com.reviewapp.adapter.memory;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.port.ReviewQueryPort;
import com.reviewapp.domain.port.ReviewStatsPort;
import com.reviewapp.domain.port.ReviewWritePort;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory adapter implementing the application ports:
 * <ul>
 *   <li>{@link ReviewWritePort} – write/persist (stores in a concurrent map)</li>
 *   <li>{@link ReviewQueryPort} – read/query operations</li>
 *   <li>{@link ReviewStatsPort} – aggregate/statistical queries</li>
 * </ul>
 *
 * <p>This implementation is suitable for tests, demos, and local development.
 * It is thread-safe for basic concurrent access since it uses a {@link ConcurrentHashMap} as a backing store.
 * There is no persistence across application runs.</p>
 *
 * <p><b>Note:</b> The filter logic below mirrors the minimal behavior present in the original code
 * (filters by exact rating only). Extend it as needed to match JDBC semantics.</p>
 */
public class InMemoryReviewRepository implements ReviewQueryPort, ReviewWritePort, ReviewStatsPort {

    /**
     * Primary in-memory store keyed by review ID.
     * Chosen as ConcurrentHashMap for simple thread-safety characteristics.
     */
    private final Map<Long, Review> store = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------------
    // Write port
    // ---------------------------------------------------------------------

    /**
     * Saves (upserts) a batch of reviews into the in-memory store.
     * The review ID acts as the key.
     *
     * @param reviews list of reviews to persist (null-safe: null/empty is a no-op)
     */
    @Override
    public void saveReviews(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) return;
        for (Review review : reviews) {
            if (review != null && review.getReviewId() != null) {
                store.put(review.getReviewId(), review);
            }
        }
    }

    // ---------------------------------------------------------------------
    // Query port
    // ---------------------------------------------------------------------

    /**
     * Returns a page of reviews ordered by {@code reviewId} ascending.
     *
     * @param page     1-based page index
     * @param pageSize number of items per page
     * @return list of reviews (possibly empty)
     */
    @Override
    public List<Review> getReviewsPage(int page, int pageSize) {
        return store.values().stream()
                .sorted(Comparator.comparing(Review::getReviewId))
                .skip(Math.max(0L, (long) (page - 1) * pageSize))
                .limit(Math.max(0L, pageSize))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total number of reviews stored.
     */
    @Override
    public int getTotalReviewCount() {
        return store.size();
    }

    /**
     * Fetches a review by its identifier.
     *
     * @param id review ID
     * @return review or {@code null} if not found
     */
    @Override
    public Review getReviewById(Long id) {
        if (id == null) return null;
        return store.get(id);
    }

    /**
     * Minimal filtered, paged retrieval of reviews.
     * <p>
     * <b>Current behavior:</b> filters only by exact rating if present in {@code filters}.
     * Extend this method to honor additional {@link Filters} fields as needed
     * (author/title/product/date/store ranges, sorting, etc.).
     *
     * @param filters  filter object (non-null expected; null treated as no filters)
     * @param page     1-based page index
     * @param pageSize number of items per page
     * @return filtered list (possibly empty)
     */
    @Override
    public List<Review> getReviewsByFilters(Filters filters, int page, int pageSize) {
        return store.values().stream()
                .filter(review -> {
                    if (filters == null) return true;
                    // Rating filters
                    if (filters.getRating() != null && !Objects.equals(review.getProductRating(), filters.getRating()))
                        return false;
                    if (filters.getMinRating() != null && (review.getProductRating() == null || review.getProductRating() < filters.getMinRating()))
                        return false;
                    if (filters.getMaxRating() != null && (review.getProductRating() == null || review.getProductRating() > filters.getMaxRating()))
                        return false;
                    // Exact date
                    if (filters.getReviewDate() != null && (review.getReviewedDate() == null || !review.getReviewedDate().toString().equals(filters.getReviewDate().toString())))
                        return false;
                    // Min date
                    if (filters.getStartDate() != null && (review.getReviewedDate() == null || review.getReviewedDate().isBefore(filters.getStartDate())))
                        return false;
                    // Max date
                    if (filters.getEndDate() != null && (review.getReviewedDate() == null || review.getReviewedDate().isAfter(filters.getEndDate())))
                        return false;
                    // Start time
                    if (filters.getStartTime() != null) {
                        // No time info in Review, so skip time filtering
                        return false;
                    }
                    // End time
                    if (filters.getEndTime() != null) {
                        // No time info in Review, so skip time filtering
                        return false;
                    }
                    // Author
                    if (filters.getAuthorName() != null && !filters.getAuthorName().isBlank() && (review.getAuthorName() == null || !review.getAuthorName().toLowerCase().contains(filters.getAuthorName().toLowerCase())))
                        return false;
                    // Title
                    if (filters.getReviewTitle() != null && !filters.getReviewTitle().isBlank() && (review.getReviewTitle() == null || !review.getReviewTitle().toLowerCase().contains(filters.getReviewTitle().toLowerCase())))
                        return false;
                    // Product
                    if (filters.getProductName() != null && !filters.getProductName().isBlank() && (review.getProductName() == null || !review.getProductName().toLowerCase().contains(filters.getProductName().toLowerCase())))
                        return false;
                    // Store
                    if (filters.getStoreName() != null && !filters.getStoreName().isBlank() && (review.getReviewSource() == null || !review.getReviewSource().toLowerCase().contains(filters.getStoreName().toLowerCase())))
                        return false;
                    return true;
                })
                .sorted(getComparator(filters))
                .skip(Math.max(0L, (long) (page - 1) * pageSize))
                .limit(Math.max(0L, pageSize))
                .collect(Collectors.toList());
    }

    /**
     * Returns a comparator for sorting reviews based on filter flags.
     */
    private static Comparator<Review> getComparator(Filters filters) {
        if (filters != null && filters.isSortByDate() && filters.isSortByRating()) {
            // First sort by rating DESC, then by date DESC within each rating group
            return Comparator.comparing(Review::getProductRating, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(Review::getReviewedDate, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if (filters != null && filters.isSortByDate()) {
            return Comparator.comparing(Review::getReviewedDate, Comparator.nullsLast(Comparator.reverseOrder()));
        } else if (filters != null && filters.isSortByRating()) {
            return Comparator.comparing(Review::getProductRating, Comparator.nullsLast(Comparator.reverseOrder()));
        } else {
            return Comparator.comparing(Review::getReviewId); // default
        }
    }

    /**
     * Returns the count of reviews that match the provided filters.
     * <p>
     * <b>Current behavior:</b> counts by exact rating only (if provided).
     *
     * @param filters filter object (may be null = no filters)
     */
    @Override
    public int getFilteredReviewCount(Filters filters) {
        return (int) store.values().stream()
                .filter(review -> {
                    if (filters == null) return true;
                    // Rating filters
                    if (filters.getRating() != null && !Objects.equals(review.getProductRating(), filters.getRating()))
                        return false;
                    if (filters.getMinRating() != null && (review.getProductRating() == null || review.getProductRating() < filters.getMinRating()))
                        return false;
                    if (filters.getMaxRating() != null && (review.getProductRating() == null || review.getProductRating() > filters.getMaxRating()))
                        return false;
                    // Exact date
                    if (filters.getReviewDate() != null && (review.getReviewedDate() == null || !review.getReviewedDate().toString().equals(filters.getReviewDate().toString())))
                        return false;
                    // Min date
                    if (filters.getStartDate() != null && (review.getReviewedDate() == null || review.getReviewedDate().isBefore(filters.getStartDate())))
                        return false;
                    // Max date
                    if (filters.getEndDate() != null && (review.getReviewedDate() == null || review.getReviewedDate().isAfter(filters.getEndDate())))
                        return false;
                    // Start time
                    if (filters.getStartTime() != null) {
                        // No time info in Review, so skip time filtering
                        return false;
                    }
                    // End time
                    if (filters.getEndTime() != null) {
                        // No time info in Review, so skip time filtering
                        return false;
                    }
                    // Author
                    if (filters.getAuthorName() != null && !filters.getAuthorName().isBlank() && (review.getAuthorName() == null || !review.getAuthorName().toLowerCase().contains(filters.getAuthorName().toLowerCase())))
                        return false;
                    // Title
                    if (filters.getReviewTitle() != null && !filters.getReviewTitle().isBlank() && (review.getReviewTitle() == null || !review.getReviewTitle().toLowerCase().contains(filters.getReviewTitle().toLowerCase())))
                        return false;
                    // Product
                    if (filters.getProductName() != null && !filters.getProductName().isBlank() && (review.getProductName() == null || !review.getProductName().toLowerCase().contains(filters.getProductName().toLowerCase())))
                        return false;
                    // Store
                    if (filters.getStoreName() != null && !filters.getStoreName().isBlank() && (review.getReviewSource() == null || !review.getReviewSource().toLowerCase().contains(filters.getStoreName().toLowerCase())))
                        return false;
                    return true;
                })
                .count();
    }


    /**
     * Keyword search across review text and titles (case-insensitive).
     *
     * @param keywords list of keywords; if null/empty returns empty list
     * @return list of matched reviews
     */
    @Override
    public List<Review> getReviewsByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return Collections.emptyList();

        // Pre-normalize keywords for efficiency
        final List<String> normalizedKeywords = keywords.stream()
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        if (normalizedKeywords.isEmpty()) return Collections.emptyList();

        return store.values().stream()
                .filter(review -> {
                    final String content = ((review.getReviewText() == null ? "" : review.getReviewText()) + " " +
                            (review.getReviewTitle() == null ? "" : review.getReviewTitle())).toLowerCase();
                    // Match if any keyword is contained in the content
                    return normalizedKeywords.stream().anyMatch(content::contains);
                })
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------------
    // Stats port
    // ---------------------------------------------------------------------

    /**
     * Total review count for stats (delegates to {@link #getTotalReviewCount()}).
     */
    @Override
    public int getTotalReviewCountStats() {
        return getTotalReviewCount();
    }

    /**
     * Average rating across all reviews (0.0 if no reviews).
     */
    @Override
    public double getAverageRating() {
        return store.values().stream()
                .mapToInt(Review::getProductRating)
                .average()
                .orElse(0.0);
    }

    /**
     * Distribution of ratings (rating → count), sorted ascending by rating.
     */
    @Override
    public Map<Integer, Integer> getRatingDistribution() {
        final Map<Integer, Integer> distribution = new TreeMap<>();
        store.values().forEach(review ->
                distribution.merge(review.getProductRating(), 1, Integer::sum)
        );
        return distribution;
    }

    /**
     * Monthly average rating map keyed by {@code YYYY-MM}.
     * Reviews without a date are grouped under {@code "unknown"}.
     */
    @Override
    public Map<String, Double> getMonthlyRatingAverage() {
        // Group ratings by month key
        final Map<String, List<Integer>> ratingsByMonth = new TreeMap<>();
        store.values().forEach(review -> {
            final String monthKey = (review.getReviewedDate() != null)
                    ? review.getReviewedDate().toString().substring(0, 7) // YYYY-MM
                    : "unknown";
            ratingsByMonth.computeIfAbsent(monthKey, k -> new ArrayList<>())
                    .add(review.getProductRating());
        });

        // Compute averages per group
        final Map<String, Double> monthlyAverage = new TreeMap<>();
        ratingsByMonth.forEach((month, ratings) ->
                monthlyAverage.put(month, ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0))
        );
        return monthlyAverage;
    }

    /**
     * Returns all reviews as a list.
     * @deprecated Use {@link #getReviewsByFilters(Filters, int, int)}
     */
    @Deprecated
    @Override
    public List<Review> getAllReviews() {
        return new ArrayList<>(store.values());
    }
}
