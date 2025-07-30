package com.example.reviewapp.adapter.ui.components;

import com.example.reviewapp.domain.model.Review;
import com.example.reviewapp.application.service.ReviewService;
import com.example.reviewapp.adapter.ui.util.AlertDialogs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Panel to search reviews by keywords (comma-separated) with results table and details pane.
 * <p>
 * Validation ensures non-empty queries. Searching runs asynchronously to keep the UI responsive.
 * Selection shows the full review text below the table.
 */
public class SearchPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Optional safety cap to avoid pathological queries. Adjust as needed. */
    private static final int MAX_KEYWORDS = 20;

    private final ReviewService reviewService;

    // UI
    private final JTextField keywordField = new JTextField(28);
    private final JButton searchButton = new JButton("Search");

    private final DefaultTableModel tableModel;
    private final JTable resultTable;

    private final JTextArea detailsArea;

    public SearchPanel(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
        setLayout(new BorderLayout());

        // Top: search controls
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Keywords (comma separated):"));
        inputPanel.add(keywordField);
        inputPanel.add(searchButton);
        add(inputPanel, BorderLayout.NORTH);

        // Table model (non-editable)
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Rating", "Date", "Product", "Store"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Long.class;    // ID
                    case 3: return Integer.class; // Rating
                    default: return String.class;
                }
            }
        };

        // Table
        resultTable = new JTable(tableModel);
        resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultTable.setAutoCreateRowSorter(true);
        configureColumnWidths(resultTable.getColumnModel());

        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // Details pane
        detailsArea = new JTextArea(6, 80);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Review Text"));
        add(detailsScroll, BorderLayout.SOUTH);

        // Actions
        searchButton.addActionListener(e -> onSearch());
        keywordField.addActionListener(e -> onSearch()); // Enter to search

        // Table selection -> details
        resultTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = resultTable.getSelectedRow();
            if (viewRow < 0) {
                detailsArea.setText("");
                return;
            }
            int modelRow = resultTable.convertRowIndexToModel(viewRow);
            Object idObj = tableModel.getValueAt(modelRow, 0);
            Long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : null;
            if (id == null) {
                detailsArea.setText("");
                return;
            }
            // Small, fast lookup OK on EDT. If slow, move to SwingWorker.
            Review review = reviewService.getReviewById(id);
            detailsArea.setText(review != null ? nvl(review.getReviewText()) : "");
            detailsArea.setCaretPosition(0);
        });
    }

    // ---------------------------------
    // Actions
    // ---------------------------------

    private void onSearch() {
        List<String> keywords;
        try {
            keywords = parseAndValidateKeywords(keywordField.getText());
        } catch (IllegalArgumentException ex) {
            AlertDialogs.warn(this, "Invalid Search", ex.getMessage());
            keywordField.requestFocusInWindow();
            return;
        }

        setBusy(true);
        tableModel.setRowCount(0);
        detailsArea.setText("");

        new SwingWorker<List<Review>, Void>() {
            @Override
            protected List<Review> doInBackground() {
                return reviewService.getReviewsByKeywords(keywords);
            }

            @Override
            protected void done() {
                try {
                    List<Review> results = get();
                    if (results == null || results.isEmpty()) {
                        AlertDialogs.info(SearchPanel.this, "No Results", "No reviews matched the provided keywords.");
                        setResults(List.of());
                        return;
                    }
                    setResults(results);
                } catch (Exception ex) {
                    AlertDialogs.error(SearchPanel.this, "Search Error", "Failed to search reviews: " + ex.getMessage());
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    // ---------------------------------
    // Helpers
    // ---------------------------------

    private List<String> parseAndValidateKeywords(String raw) {
        String input = (raw == null) ? "" : raw.trim();
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Please enter one or more keywords.");
        }
        String[] tokens = input.split(",");
        List<String> keywords = new ArrayList<>(tokens.length);
        for (String t : tokens) {
            String k = t.trim();
            if (!k.isEmpty()) keywords.add(k);
        }
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("Please enter one or more non-empty keywords.");
        }
        if (keywords.size() > MAX_KEYWORDS) {
            throw new IllegalArgumentException("Too many keywords. Please provide at most " + MAX_KEYWORDS + ".");
        }
        return keywords;
    }

    private void setResults(List<Review> reviews) {
        tableModel.setRowCount(0);
        if (reviews == null || reviews.isEmpty()) {
            detailsArea.setText("");
            return;
        }
        for (Review r : reviews) {
            tableModel.addRow(new Object[]{
                    r.getReviewId(),
                    nvl(r.getReviewTitle()),
                    nvl(r.getAuthorName()),
                    r.getProductRating(),
                    r.getReviewedDate(), // LocalDate -> String via toString()
                    nvl(r.getProductName()),
                    nvl(r.getReviewSource())
            });
        }
        // Select first row by default (optional)
        if (resultTable.getRowCount() > 0) {
            resultTable.setRowSelectionInterval(0, 0);
        }
    }

    private void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        searchButton.setEnabled(!busy);
        keywordField.setEnabled(!busy);
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

    private static String nvl(String s) { return (s == null) ? "" : s; }
}
