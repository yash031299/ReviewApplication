package com.reviewapp.boot.factory;

import com.reviewapp.boot.config.DataStoreConfig;
import com.reviewapp.domain.port.ReviewQueryPort;
import com.reviewapp.domain.port.ReviewStatsPort;
import com.reviewapp.domain.port.ReviewWritePort;
import com.reviewapp.adapter.jdbc.SqliteReviewRepository;
import com.reviewapp.adapter.memory.InMemoryReviewRepository;

/**
 * Factory class for creating repository bundles based on the provided DataStoreConfig.
 * This class cannot be instantiated.
 */
public final class ReviewRepositoryFactory {

    /** Private constructor to prevent instantiation of the factory class. */
    private ReviewRepositoryFactory() {}

    /**
     * Creates a RepositoryBundle based on the given DataStoreConfig.
     *
     * @param config the configuration specifying the type of data store
     * @return RepositoryBundle containing the appropriate repository implementations
     * @throws IllegalArgumentException if the data store type is unsupported
     */
    public static RepositoryBundle create(DataStoreConfig config) {
        if (config.getType() == null) {
            throw new IllegalArgumentException("DataStore type cannot be null");
        }
        switch (config.getType()) {
            case SQLITE_JDBC: {
                // Use the same SqliteReviewRepository instance for all ports
                SqliteReviewRepository repo = new SqliteReviewRepository(config.getJdbcUrl());
                return new RepositoryBundle(repo, repo, repo);
            }
            case IN_MEMORY: {
                // Use the same InMemoryReviewRepository instance for all ports
                InMemoryReviewRepository repo = new InMemoryReviewRepository();
                return new RepositoryBundle(repo, repo, repo);
            }
            default:
                throw new IllegalArgumentException("Unsupported datastore: " + config.getType());
        }
    }

    /**Container for holding repository port implementations.*/
    public static final class RepositoryBundle {
        private final ReviewQueryPort queryPort;
        private final ReviewWritePort writePort;
        private final ReviewStatsPort statsPort;

        /**
         * Constructs a RepositoryBundle with the specified ports.
         *
         * @param queryPort the implementation of ReviewQueryPort
         * @param writePort the implementation of ReviewWritePort
         * @param statsPort the implementation of ReviewStatsPort
         */
        public RepositoryBundle(ReviewQueryPort queryPort, ReviewWritePort writePort, ReviewStatsPort statsPort) {
            this.queryPort = queryPort;
            this.writePort = writePort;
            this.statsPort = statsPort;
        }
        /**
         * Returns the ReviewQueryPort implementation.
         * @return query port
         */
        public ReviewQueryPort query() {
            return queryPort;
        }
        /**
         * Returns the ReviewWritePort implementation.
         * @return write port
         */
        public ReviewWritePort write() {
            return writePort;
        }
        /**
         * Returns the ReviewStatsPort implementation.
         * @return stats port
         */
        public ReviewStatsPort stats() {
            return statsPort;
        }
    }
}
