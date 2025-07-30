package com.example.reviewapp.adapter.ui.components;

import com.example.reviewapp.domain.model.Review;
import com.example.reviewapp.application.service.ReviewService;
import com.example.reviewapp.adapter.ui.util.AlertDialogs;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Panel for fetching and displaying a single review by its ID.
 * <p>
 * Asynchronous fetch via SwingWorker keeps the UI responsive.
 * Validation errors and not-found cases are shown via AlertDialogs.
 */
public class ReviewByIdPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ReviewService reviewService;

    // UI
    private final JTextField idField = new JTextField(12);
    private final JButton fetchButton = new JButton("Fetch Review");
    private final JTextArea reviewArea = new JTextArea(10, 70);

    public ReviewByIdPanel(ReviewService reviewService) {
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
        setLayout(new BorderLayout());

        // Input row
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(new JLabel("Review ID:"));
        inputPanel.add(idField);
        inputPanel.add(fetchButton);

        // Output area
        reviewArea.setEditable(false);
        reviewArea.setLineWrap(true);
        reviewArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(reviewArea);

        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Actions
        fetchButton.addActionListener(e -> onFetch());
        idField.addActionListener(e -> onFetch()); // press Enter to fetch
    }

    private void onFetch() {
        String raw = idField.getText() != null ? idField.getText().trim() : "";
        if (raw.isEmpty()) {
            AlertDialogs.warn(this, "Missing Input", "Please enter a review ID.");
            idField.requestFocusInWindow();
            return;
        }

        long id;
        try {
            id = Long.parseLong(raw);
            if (id <= 0) {
                AlertDialogs.warn(this, "Invalid ID", "Review ID must be a positive integer.");
                idField.requestFocusInWindow();
                return;
            }
        } catch (NumberFormatException nfe) {
            AlertDialogs.warn(this, "Invalid ID", "Please enter a valid integer for the review ID.");
            idField.requestFocusInWindow();
            return;
        }

        // Async load
        setBusy(true);
        reviewArea.setText(""); // clear previous
        new SwingWorker<Review, Void>() {
            @Override
            protected Review doInBackground() {
                return reviewService.getReviewById(id);
            }

            @Override
            protected void done() {
                try {
                    Review review = get();
                    if (review == null) {
                        AlertDialogs.info(ReviewByIdPanel.this, "Not Found", "No review found for ID: " + id);
                        reviewArea.setText("");
                    } else {
                        reviewArea.setText(formatReview(review));
                        reviewArea.setCaretPosition(0);
                    }
                } catch (Exception ex) {
                    AlertDialogs.error(ReviewByIdPanel.this, "Error", "Failed to fetch review: " + ex.getMessage());
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void setBusy(boolean busy) {
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        fetchButton.setEnabled(!busy);
        idField.setEnabled(!busy);
    }

    private static String formatReview(Review r) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ID: ").append(r.getReviewId()).append('\n');
        sb.append("Title: ").append(nvl(r.getReviewTitle())).append('\n');
        sb.append("Author: ").append(nvl(r.getAuthorName())).append('\n');
        sb.append("Rating: ").append(r.getProductRating()).append('\n');
        sb.append("Date: ").append(r.getReviewedDate()).append('\n');
        sb.append("Product: ").append(nvl(r.getProductName())).append('\n');
        sb.append("Store: ").append(nvl(r.getReviewSource())).append("\n\n");
        sb.append("Review Text:\n").append(nvl(r.getReviewText()));
        return sb.toString();
    }

    private static String nvl(String s) {
        return (s == null) ? "" : s;
    }
}
