package com.example.reviewapp.adapter.ui.components;

import com.example.reviewapp.domain.model.Filters;
import com.example.reviewapp.domain.model.Review;
import com.example.reviewapp.application.service.ReviewService;
import com.example.reviewapp.adapter.ui.util.AlertDialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Panel for applying filters to reviews with paginated results and a details pane.
 * <p>
 * This component depends only on {@link ReviewService}. It performs data loading
 * asynchronously via {@link SwingWorker} to keep the UI responsive.
 * <p>
 * It shows user-friendly popups (via {@link AlertDialogs}) for errors and no-result cases.
 */
public class FilterReviewsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    // Filters state
    private Filters lastFilters;

    // Paging state
    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize;

    // UI
    private final JTextField authorField = new JTextField(10);
    private final JTextField minRatingField = new JTextField(5);
    private final JTextField maxRatingField = new JTextField(5);
    private final JTextField titleField = new JTextField(10);
    private final JTextField productField = new JTextField(10);
    private final JTextField storeField = new JTextField(10);
    private final JTextField dateField = new JTextField(10);
    private final JTextField minDateField = new JTextField(10);
    private final JTextField maxDateField = new JTextField(10);
    private final JTextField minTimeField = new JTextField(10);
    private final JTextField maxTimeField = new JTextField(10);
    private final JCheckBox sortByDateBox = new JCheckBox("Sort by Date");
    private final JCheckBox sortByRatingsBox = new JCheckBox("Sort by Ratings");

    private final JButton applyFiltersButton = new JButton("Apply Filters");
    private final JButton prevButton = new JButton("Previous");
    private final JButton nextButton = new JButton("Next");
    private final JLabel pageLabel = new JLabel("Page 1 of 1");

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextArea detailsArea;

    public FilterReviewsPanel(ReviewService reviewService) {
        this(reviewService, DEFAULT_PAGE_SIZE);
    }

    public FilterReviewsPanel(ReviewService reviewService, int pageSize) {
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
        if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
        this.pageSize = pageSize;

        setLayout(new BorderLayout());

        // Filters form (top)
        add(buildFiltersForm(), BorderLayout.NORTH);

        // Table (center)
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Rating", "Date", "Product", "Store"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Long.class;    // ID
                    case 3: return Integer.class; // Rating
                    default: return String.class;
                }
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        configureColumnWidths(table.getColumnModel());

        // Details (bottom)
        detailsArea = new JTextArea(5, 80);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Review Text"));

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(detailsScroll, BorderLayout.SOUTH);

        // Selection listener for details
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = table.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    Object idObj = tableModel.getValueAt(modelRow, 0);
                    Long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : null;
                    if (id != null) {
                        Review review = reviewService.getReviewById(id);
                        detailsArea.setText(review != null ? nullToEmpty(review.getReviewText()) : "");
                    } else {
                        detailsArea.setText("");
                    }
                } else {
                    detailsArea.setText("");
                }
            }
        });

        // Initial state (no data); user applies filters to load
        updatePaginationControls();
    }

    // ------------------------------
    // UI building
    // ------------------------------

    private JComponent buildFiltersForm() {
        JPanel container = new JPanel(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(0, 2, 5, 5));
        grid.add(new JLabel("Author:"));                      grid.add(authorField);
        grid.add(new JLabel("Min Rating:"));                  grid.add(minRatingField);
        grid.add(new JLabel("Max Rating:"));                  grid.add(maxRatingField);
        grid.add(new JLabel("Title:"));                       grid.add(titleField);
        grid.add(new JLabel("Product Name:"));                grid.add(productField);
        grid.add(new JLabel("Store:"));                       grid.add(storeField);
        grid.add(new JLabel("Date (YYYY-MM-DD):"));           grid.add(dateField);
        grid.add(new JLabel("Min Date (YYYY-MM-DD):"));       grid.add(minDateField);
        grid.add(new JLabel("Max Date (YYYY-MM-DD):"));       grid.add(maxDateField);
        grid.add(new JLabel("Min Time (HH:mm or HH:mm:ss):"));grid.add(minTimeField);
        grid.add(new JLabel("Max Time (HH:mm or HH:mm:ss):"));grid.add(maxTimeField);
        grid.add(sortByDateBox);                               grid.add(sortByRatingsBox);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        applyFiltersButton.addActionListener(e -> onApplyFilters());
        actions.add(applyFiltersButton);

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        prevButton.addActionListener(e -> loadPageAsync(lastFilters, currentPage - 1));
        nextButton.addActionListener(e -> loadPageAsync(lastFilters, currentPage + 1));
        pager.add(prevButton);
        pager.add(pageLabel);
        pager.add(nextButton);

        container.add(grid, BorderLayout.CENTER);
        container.add(actions, BorderLayout.WEST);
        container.add(pager, BorderLayout.EAST);
        return container;
    }

    // ------------------------------
    // Actions
    // ------------------------------

    private void onApplyFilters() {
        try {
            ParsedInputs parsed = validateAndParseForm(); // validates & returns parsed values
            if (parsed == null) {
                // Validation already showed an alert; just return
                return;
            }
            // Build filters from parsed values (no more parsing here)
            Filters filters = buildFiltersFromParsed(parsed);
            this.lastFilters = filters;
            loadPageAsync(filters, 1);
        } catch (Exception ex) {
            AlertDialogs.error(this, "Error", "Failed to apply filters: " + ex.getMessage());
        }
    }

    private Filters buildFiltersFromParsed(ParsedInputs parsed) {
        Filters.Builder builder = new Filters.Builder();

        parsed.author().ifPresent(builder::setAuthorName);
        parsed.minRating().ifPresent(builder::setMinRating);
        parsed.maxRating().ifPresent(builder::setMaxRating);
        parsed.title().ifPresent(builder::setReviewTitle);
        parsed.product().ifPresent(builder::setProductName);
        parsed.store().ifPresent(builder::setStoreName);
        parsed.date().ifPresent(builder::setReviewDate);
        parsed.startDate().ifPresent(builder::setStartDate);
        parsed.endDate().ifPresent(builder::setEndDate);
        parsed.startTime().ifPresent(builder::setStartTime);
        parsed.endTime().ifPresent(builder::setEndTime);
        builder.setSortByDate(sortByDateBox.isSelected());
        builder.setSortByRating(sortByRatingsBox.isSelected());

        return builder.build(); // builder still validates ranges internally
    }

    // ------------------------------
    // Data loading (async)
    // ------------------------------

    private void loadPageAsync(Filters filters, int requestedPage) {
        if (filters == null) {
            AlertDialogs.info(this, "No Filters", "Please enter some criteria and click 'Apply Filters'.");
            return;
        }

        setLoading(true);

        new SwingWorker<PageData, Void>() {
            @Override
            protected PageData doInBackground() {
                int total = reviewService.getFilteredReviewCount(filters);
                int pages = Math.max(1, (int) Math.ceil((double) total / pageSize));
                int page = clamp(requestedPage, 1, pages);
                List<Review> rows = (total == 0) ? List.of() : reviewService.getFilteredReviewsPage(filters, page, pageSize);
                return new PageData(total, page, pages, rows);
            }

            @Override
            protected void done() {
                try {
                    PageData data = get();
                    currentPage = data.page();
                    totalPages = data.totalPages();
                    refreshTable(data.reviews());

                    // Inform user if no results (on first page request)
                    if (data.total() == 0) {
                        pageLabel.setText("No results");
                        AlertDialogs.info(FilterReviewsPanel.this, "No Results", "No reviews matched the provided filters.");
                    } else {
                        updatePaginationControls();
                    }
                } catch (Exception ex) {
                    AlertDialogs.error(FilterReviewsPanel.this, "Load Error", "Failed to load filtered reviews: " + ex.getMessage());
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    // ------------------------------
    // UI helpers
    // ------------------------------

    private void refreshTable(List<Review> reviews) {
        tableModel.setRowCount(0);
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        for (Review r : reviews) {
            tableModel.addRow(new Object[]{
                    r.getReviewId(),
                    nullToEmpty(r.getReviewTitle()),
                    nullToEmpty(r.getAuthorName()),
                    r.getProductRating(),
                    r.getReviewedDate(),
                    nullToEmpty(r.getProductName()),
                    nullToEmpty(r.getReviewSource())
            });
        }
    }

    private void updatePaginationControls() {
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        applyFiltersButton.setEnabled(!loading);
        prevButton.setEnabled(!loading && currentPage > 1);
        nextButton.setEnabled(!loading && currentPage < totalPages);
    }

    private static void configureColumnWidths(TableColumnModel columns) {
        columns.getColumn(0).setPreferredWidth(60);   // ID
        columns.getColumn(1).setPreferredWidth(220);  // Title
        columns.getColumn(2).setPreferredWidth(140);  // Author
        columns.getColumn(3).setPreferredWidth(60);   // Rating
        columns.getColumn(4).setPreferredWidth(100);  // Date
        columns.getColumn(5).setPreferredWidth(160);  // Product
        columns.getColumn(6).setPreferredWidth(120);  // Store
    }

    private static int parseInt(String value, String field, int min, int max) {
        try {
            int v = Integer.parseInt(value.trim());
            if (v < min || v > max) throw new IllegalArgumentException(field + " must be between " + min + " and " + max);
            return v;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(field + " must be an integer");
        }
    }

    private static LocalTime parseTime(String value) {
        String v = value.trim();
        long colons = v.chars().filter(ch -> ch == ':').count();
        if (colons == 1) v += ":00"; // HH:mm -> HH:mm:ss
        return LocalTime.parse(v);   // throws DateTimeParseException if invalid
    }

    private static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    // DTO for page transfer
    private record PageData(int total, int page, int totalPages, List<Review> reviews) {}

    // ---- Validation & parsing ----

    /**
     * Validates user input fields and returns a ParsedInputs object if valid.
     * Shows a single consolidated error dialog when invalid.
     *
     * @return ParsedInputs or null if validation failed (and alert was shown)
     */
    private ParsedInputs validateAndParseForm() {
        String author = trimToNull(authorField.getText());
        String title  = trimToNull(titleField.getText());
        String product= trimToNull(productField.getText());
        String store  = trimToNull(storeField.getText());

        String minRatingStr = trimToNull(minRatingField.getText());
        String maxRatingStr = trimToNull(maxRatingField.getText());

        String dateStr      = trimToNull(dateField.getText());
        String startDateStr = trimToNull(minDateField.getText());
        String endDateStr   = trimToNull(maxDateField.getText());

        String startTimeStr = trimToNull(minTimeField.getText());
        String endTimeStr   = trimToNull(maxTimeField.getText());

        // Collect all errors to show at once
        StringBuilder errors = new StringBuilder();

        // Ratings
        Integer minRating = null;
        Integer maxRating = null;
        try {
            if (minRatingStr != null) minRating = parseIntInRange(minRatingStr, 1, 5, "Min Rating");
        } catch (IllegalArgumentException ex) {
            errors.append("• ").append(ex.getMessage()).append("\n");
        }
        try {
            if (maxRatingStr != null) maxRating = parseIntInRange(maxRatingStr, 1, 5, "Max Rating");
        } catch (IllegalArgumentException ex) {
            errors.append("• ").append(ex.getMessage()).append("\n");
        }
        if (minRating != null && maxRating != null && minRating > maxRating) {
            errors.append("• Min Rating cannot be greater than Max Rating.\n");
        }

        // Dates
        java.time.LocalDate date = null;
        java.time.LocalDate startDate = null;
        java.time.LocalDate endDate = null;

        try {
            if (dateStr != null) date = parseFlexibleDate(dateStr);
        } catch (Exception ex) {
            errors.append("• Date must be in format YYYY-MM-DD, YYYY/MM/DD, or MM/DD/YYYY.\n");
        }
        try {
            if (startDateStr != null) startDate = parseFlexibleDate(startDateStr);
        } catch (Exception ex) {
            errors.append("• Min Date must be in format YYYY-MM-DD, YYYY/MM/DD, or MM/DD/YYYY.\n");
        }
        try {
            if (endDateStr != null) endDate = parseFlexibleDate(endDateStr);
        } catch (Exception ex) {
            errors.append("• Max Date must be in format YYYY-MM-DD, YYYY/MM/DD, or MM/DD/YYYY.\n");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            errors.append("• Min Date cannot be after Max Date.\n");
        }

        // Times
        java.time.LocalTime startTime = null;
        java.time.LocalTime endTime   = null;
        try {
            if (startTimeStr != null) startTime = parseTimeLenient(startTimeStr, "Min Time");
        } catch (IllegalArgumentException ex) {
            errors.append("• ").append(ex.getMessage()).append("\n");
        }
        try {
            if (endTimeStr != null) endTime = parseTimeLenient(endTimeStr, "Max Time");
        } catch (IllegalArgumentException ex) {
            errors.append("• ").append(ex.getMessage()).append("\n");
        }
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            errors.append("• Min Time cannot be after Max Time.\n");
        }

        // Optional: simple length sanity checks (avoid absurd inputs)
        if (author != null && author.length() > 200)  errors.append("• Author is too long (max 200 chars).\n");
        if (title  != null && title.length()  > 300)  errors.append("• Title is too long (max 300 chars).\n");
        if (product!= null && product.length()> 200)  errors.append("• Product Name is too long (max 200 chars).\n");
        if (store  != null && store.length()  > 200)  errors.append("• Store is too long (max 200 chars).\n");

        if (errors.length() > 0) {
            AlertDialogs.error(this, "Invalid Filters", errors.toString().trim());
            return null;
        }

        return new ParsedInputs(
                opt(author), opt(title), opt(product), opt(store),
                opt(minRating), opt(maxRating),
                opt(date), opt(startDate), opt(endDate),
                opt(startTime), opt(endTime)
        );
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static java.util.Optional<String> opt(String v) { return java.util.Optional.ofNullable(v); }
    private static java.util.Optional<Integer> opt(Integer v) { return java.util.Optional.ofNullable(v); }
    private static java.util.Optional<java.time.LocalDate> opt(java.time.LocalDate v) { return java.util.Optional.ofNullable(v); }
    private static java.util.Optional<java.time.LocalTime> opt(java.time.LocalTime v) { return java.util.Optional.ofNullable(v); }

    private static int parseIntInRange(String s, int min, int max, String fieldLabel) {
        try {
            int v = Integer.parseInt(s.trim());
            if (v < min || v > max) throw new IllegalArgumentException(fieldLabel + " must be between " + min + " and " + max + ".");
            return v;
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(fieldLabel + " must be an integer.");
        }
    }

    /**
     * Accepts HH:mm or HH:mm:ss. If HH:mm provided, seconds default to :00.
     */
    private static java.time.LocalTime parseTimeLenient(String s, String fieldLabel) {
        String t = s.trim();
        long colons = t.chars().filter(ch -> ch == ':').count();
        if (colons == 1) t += ":00";
        try {
            return java.time.LocalTime.parse(t);
        } catch (Exception ex) {
            throw new IllegalArgumentException(fieldLabel + " must be HH:mm or HH:mm:ss.");
        }
    }

    private static LocalDate parseFlexibleDate(String input) {
        String v = input.trim();
        if (v.isEmpty()) throw new IllegalArgumentException("Empty date");
        // Try ISO first
        try { return LocalDate.parse(v); } catch (Exception ignore) {}
        // Try yyyy/MM/dd
        try { return LocalDate.parse(v.replace('/', '-')); } catch (Exception ignore) {}
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
        throw new IllegalArgumentException("Unrecognized date format: " + input);
    }

    /** Holder for parsed & validated inputs so we don't parse twice. */
    private record ParsedInputs(
            java.util.Optional<String> author,
            java.util.Optional<String> title,
            java.util.Optional<String> product,
            java.util.Optional<String> store,
            java.util.Optional<Integer> minRating,
            java.util.Optional<Integer> maxRating,
            java.util.Optional<java.time.LocalDate> date,
            java.util.Optional<java.time.LocalDate> startDate,
            java.util.Optional<java.time.LocalDate> endDate,
            java.util.Optional<java.time.LocalTime> startTime,
            java.util.Optional<java.time.LocalTime> endTime
    ) {}

}
