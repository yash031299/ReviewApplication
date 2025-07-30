package com.example.reviewapp.adapter.jdbc;

import com.example.reviewapp.domain.model.Filters;
import com.example.reviewapp.domain.model.Review;
import com.example.reviewapp.domain.port.ReviewQueryPort;
import com.example.reviewapp.domain.port.ReviewStatsPort;
import com.example.reviewapp.domain.port.ReviewWritePort;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * SQLite JDBC adapter implementing the application ports:
 * <ul>
 *   <li>{@link ReviewQueryPort} – read/query operations</li>
 *   <li>{@link ReviewWritePort} – write/persist operations</li>
 *   <li>{@link ReviewStatsPort} – aggregate/statistical queries</li>
 * </ul>
 *
 * <p>This adapter isolates JDBC/SQLite specifics from the application layer.
 * The application depends only on the ports, so the underlying datastore can be
 * swapped without touching services or UI.</p>
 *
 * <p><b>Thread-safety:</b> This class is stateless (holds only the JDBC URL),
 * and each call opens a new connection; thus, it is safe to share across threads.
 * If you later add a connection pool, keep the same port contract.</p>
 */
public class SqliteReviewRepository implements ReviewQueryPort, ReviewWritePort, ReviewStatsPort {

    /** JDBC URL for SQLite, e.g., {@code jdbc:sqlite:reviews.db}. */
    private final String jdbcUrl;

    /**
     * Constructs the SQLite adapter and ensures the {@code reviews} table exists.
     *
     * @param jdbcUrl JDBC URL (must not be {@code null})
     * @throws NullPointerException if {@code jdbcUrl} is null
     * @throws RuntimeException     if the table creation DDL fails
     */
    public SqliteReviewRepository(String jdbcUrl) {
        this.jdbcUrl = Objects.requireNonNull(jdbcUrl, "jdbcUrl must not be null");
        createTableIfNotExists();
    }

    /**
     * Creates the {@code reviews} table if it does not already exist.
     * Uses simple DDL with all fields stored as text/int; dates are persisted as ISO strings.
     */
    private void createTableIfNotExists() {
        final String sql = "CREATE TABLE IF NOT EXISTS reviews (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "review TEXT," +
                "author TEXT," +
                "reviewSource TEXT," +
                "title TEXT," +
                "productName TEXT," +
                "reviewedDate TEXT," +
                "rating INTEGER)";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create 'reviews' table", exception);
        }
    }

    // ---------------------------------------------------------------------
    // Write port
    // ---------------------------------------------------------------------

    /**
     * Persists or updates a batch of reviews using {@code INSERT OR REPLACE}.
     * The review ID is treated as the primary key.
     *
     * @param reviews list of reviews to persist (non-null; items must be valid per domain constraints)
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public void saveReviews(List<Review> reviews) {
        final String sql = "INSERT OR REPLACE INTO reviews " +
                "(id, review, author, reviewSource, title, productName, reviewedDate, rating) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            connection.setAutoCommit(false);

            for (Review review : reviews) {
                preparedStatement.setLong(1, review.getReviewId());
                preparedStatement.setString(2, review.getReviewText());
                preparedStatement.setString(3, review.getAuthorName());
                preparedStatement.setString(4, review.getReviewSource());
                preparedStatement.setString(5, review.getReviewTitle());
                preparedStatement.setString(6, review.getProductName());
                preparedStatement.setString(7, review.getReviewedDate() != null ? review.getReviewedDate().toString() : null);
                preparedStatement.setInt(8, review.getProductRating());
                preparedStatement.addBatch();
            }

            preparedStatement.executeBatch();
            connection.commit();
        } catch (SQLException sqlException) {
            throw new RuntimeException("Error saving reviews batch", sqlException);
        }
    }

    // ---------------------------------------------------------------------
    // Query port
    // ---------------------------------------------------------------------

    /**
     * Returns a page of reviews ordered by table natural order unless overridden by SQL.
     *
     * @param page     1-based page index
     * @param pageSize number of items per page
     * @return list of reviews (possibly empty)
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public List<Review> getReviewsPage(int page, int pageSize) {
        final List<Review> pagedReviews = new ArrayList<>();
        final String sql = "SELECT * FROM reviews LIMIT ? OFFSET ?";

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, pageSize);
            preparedStatement.setInt(2, (page - 1) * pageSize);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    pagedReviews.add(mapRow(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("SQL error loading paged reviews", sqlException);
        }
        return pagedReviews;
    }

    /**
     * Returns the total count of reviews stored.
     *
     * @return total number of reviews
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public int getTotalReviewCount() {
        final String sql = "SELECT COUNT(*) FROM reviews";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException sqlException) {
            throw new RuntimeException("SQL error counting reviews", sqlException);
        }
    }

    /**
     * Fetches a single review by its identifier.
     *
     * @param id review ID
     * @return the review if found; otherwise {@code null}
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public Review getReviewById(long id) {
        final String sql = "SELECT * FROM reviews WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? mapRow(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error fetching review by id: " + id, e);
        }
    }

    /**
     * Returns a filtered, paged list of reviews.
     * Only non-null/non-blank filter fields are applied.
     * Sorting supports either date-desc or rating-desc (exclusive).
     *
     * @param filters  filter object (non-null)
     * @param page     1-based page index
     * @param pageSize number of items per page
     * @return filtered list (possibly empty)
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public List<Review> getReviewsByFilters(Filters filters, int page, int pageSize) {
        final List<Review> filteredReviews = new ArrayList<>();
        final StringBuilder sql = new StringBuilder("SELECT * FROM reviews WHERE 1=1");
        final List<Object> parameters = new ArrayList<>();

        // Dynamically build WHERE clause
        if (filters.getRating() != null) {
            sql.append(" AND rating = ?");
            parameters.add(filters.getRating());
        }
        if (filters.getMinRating() != null) {
            sql.append(" AND rating >= ?");
            parameters.add(filters.getMinRating());
        }
        if (filters.getMaxRating() != null) {
            sql.append(" AND rating <= ?");
            parameters.add(filters.getMaxRating());
        }
        if (isNotBlank(filters.getAuthorName())) {
            sql.append(" AND author LIKE ?");
            parameters.add("%" + filters.getAuthorName() + "%");
        }
        if (isNotBlank(filters.getReviewTitle())) {
            sql.append(" AND title LIKE ?");
            parameters.add("%" + filters.getReviewTitle() + "%");
        }
        if (isNotBlank(filters.getProductName())) {
            sql.append(" AND productName LIKE ?");
            parameters.add("%" + filters.getProductName() + "%");
        }
        if (filters.getReviewDate() != null) { 
            // Use LIKE to match date even if time is present in DB
            sql.append(" AND reviewedDate LIKE ?");
            parameters.add(filters.getReviewDate().toString() + "%");
        }
        if (isNotBlank(filters.getStoreName())) {
            sql.append(" AND reviewSource LIKE ?");
            parameters.add("%" + filters.getStoreName() + "%");
        }
        if (filters.getStartDate() != null) {
            sql.append(" AND reviewedDate >= ?");
            parameters.add(filters.getStartDate().toString());
        }
        if (filters.getEndDate() != null) {
            sql.append(" AND reviewedDate <= ?");
            parameters.add(filters.getEndDate().toString());
        }
        if (filters.getStartTime() != null) {
            sql.append(" AND time(substr(reviewedDate, 12)) >= ?");
            parameters.add(filters.getStartTime().toString());
        }
        if (filters.getEndTime() != null) {
            sql.append(" AND time(substr(reviewedDate, 12)) <= ?");
            parameters.add(filters.getEndTime().toString());
        }

        // Sorting (exclusive priority: date-desc else rating-desc)
        if (filters.isSortByDate() && filters.isSortByRating()) {
            // First sort by rating DESC, then by date DESC within each rating group
            sql.append(" ORDER BY rating DESC, reviewedDate DESC");
        } else if (filters.isSortByDate()) {
            sql.append(" ORDER BY reviewedDate DESC");
        } else if (filters.isSortByRating()) {
            sql.append(" ORDER BY rating DESC");
        }

        // Pagination
        sql.append(" LIMIT ? OFFSET ?");
        parameters.add(pageSize);
        parameters.add((page - 1) * pageSize);

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    filteredReviews.add(mapRow(resultSet));
                }
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException("SQL error loading filtered reviews", sqlException);
        }
        return filteredReviews;
    }

    /**
     * Returns the count of reviews that match the provided filters.
     *
     * @param filters filter object (non-null)
     * @return count of matching reviews
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public int getFilteredReviewCount(Filters filters) {
        final StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM reviews WHERE 1=1");
        final List<Object> parameters = new ArrayList<>();

        if (filters.getRating() != null) { sql.append(" AND rating = ?"); parameters.add(filters.getRating()); }
        if (filters.getMinRating() != null) { sql.append(" AND rating >= ?"); parameters.add(filters.getMinRating()); }
        if (filters.getMaxRating() != null) { sql.append(" AND rating <= ?"); parameters.add(filters.getMaxRating()); }
        if (isNotBlank(filters.getAuthorName())) { sql.append(" AND author LIKE ?"); parameters.add("%" + filters.getAuthorName() + "%"); }
        if (isNotBlank(filters.getReviewTitle())) { sql.append(" AND title LIKE ?"); parameters.add("%" + filters.getReviewTitle() + "%"); }
        if (isNotBlank(filters.getProductName())) { sql.append(" AND productName LIKE ?"); parameters.add("%" + filters.getProductName() + "%"); }
        if (filters.getReviewDate() != null) { 
            // Use LIKE to match date even if time is present in DB
            sql.append(" AND reviewedDate LIKE ?"); 
            parameters.add(filters.getReviewDate().toString() + "%");
        }
        if (isNotBlank(filters.getStoreName())) { sql.append(" AND reviewSource LIKE ?"); parameters.add("%" + filters.getStoreName() + "%"); }
        if (filters.getStartDate() != null) { sql.append(" AND reviewedDate >= ?"); parameters.add(filters.getStartDate().toString()); }
        if (filters.getEndDate() != null) { sql.append(" AND reviewedDate <= ?"); parameters.add(filters.getEndDate().toString()); }
        if (filters.getStartTime() != null) {
            sql.append(" AND time(substr(reviewedDate, 12)) >= ?");
            parameters.add(filters.getStartTime().toString());
        }
        if (filters.getEndTime() != null) {
            sql.append(" AND time(substr(reviewedDate, 12)) <= ?");
            parameters.add(filters.getEndTime().toString());
        }

        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                preparedStatement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error counting filtered reviews", e);
        }
    }

    /**
     * Executes a keyword search across review body and title (case-insensitive).
     *
     * @param keywords list of keywords; if empty or null, returns an empty list
     * @return list of matched reviews
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public List<Review> getReviewsByKeywords(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return Collections.emptyList();

        final StringBuilder sql = new StringBuilder("SELECT * FROM reviews WHERE ");
        final List<String> conditions = new ArrayList<>();
        for (int i = 0; i < keywords.size(); i++) {
            conditions.add("(LOWER(review) LIKE ? OR LOWER(title) LIKE ?)");
        }
        sql.append(String.join(" OR ", conditions));

        final List<Review> result = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

            int parameterIndex = 1;
            for (String keyword : keywords) {
                final String like = "%" + keyword.toLowerCase() + "%";
                preparedStatement.setString(parameterIndex++, like);
                preparedStatement.setString(parameterIndex++, like);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error searching reviews by keywords", e);
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // Stats port
    // ---------------------------------------------------------------------

    /**
     * Returns total review count (delegates to {@link #getTotalReviewCount()}).
     */
    @Override
    public int getTotalReviewCountStats() {
        return getTotalReviewCount();
    }

    /**
     * Returns the average rating across all reviews (as a SQL {@code AVG} over {@code rating}).
     *
     * @return average rating, or 0.0 if no rows
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public double getAverageRating() {
        final String sql = "SELECT AVG(rating) FROM reviews";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next() ? resultSet.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("SQL error calculating average rating", e);
        }
    }

    /**
     * Returns a map from rating value to count.
     *
     * @return sorted map of rating → count (ascending by rating)
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public Map<Integer, Integer> getRatingDistribution() {
        final Map<Integer, Integer> distribution = new TreeMap<>();
        final String sql = "SELECT rating, COUNT(*) FROM reviews GROUP BY rating";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                distribution.put(resultSet.getInt(1), resultSet.getInt(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error getting rating distribution", e);
        }
        return distribution;
    }

    /**
     * Returns a map from {@code YYYY-MM} to average rating for that month.
     *
     * @return sorted map of month → average rating
     * @throws RuntimeException if a JDBC error occurs
     */
    @Override
    public Map<String, Double> getMonthlyRatingAverage() {
        final Map<String, Double> monthAverage = new TreeMap<>();
        final String sql = "SELECT SUBSTR(reviewedDate,1,7) as month, AVG(rating) " +
                "FROM reviews GROUP BY month ORDER BY month";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                monthAverage.put(resultSet.getString(1), resultSet.getDouble(2));
            }
        } catch (SQLException e) {
            throw new RuntimeException("SQL error getting monthly rating average", e);
        }
        return monthAverage;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /**
     * Simple "not blank" check used when building WHERE clauses.
     */
    private static boolean isNotBlank(String text) {
        return text != null && !text.isEmpty();
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Review} domain object.
     * Assumes column names match the table DDL.
     */
    private static Review mapRow(ResultSet resultSet) throws SQLException {
        return new Review.Builder()
                .setReviewId(resultSet.getLong("id"))
                .setReviewText(resultSet.getString("review"))
                .setAuthorName(resultSet.getString("author"))
                .setReviewSource(resultSet.getString("reviewSource"))
                .setReviewTitle(resultSet.getString("title"))
                .setProductName(resultSet.getString("productName"))
                .setReviewedDate(parseFlexibleDate(resultSet.getString("reviewedDate")))
                .setProductRating(resultSet.getInt("rating"))
                .build();
    }

    /**
     * Parses a date string that may be {@code yyyy-MM-dd} or longer (e.g., ISO date-time).
     * Only the first 10 chars (date part) are considered; returns {@code null} on failure.
     */
    private static LocalDate parseFlexibleDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            return LocalDate.parse(dateString.substring(0, 10));
        } catch (Exception ignore) {
            return null;
        }
    }
}
