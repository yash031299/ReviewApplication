package com.example.reviewapp.boot.bootstrap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

/**
 * Utility for deciding whether the local review database should be (re)built
 * from a JSON source file, based on file modification times.
 * <p>
 * Semantics: parsing/import is required if
 * <ul>
 *   <li>the database file does not exist, or</li>
 *   <li>the JSON source file has a newer last-modified time than the DB.</li>
 * </ul>
 */
public final class FileStalenessChecker {

    /** Default SQLite file name used by the application when no explicit DB path is provided. */
    public static final String DEFAULT_DB_FILENAME = "reviews.db";

    /** Non-instantiable utility class. */
    private FileStalenessChecker() {
        throw new AssertionError("Do not instantiate utility class FileStalenessChecker");
    }

    /**
     * Determines whether JSON parsing is required using the default database file
     * ({@link #DEFAULT_DB_FILENAME}) in the current working directory.
     *
     * @param jsonFilePath path to the JSON source file (must exist)
     * @return {@code true} if parsing/import is required; {@code false} otherwise
     * @throws IllegalArgumentException if {@code jsonFilePath} is null/blank or the JSON file does not exist
     * @throws IllegalStateException if file metadata cannot be read (I/O error)
     */
    public static boolean isParsingRequiredFromJson(String jsonFilePath) {
        Objects.requireNonNull(jsonFilePath, "jsonFilePath must not be null");
        String trimmed = jsonFilePath.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("jsonFilePath must not be blank");
        }
        Path dbPath = Paths.get(DEFAULT_DB_FILENAME);
        Path jsonPath = Paths.get(trimmed);
        return isParsingRequiredFromJson(dbPath, jsonPath);
    }

    /**
     * Determines whether JSON parsing is required by comparing the JSON file's
     * last-modified time with the DB file's last-modified time.
     *
     * @param dbPath   path to the database file (may or may not exist)
     * @param jsonPath path to the JSON source file (must exist)
     * @return {@code true} if the DB does not exist or JSON is newer than DB; {@code false} otherwise
     * @throws IllegalArgumentException if {@code dbPath} or {@code jsonPath} is null, or if the JSON file does not exist
     * @throws IllegalStateException if file metadata cannot be read (I/O error)
     */
    public static boolean isParsingRequiredFromJson(Path dbPath, Path jsonPath) {
        Objects.requireNonNull(dbPath, "dbPath must not be null");
        Objects.requireNonNull(jsonPath, "jsonPath must not be null");

        if (!Files.exists(jsonPath)) {
            throw new IllegalArgumentException("JSON file does not exist: " + jsonPath.toAbsolutePath());
        }

        // If DB doesn't exist, we must (re)parse.
        if (!Files.exists(dbPath)) {
            return true;
        }

        try {
            FileTime jsonLastModified = Files.getLastModifiedTime(jsonPath);
            FileTime dbLastModified   = Files.getLastModifiedTime(dbPath);
            return jsonLastModified.compareTo(dbLastModified) > 0;
        } catch (IOException io) {
            // Escalate as unchecked since caller generally cannot recover here
            throw new IllegalStateException("Failed to read file modification times", io);
        }
    }
}
