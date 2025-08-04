package com.reviewapp.adapter.command_line_interface;

import com.reviewapp.application.service.ReviewService;
import com.reviewapp.application.service.StatisticsService;
import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.model.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    /**
     * Sets up the test fixture before each test.
     * Initializes InputHandler with mocked services and redirects System.out for output capture.
     */
    @BeforeEach
    void setUp() {
        inputHandler = new InputHandler(reviewService, statisticsService);
        // Redirect System.out to capture output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    /**
     * Restores System.out after each test to avoid side effects.
     */
    @AfterEach
    void restoreSystemOut() {
        System.setOut(originalOut);
    }

    /**
     * Verifies the welcome and exit messages are printed when the REPL starts and exits.
     */
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

    /**
     * Verifies the help command prints the list of supported commands.
     */
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

    /**
     * Verifies the all command prints all reviews from the service.
     */
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

    /**
     * Verifies that a valid id command prints the expected review.
     */
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

    /**
     * Verifies that an invalid id prints 'Not found'.
     */
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

    /**
     * Verifies that a malformed id command prints an error message.
     */
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

    /**
     * Verifies the filters command prints available filter keys and formats.
     */
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

    /**
     * Verifies that a valid filter command prints the filtered reviews.
     */
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

    /**
     * Verifies that an invalid filter command prints an error message.
     */
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

    /**
     * Verifies the search command prints reviews matching the search keywords.
     */
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

    /**
     * Verifies the stats command prints average rating and total reviews.
     */
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

    /**
     * Verifies the distr command prints the rating distribution.
     */
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

    /**
     * Verifies the monthly command prints monthly average ratings.
     */
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

    /**
     * Verifies the clear command attempts to clear the terminal screen.
     */
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

    /**
     * Verifies the exit command exits the REPL and prints the goodbye message.
     */
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

    /**
     * Verifies that receiving EOF exits the REPL gracefully.
     */
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

    /**
     * Verifies that an unknown command prints an appropriate error message.
     */
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
}