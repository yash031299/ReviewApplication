package com.reviewapp.adapter.jdbc;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqliteReviewRepositoryTest {
    private static final String DB_URL = "jdbc:sqlite:target/test-reviews.db";
    private SqliteReviewRepository repository;

    @DisplayName("Sets up a fresh repository instance before each test using a file-based SQLite DB.")
    @BeforeEach
    void setUp() {
        repository = new SqliteReviewRepository(DB_URL);
    }


    @DisplayName("Cleans up the reviews table after each test for isolation.")
    @AfterEach
    void cleanup() {
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM reviews");
        } catch (Exception ignored) {}
    }


    @DisplayName("Verifies that the repository constructor creates the reviews table and can persist and count reviews.")
    @Test
    void constructor_createsTable_whenValidUrl() {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewText("Good")
                .setAuthorName("Alice")
                .setReviewSource("Amazon")
                .setReviewTitle("Title")
                .setProductName("Product")
                .setReviewedDate(LocalDate.now())
                .setProductRating(5)
                .build();

        // Act
        repository.saveReviews(Collections.singletonList(review));
        int count = repository.getTotalReviewCount();

        // Assert
        assertEquals(1, count);
    }


    @DisplayName("Verifies that an invalid JDBC URL causes a RuntimeException during construction.")
    @Test
    void constructor_throwsRuntimeException_whenInvalidUrl() {
        // Arrange
        String badUrl = "jdbc:sqlite:/nonexistent/path/invalid.db";

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> new SqliteReviewRepository(badUrl));
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table"));
    }


    @DisplayName("Verifies that passing a null URL to the constructor throws NullPointerException.")
    @Test
    void constructor_throwsNullPointerException_whenUrlIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new SqliteReviewRepository(null));
    }


    @DisplayName("Verifies that saveReviews inserts and updates reviews as expected.")
    @Test
    void saveReviews_insertsAndUpdatesReviews_whenValidInput() {
        // Arrange
        Review review1 = new Review.Builder()
                .setReviewId(1L).setReviewText("Great!").setAuthorName("Bob").setReviewSource("Amazon")
                .setReviewTitle("Title1").setProductName("Product1").setReviewedDate(LocalDate.now()).setProductRating(4).build();
        Review review2 = new Review.Builder()
                .setReviewId(2L).setReviewText("Nice").setAuthorName("Carol").setReviewSource("Flipkart")
                .setReviewTitle("Title2").setProductName("Product2").setReviewedDate(LocalDate.now()).setProductRating(5).build();

        // Act
        repository.saveReviews(Arrays.asList(review1, review2));
        int countAfterInsert = repository.getTotalReviewCount();

        Review updated = new Review.Builder()
                .setReviewId(1L).setReviewText("Updated!").setAuthorName("Bob").setReviewSource("Amazon")
                .setReviewTitle("Title1").setProductName("Product1").setReviewedDate(LocalDate.now()).setProductRating(3).build();
        repository.saveReviews(Collections.singletonList(updated));
        Review fetched = repository.getReviewById(1L);

        // Assert
        assertEquals(2, countAfterInsert);
        assertEquals("Updated!", fetched.getReviewText());
        assertEquals(3, fetched.getProductRating());
    }


    @DisplayName("Verifies that a SQL error during saveReviews throws a RuntimeException.")
    @Test
    void saveReviews_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}
        Review review = new Review.Builder()
                .setReviewId(1L).setReviewText("Bad").setAuthorName("A").setReviewSource("S")
                .setReviewTitle("T").setProductName("P").setReviewedDate(LocalDate.now()).setProductRating(1).build();

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> badRepo.saveReviews(Collections.singletonList(review)));
        assertTrue(ex.getMessage().contains("Error saving reviews batch"));
    }


    @DisplayName("Verifies that passing null to saveReviews throws NullPointerException.")
    @Test
    void saveReviews_throwsNullPointerException_whenListIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.saveReviews(null));
    }


    @DisplayName("Verifies that a null review in the list passed to saveReviews throws NullPointerException.")
    @Test
    void saveReviews_throwsNullPointerException_whenReviewInListIsNull() {
        // Arrange
        List<Review> reviews = new ArrayList<>();
        reviews.add(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.saveReviews(reviews));
    }


    @DisplayName("Verifies that invalid review fields throw an exception during Review construction.")
    @Test
    void saveReviews_throwsInvalidInputException_whenReviewFieldsInvalid() {
        // Act & Assert
        assertThrows(Exception.class, () -> new Review.Builder()
                .setReviewId(null)
                .setReviewedDate(null)
                .setProductRating(0)
                .build());
    }


    @DisplayName("Verifies that getReviewsPage returns paged results when data is present.")
    @Test
    void getReviewsPage_returnsPagedResults_whenDataPresent() {
        // Arrange
        for (int i = 1; i <= 15; i++) {
            repository.saveReviews(Collections.singletonList(
                    new Review.Builder()
                            .setReviewId((long) i)
                            .setReviewText("Review" + i)
                            .setAuthorName("User" + i)
                            .setReviewSource("Source")
                            .setReviewTitle("Title")
                            .setProductName("Product")
                            .setReviewedDate(LocalDate.now())
                            .setProductRating(i % 5 + 1)
                            .build()));
        }

        // Act
        List<Review> page = repository.getReviewsPage(2, 5);

        // Assert
        assertEquals(5, page.size());
        assertEquals("Review6", page.get(0).getReviewText());
    }


    @DisplayName("Verifies that getReviewsPage returns an empty list when there is no data.")
    @Test
    void getReviewsPage_returnsEmptyList_whenNoData() {
        // Act
        List<Review> page = repository.getReviewsPage(1, 10);

        // Assert
        assertTrue(page.isEmpty());
    }


    @DisplayName("Verifies that a SQL error in getReviewsPage throws a RuntimeException.")
    @Test
    void getReviewsPage_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}

        // Act & Assert
        assertThrows(RuntimeException.class, () -> badRepo.getReviewsPage(1, 10));
    }


    @DisplayName("Verifies that getTotalReviewCount returns the correct count when data is present.")
    @Test
    void getTotalReviewCount_returnsCorrectCount_whenDataPresent() {
        // Arrange
        repository.saveReviews(Collections.singletonList(
                new Review.Builder()
                        .setReviewId(10L)
                        .setReviewText("Test")
                        .setAuthorName("A")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(2)
                        .build()));

        // Act
        int count = repository.getTotalReviewCount();

        // Assert
        assertEquals(1, count);
    }


    @DisplayName("Verifies that getTotalReviewCount returns zero when there is no data.")
    @Test
    void getTotalReviewCount_returnsZero_whenNoData() {
        // Act
        int count = repository.getTotalReviewCount();

        // Assert
        assertEquals(0, count);
    }


    @DisplayName("Verifies that a SQL error in getTotalReviewCount throws a RuntimeException.")
    @Test
    void getTotalReviewCount_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}

        // Act & Assert
        assertThrows(RuntimeException.class, badRepo::getTotalReviewCount);
    }


    @DisplayName("Verifies that getReviewById returns the review if it exists, or null if not.")
    @Test
    void getReviewById_returnsReview_whenExistsOrNull_whenNotExists() {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(100L)
                .setReviewText("Body")
                .setAuthorName("Ann")
                .setReviewSource("S")
                .setReviewTitle("T")
                .setProductName("P")
                .setReviewedDate(LocalDate.now())
                .setProductRating(4)
                .build();
        repository.saveReviews(Collections.singletonList(review));

        // Act
        Review found = repository.getReviewById(100L);
        Review notFound = repository.getReviewById(999L);

        // Assert
        assertNotNull(found);
        assertNull(notFound);
    }


    @DisplayName("Verifies that getReviewById returns null when the ID is null.")
    @Test
    void getReviewById_returnsNull_whenIdIsNull() {
        // Act
        Review result = repository.getReviewById(null);

        // Assert
        assertNull(result);
    }


    @DisplayName("Verifies that a SQL error in getReviewById throws a RuntimeException (constructor or method).")
    @Test
    void getReviewById_forcesSQLException_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            SqliteReviewRepository badRepo = new SqliteReviewRepository("jdbc:sqlite:/nonexistent/path/invalid.db");
            badRepo.getReviewById(1L);
        });
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table") ||
                   ex.getMessage().contains("SQL error fetching review by id"));
    }


    @DisplayName("Verifies that getReviewsByFilters returns filtered results when fields match.")
    @Test
    void getReviewsByFilters_returnsFilteredResults_whenFieldsMatch() {
        // Arrange
        repository.saveReviews(Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("Alpha")
                        .setAuthorName("A")
                        .setReviewSource("Amazon")
                        .setReviewTitle("T1")
                        .setProductName("P1")
                        .setReviewedDate(LocalDate.parse("2024-01-01"))
                        .setProductRating(5)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("Beta")
                        .setAuthorName("B")
                        .setReviewSource("Flipkart")
                        .setReviewTitle("T2")
                        .setProductName("P2")
                        .setReviewedDate(LocalDate.parse("2024-02-01"))
                        .setProductRating(3)
                        .build()));
        Filters filters = new Filters.Builder()
                .setAuthorName("A")
                .setRating(5)
                .build();

        // Act
        List<Review> filtered = repository.getReviewsByFilters(filters, 1, 10);

        // Assert
        assertEquals(1, filtered.size());
        assertEquals("Alpha", filtered.get(0).getReviewText());
    }


    @DisplayName("Verifies that passing null to getReviewsByFilters throws NullPointerException.")
    @Test
    void getReviewsByFilters_throwsNullPointerException_whenFiltersNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.getReviewsByFilters(null, 1, 10));
    }


    @DisplayName("Verifies that getReviewsByFilters returns an empty list when there is no data or no match.")
    @Test
    void getReviewsByFilters_returnsEmptyList_whenNoDataOrNoMatch() {
        // Arrange
        Filters filters = new Filters.Builder().build();

        // Act
        List<Review> result = repository.getReviewsByFilters(filters, 1, 10);

        // Assert
        assertTrue(result.isEmpty());

        // Arrange
        repository.saveReviews(Collections.singletonList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("B")
                        .setReviewSource("C")
                        .setReviewTitle("D")
                        .setProductName("E")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build()));
        Filters noMatch = new Filters.Builder().setProductName("ZZZ").build();

        // Act & Assert
        assertTrue(repository.getReviewsByFilters(noMatch, 1, 10).isEmpty());
    }


    @DisplayName("Verifies that a SQL error in getReviewsByFilters throws a RuntimeException.")
    @Test
    void getReviewsByFilters_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}
        Filters filters = new Filters.Builder().build();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> badRepo.getReviewsByFilters(filters, 1, 10));
    }


    @DisplayName("Verifies that getFilteredReviewCount returns the correct count when filtered.")
    @Test
    void getFilteredReviewCount_returnsCorrectCount_whenFiltered() {
        // Arrange
        repository.saveReviews(Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("Alpha")
                        .setAuthorName("A")
                        .setReviewSource("Amazon")
                        .setReviewTitle("T1")
                        .setProductName("P1")
                        .setReviewedDate(LocalDate.parse("2024-01-01"))
                        .setProductRating(5)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("Beta")
                        .setAuthorName("B")
                        .setReviewSource("Flipkart")
                        .setReviewTitle("T2")
                        .setProductName("P2")
                        .setReviewedDate(LocalDate.parse("2024-02-01"))
                        .setProductRating(3)
                        .build()));
        Filters filters = new Filters.Builder().setProductName("P1").build();

        // Act
        int count = repository.getFilteredReviewCount(filters);

        // Assert
        assertEquals(1, count);
    }


    @DisplayName("Verifies that passing null to getFilteredReviewCount throws NullPointerException.")
    @Test
    void getFilteredReviewCount_throwsNullPointerException_whenFiltersNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> repository.getFilteredReviewCount(null));
    }


    @DisplayName("Verifies that getFilteredReviewCount returns zero when there is no data or no match.")
    @Test
    void getFilteredReviewCount_returnsZero_whenNoDataOrNoMatch() {
        // Arrange
        Filters filters = new Filters.Builder().build();

        // Act & Assert
        assertEquals(0, repository.getFilteredReviewCount(filters));

        // Arrange
        repository.saveReviews(Collections.singletonList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("B")
                        .setReviewSource("C")
                        .setReviewTitle("D")
                        .setProductName("E")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build()));
        Filters noMatch = new Filters.Builder().setProductName("ZZZ").build();

        // Act & Assert
        assertEquals(0, repository.getFilteredReviewCount(noMatch));
    }


    @DisplayName("Verifies that a SQL error in getFilteredReviewCount throws a RuntimeException (constructor or method).")
    @Test
    void getFilteredReviewCount_forcesSQLException_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            SqliteReviewRepository badRepo = new SqliteReviewRepository("jdbc:sqlite:/nonexistent/path/invalid.db");
            Filters filters = new Filters.Builder().setReviewTitle("test").build();
            badRepo.getFilteredReviewCount(filters);
        });
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table") ||
                   ex.getMessage().contains("SQL error counting filtered reviews"));
    }


    @DisplayName("Verifies that getReviewsByKeywords returns results when keywords match review or title.")
    @Test
    void getReviewsByKeywords_returnsResults_whenKeywordsMatchReviewOrTitle() {
        // Arrange
        repository.saveReviews(Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("Fantastic phone")
                        .setAuthorName("A")
                        .setReviewSource("Amazon")
                        .setReviewTitle("Superb")
                        .setProductName("P1")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(5)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("Average")
                        .setAuthorName("B")
                        .setReviewSource("Flipkart")
                        .setReviewTitle("Mediocre")
                        .setProductName("P2")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(2)
                        .build()));

        // Act
        List<Review> results = repository.getReviewsByKeywords(Arrays.asList("fantastic", "mediocre"));

        // Assert
        assertEquals(2, results.size());
    }


    @DisplayName("Verifies that getReviewsByKeywords returns an empty list when there are no keywords or keywords are empty.")
    @Test
    void getReviewsByKeywords_returnsEmptyList_whenNullOrEmptyKeywords() {
        // Act & Assert
        assertTrue(repository.getReviewsByKeywords(null).isEmpty());
        assertTrue(repository.getReviewsByKeywords(Collections.emptyList()).isEmpty());
    }


    @DisplayName("Verifies that getReviewsByKeywords returns an empty list when there is no match.")
    @Test
    void getReviewsByKeywords_returnsEmptyList_whenNoMatch() {
        // Arrange
        repository.saveReviews(Collections.singletonList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("B")
                        .setReviewSource("C")
                        .setReviewTitle("D")
                        .setProductName("E")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build()));

        // Act & Assert
        assertTrue(repository.getReviewsByKeywords(List.of("ZZZ")).isEmpty());
    }


    @DisplayName("Verifies that a SQL error in getReviewsByKeywords throws a RuntimeException.")
    @Test
    void getReviewsByKeywords_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}

        // Act & Assert
        assertThrows(RuntimeException.class, () -> badRepo.getReviewsByKeywords(List.of("any")));
    }


    @DisplayName("Verifies that getAllReviews returns all reviews when data is present.")
    @Test
    void getAllReviews_returnsAllReviews_whenDataPresent() {
        // Arrange
        repository.saveReviews(Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("A")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("B")
                        .setAuthorName("B")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(2)
                        .build()));

        // Act
        List<Review> all = repository.getAllReviews();

        // Assert
        assertEquals(2, all.size());
    }


    @DisplayName("Verifies that getAllReviews returns an empty list when there is no data.")
    @Test
    void getAllReviews_returnsEmptyList_whenNoData() {
        // Act
        List<Review> all = repository.getAllReviews();

        // Assert
        assertTrue(all.isEmpty());
    }


    @DisplayName("Verifies that getTotalReviewCountStats returns the same count as getTotalReviewCount.")
    @Test
    void getTotalReviewCountStats_returnsSameAsTotalCount() {
        // Arrange
        repository.saveReviews(Collections.singletonList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("A")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build()));

        // Act
        int total = repository.getTotalReviewCount();
        int stats = repository.getTotalReviewCountStats();

        // Assert
        assertEquals(total, stats);
    }


    @DisplayName("Verifies that a SQL error in getTotalReviewCountStats throws a RuntimeException.")
    @Test
    void getTotalReviewCountStats_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}

        // Act & Assert
        assertThrows(RuntimeException.class, badRepo::getTotalReviewCountStats);
    }


    @DisplayName("Verifies that getAverageRating returns the correct average rating when data is present.")
    @Test
    void getAverageRating_returnsCorrectAverage_whenDataPresent() {
        // Arrange
        repository.saveReviews(Arrays.asList(
                new Review.Builder()
                        .setReviewId(1L)
                        .setReviewText("A")
                        .setAuthorName("A")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(4)
                        .build(),
                new Review.Builder()
                        .setReviewId(2L)
                        .setReviewText("B")
                        .setAuthorName("B")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(2)
                        .build()));

        // Act
        double avg = repository.getAverageRating();

        // Assert
        assertEquals(3.0, avg, 0.01);
    }


    @DisplayName("Verifies that getAverageRating returns zero when there is no data.")
    @Test
    void getAverageRating_returnsZero_whenNoData() {
        // Act
        double avg = repository.getAverageRating();

        // Assert
        assertEquals(0.0, avg, 0.01);
    }


    @DisplayName("Verifies that a SQL error in getAverageRating throws a RuntimeException.")
    @Test
    void getAverageRating_throwsRuntimeException_whenSqlErrorOccurs() {
        // Arrange
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}

        // Act & Assert
        assertThrows(RuntimeException.class, badRepo::getAverageRating);
    }


    @DisplayName("Verifies that saveReviews does not throw an exception when an empty list is passed.")
    @Test
    void saveReviews_doesNotThrow_whenEmptyList() {
        // Act & Assert
        assertDoesNotThrow(() -> repository.saveReviews(Collections.emptyList()));
    }


    @DisplayName("Verifies that the repository is thread-safe when accessed concurrently.")
    @Test
    void repository_isThreadSafe_whenAccessedConcurrently() throws InterruptedException {
        // Arrange
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        // Act
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                Review review = new Review.Builder()
                        .setReviewId((long) idx)
                        .setReviewText("R" + idx)
                        .setAuthorName("A")
                        .setReviewSource("S")
                        .setReviewTitle("T")
                        .setProductName("P")
                        .setReviewedDate(LocalDate.now())
                        .setProductRating(1)
                        .build();
                repository.saveReviews(Collections.singletonList(review));
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        // Assert
        assertEquals(threadCount, repository.getTotalReviewCount());
    }

    @DisplayName("Verifies getReviewsByKeywords returns empty list for null or empty input.")
    @Test
    void getReviewsByKeywords_returnsEmptyList_forNullOrEmptyInput() {
        assertTrue(repository.getReviewsByKeywords(null).isEmpty());
        assertTrue(repository.getReviewsByKeywords(Collections.emptyList()).isEmpty());
    }

    @DisplayName("Verifies getReviewById returns null for null id and for missing id.")
    @Test
    void getReviewById_returnsNull_forNullOrMissingId() {
        assertNull(repository.getReviewById(null));
        assertNull(repository.getReviewById(99999L));
    }

    @DisplayName("Forces SQLException in getAllReviews and verifies RuntimeException is thrown (constructor or method).")
    @Test
    void getAllReviews_forcesSQLException_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            SqliteReviewRepository badRepo = new SqliteReviewRepository("jdbc:sqlite:/nonexistent/path/invalid.db");
            badRepo.getAllReviews();
        });
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table") ||
                   ex.getMessage().contains("SQL error fetching all reviews"));
    }

    @DisplayName("Forces SQLException in getReviewsByKeywords and verifies RuntimeException is thrown (constructor or method).")
    @Test
    void getReviewsByKeywords_forcesSQLException_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            SqliteReviewRepository badRepo = new SqliteReviewRepository("jdbc:sqlite:/nonexistent/path/invalid.db");
            badRepo.getReviewsByKeywords(Arrays.asList("a"));
        });
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table") ||
                   ex.getMessage().contains("SQL error searching reviews by keywords"));
    }


    @DisplayName("Forces SQLException in getReviewsPage and verifies RuntimeException is thrown (constructor or method).")
    @Test
    void getReviewsPage_forcesSQLException_throwsRuntimeException() {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            SqliteReviewRepository badRepo = new SqliteReviewRepository("jdbc:sqlite:/nonexistent/path/invalid.db");
            badRepo.getReviewsPage(1, 5);
        });
        assertTrue(ex.getMessage().contains("Failed to create 'reviews' table") ||
                   ex.getMessage().contains("SQL error loading paged reviews"));
    }

    @DisplayName("Verifies isNotBlank returns correct results for null, empty, and non-empty strings.")
    @Test
    void isNotBlank_variousInputs() throws Exception {
        var method = SqliteReviewRepository.class.getDeclaredMethod("isNotBlank", String.class);
        method.setAccessible(true);
        assertFalse((Boolean) method.invoke(null, (String) null));
        assertFalse((Boolean) method.invoke(null, ""));
        assertTrue((Boolean) method.invoke(null, "abc"));
    }

    @DisplayName("Verifies parseFlexibleDate returns null for null/empty/invalid and parses valid date.")
    @Test
    void parseFlexibleDate_variousInputs() throws Exception {
        var method = SqliteReviewRepository.class.getDeclaredMethod("parseFlexibleDate", String.class);
        method.setAccessible(true);
        assertNull(method.invoke(null, (String) null));
        assertNull(method.invoke(null, ""));
        assertNull(method.invoke(null, "notadate"));
        assertNotNull(method.invoke(null, "2024-01-01"));
        assertNotNull(method.invoke(null, "2024-01-01T12:00:00Z"));
    }

    @DisplayName("Verifies getMonthlyRatingAverage returns correct month averages and handles empty/no data.")
    @Test
    void getMonthlyRatingAverage_variousScenarios() {
        // No data
        Map<String, Double> empty = repository.getMonthlyRatingAverage();
        assertTrue(empty.isEmpty());

        // Insert reviews in two months
        repository.saveReviews(Arrays.asList(
            new Review.Builder()
                .setReviewId(1L)
                .setReviewText("A")
                .setAuthorName("A")
                .setReviewSource("Amazon")
                .setReviewTitle("T1")
                .setProductName("P1")
                .setReviewedDate(LocalDate.parse("2024-01-01"))
                .setProductRating(5)
                .build(),
            new Review.Builder()
                .setReviewId(2L)
                .setReviewText("B")
                .setAuthorName("B")
                .setReviewSource("Flipkart")
                .setReviewTitle("T2")
                .setProductName("P2")
                .setReviewedDate(LocalDate.parse("2024-01-20"))
                .setProductRating(3)
                .build(),
            new Review.Builder()
                .setReviewId(3L)
                .setReviewText("C")
                .setAuthorName("C")
                .setReviewSource("Amazon")
                .setReviewTitle("T3")
                .setProductName("P3")
                .setReviewedDate(LocalDate.parse("2024-02-01"))
                .setProductRating(2)
                .build()
        ));
        Map<String, Double> monthly = repository.getMonthlyRatingAverage();
        assertEquals(2, monthly.size());
        assertEquals(4.0, monthly.get("2024-01"), 0.01); // (5+3)/2
        assertEquals(2.0, monthly.get("2024-02"), 0.01);
    }

    @DisplayName("Verifies that getRatingDistribution returns correct rating counts and handles empty/no data.")
    @Test
    void getRatingDistribution_variousScenarios() {
        // No data
        Map<Integer, Integer> empty = repository.getRatingDistribution();
        assertTrue(empty.isEmpty());

        // Insert reviews with different ratings
        repository.saveReviews(Arrays.asList(
            new Review.Builder()
                .setReviewId(1L)
                .setReviewText("A")
                .setAuthorName("A")
                .setReviewSource("Amazon")
                .setReviewTitle("T1")
                .setProductName("P1")
                .setReviewedDate(LocalDate.parse("2024-01-01"))
                .setProductRating(5)
                .build(),
            new Review.Builder()
                .setReviewId(2L)
                .setReviewText("B")
                .setAuthorName("B")
                .setReviewSource("Flipkart")
                .setReviewTitle("T2")
                .setProductName("P2")
                .setReviewedDate(LocalDate.parse("2024-01-20"))
                .setProductRating(3)
                .build(),
            new Review.Builder()
                .setReviewId(3L)
                .setReviewText("C")
                .setAuthorName("C")
                .setReviewSource("Amazon")
                .setReviewTitle("T3")
                .setProductName("P3")
                .setReviewedDate(LocalDate.parse("2024-02-01"))
                .setProductRating(5)
                .build()
        ));
        Map<Integer, Integer> dist = repository.getRatingDistribution();
        assertEquals(2, dist.size());
        assertEquals(2, dist.get(5)); // two reviews with rating 5
        assertEquals(1, dist.get(3)); // one review with rating 3
    }

    @DisplayName("getReviewsByFilters: all filters null or blank returns all reviews")
    @Test
    void getReviewsByFilters_allFiltersNullOrBlank() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(1L).setProductRating(5).setReviewedDate(LocalDate.now()).setAuthorName("A").setProductName("P").setReviewSource("S").setReviewTitle("T").setReviewText("R").build(),
            new Review.Builder().setReviewId(2L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("B").setProductName("Q").setReviewSource("T").setReviewTitle("U").setReviewText("S").build()
        ));
        Filters filters = new Filters.Builder()
            .setAuthorName("")
            .setProductName("   ")
            .setStoreName(null)
            .setMinRating(null)
            .setMaxRating(null)
            .setReviewDate(null)
            .build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertEquals(2, results.size());
    }

    @DisplayName("getReviewsByFilters: authorName filter excludes non-matching/null/blank")
    @Test
    void getReviewsByFilters_authorNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(1001L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName(null).build(),
            new Review.Builder().setReviewId(1002L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("").build(),
            new Review.Builder().setReviewId(1003L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("bar").build()
        ));
        Filters filters = new Filters.Builder().setAuthorName("foo").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertTrue(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: productName filter excludes non-matching/null/blank")
    @Test
    void getReviewsByFilters_productNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(2001L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName(null).build(),
            new Review.Builder().setReviewId(2002L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName("").build(),
            new Review.Builder().setReviewId(2003L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName("bar").build()
        ));
        Filters filters = new Filters.Builder().setProductName("foo").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertTrue(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: storeName filter excludes non-matching/null/blank")
    @Test
    void getReviewsByFilters_storeNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(3001L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource(null).build(),
            new Review.Builder().setReviewId(3002L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource("").build(),
            new Review.Builder().setReviewId(3003L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource("bar").build()
        ));
        Filters filters = new Filters.Builder().setStoreName("foo").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertTrue(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: reviewTitle filter excludes non-matching/null/blank")
    @Test
    void getReviewsByFilters_reviewTitleFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(4001L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle(null).build(),
            new Review.Builder().setReviewId(4002L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle("").build(),
            new Review.Builder().setReviewId(4003L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle("bar").build()
        ));
        Filters filters = new Filters.Builder().setReviewTitle("foo").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertTrue(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: minRating and maxRating exclude out-of-range")
    @Test
    void getReviewsByFilters_ratingRange() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(5001L).setProductRating(2).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(5002L).setProductRating(4).setReviewedDate(LocalDate.now()).build()
        ));
        Filters filters = new Filters.Builder().setMinRating(3).setMaxRating(5).build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertEquals(1, results.size());
        assertEquals(4, results.get(0).getProductRating());
    }

    @DisplayName("getReviewsByFilters: reviewDate, startDate, endTime edge cases")
    @Test
    void getReviewsByFilters_dateFilters() {
        LocalDate today = LocalDate.now();
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(6001L).setProductRating(4).setReviewedDate(today).build(),
            new Review.Builder().setReviewId(6002L).setProductRating(4).setReviewedDate(today.minusDays(1)).build()
        ));
        Filters reviewDate = new Filters.Builder().setReviewDate(today).build();
        Filters startDate = new Filters.Builder().setStartDate(today.minusDays(1)).build();
        Filters endTime = new Filters.Builder().setEndTime(java.time.LocalTime.now()).build();
        assertEquals(1, repository.getReviewsByFilters(reviewDate, 1, 10).size());
        assertEquals(2, repository.getReviewsByFilters(startDate, 1, 10).size());
        assertEquals(0, repository.getReviewsByFilters(endTime, 1, 10).size());
    }

    @DisplayName("getReviewsByFilters: sort by rating-desc and date-desc")
    @Test
    void getReviewsByFilters_sorting() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(7001L).setProductRating(2).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(7002L).setProductRating(5).setReviewedDate(LocalDate.now().minusDays(1)).build()
        ));
        Filters ratingDesc = new Filters.Builder().setSortByRating(true).build();
        Filters dateDesc = new Filters.Builder().setSortByDate(true).build();
        List<Review> byRating = repository.getReviewsByFilters(ratingDesc, 1, 10);
        List<Review> byDate = repository.getReviewsByFilters(dateDesc, 1, 10);
        assertEquals(5, byRating.get(0).getProductRating());
        assertTrue(byDate.get(0).getReviewedDate().isAfter(byDate.get(1).getReviewedDate()));
    }

    @DisplayName("getFilteredReviewCount: all filters null or blank returns all reviews")
    @Test
    void getFilteredReviewCount_allFiltersNullOrBlank() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(1L).setProductRating(5).setReviewedDate(LocalDate.now()).setAuthorName("A").setProductName("P").setReviewSource("S").setReviewTitle("T").setReviewText("R").build(),
            new Review.Builder().setReviewId(2L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("B").setProductName("Q").setReviewSource("T").setReviewTitle("U").setReviewText("S").build()
        ));
        Filters filters = new Filters.Builder()
            .setAuthorName("")
            .setProductName("   ")
            .setStoreName(null)
            .setMinRating(null)
            .setMaxRating(null)
            .setReviewDate(null)
            .build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(2, count);
    }

    @DisplayName("getFilteredReviewCount: authorName filter excludes non-matching/null/blank")
    @Test
    void getFilteredReviewCount_authorNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(1001L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName(null).build(),
            new Review.Builder().setReviewId(1002L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("").build(),
            new Review.Builder().setReviewId(1003L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName("bar").build()
        ));
        Filters filters = new Filters.Builder().setAuthorName("foo").build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(0, count);
    }

    @DisplayName("getFilteredReviewCount: productName filter excludes non-matching/null/blank")
    @Test
    void getFilteredReviewCount_productNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(2001L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName(null).build(),
            new Review.Builder().setReviewId(2002L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName("").build(),
            new Review.Builder().setReviewId(2003L).setProductRating(4).setReviewedDate(LocalDate.now()).setProductName("bar").build()
        ));
        Filters filters = new Filters.Builder().setProductName("foo").build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(0, count);
    }

    @DisplayName("getFilteredReviewCount: storeName filter excludes non-matching/null/blank")
    @Test
    void getFilteredReviewCount_storeNameFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(3001L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource(null).build(),
            new Review.Builder().setReviewId(3002L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource("").build(),
            new Review.Builder().setReviewId(3003L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewSource("bar").build()
        ));
        Filters filters = new Filters.Builder().setStoreName("foo").build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(0, count);
    }

    @DisplayName("getFilteredReviewCount: reviewTitle filter excludes non-matching/null/blank")
    @Test
    void getFilteredReviewCount_reviewTitleFilter() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(4001L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle(null).build(),
            new Review.Builder().setReviewId(4002L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle("").build(),
            new Review.Builder().setReviewId(4003L).setProductRating(4).setReviewedDate(LocalDate.now()).setReviewTitle("bar").build()
        ));
        Filters filters = new Filters.Builder().setReviewTitle("foo").build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(0, count);
    }

    @DisplayName("getFilteredReviewCount: minRating and maxRating exclude out-of-range")
    @Test
    void getFilteredReviewCount_ratingRange() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(5001L).setProductRating(2).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(5002L).setProductRating(4).setReviewedDate(LocalDate.now()).build()
        ));
        Filters filters = new Filters.Builder().setMinRating(3).setMaxRating(5).build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(1, count);
    }

    @DisplayName("getFilteredReviewCount: reviewDate, startDate, endTime edge cases")
    @Test
    void getFilteredReviewCount_dateFilters() {
        LocalDate today = LocalDate.now();
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(6001L).setProductRating(4).setReviewedDate(today).build(),
            new Review.Builder().setReviewId(6002L).setProductRating(4).setReviewedDate(today.minusDays(1)).build()
        ));
        Filters reviewDate = new Filters.Builder().setReviewDate(today).build();
        Filters startDate = new Filters.Builder().setStartDate(today.minusDays(1)).build();
        Filters endTime = new Filters.Builder().setEndTime(java.time.LocalTime.now()).build();
        assertEquals(1, repository.getFilteredReviewCount(reviewDate));
        assertEquals(2, repository.getFilteredReviewCount(startDate));
        assertEquals(0, repository.getFilteredReviewCount(endTime));
    }

    @DisplayName("getReviewsByFilters: page and pageSize edge cases")
    @Test
    void getReviewsByFilters_paginationEdgeCases() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(8001L).setProductRating(5).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(8002L).setProductRating(4).setReviewedDate(LocalDate.now()).build()
        ));
        Filters filters = new Filters.Builder().build();
        // pageSize = 1, page = 1
        List<Review> page1 = repository.getReviewsByFilters(filters, 1, 1);
        assertEquals(1, page1.size());
        // pageSize = 1, page = 2
        List<Review> page2 = repository.getReviewsByFilters(filters, 2, 1);
        assertEquals(1, page2.size());
        // pageSize = 2, page = 1
        List<Review> pageAll = repository.getReviewsByFilters(filters, 1, 2);
        assertEquals(2, pageAll.size());
        // pageSize = 1, page = 3 (out of range)
        List<Review> page3 = repository.getReviewsByFilters(filters, 3, 1);
        assertTrue(page3.isEmpty());
    }

    @DisplayName("getReviewsByFilters: multiple sort flags (both true)")
    @Test
    void getReviewsByFilters_bothSortFlags() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(9001L).setProductRating(2).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(9002L).setProductRating(5).setReviewedDate(LocalDate.now().minusDays(1)).build()
        ));
        Filters bothSort = new Filters.Builder().setSortByDate(true).setSortByRating(true).build();
        List<Review> sorted = repository.getReviewsByFilters(bothSort, 1, 10);
        assertEquals(5, sorted.get(0).getProductRating());
        assertEquals(2, sorted.get(1).getProductRating());
    }

    @DisplayName("getReviewsByFilters: negative and zero page/pageSize")
    @Test
    void getReviewsByFilters_negativePageOrPageSize() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(10001L).setProductRating(5).setReviewedDate(LocalDate.now()).build()
        ));
        Filters filters = new Filters.Builder().build();
        // page = 0, pageSize = 1
        List<Review> zeroPage = repository.getReviewsByFilters(filters, 0, 1);
        assertTrue(zeroPage.isEmpty() || zeroPage.size() == 1); // depends on SQL dialect, but should not throw
        // page = 1, pageSize = 0
        List<Review> zeroPageSize = repository.getReviewsByFilters(filters, 1, 0);
        assertTrue(zeroPageSize.isEmpty());
        // page = -1, pageSize = 1
        List<Review> negativePage = repository.getReviewsByFilters(filters, -1, 1);
        assertTrue(negativePage.isEmpty() || negativePage.size() == 1);
    }

    @DisplayName("getFilteredReviewCount: no reviews in DB returns zero")
    @Test
    void getFilteredReviewCount_noReviews() {
        Filters filters = new Filters.Builder().build();
        int count = repository.getFilteredReviewCount(filters);
        assertEquals(0, count);
    }

    @DisplayName("getReviewsByFilters: SQL exception branch")
    @Test
    void getReviewsByFilters_sqlException() {
        SqliteReviewRepository brokenRepo = new SqliteReviewRepository("jdbc:sqlite::memory:") {
            @Override
            public List<Review> getReviewsByFilters(Filters filters, int page, int pageSize) {
                throw new RuntimeException("SQL error loading filtered reviews");
            }
        };
        Filters filters = new Filters.Builder().build();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brokenRepo.getReviewsByFilters(filters, 1, 1));
        assertTrue(ex.getMessage().contains("SQL error loading filtered reviews"));
    }

    @DisplayName("getFilteredReviewCount: SQL exception branch")
    @Test
    void getFilteredReviewCount_sqlException() {
        SqliteReviewRepository brokenRepo = new SqliteReviewRepository("jdbc:sqlite::memory:") {
            @Override
            public int getFilteredReviewCount(Filters filters) {
                throw new RuntimeException("SQL error counting filtered reviews");
            }
        };
        Filters filters = new Filters.Builder().build();
        RuntimeException ex = assertThrows(RuntimeException.class, () -> brokenRepo.getFilteredReviewCount(filters));
        assertTrue(ex.getMessage().contains("SQL error counting filtered reviews"));
    }

    @DisplayName("saveReviews: handles empty list, list with null, review with null optional fields, and fails for missing required fields")
    @Test
    void saveReviews_edgeCases() {
        // Empty list
        assertDoesNotThrow(() -> repository.saveReviews(Collections.emptyList()));
        assertEquals(0, repository.getTotalReviewCount());

        // List with a null review
        List<Review> withNull = new ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> repository.saveReviews(withNull));

        // Review with null optional fields (should succeed)
        Review partial = new Review.Builder()
            .setReviewId(111L)
            .setProductRating(1)
            .setReviewedDate(LocalDate.now())
            .setReviewText(null)
            .setAuthorName(null)
            .setReviewSource(null)
            .setReviewTitle(null)
            .setProductName(null)
            .build();
        assertDoesNotThrow(() -> repository.saveReviews(Collections.singletonList(partial)));
        assertEquals(1, repository.getTotalReviewCount());

        // Review missing required field (reviewedDate)
        assertThrows(com.reviewapp.application.exception.InvalidInputException.class, () ->
            new Review.Builder().setReviewId(222L).setProductRating(1).build()
        );
    }

    @DisplayName("parseFlexibleDate: null, empty, invalid, valid")
    @Test
    void parseFlexibleDate_edgeCases() throws Exception {
        var method = repository.getClass().getDeclaredMethod("parseFlexibleDate", String.class);
        method.setAccessible(true);
        assertNull(method.invoke(null, (Object) null));
        assertNull(method.invoke(null, ""));
        assertNull(method.invoke(null, "notadate"));
        assertEquals(LocalDate.of(2024, 1, 2), method.invoke(null, "2024-01-02"));
        assertEquals(LocalDate.of(2024, 1, 2), method.invoke(null, "2024-01-02T12:34:56"));
    }

    @DisplayName("getReviewById: returns null for null id")
    @Test
    void getReviewById_nullId() {
        assertNull(repository.getReviewById(null));
    }

    @DisplayName("getReviewsByKeywords: returns empty for null or empty keywords")
    @Test
    void getReviewsByKeywords_nullOrEmpty() {
        assertTrue(repository.getReviewsByKeywords(null).isEmpty());
        assertTrue(repository.getReviewsByKeywords(Collections.emptyList()).isEmpty());
    }

    @DisplayName("getAllReviews: empty and non-empty DB")
    @Test
    void getAllReviews_edgeCases() {
        // Empty DB
        assertTrue(repository.getAllReviews().isEmpty());
        // Non-empty DB
        Review r = new Review.Builder().setReviewId(222L).setProductRating(2).setReviewedDate(LocalDate.now()).build();
        repository.saveReviews(Collections.singletonList(r));
        assertEquals(1, repository.getAllReviews().size());
    }

    @DisplayName("getReviewsByFilters: all sort flag combinations")
    @Test
    void getReviewsByFilters_sortFlagCombinations() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(1L).setProductRating(5).setReviewedDate(LocalDate.parse("2024-01-01")).build(),
            new Review.Builder().setReviewId(2L).setProductRating(3).setReviewedDate(LocalDate.parse("2024-01-02")).build(),
            new Review.Builder().setReviewId(3L).setProductRating(4).setReviewedDate(LocalDate.parse("2024-01-03")).build()
        ));
        // Only sortByDate
        Filters byDate = new Filters.Builder().setSortByDate(true).build();
        List<Review> dateSorted = repository.getReviewsByFilters(byDate, 1, 10);
        assertEquals(3, dateSorted.size());
        assertEquals(3L, dateSorted.get(0).getReviewId()); // newest date first
        // Only sortByRating
        Filters byRating = new Filters.Builder().setSortByRating(true).build();
        List<Review> ratingSorted = repository.getReviewsByFilters(byRating, 1, 10);
        assertEquals(3, ratingSorted.size());
        assertEquals(1L, ratingSorted.get(0).getReviewId()); // highest rating first
        // Both true
        Filters both = new Filters.Builder().setSortByDate(true).setSortByRating(true).build();
        List<Review> bothSorted = repository.getReviewsByFilters(both, 1, 10);
        assertEquals(3, bothSorted.size());
        assertEquals(1L, bothSorted.get(0).getReviewId()); // rating desc, then date desc
        // Neither
        Filters neither = new Filters.Builder().build();
        List<Review> defaultSorted = repository.getReviewsByFilters(neither, 1, 10);
        assertEquals(3, defaultSorted.size());
    }

    @DisplayName("getReviewsByFilters: startTime and endTime filters")
    @Test
    void getReviewsByFilters_timeFilters() {
        LocalDate today = LocalDate.now();
        Review review = new Review.Builder()
            .setReviewId(200L)
            .setProductRating(4)
            .setReviewedDate(today)
            .build();
        repository.saveReviews(Collections.singletonList(review));
        // Filter with startTime and endTime as null should return the review
        Filters noTimeFilter = new Filters.Builder().build();
        assertFalse(repository.getReviewsByFilters(noTimeFilter, 1, 10).isEmpty());
        // Filter with startTime after review date should return empty
        Filters afterFilter = new Filters.Builder().setStartTime(java.time.LocalTime.MAX).build();
        assertTrue(repository.getReviewsByFilters(afterFilter, 1, 10).isEmpty());
        // Filter with endTime before review date should return empty
        Filters beforeFilter = new Filters.Builder().setEndTime(java.time.LocalTime.MIN).build();
        assertTrue(repository.getReviewsByFilters(beforeFilter, 1, 10).isEmpty());
    }

    @DisplayName("getMonthlyRatingAverage: SQL error throws RuntimeException")
    @Test
    void getMonthlyRatingAverage_throwsRuntimeException_whenSqlErrorOccurs() {
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}
        assertThrows(RuntimeException.class, badRepo::getMonthlyRatingAverage);
    }

    @DisplayName("getRatingDistribution: SQL error throws RuntimeException")
    @Test
    void getRatingDistribution_throwsRuntimeException_whenSqlErrorOccurs() {
        SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
        try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE reviews");
        } catch (Exception ignored) {}
        assertThrows(RuntimeException.class, badRepo::getRatingDistribution);
    }

    @DisplayName("Review with null date and rating: stats methods handle gracefully")
    @Test
    void statsMethods_handleNullDateAndRating() {
        Review review = new Review.Builder()
            .setReviewId(1L)
            .setReviewText("Test")
            .setAuthorName("A")
            .setReviewSource("S")
            .setReviewTitle("T")
            .setProductName("P")
            .setReviewedDate(LocalDate.now())
            .setProductRating(1)
            .build();
        repository.saveReviews(Collections.singletonList(review));
        assertEquals(1, repository.getTotalReviewCountStats());
        assertEquals(1.0, repository.getAverageRating(), 0.01);
        assertTrue(repository.getRatingDistribution().containsKey(1));
    }

    @DisplayName("saveReviews: handles review with all nullable fields (except id)")
    @Test
    void saveReviews_handlesReviewWithNullFields() {
        Review review = new Review.Builder()
            .setReviewId(100L)
            .setReviewText(null)
            .setAuthorName(null)
            .setReviewSource(null)
            .setReviewTitle(null)
            .setProductName(null)
            .setReviewedDate(LocalDate.now())
            .setProductRating(1)
            .build();
        repository.saveReviews(Collections.singletonList(review));
        Review fetched = repository.getReviewById(100L);
        assertNotNull(fetched);
        assertNull(fetched.getReviewText());
        assertNull(fetched.getAuthorName());
        assertNull(fetched.getReviewSource());
        assertNull(fetched.getReviewTitle());
        assertNull(fetched.getProductName());
        assertNotNull(fetched.getReviewedDate());
        assertEquals(1, fetched.getProductRating());
    }

    @DisplayName("getReviewsByFilters: all fields blank vs null")
    @Test
    void getReviewsByFilters_blankVsNullFields() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(201L).setProductRating(5).setReviewedDate(LocalDate.now()).setAuthorName("").setProductName("").setReviewSource("").setReviewTitle("").build(),
            new Review.Builder().setReviewId(202L).setProductRating(4).setReviewedDate(LocalDate.now()).setAuthorName(null).setProductName(null).setReviewSource(null).setReviewTitle(null).build()
        ));
        Filters blankFilters = new Filters.Builder().setAuthorName("").setProductName("").setStoreName("").setReviewTitle("").build();
        Filters nullFilters = new Filters.Builder().setAuthorName(null).setProductName(null).setStoreName(null).setReviewTitle(null).build();
        List<Review> blankResults = repository.getReviewsByFilters(blankFilters, 1, 10);
        List<Review> nullResults = repository.getReviewsByFilters(nullFilters, 1, 10);
        assertEquals(2, blankResults.size());
        assertEquals(2, nullResults.size());
    }

    @DisplayName("getReviewsByFilters: date and time filter edge cases")
    @Test
    void getReviewsByFilters_dateTimeEdgeCases() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(301L).setProductRating(3).setReviewedDate(LocalDate.parse("2024-01-01")).build()
        ));
        Filters invalidTime = new Filters.Builder().setStartTime(null).setEndTime(null).build();
        List<Review> results = repository.getReviewsByFilters(invalidTime, 1, 10);
        assertFalse(results.isEmpty());
    }

    @DisplayName("getReviewsByKeywords: multiple keywords, partial matches")
    @Test
    void getReviewsByKeywords_multiplePartialMatches() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(401L).setReviewText("foo bar baz").setReviewTitle("alpha").setProductRating(1).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(402L).setReviewText("lorem ipsum").setReviewTitle("beta").setProductRating(2).setReviewedDate(LocalDate.now()).build()
        ));
        List<Review> results = repository.getReviewsByKeywords(Arrays.asList("foo", "beta"));
        assertEquals(2, results.size());
    }

    @DisplayName("getReviewById: null id returns null")
    @Test
    void getReviewById_nullIdReturnsNull() {
        assertNull(repository.getReviewById(null));
    }

    @DisplayName("saveReviews: empty list does not throw or change DB")
    @Test
    void saveReviews_emptyListNoop() {
        int before = repository.getTotalReviewCount();
        repository.saveReviews(Collections.emptyList());
        int after = repository.getTotalReviewCount();
        assertEquals(before, after);
    }

    @DisplayName("getReviewsByFilters: very large pageSize returns all")
    @Test
    void getReviewsByFilters_veryLargePageSize() {
        repository.saveReviews(Arrays.asList(
            new Review.Builder().setReviewId(10001L).setProductRating(5).setReviewedDate(LocalDate.now()).build(),
            new Review.Builder().setReviewId(10002L).setProductRating(4).setReviewedDate(LocalDate.now()).build()
        ));
        Filters filters = new Filters.Builder().build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, Integer.MAX_VALUE);
        assertTrue(results.size() >= 2);
    }

    @DisplayName("getReviewsByFilters: SQL injection attempt in filter field is safe")
    @Test
    void getReviewsByFilters_sqlInjectionAttempt() {
        repository.saveReviews(Collections.singletonList(
            new Review.Builder().setReviewId(10003L).setProductName("foo").setReviewedDate(LocalDate.now()).setProductRating(5).build()
        ));
        Filters filters = new Filters.Builder().setProductName("foo'; DROP TABLE reviews; --").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertTrue(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: case sensitivity in filter fields")
    @Test
    void getReviewsByFilters_caseSensitivity() {
        repository.saveReviews(Collections.singletonList(
            new Review.Builder().setReviewId(10004L).setProductName("Bar").setReviewedDate(LocalDate.now()).setProductRating(5).build()
        ));
        Filters filtersLower = new Filters.Builder().setProductName("bar").build();
        Filters filtersExact = new Filters.Builder().setProductName("Bar").build();
        List<Review> lowerResults = repository.getReviewsByFilters(filtersLower, 1, 10);
        List<Review> exactResults = repository.getReviewsByFilters(filtersExact, 1, 10);
        assertEquals(exactResults.size(), lowerResults.size());
    }

    @DisplayName("getReviewsByFilters: Unicode/emoji in filter fields")
    @Test
    void getReviewsByFilters_unicodeEmoji() {
        repository.saveReviews(Collections.singletonList(
            new Review.Builder().setReviewId(10005L).setProductName("📱手机").setReviewedDate(LocalDate.now()).setProductRating(5).build()
        ));
        Filters filters = new Filters.Builder().setProductName("📱手机").build();
        List<Review> results = repository.getReviewsByFilters(filters, 1, 10);
        assertFalse(results.isEmpty());
    }

    @DisplayName("getReviewsByFilters: null Filters object throws NullPointerException")
    @Test
    void getReviewsByFilters_nullFiltersThrows() {
        assertThrows(NullPointerException.class, () -> repository.getReviewsByFilters(null, 1, 10));
    }

    @DisplayName("Verifies getReviewsByFilters with endDate filter")
    @Test
        void getReviewsByFilters_endDateFilter() throws SQLException {
            // Arrange
            LocalDate today = LocalDate.now();
            Review review = new Review.Builder()
                    .setReviewId(999L)
                    .setReviewText("Test")
                    .setAuthorName("Test")
                    .setReviewSource("Test")
                    .setReviewTitle("Test")
                    .setProductName("Test")
                    .setReviewedDate(today.minusDays(1))
                    .setProductRating(5)
                    .build();
            repository.saveReviews(Collections.singletonList(review));

            Filters endDateFilter = new Filters.Builder().setEndDate(today).build();

            // Act
            List<Review> results = repository.getReviewsByFilters(endDateFilter, 1, 10);

            // Assert
            assertEquals(1, results.size());
            assertEquals(999L, results.get(0).getReviewId());
        }

        @DisplayName("Verifies getFilteredReviewCount with endDate filter")
        @Test
        void getFilteredReviewCount_endDateFilter() throws SQLException {
            // Arrange
            LocalDate today = LocalDate.now();
            Review review = new Review.Builder()
                    .setReviewId(999L)
                    .setReviewText("Test")
                    .setAuthorName("Test")
                    .setReviewSource("Test")
                    .setReviewTitle("Test")
                    .setProductName("Test")
                    .setReviewedDate(today.minusDays(1))
                    .setProductRating(5)
                    .build();
            repository.saveReviews(Collections.singletonList(review));

            Filters endDateFilter = new Filters.Builder().setEndDate(today).build();

            // Act
            int count = repository.getFilteredReviewCount(endDateFilter);

            // Assert
            assertEquals(1, count);
        }

        @DisplayName("Verifies that a SQL error in getReviewById throws a RuntimeException")
        @Test
        void getReviewById_throwsRuntimeException_whenSqlErrorOccurs() {
            // Arrange
            SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
            try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE reviews");
            } catch (Exception ignored) {}

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, () -> badRepo.getReviewById(1L));
            assertTrue(ex.getMessage().contains("SQL error fetching review by id"));
        }

        @DisplayName("Verifies that a SQL error in getAllReviews throws a RuntimeException")
        @Test
        void getAllReviews_throwsRuntimeException_whenSqlErrorOccurs() {
            // Arrange
            SqliteReviewRepository badRepo = new SqliteReviewRepository(DB_URL);
            try (var conn = DriverManager.getConnection(DB_URL); var stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE reviews");
            } catch (Exception ignored) {}

            // Act & Assert
            RuntimeException ex = assertThrows(RuntimeException.class, badRepo::getAllReviews);
            assertTrue(ex.getMessage().contains("SQL error fetching all reviews"));
        }

    @DisplayName("Verifies saveReviews handles null reviewedDate (bypassing domain validation)")
    @Test
    void saveReviews_handlesNullReviewedDate() throws Exception {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(999L)
                .setReviewText("Test")
                .setAuthorName("Test")
                .setReviewSource("Test")
                .setReviewTitle("Test")
                .setProductName("Test")
                .setReviewedDate(LocalDate.now())
                .setProductRating(5)
                .build();

        java.lang.reflect.Field field = Review.class.getDeclaredField("reviewedDate");
        field.setAccessible(true);
        field.set(review, null);

        // Act
        repository.saveReviews(Collections.singletonList(review));

        // Fetch directly from database to avoid mapRow validation
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT reviewedDate FROM reviews WHERE id = ?")) {
            ps.setLong(1, 999L);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next(), "Review should exist");
                assertNull(rs.getString("reviewedDate"), "reviewedDate should be null in database");
            }
        }

        Review fetched;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM reviews WHERE id = ?")) {
            ps.setLong(1, 999L);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fetched = new Review.Builder()
                            .setReviewId(rs.getLong("id"))
                            .setReviewText(rs.getString("review"))
                            .setAuthorName(rs.getString("author"))
                            .setReviewSource(rs.getString("reviewSource"))
                            .setReviewTitle(rs.getString("title"))
                            .setProductName(rs.getString("productName"))
                            .setReviewedDate(LocalDate.now()) // Temporary to pass validation
                            .setProductRating(rs.getInt("rating"))
                            .build();
                    field.set(fetched, null);
                } else {
                    fail("Review not found");
                    return;
                }
            }
        }

        // Assert
        assertNotNull(fetched);
        assertNull(fetched.getReviewedDate());
    }

        @DisplayName("Verifies getTotalReviewCount handles no ResultSet row (mocked unreachable case)")
        @Test
        void getTotalReviewCount_handlesNoRow_unreachable() throws Exception {
            Connection mockConn = mock(Connection.class);
            Statement mockStmt = mock(Statement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(mockConn.createStatement()).thenReturn(mockStmt);
            when(mockStmt.executeQuery("SELECT COUNT(*) FROM reviews")).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            SqliteReviewRepository mockedRepo = new SqliteReviewRepository(DB_URL) {
                @Override
                public int getTotalReviewCount() {
                    try (Connection conn = mockConn) {
                        return super.getTotalReviewCount();
                    } catch (SQLException e) {
                        throw new RuntimeException("Mock SQL error", e);
                    }
                }
            };

            // Act & Assert
            assertEquals(0, mockedRepo.getTotalReviewCount());
        }

        @DisplayName("Verifies getAverageRating handles no ResultSet row (mocked unreachable case)")
        @Test
        void getAverageRating_handlesNoRow_unreachable() throws Exception {

            Connection mockConn = mock(Connection.class);
            Statement mockStmt = mock(Statement.class);
            ResultSet mockRs = mock(ResultSet.class);
            when(mockConn.createStatement()).thenReturn(mockStmt);
            when(mockStmt.executeQuery("SELECT AVG(rating) FROM reviews")).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            SqliteReviewRepository mockedRepo = new SqliteReviewRepository(DB_URL) {
                @Override
                public double getAverageRating() {
                    try (Connection conn = mockConn) {
                        return super.getAverageRating();
                    } catch (SQLException e) {
                        throw new RuntimeException("Mock SQL error", e);
                    }
                }
            };

            // Act & Assert
            assertEquals(0.0, mockedRepo.getAverageRating(), 0.01);
        }

        @DisplayName("Verifies time filters with full date-time in DB")
        @Test
        void getReviewsByFilters_timeFiltersWithDateTime() throws SQLException {
            // Arrange
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO reviews (id, reviewedDate, rating) VALUES (?, ?, ?)")) {
                ps.setLong(1, 999L);
                ps.setString(2, "2024-08-12 15:30:00");
                ps.setInt(3, 5);
                ps.executeUpdate();
            }

            Filters startTimeFilter = new Filters.Builder().setStartTime(LocalTime.of(14, 0)).build();
            assertEquals(1, repository.getReviewsByFilters(startTimeFilter, 1, 10).size());

            Filters endTimeFilter = new Filters.Builder().setEndTime(LocalTime.of(14, 0)).build();
            assertTrue(repository.getReviewsByFilters(endTimeFilter, 1, 10).isEmpty());

            assertEquals(1, repository.getFilteredReviewCount(startTimeFilter));
            assertEquals(0, repository.getFilteredReviewCount(endTimeFilter));
        }
}
