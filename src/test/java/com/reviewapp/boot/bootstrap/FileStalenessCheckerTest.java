package com.reviewapp.boot.bootstrap;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FileStalenessChecker} covering file staleness logic, error handling, and edge cases.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class FileStalenessCheckerTest {

    /**
     * Tests that the utility class cannot be instantiated and throws AssertionError.
     */
    @Test
    void classLoadsWithoutError() {
        // Act & Assert
        assertThrows(AssertionError.class, FileStalenessChecker::new);
    }

    /**
     * Tests that passing null as the JSON path string throws NullPointerException.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonPathIsNull_throwsNullPointerException() {
        // Arrange
        // Act & Assert
        assertThrows(NullPointerException.class, () -> FileStalenessChecker.isParsingRequiredFromJson((String)null));
    }

    /**
     * Tests that passing a blank JSON path string throws IllegalArgumentException.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonPathIsBlank_throwsIllegalArgumentException() {
        // Arrange
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> FileStalenessChecker.isParsingRequiredFromJson("   "));
    }

    /**
     * Tests that passing a non-existent JSON file path throws IllegalArgumentException.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonFileDoesNotExist_throwsIllegalArgumentException() {
        // Arrange
        Path missing = Path.of("missing_file.json");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> FileStalenessChecker.isParsingRequiredFromJson(missing.toString()));
    }

    /**
     * Tests that parsing is required if the DB file does not exist.
     */
    @Test
    void isParsingRequiredFromJson_whenDbDoesNotExist_returnsTrue() throws IOException {
        // Arrange
        Path json = Files.createTempFile("json", ".json");
        Files.writeString(json, "{}\n");
        Path db = Path.of("nonexistent.db");
        // Act
        boolean result = FileStalenessChecker.isParsingRequiredFromJson(db, json);
        // Assert
        assertTrue(result);
        Files.delete(json);
    }

    /**
     * Tests that parsing is required if the JSON file is newer than the DB file.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonIsNewerThanDb_returnsTrue() throws IOException {
        // Arrange
        Path db = Files.createTempFile("db", ".db");
        Path json = Files.createTempFile("json", ".json");
        Files.writeString(db, "db");
        Files.writeString(json, "{}");
        FileTime oldTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        FileTime newTime = FileTime.fromMillis(System.currentTimeMillis());
        Files.setLastModifiedTime(db, oldTime);
        Files.setLastModifiedTime(json, newTime);
        // Act
        boolean result = FileStalenessChecker.isParsingRequiredFromJson(db, json);
        // Assert
        assertTrue(result);
        Files.delete(db);
        Files.delete(json);
    }

    /**
     * Tests that parsing is not required if the JSON file is older than the DB file.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonIsOlderThanDb_returnsFalse() throws IOException {
        // Arrange
        Path db = Files.createTempFile("db", ".db");
        Path json = Files.createTempFile("json", ".json");
        Files.writeString(db, "db");
        Files.writeString(json, "{}");
        FileTime oldTime = FileTime.fromMillis(System.currentTimeMillis() - 10000);
        FileTime newTime = FileTime.fromMillis(System.currentTimeMillis());
        Files.setLastModifiedTime(db, newTime);
        Files.setLastModifiedTime(json, oldTime);
        // Act
        boolean result = FileStalenessChecker.isParsingRequiredFromJson(db, json);
        // Assert
        assertFalse(result);
        Files.delete(db);
        Files.delete(json);
    }

    /**
     * Tests that an IO error while checking file times throws IllegalStateException.
     */
    @Test
    void isParsingRequiredFromJson_whenIoErrorOccurs_throwsIllegalStateException() throws Exception {
        Path db = Files.createTempFile("test", ".db");
        Path json = Files.createTempFile("test", ".json");
        try (var mocked = org.mockito.Mockito.mockStatic(java.nio.file.Files.class, org.mockito.Mockito.CALLS_REAL_METHODS)) {
            mocked.when(() -> java.nio.file.Files.getLastModifiedTime(json)).thenThrow(new java.io.IOException("Simulated IO error"));
            // Act & Assert
            assertThrows(IllegalStateException.class, () -> FileStalenessChecker.isParsingRequiredFromJson(db, json));
        } finally {
            Files.deleteIfExists(db);
            Files.deleteIfExists(json);
        }
    }

    /**
     * Tests that null DB path throws NullPointerException (Path overload).
     */
    @Test
    void isParsingRequiredFromJson_whenDbPathIsNull_throwsNullPointerException() {
        // Arrange
        Path json = Path.of("some.json");
        // Act & Assert
        assertThrows(NullPointerException.class, () -> FileStalenessChecker.isParsingRequiredFromJson(null, json));
    }

    /**
     * Tests that null JSON path throws NullPointerException (Path overload).
     */
    @Test
    void isParsingRequiredFromJson_whenJsonPathIsNullPathOverload_throwsNullPointerException() {
        // Arrange
        Path db = Path.of("some.db");
        // Act & Assert
        assertThrows(NullPointerException.class, () -> FileStalenessChecker.isParsingRequiredFromJson(db, null));
    }

    /**
     * Tests that a non-existent JSON file (Path overload) throws IllegalArgumentException.
     */
    @Test
    void isParsingRequiredFromJson_whenJsonFileDoesNotExistPathOverload_throwsIllegalArgumentException() {
        // Arrange
        Path db = Path.of("some.db");
        Path missingJson = Path.of("missing_file.json");
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> FileStalenessChecker.isParsingRequiredFromJson(db, missingJson));
    }

    /**
     * Tests that the utility class constructor throws AssertionError (duplicate for completeness).
     */
    @Test
    void constructor_whenInstantiated_throwsAssertionError() {
        // Arrange
        // Act & Assert
        assertThrows(AssertionError.class, FileStalenessChecker::new);
    }
}
