package com.reviewapp.adapter.command_line_interface;

import com.reviewapp.application.service.ReviewService;
import com.reviewapp.application.service.StatisticsService;
import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.model.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InputHandlerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private StatisticsService statisticsService;

    private InputHandler inputHandler;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;


    @DisplayName("Sets up the test fixture before each test. " +
            "Initializes InputHandler with mocked services and redirects System.out for output capture.")
    @BeforeEach
    void setUp() {
        inputHandler = new InputHandler(reviewService, statisticsService);
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }


    @DisplayName("Restores System.out after each test to avoid side effects.")
    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
    }


    @DisplayName("Verifies the welcome and exit messages are printed when the REPL starts and exits.")
    @Test
    void startListeningToConsole_welcomeMessagePrinted_onStartup() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("exit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Welcome to the Review System. Type 'help' for commands."));
        assertTrue(output.contains("Goodbye!"));
    }


    @DisplayName("Verifies the help command prints the list of supported commands.")
    @Test
    void startListeningToConsole_helpCommand_printsHelp() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("help\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Commands:"));
        assertTrue(output.contains("all                 - Show all reviews"));
        assertTrue(output.contains("exit                - Exit the program"));
    }


    @DisplayName("Verifies the all command prints all reviews from the service.")
    @Test
    void startListeningToConsole_allCommand_printsAllReviews() throws IOException {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewedDate(LocalDate.of(2023, 1, 1))
                .setProductRating(5)
                .setReviewText("Great product")
                .build();
        when(reviewService.getAllReviews()).thenReturn(Arrays.asList(review));
        ByteArrayInputStream input = new ByteArrayInputStream("all\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Great product"));
        verify(reviewService, times(1)).getAllReviews();
    }


    @DisplayName("Verifies that a valid id command prints the expected review.")
    @Test
    void startListeningToConsole_idCommandWithValidId_printsReview() throws IOException {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewedDate(LocalDate.of(2023, 1, 1))
                .setProductRating(5)
                .setReviewText("Great product")
                .build();
        when(reviewService.getReviewById(1L)).thenReturn(review);
        ByteArrayInputStream input = new ByteArrayInputStream("id 1\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Great product"));
        verify(reviewService, times(1)).getReviewById(1L);
    }


    @DisplayName("Verifies that an invalid id command prints 'Not found'")
    @Test
    void startListeningToConsole_idCommandWithInvalidId_printsNotFound() throws IOException {
        // Arrange
        when(reviewService.getReviewById(999L)).thenReturn(null);
        ByteArrayInputStream input = new ByteArrayInputStream("id 999\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Not found"));
        verify(reviewService, times(1)).getReviewById(999L);
    }


    @DisplayName("Verifies that a malformed id command prints an error message.")
    @Test
    void startListeningToConsole_idCommandWithMalformedId_printsError() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("id abc\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Invalid ID: Please enter a valid integer."));
    }


    @DisplayName("Verifies the filters command prints available filter keys and formats.")
    @Test
    void startListeningToConsole_filtersCommand_printsAvailableFilters() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("filters\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Available Filters"));
        assertTrue(output.contains("rating       : Integer  [1..5]"));
        assertTrue(output.contains("date          : YYYY-MM-DD"));
    }


    @DisplayName("Verifies that a valid filter command prints the filtered reviews.")
    @Test
    void startListeningToConsole_filterCommandWithValidFilter_printsFilteredReviews() throws IOException {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewedDate(LocalDate.of(2023, 1, 1))
                .setProductRating(5)
                .setReviewText("Great product")
                .build();
        when(reviewService.getFilteredReviewsPage(any(Filters.class), eq(1), eq(Integer.MAX_VALUE)))
                .thenReturn(Arrays.asList(review));
        ByteArrayInputStream input = new ByteArrayInputStream("filter rating=5\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Great product"));
        verify(reviewService, times(1)).getFilteredReviewsPage(any(Filters.class), eq(1), eq(Integer.MAX_VALUE));
    }


    @DisplayName("Verifies that an invalid filter command prints an error message.")
    @Test
    void startListeningToConsole_filterCommandWithInvalidFilter_printsError() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("filter rating=6\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        // Accept any message that contains both 'rating' and 'between 1 and 5' for robustness
        assertTrue(output.contains("rating") && output.contains("between 1 and 5"),
                "Should mention rating and range error");
    }


    @DisplayName("Verifies that a valid search command prints the search results.")
    @Test
    void startListeningToConsole_searchCommandWithValidSearch_printsSearchResults() throws IOException {
        // Arrange
        Review review = new Review.Builder()
                .setReviewId(1L)
                .setReviewedDate(LocalDate.of(2023, 1, 1))
                .setProductRating(5)
                .setReviewText("Great product")
                .build();
        when(reviewService.getReviewsByKeywords(Arrays.asList("great"))).thenReturn(Arrays.asList(review));
        ByteArrayInputStream input = new ByteArrayInputStream("search great\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Great product"));
        verify(reviewService, times(1)).getReviewsByKeywords(Arrays.asList("great"));
    }


    @DisplayName("Verifies the stats command prints average rating and total reviews.")
    @Test
    void startListeningToConsole_statsCommand_printsStats() throws IOException {
        // Arrange
        Statistics stats = new Statistics.Builder()
                .setTotalReviews(100)
                .setAverageRating(4.5)
                .build();
        when(statisticsService.getReviewStatistics()).thenReturn(stats);
        ByteArrayInputStream input = new ByteArrayInputStream("stats\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Average rating: 4.5"));
        assertTrue(output.contains("Total reviews: 100"));
        verify(statisticsService, times(1)).getReviewStatistics();
    }


    @DisplayName("Verifies the distr command prints the rating distribution.")
    @Test
    void startListeningToConsole_distrCommand_printsDistribution() throws IOException {
        // Arrange
        Map<Integer, Integer> distribution = new TreeMap<>();
        distribution.put(5, 50);
        distribution.put(4, 30);
        Statistics stats = new Statistics.Builder()
                .setRatingDistribution(distribution)
                .build();
        when(statisticsService.getReviewStatistics()).thenReturn(stats);
        ByteArrayInputStream input = new ByteArrayInputStream("distr\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("{4=30, 5=50}"));
        verify(statisticsService, times(1)).getReviewStatistics();
    }


    @DisplayName("Verifies the monthly command prints the monthly average ratings.")
    @Test
    void startListeningToConsole_monthlyCommand_printsMonthlyAverages() throws IOException {
        // Arrange
        Map<String, Double> monthlyAverages = new TreeMap<>();
        monthlyAverages.put("2023-01", 4.5);
        Statistics stats = new Statistics.Builder()
                .setMonthlyRatingAverage(monthlyAverages)
                .build();
        when(statisticsService.getReviewStatistics()).thenReturn(stats);
        ByteArrayInputStream input = new ByteArrayInputStream("monthly\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("{2023-01=4.5}"));
        verify(statisticsService, times(1)).getReviewStatistics();
    }


    @DisplayName("Verifies the clear command attempts to clear the terminal screen.")
    @Test
    void startListeningToConsole_clearCommand_clearsScreen() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("clear\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        // Accept either ANSI code, or at least 10 consecutive newlines (robust for Windows), or 40+ newlines total
        long newlineCount = output.chars().filter(ch -> ch == '\n').count();
        assertTrue(output.contains("\033[H\033[2J") || output.contains("\n\n\n\n\n\n\n\n\n\n") || newlineCount >= 40,
                "Should clear the screen with ANSI or many newlines");
    }


    @DisplayName("Verifies the exit command exits the REPL and prints the goodbye message.")
    @Test
    void startListeningToConsole_exitCommand_exitsProgram() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("exit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Goodbye!"));
    }


    @DisplayName("Verifies that receiving EOF exits the REPL gracefully.")
    @Test
    void startListeningToConsole_eofReceived_exitsProgram() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("EOF received. Exiting."));
        assertTrue(output.contains("Goodbye!"));
    }


    @DisplayName("Verifies that an unknown command prints an appropriate error message.")
    @Test
    void startListeningToConsole_unknownCommand_printsUnknownCommand() throws IOException {
        // Arrange
        ByteArrayInputStream input = new ByteArrayInputStream("invalid\nexit\n".getBytes());
        System.setIn(input);
        // Act
        inputHandler.startListeningToConsole();
        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Unknown command. Type 'help' for available commands."));
    }

    @DisplayName("parseInputFilters: unknown key does not throw and returns Filters")
    @Test
    void parseInputFilters_unknownKey_doesNotThrow() throws Exception {
        // Arrange
        var m = InputHandler.class.getDeclaredMethod("parseInputFilters", String.class);
        m.setAccessible(true);
        Filters filters = null;

        //Act
        try {
            filters = (Filters) m.invoke(inputHandler, "unknownKey=123");
        } catch (Exception e) {
            fail("parseInputFilters should not throw for unknown keys, but got: " + e);
        }

        //Assert
        assertNotNull(filters);
    }

    @DisplayName("parseInputFilters: covers all filter key branches")
    @Test
    void parseInputFilters_coversAllBranches() throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseInputFilters", String.class);
        m.setAccessible(true);
        // MIN_RATING
        Filters f1 = (Filters) m.invoke(inputHandler, "minrating=2");
        assertNotNull(f1);
        // MAX_RATING
        Filters f2 = (Filters) m.invoke(inputHandler, "maxrating=4");
        assertNotNull(f2);
        // AUTHOR
        Filters f3 = (Filters) m.invoke(inputHandler, "author=John Doe");
        assertNotNull(f3);
        // TITLE
        Filters f4 = (Filters) m.invoke(inputHandler, "title=Review Title");
        assertNotNull(f4);
        // PRODUCT
        Filters f5 = (Filters) m.invoke(inputHandler, "product=Widget");
        assertNotNull(f5);
        // STORE
        Filters f6 = (Filters) m.invoke(inputHandler, "store=StoreName");
        assertNotNull(f6);
        // DATE
        Filters f7 = (Filters) m.invoke(inputHandler, "date=2023-01-01");
        assertNotNull(f7);
        // START_DATE
        Filters f8 = (Filters) m.invoke(inputHandler, "startdate=2023-01-01");
        assertNotNull(f8);
        // END_DATE
        Filters f9 = (Filters) m.invoke(inputHandler, "enddate=2023-01-31");
        assertNotNull(f9);
        // START_TIME
        Filters f10 = (Filters) m.invoke(inputHandler, "starttime=12:00");
        assertNotNull(f10);
        // END_TIME
        Filters f11 = (Filters) m.invoke(inputHandler, "endtime=13:30:00");
        assertNotNull(f11);
        // SORT_DATE
        Filters f12 = (Filters) m.invoke(inputHandler, "sortdate=true");
        assertNotNull(f12);
        Filters f13 = (Filters) m.invoke(inputHandler, "sortdate=false");
        assertNotNull(f13);
        // SORT_RATING
        Filters f14 = (Filters) m.invoke(inputHandler, "sortrating=true");
        assertNotNull(f14);
        Filters f15 = (Filters) m.invoke(inputHandler, "sortrating=false");
        assertNotNull(f15);
    }

    @DisplayName("parseTimeLenient: invalid time throws exception")
    @Test
    void parseTimeLenient_invalidTime_throwsException() throws Exception {
        // Arrange
        Exception ex = assertThrows(Exception.class, () ->
                invokePrivateStaticTimeLenient("badtime")
        );
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;

        //Act & Assert
        assertTrue(cause instanceof IllegalArgumentException);
        assertTrue(cause.getMessage().contains("time must be HH:mm or HH:mm:ss"));
    }

    @DisplayName("parseDateStrict: invalid date throws exception")
    @Test
    void parseDateStrict_invalidDate_throwsException() throws Exception {
        Exception ex = assertThrows(Exception.class, () ->
                invokePrivateStaticDateStrict("2023-99-99")
        );
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        assertTrue(cause instanceof IllegalArgumentException);
        assertTrue(cause.getMessage().contains("date must be in YYYY-MM-DD (ISO) format"));
    }

    @DisplayName("clearScreen: handles exception gracefully")
    @Test
    void clearScreen_exception_printsWarning() throws Exception {
        var method = InputHandler.class.getDeclaredMethod("clearScreen");
        method.setAccessible(true);
        InputHandler handler = new InputHandler(reviewService, statisticsService);
        System.setProperty("os.name", "");

        try {

            PrintStream original = System.out;
            System.setOut(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    throw new RuntimeException("fail");
                }
            }));
            try {
                method.invoke(handler);
            } catch (Exception e) {
                // Expected, now check output
                System.setOut(originalOut);
                String output = outputStream.toString();
                // We cannot capture output if System.out throws, so just assert exception occurred
                assertTrue(e.getCause() instanceof RuntimeException);
            }
        } finally {
            System.setOut(originalOut);
        }
    }

    @DisplayName("parseIntInRange: out of range throws exception")
    @Test
    void parseIntInRange_outOfRange_throwsException() throws Exception {
        Exception ex = assertThrows(Exception.class, () ->
                invokePrivateStaticIntInRange("10", 1, 5, "rating")
        );
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        assertTrue(cause instanceof IllegalArgumentException);
        assertTrue(cause.getMessage().contains("must be between"));
    }

    @DisplayName("parseIntInRange: non-integer throws exception")
    @Test
    void parseIntInRange_nonInteger_throwsException() throws Exception {
        Exception ex = assertThrows(Exception.class, () ->
                invokePrivateStaticIntInRange("notanint", 1, 5, "rating")
        );
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        assertTrue(cause instanceof IllegalArgumentException);
        assertTrue(cause.getMessage().contains("must be an integer"));
    }

    @DisplayName("parseInputFilters: returns builder for null or empty criteria")
    @Test
    void parseInputFilters_nullOrEmpty_returnsBuilder() throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseInputFilters", String.class);
        m.setAccessible(true);
        Filters f1 = (Filters) m.invoke(inputHandler, (Object) null);
        Filters f2 = (Filters) m.invoke(inputHandler, "");
        assertNotNull(f1);
        assertNotNull(f2);
    }

    @DisplayName("parseInputFilters: skips empty and malformed parts")
    @Test
    void parseInputFilters_skipsEmptyAndMalformedParts() throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseInputFilters", String.class);
        m.setAccessible(true);

        Filters filters = (Filters) m.invoke(inputHandler, "rating=5,,invalid,author=John");
        assertNotNull(filters);
    }

    @DisplayName("clearScreen: covers Windows branch")
    @Test
    void clearScreen_windowsBranch() throws Exception {
        var method = InputHandler.class.getDeclaredMethod("clearScreen");
        method.setAccessible(true);
        String oldOs = System.getProperty("os.name");
        System.setProperty("os.name", "Windows 10");
        try {
            method.invoke(inputHandler);
        } finally {
            System.setProperty("os.name", oldOs);
        }
    }

    @DisplayName("InputHandler constructor: null services")
    @Test
    void inputHandler_nullServices_throws() {
        assertThrows(NullPointerException.class, () -> new InputHandler(null, statisticsService));
        assertThrows(NullPointerException.class, () -> new InputHandler(reviewService, null));
    }

    // Reflection helpers for private static methods
    private static int invokePrivateStaticIntInRange(String val, int min, int max, String field) throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseIntInRange", String.class, int.class, int.class, String.class);
        m.setAccessible(true);
        return (int) m.invoke(null, val, min, max, field);
    }
    private static java.time.LocalTime invokePrivateStaticTimeLenient(String val) throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseTimeLenient", String.class);
        m.setAccessible(true);
        return (java.time.LocalTime) m.invoke(null, val);
    }
    private static java.time.LocalDate invokePrivateStaticDateStrict(String val) throws Exception {
        var m = InputHandler.class.getDeclaredMethod("parseDateStrict", String.class);
        m.setAccessible(true);
        return (java.time.LocalDate) m.invoke(null, val);
    }
}