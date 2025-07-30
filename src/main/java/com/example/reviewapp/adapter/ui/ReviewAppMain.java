package com.example.reviewapp.adapter.ui;

import com.example.reviewapp.application.service.ReviewService;
import com.example.reviewapp.application.service.StatisticsService;
import com.example.reviewapp.adapter.ui.components.AllReviewsPanel;
import com.example.reviewapp.adapter.ui.components.FilterReviewsPanel;
import com.example.reviewapp.adapter.ui.components.ReviewByIdPanel;
import com.example.reviewapp.adapter.ui.components.SearchPanel;
import com.example.reviewapp.adapter.ui.components.StatisticsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * Top-level Swing window for the Review application.
 * <p>
 * This class depends only on application services and remains completely
 * decoupled from infrastructure (ports/adapters). The choice of datastore
 * (SQLite, in-memory, etc.) is made in the bootstrap (Main) and injected here
 * via the services.
 */
public class ReviewAppMain extends JFrame {

    private static final long serialVersionUID = 1L;

    private final ReviewService reviewService;
    private final StatisticsService statisticsService;

    /**
     * Constructs the main window with provided application services.
     *
     * @param reviewService     the review application service (non-null)
     * @param statisticsService the statistics application service (non-null)
     */
    public ReviewAppMain(ReviewService reviewService, StatisticsService statisticsService) {
        this.reviewService = Objects.requireNonNull(reviewService, "reviewService must not be null");
        this.statisticsService = Objects.requireNonNull(statisticsService, "statisticsService must not be null");

        setTitle("Review Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildTabs(), BorderLayout.CENTER);
    }

    /** Builds the tabbed UI using decoupled panels that each depend only on services. */
    private JTabbedPane buildTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("All Reviews", new AllReviewsPanel(reviewService));
        tabbedPane.addTab("Review by ID", new ReviewByIdPanel(reviewService));
        tabbedPane.addTab("Filter Reviews", new FilterReviewsPanel(reviewService));
        tabbedPane.addTab("Search", new SearchPanel(reviewService));
        tabbedPane.addTab("Statistics", new StatisticsPanel(statisticsService));
        return tabbedPane;
    }
}
