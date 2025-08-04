package com.reviewapp;

import com.reviewapp.boot.config.DataStoreConfig;
import com.reviewapp.boot.factory.ReviewRepositoryFactory;
import com.reviewapp.boot.factory.ReviewRepositoryFactory.RepositoryBundle;
import com.reviewapp.domain.model.Review;
import com.reviewapp.boot.bootstrap.FileStalenessChecker;
import com.reviewapp.application.service.ReviewService;
import com.reviewapp.application.service.StatisticsService;
import com.reviewapp.adapter.user_interface.ReviewAppMain;
import com.reviewapp.adapter.ingest.JsonParser;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== App Review System ===");
        final String jsonFileName = "alexa.json";

        try {
            // ------------------------------------------------------------------
            // 1) Choose datastore adapter (flip between SQLite and InMemory here)
            // ------------------------------------------------------------------
            // DataStoreConfig config = DataStoreConfig.inMemory();
            DataStoreConfig config = DataStoreConfig.sqlite("reviews.db");

            // Build ports (query/write/stats) via factory
            RepositoryBundle bundle = ReviewRepositoryFactory.create(config);

            // 2) Construct services using ports (no direct dependency on a concrete repo)
            ReviewService reviewService = new ReviewService(bundle.query(), bundle.write());
            StatisticsService statisticsService = new StatisticsService(bundle.stats());

            // ------------------------------------------------------------------
            // 3) Initial data load: parse JSON if DB is missing or stale
            // ------------------------------------------------------------------
            boolean parseRequiredFromJson = FileStalenessChecker.isParsingRequiredFromJson(jsonFileName);
            if (parseRequiredFromJson) {
                System.out.println("Parsing reviews from JSON file...");
                List<Review> reviews = JsonParser.parseReviewsFromFile(jsonFileName);
                System.out.println("Parsed " + reviews.size() + " reviews.");
                System.out.println("Saving reviews to datastore...");
                reviewService.saveReviews(reviews);
                System.out.println("Saved " + reviews.size() + " reviews.");
            } else {
                System.out.println("Using existing datastore...");
            }

            // ------------------------------------------------------------------
            // 4) Launch UI (depends only on services)
            // ------------------------------------------------------------------
            javax.swing.SwingUtilities.invokeLater(() -> {
                new ReviewAppMain(reviewService, statisticsService).setVisible(true);
            });

        } catch (Exception exception) {
            System.err.println("[ERROR] Unable to start the application: " + exception.getMessage());
            exception.printStackTrace(System.err);
        }
    }
}
