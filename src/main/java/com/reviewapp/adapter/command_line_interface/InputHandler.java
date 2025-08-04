package com.reviewapp.adapter.command_line_interface;

import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import com.reviewapp.domain.model.Statistics;
import com.reviewapp.application.service.ReviewService;
import com.reviewapp.application.service.StatisticsService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Handles interactive console input for the Review application.
 * <p>
 * This class implements a simple REPL (read–eval–print loop) that accepts commands,
 * delegates business operations to {@link ReviewService} and {@link StatisticsService},
 * and prints results to {@code System.out}.
 * <p>
 * <b>Supported commands:</b>
 * <ul>
 *     <li>{@code help} – List commands.</li>
 *     <li>{@code all} – Show all reviews.</li>
 *     <li>{@code id &lt;id&gt;} – Show a single review by numeric ID.</li>
 *     <li>{@code filters} – List available filter keys and formats.</li>
 *     <li>{@code filter &lt;criteria&gt;} – Apply filters; criteria is a comma-separated list of {@code key=value} pairs.</li>
 *     <li>{@code search &lt;kw1,kw2,...&gt;} – Keyword search across review text and titles.</li>
 *     <li>{@code stats} – Print average rating and total review count.</li>
 *     <li>{@code distr} – Print rating distribution.</li>
 *     <li>{@code monthly} – Print monthly average ratings.</li>
 *     <li>{@code clear} – Clear the terminal screen (best-effort).</li>
 *     <li>{@code exit} – Exit the REPL.</li>
 * </ul>
 * <p>
 * <b>Thread-safety:</b> This class is <i>not</i> thread-safe. It is intended for single-threaded
 * interactive usage bound to {@code System.in/System.out}.
 * <p>
 * <b>I/O:</b> Blocking on standard input while reading commands; writes synchronous output to the console.
 */
public class InputHandler {

    // ---------------------------------------------------------------------
    // Filter keys: centralized to keep parser and help text in sync
    // ---------------------------------------------------------------------

    /** Filter key for an exact rating value (1..5). */
    private static final String RATING      = "rating";
    /** Filter key for minimum rating (1..5 inclusive). */
    private static final String MIN_RATING  = "minrating";
    /** Filter key for maximum rating (1..5 inclusive). */
    private static final String MAX_RATING  = "maxrating";
    /** Filter key for author substring match (case-insensitive). */
    private static final String AUTHOR      = "author";
    /** Filter key for title substring match (case-insensitive). */
    private static final String TITLE       = "title";
    /** Filter key for exact product name (case-insensitive). */
    private static final String PRODUCT     = "productname";
    /** Filter key for exact review date in ISO format (YYYY-MM-DD). */
    private static final String DATE        = "date";
    /** Filter key for review source/store substring match (case-insensitive). */
    private static final String STORE       = "store";
    /** Sort flag: sort by date when {@code true}. */
    private static final String SORT_DATE   = "sortbydate";
    /** Sort flag: sort by rating when {@code true}. */
    private static final String SORT_RATING = "sortbyratings";
    /** Filter key for start date (inclusive, ISO YYYY-MM-DD). */
    private static final String START_DATE  = "startdate";
    /** Filter key for end date (inclusive, ISO YYYY-MM-DD). */
    private static final String END_DATE    = "enddate";
    /**
     * Filter key for start time (inclusive). Accepts {@code HH:mm} or {@code HH:mm:ss}.
     * <p><b>Note:</b> Effective only if the underlying model and repository retain time information.
     */
    private static final String START_TIME  = "starttime";
    /**
     * Filter key for end time (inclusive). Accepts {@code HH:mm} or {@code HH:mm:ss}.
     * <p><b>Note:</b> Effective only if the underlying model and repository retain time information.
     */
    private static final String END_TIME    = "endtime";

    private final ReviewService reviewService;
    private final StatisticsService statisticsService;

    /**
     * Constructs an {@code InputHandler} bound to the given services.
     *
     * @param reviewService      business service for review retrieval, searching, and filtering
     * @param statisticsService  business service for computed statistics
     * @throws NullPointerException if any argument is {@code null}
     */
    public InputHandler(ReviewService reviewService, StatisticsService statisticsService) {
        if (reviewService == null || statisticsService == null) {
            throw new NullPointerException("reviewService and statisticsService must not be null");
        }
        this.reviewService = reviewService;
        this.statisticsService = statisticsService;
    }

    /**
     * Starts a blocking REPL that reads commands from {@code System.in} and writes output to {@code System.out}.
     * <p>
     * The loop terminates when:
     * <ul>
     *     <li>{@code exit} command is received.</li>
     *     <li>EOF is detected on the input stream (CTRL+D/CTRL+Z), in which case a graceful exit message is printed.</li>
     * </ul>
     * <p>
     * <b>Error handling:</b>
     * <ul>
     *     <li>Input validation and filter parsing errors are reported with human-friendly messages.</li>
     *     <li>Unexpected exceptions are caught and logged to the console without terminating the loop.</li>
     * </ul>
     */
    public void startListeningToConsole() {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
            println("Welcome to the Review System. Type 'help' for commands.");
            while (true) {
                print("> ");
                String rawLine = readLineSafe(bufferedReader);
                if (rawLine == null) { // EOF => exit gracefully
                    println("\nEOF received. Exiting.");
                    break;
                }
                final String command = rawLine.trim();
                if (command.isEmpty()) continue;

                try {
                    if (equalsIgnoreCase(command, "exit")) {
                        break;
                    } else if (equalsIgnoreCase(command, "help")) {
                        printHelp();
                    } else if (equalsIgnoreCase(command, "all")) {
                        List<Review> reviews = reviewService.getAllReviews();
                        reviews.forEach(System.out::println);
                    } else if (command.startsWith("id ")) {
                        handleById(command.substring(3).trim());
                    } else if (command.startsWith("filter ")) {
                        Filters filters = parseInputFilters(command.substring(7).trim());
                        reviewService.getFilteredReviewsPage(filters, 1, Integer.MAX_VALUE).forEach(System.out::println);
                    } else if (equalsIgnoreCase(command, "filters")) {
                        printFilters();
                    } else if (command.startsWith("search ")) {
                        String[] keywordTokens = command.substring(7).trim().split(",");
                        List<Review> matchedReviews = reviewService.getReviewsByKeywords(Arrays.asList(keywordTokens));
                        matchedReviews.forEach(System.out::println);
                    } else if (equalsIgnoreCase(command, "stats")) {
                        Statistics statistics = statisticsService.getReviewStatistics();
                        println(" Average rating: " + statistics.getAverageRating() + "\n Total reviews: " + statistics.getTotalReviews());
                    } else if (equalsIgnoreCase(command, "distr")) {
                        Statistics statistics = statisticsService.getReviewStatistics();
                        println(String.valueOf(statistics.getRatingDistribution()));
                    } else if (equalsIgnoreCase(command, "monthly")) {
                        Statistics statistics = statisticsService.getReviewStatistics();
                        println(String.valueOf(statistics.getMonthlyRatingAverage()));
                    } else if (equalsIgnoreCase(command, "clear")) {
                        clearScreen();
                    } else {
                        println("Unknown command. Type 'help' for available commands.");
                    }
                } catch (IllegalArgumentException validationException) {
                    // From Filters.Builder.validate() or parse errors
                    println("Input error: " + validationException.getMessage());
                } catch (Exception unexpectedException) {
                    println("Error: " + unexpectedException.getMessage());
                }
            }
        } catch (IOException ioException) {
            System.err.println("[FATAL] Unable to read console input: " + ioException.getMessage());
        }
        println("Goodbye!");
    }

    /**
     * Parses a review ID token and prints the matching review or an error message.
     * <p>
     * <b>Behavior:</b> Attempts to parse the provided token as a {@code long} identifier and
     * delegates to {@link ReviewService#getReviewById(long)}. Prints the review if found,
     * otherwise prints {@code "Not found"}.
     *
     * @param idToken the raw string token following the {@code id } command; expected to be a base-10 integer
     */
    private void handleById(String idToken) {
        try {
            long reviewId = Long.parseLong(idToken);
            Review review = reviewService.getReviewById(reviewId);
            println(review != null ? review.toString() : "Not found");
        } catch (NumberFormatException numberFormatException) {
            println("Invalid ID: Please enter a valid integer.");
        }
    }

    /**
     * Parses a comma-separated list of {@code key=value} filter pairs into a {@link Filters} object.
     * <p>
     * <b>Accepted keys:</b> see the {@code *} constants (e.g., {@link #RATING}, {@link #MIN_RATING}, etc.).<br>
     * <b>Date format:</b> ISO 8601 {@code YYYY-MM-DD}.<br>
     * <b>Time format:</b> {@code HH:mm} or {@code HH:mm:ss}. <i>Only effective if underlying data contains time.</i>
     * <p>
     * Unknown keys are ignored with an informational message to the console.
     *
     * @param criteria the raw criteria string after {@code filter } (may be empty or null)
     * @return an immutable {@link Filters} instance representing the parsed criteria
     * @throws IllegalArgumentException if any value fails validation (e.g., rating out of range, bad date format)
     */
    private Filters parseInputFilters(String criteria) {
        Filters.Builder builder = new Filters.Builder();
        if (criteria == null || criteria.isEmpty()) return builder.build();

        for (String rawPart : criteria.split(",")) {
            String part = rawPart.trim();
            if (part.isEmpty()) continue;

            String[] keyValue = part.split("=", 2);
            if (keyValue.length < 2) continue;
            String key = keyValue[0].trim().toLowerCase();
            String value = keyValue[1].trim();

            try {
                switch (key) {
                    case RATING:      builder.setRating(parseIntInRange(value, 1, 5, "rating")); break;
                    case MIN_RATING:  builder.setMinRating(parseIntInRange(value, 1, 5, "minrating")); break;
                    case MAX_RATING:  builder.setMaxRating(parseIntInRange(value, 1, 5, "maxrating")); break;
                    case AUTHOR:      builder.setAuthorName(value); break;
                    case TITLE:       builder.setReviewTitle(value); break;
                    case PRODUCT:     builder.setProductName(value); break;
                    case STORE:       builder.setStoreName(value); break;
                    case DATE:        builder.setReviewDate(parseDateStrict(value)); break;
                    case START_DATE:  builder.setStartDate(parseDateStrict(value)); break;
                    case END_DATE:    builder.setEndDate(parseDateStrict(value)); break;
                    case START_TIME:  builder.setStartTime(parseTimeLenient(value)); break; // effective only if model has time
                    case END_TIME:    builder.setEndTime(parseTimeLenient(value)); break;
                    case SORT_DATE:   builder.setSortByDate(Boolean.parseBoolean(value)); break;
                    case SORT_RATING: builder.setSortByRating(Boolean.parseBoolean(value)); break;
                    default:
                        // Unknown keys are ignored intentionally, but we notify the user for transparency.
                        println("Ignoring unknown filter key: " + key);
                }
            } catch (IllegalArgumentException valueException) {
                throw new IllegalArgumentException("Invalid value for '" + key + "': " + valueException.getMessage(), valueException);
            }
        }
        return builder.build();
    }

    /**
     * Parses an integer value and validates it in the provided range (inclusive).
     *
     * @param input     the string to parse
     * @param min       minimum allowed value (inclusive)
     * @param max       maximum allowed value (inclusive)
     * @param fieldName label used in error messages
     * @return the parsed integer
     * @throws IllegalArgumentException if parsing fails or the value is out of range
     */
    private static int parseIntInRange(String input, int min, int max, String fieldName) {
        try {
            int numericValue = Integer.parseInt(input);
            if (numericValue < min || numericValue > max) {
                throw new IllegalArgumentException(fieldName + " must be between " + min + " and " + max);
            }
            return numericValue;
        } catch (NumberFormatException numberFormatException) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
    }

    /**
     * Parses an ISO-8601 date string ({@code YYYY-MM-DD}) into a {@link LocalDate}.
     *
     * @param input the input date string (whitespace is trimmed)
     * @return the parsed {@link LocalDate}
     * @throws IllegalArgumentException if the input is not a valid ISO date
     */
    private static LocalDate parseDateStrict(String input) {
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException dateTimeParseException) {
            throw new IllegalArgumentException("date must be in YYYY-MM-DD (ISO) format");
        }
    }

    /**
     * Parses a time string in {@code HH:mm} or {@code HH:mm:ss} format into a {@link LocalTime}.
     * <p>
     * If {@code HH:mm} is provided, {@code :00} seconds are appended implicitly.
     *
     * @param input the input time string (whitespace is trimmed)
     * @return the parsed {@link LocalTime}
     * @throws IllegalArgumentException if the input is not in an accepted time format
     */
    private static LocalTime parseTimeLenient(String input) {
        String timeString = input.trim();
        try {
            long colonCount = timeString.chars().filter(ch -> ch == ':').count();
            if (colonCount == 2) {
                return LocalTime.parse(timeString);
            }
            if (colonCount == 1) {
                return LocalTime.parse(timeString + ":00");
            }
            throw new IllegalArgumentException("time must be HH:mm or HH:mm:ss");
        } catch (DateTimeParseException dateTimeParseException) {
            throw new IllegalArgumentException("time must be HH:mm or HH:mm:ss");
        }
    }

    /**
     * Prints concise help text describing the supported commands and their purpose.
     * <p>
     * <b>Note:</b> Filter key names and formats are kept consistent with parser constants.
     */
    private void printHelp() {
        println("Commands:");
        println("  all                 - Show all reviews");
        println("  id <id>             - Show review by ID");
        println("  filters             - List available filters");
        println("  filter <criteria>   - Filter reviews (e.g., author=John," + MIN_RATING + "=3)");
        println("  search <kw1,kw2>    - Search reviews by keywords (e.g., best app,worst app)");
        println("  stats               - Show review statistics");
        println("  distr               - Show rating distribution");
        println("  monthly             - Show monthly average ratings");
        println("  clear               - Clear the terminal");
        println("  exit                - Exit the program");
    }

    /**
     * Prints the available filter keys, expected formats, and sample usage.
     * <p>
     * Dates are ISO ({@code YYYY-MM-DD}); times are {@code HH:mm} or {@code HH:mm:ss}.
     * Time-based filters are only effective if the underlying data includes time-of-day.
     */
    private void printFilters() {
        println("Available Filters (key=value, comma-separated). Dates are ISO (YYYY-MM-DD).");
        println(" " + RATING      + "       : Integer  [1..5]         e.g., " + RATING + "=5");
        println(" " + MIN_RATING  + "   : Integer  [1..5]         e.g., " + MIN_RATING + "=3");
        println(" " + MAX_RATING  + "   : Integer  [1..5]         e.g., " + MAX_RATING + "=5");
        println(" " + AUTHOR      + "        : String                 e.g., " + AUTHOR + "=John Doe");
        println(" " + TITLE       + "         : String                 e.g., " + TITLE + "=iPhone 13");
        println(" " + PRODUCT     + "   : String                 e.g., " + PRODUCT + "=iPhone 13");
        println(" " + DATE        + "          : YYYY-MM-DD            e.g., " + DATE + "=2018-01-01");
        println(" " + STORE       + "         : String                 e.g., " + STORE + "=Amazon");
        println(" " + SORT_DATE   + "    : Boolean                e.g., " + SORT_DATE + "=true");
        println(" " + SORT_RATING + " : Boolean                e.g., " + SORT_RATING + "=false");
        println(" " + START_DATE  + "    : YYYY-MM-DD            e.g., " + START_DATE + "=2018-01-01");
        println(" " + END_DATE    + "      : YYYY-MM-DD            e.g., " + END_DATE + "=2018-12-31");
        println(" " + START_TIME  + "    : HH:mm[:ss] (effective only if review time exists)");
        println(" " + END_TIME    + "      : HH:mm[:ss] (effective only if review time exists)");
        println("Example: filter " + RATING + "=5," + STORE + "=Amazon," + DATE + "=2018-01-01");
    }

    /**
     * Reads a line from the given {@link BufferedReader}.
     *
     * @param reader the buffered reader (non-null)
     * @return the line read; may be {@code null} on EOF
     * @throws IOException if an I/O error occurs while reading
     */
    private static String readLineSafe(BufferedReader reader) throws IOException {
        return reader.readLine(); // may return null (EOF) – caller handles
    }

    /**
     * Case-insensitive string equality.
     *
     * @param firstString  the first string
     * @param secondString the second string
     * @return {@code true} if both are equal ignoring case; otherwise {@code false}
     */
    private static boolean equalsIgnoreCase(String firstString, String secondString) {
        return firstString.equalsIgnoreCase(secondString);
    }

    /**
     * Prints a line to {@code System.out} with a trailing newline.
     *
     * @param message the text to print (non-null)
     */
    private static void println(String message) {
        System.out.println(message);
    }

    /**
     * Prints text to {@code System.out} without a trailing newline.
     *
     * @param text the text to print (non-null)
     */
    private static void print(String text) {
        System.out.print(text);
    }

    /**
     * Attempts to clear the terminal screen.
     * <p>
     * On Windows consoles (detection is heuristic), prints multiple newlines.
     * On ANSI-capable terminals (Linux/macOS), emits {@code ESC[H ESC[2J}.
     * If clearing is not possible, prints a warning.
     * <p>
     * This method is best-effort and may not work on all terminals/IDEs.
     */
    private void clearScreen() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                for (int i = 0; i < 50; i++) System.out.println();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception exception) {
            System.out.println("Unable to clear the terminal.");
        }
    }
}
