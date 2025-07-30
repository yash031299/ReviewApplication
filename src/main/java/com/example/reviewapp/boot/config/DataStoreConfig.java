package com.example.reviewapp.boot.config;

/**
 * Immutable configuration for selecting the application's review data storage backend.
 * <p>
 * Supports multiple datastore types (e.g., SQLite via JDBC, in-memory for testing/demo).
 * Use {@link #sqlite(String)} or {@link #inMemory()} to construct configurations for
 * the desired backend. This enables easy switching between production and test modes.
 * <ul>
 *   <li>{@code SQLITE_JDBC}: Uses a file-based SQLite database via JDBC. Requires a file path.</li>
 *   <li>{@code IN_MEMORY}: Uses an in-memory store (non-persistent, for tests/demos).</li>
 * </ul>
 * Example usage:
 * <pre>
 *   DataStoreConfig config = DataStoreConfig.sqlite("reviews.db");
 *   // or
 *   DataStoreConfig config = DataStoreConfig.inMemory();
 * </pre>
 */
/** Configuration class for specifying the type and connection details of a data store. */
public final class DataStoreConfig {
    /**
     * Enum representing supported data store types.
     */
    public enum DataStoreType {
        SQLITE_JDBC,
        IN_MEMORY 
    }

    /** The type of data store (e.g., SQLITE_JDBC or IN_MEMORY). */
    private final DataStoreType dataStoreType;
    /** The JDBC URL for database connections. Only used by JDBC implementations; null for in-memory. */
    private final String jdbcUrl;

    /**
     * Constructs a DataStoreConfig with the specified type and JDBC URL.
     *
     * @param dataStoreType the type of data store
     * @param jdbcUrl the JDBC URL (null for in-memory stores)
     */
    private DataStoreConfig(DataStoreType dataStoreType, String jdbcUrl) {
        this.dataStoreType = dataStoreType;
        this.jdbcUrl = jdbcUrl;
    }

    /**
     * Creates a DataStoreConfig for a SQLite JDBC database using the provided file path.
     * @param dbFilePath the file path for the SQLite database
     * @return a DataStoreConfig instance for the SQLite JDBC database
     */
    public static DataStoreConfig sqlite(String dbFilePath) {
        return new DataStoreConfig(DataStoreType.SQLITE_JDBC, "jdbc:sqlite:" + dbFilePath);
    }

    /**
     * Creates a DataStoreConfig for an in-memory data store implementation.
     * @return a DataStoreConfig instance for the in-memory data store
     */
    public static DataStoreConfig inMemory() {
        return new DataStoreConfig(DataStoreType.IN_MEMORY, null);
    }

    /**
     * Returns the type of data store (SQLITE_JDBC or IN_MEMORY).
     * @return the data store type
     */
    public DataStoreType getType() {
        return dataStoreType;
    }
    
    /**
     * Returns the JDBC URL if applicable, or null for in-memory stores.
     * @return the JDBC URL or null
     */
    public String getJdbcUrl() {
        return jdbcUrl;
    }
}
