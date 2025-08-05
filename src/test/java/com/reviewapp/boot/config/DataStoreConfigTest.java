package com.reviewapp.boot.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DataStoreConfig} covering configuration creation, type checking, and enum values.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class DataStoreConfigTest {

    @DisplayName("Tests that the DataStoreConfig constructor is private and cannot be accessed via reflection.")
    @Test
    void constructor_whenAccessedViaReflection_isPrivate() throws Exception {
        // Arrange
        var constructor = DataStoreConfig.class.getDeclaredConstructor();
        // Act
        constructor.setAccessible(true);
        // Assert
        assertThrows(Exception.class, constructor::newInstance);
    }


    @DisplayName("Tests that sqlite() creates a config with the correct JDBC URL.")
    @Test
    void sqlite_whenGivenFileName_createsJdbcConfigWithCorrectUrl() {
        // Arrange
        String dbFile = "foo.db";
        // Act
        DataStoreConfig config = DataStoreConfig.sqlite(dbFile);
        // Assert
        assertEquals(DataStoreConfig.DataStoreType.SQLITE_JDBC, config.getType());
        assertEquals("jdbc:sqlite:foo.db", config.getJdbcUrl());
    }


    @DisplayName("Tests that inMemory() creates a config with type IN_MEMORY and null JDBC URL.")
    @Test
    void inMemory_whenCalled_createsInMemoryConfigWithNullJdbcUrl() {
        // Arrange
        // Act
        DataStoreConfig config = DataStoreConfig.inMemory();
        // Assert
        assertEquals(DataStoreConfig.DataStoreType.IN_MEMORY, config.getType());
        assertNull(config.getJdbcUrl());
    }


    @DisplayName("Tests that sqlite() returns distinct config instances for the same file name.")
    @Test
    void sqlite_whenCalledTwiceWithSameFileName_returnsDistinctConfigs() {
        // Arrange
        // Act
        DataStoreConfig c1 = DataStoreConfig.sqlite("a.db");
        DataStoreConfig c2 = DataStoreConfig.sqlite("a.db");
        // Assert
        assertNotSame(c1, c2);
        assertEquals(c1.getJdbcUrl(), c2.getJdbcUrl());
    }


    @DisplayName("Tests that inMemory() returns a config with null JDBC URL.")
    @Test
    void inMemory_whenCalled_getJdbcUrlReturnsNull() {
        // Arrange
        // Act
        String jdbcUrl = DataStoreConfig.inMemory().getJdbcUrl();
        // Assert
        assertNull(jdbcUrl);
    }


    @DisplayName("Tests that getType() on a SQLite config returns SQLITE_JDBC.")
    @Test
    void getType_whenCalledOnSqliteConfig_returnsSqliteJdbc() {
        // Arrange
        DataStoreConfig config = DataStoreConfig.sqlite("b.db");
        // Act
        DataStoreConfig.DataStoreType type = config.getType();
        // Assert
        assertEquals(DataStoreConfig.DataStoreType.SQLITE_JDBC, type);
    }


    @DisplayName("Tests that getType() on an in-memory config returns IN_MEMORY.")
    @Test
    void getType_whenCalledOnInMemoryConfig_returnsInMemory() {
        // Arrange
        DataStoreConfig config = DataStoreConfig.inMemory();
        // Act
        DataStoreConfig.DataStoreType type = config.getType();
        // Assert
        assertEquals(DataStoreConfig.DataStoreType.IN_MEMORY, type);
    }


    @DisplayName("Tests that the DataStoreType enum contains SQLITE_JDBC and IN_MEMORY.")
    @Test
    void enumValues_shouldContainSqliteJdbcAndInMemory() {
        // Arrange
        // Act
        DataStoreConfig.DataStoreType[] values = DataStoreConfig.DataStoreType.values();
        // Assert
        assertTrue(java.util.Arrays.asList(values).contains(DataStoreConfig.DataStoreType.SQLITE_JDBC));
        assertTrue(java.util.Arrays.asList(values).contains(DataStoreConfig.DataStoreType.IN_MEMORY));
    }
}
