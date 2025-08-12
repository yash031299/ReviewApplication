package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.domain.model.Review;
import com.reviewapp.application.service.ReviewService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Displays a paginated table of reviews with a details pane.
 * <p>
 * This component depends only on {@link ReviewService} and is completely
 * infrastructure-agnostic. Data loading is performed asynchronously via
 * {@link SwingWorker} to keep the UI responsive.
 */
public class AllReviewsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ReviewService reviewService;

    private final JTable reviewTable;
    private final DefaultTableModel tableModel;
    private final JTextArea reviewDetailsArea;
    private final JButton prevButton;
    private final JButton nextButton;
    private final JLabel pageLabel;

    private int currentPage = 1;
    private int totalPages = 1;
    private final int pageSize;

    /**
     * Create the panel with the default page size (20).
     */
    public AllReviewsPanel(ReviewService reviewService) {
        this(reviewService, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create the panel.
     *
     * @param reviewService application service used to fetch data (non-null)
     * @param pageSize      items per page (must be &gt;= 1)
     */
    public AllReviewsPanel(ReviewService reviewService, int pageSize) {
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
        if (pageSize < 1) throw new IllegalArgumentException("pageSize must be >= 1");
        this.pageSize = pageSize;

        setLayout(new BorderLayout());

        // Top (pagination) bar
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel paginationPanel = new JPanel();
        prevButton = new JButton("Previous");
        nextButton = new JButton("Next");
        pageLabel = new JLabel("Page 1 of 1");
        prevButton.addActionListener(e -> loadPageAsync(currentPage - 1));
        nextButton.addActionListener(e -> loadPageAsync(currentPage + 1));
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);
        topPanel.add(paginationPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Table model (non-editable)
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Title", "Author", "Rating", "Date", "Product", "Store"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: return Long.class;   // ID
                    case 3: return Integer.class; // Rating
                    default: return String.class;
                }
            }
        };

        // Table
        reviewTable = new JTable(tableModel);
        reviewTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reviewTable.setAutoCreateRowSorter(true); // user can sort columns locally
        configureColumnWidths(reviewTable.getColumnModel());

        // Details area
        reviewDetailsArea = new JTextArea(5, 80);
        reviewDetailsArea.setEditable(false);
        reviewDetailsArea.setLineWrap(true);
        reviewDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScroll = new JScrollPane(reviewDetailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Review Text"));

        add(new JScrollPane(reviewTable), BorderLayout.CENTER);
        add(detailsScroll, BorderLayout.SOUTH);

        // Show details for the selected row
        reviewTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = reviewTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = reviewTable.convertRowIndexToModel(viewRow);
                    Object idObj = tableModel.getValueAt(modelRow, 0);
                    Long id = (idObj instanceof Number) ? ((Number) idObj).longValue() : null;
                    if (id != null) {
                        // Fetch from service (fast path; small single query is fine on EDT)
                        Review r = reviewService.getReviewById(id);
                        reviewDetailsArea.setText(r != null ? nullToEmpty(r.getReviewText()) : "");
                    } else {
                        reviewDetailsArea.setText("");
                    }
                } else {
                    reviewDetailsArea.setText("");
                }
            }
        });

        // Initial load
        loadPageAsync(1);
    }

    // --------------------------
    // Data loading & pagination
    // --------------------------

    /**
     * Loads a page asynchronously and updates the table when done.
     */
    private void loadPageAsync(int requestedPage) {
        setLoading(true);

        new SwingWorker<PageData, Void>() {
            @Override
            protected PageData doInBackground() {
                int totalReviews = reviewService.getTotalReviewCount();
                int computedTotalPages = Math.max(1, (int) Math.ceil((double) totalReviews / pageSize));
                int page = clamp(requestedPage, 1, computedTotalPages);

                List<Review> reviews = reviewService.getReviewsPage(page, pageSize);
                return new PageData(page, computedTotalPages, reviews);
            }

            @Override
            protected void done() {
                try {
                    PageData data = get();
                    currentPage = data.page();
                    totalPages = data.totalPages();
                    refreshTable(data.reviews());
                    updatePaginationControls();
                } catch (Exception ex) {
                    // Present a user-friendly message; keep the UI stable.
                    JOptionPane.showMessageDialog(
                            AllReviewsPanel.this,
                            "Failed to load reviews: " + ex.getMessage(),
                            "Load Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    setLoading(false);
                }
            }
        }.execute();
    }

    private void refreshTable(List<Review> reviews) {
        tableModel.setRowCount(0);
        if (reviews == null || reviews.isEmpty()) {
            pageLabel.setText("No reviews to display");
            return;
        }
        for (Review r : reviews) {
            if (r == null) continue;
            tableModel.addRow(new Object[]{
                    r.getReviewId(),
                    nullToEmpty(r.getReviewTitle()),
                    nullToEmpty(r.getAuthorName()),
                    r.getProductRating(),
                    r.getReviewedDate(), // LocalDate renders via toString()
                    nullToEmpty(r.getProductName()),
                    nullToEmpty(r.getReviewSource())
            });
        }
    }

    private void updatePaginationControls() {
        if (totalPages <= 0) totalPages = 1;
        pageLabel.setText("Page " + currentPage + " of " + totalPages);
        prevButton.setEnabled(currentPage > 1);
        nextButton.setEnabled(currentPage < totalPages);
    }

    private void setLoading(boolean loading) {
        setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        prevButton.setEnabled(!loading && currentPage > 1);
        nextButton.setEnabled(!loading && currentPage < totalPages);
    }

    // --------------------------
    // UI helpers
    // --------------------------

    private static void configureColumnWidths(TableColumnModel columns) {
        // Reasonable defaults; tweak to taste
        columns.getColumn(0).setPreferredWidth(60);   // ID
        columns.getColumn(1).setPreferredWidth(220);  // Title
        columns.getColumn(2).setPreferredWidth(140);  // Author
        columns.getColumn(3).setPreferredWidth(60);   // Rating
        columns.getColumn(4).setPreferredWidth(100);  // Date
        columns.getColumn(5).setPreferredWidth(160);  // Product
        columns.getColumn(6).setPreferredWidth(120);  // Store
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String nullToEmpty(String s) {
        return (s == null) ? "" : s;
    }

    public void onLoadError(Throwable t) { }

    // --------------------------
    // Simple DTO for page data
    // --------------------------

    private record PageData(int page, int totalPages, List<Review> reviews) {}
}
