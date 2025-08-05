package com.reviewapp.adapter.jdbc;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.*;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SqliteReviewRepositoryTest {
    // Use a file-based SQLite DB for test reliability
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
        // Optionally clear the table after each test for isolation
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
        Filters noMatch = new Filters.Builder().setAuthorName("ZZZ").build();

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
}
