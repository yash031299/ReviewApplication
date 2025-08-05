package com.reviewapp.boot.factory;

import com.reviewapp.boot.config.DataStoreConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ReviewRepositoryFactoryTest {

    @DisplayName("Verifies that all ports are created and are the same instance")
    @Test
    void create_whenSqliteConfig_returnsBundleWithAllPortsSameInstance() {
        // Arrange
        DataStoreConfig config = DataStoreConfig.sqlite("foo.db");
        // Act
        ReviewRepositoryFactory.RepositoryBundle bundle = ReviewRepositoryFactory.create(config);
        // Assert
        assertNotNull(bundle.query());
        assertNotNull(bundle.write());
        assertNotNull(bundle.stats());
        assertSame(bundle.query(), bundle.write());
        assertSame(bundle.write(), bundle.stats());
    }

    @DisplayName("Verifies that when in-memory config is used, then all ports are created and are the same instance")
    @Test
    void create_whenInMemoryConfig_returnsBundleWithAllPortsSameInstance() {
        // Arrange
        DataStoreConfig config = DataStoreConfig.inMemory();
        // Act
        ReviewRepositoryFactory.RepositoryBundle bundle = ReviewRepositoryFactory.create(config);
        // Assert
        assertNotNull(bundle.query());
        assertNotNull(bundle.write());
        assertNotNull(bundle.stats());
        assertSame(bundle.query(), bundle.write());
        assertSame(bundle.write(), bundle.stats());
    }

    @DisplayName("Verifies that when unsupported config type is used, then IllegalArgumentException is thrown")
    @Test
    void create_whenConfigTypeUnsupported_throwsIllegalArgumentException() {
        // Arrange
        DataStoreConfig config = Mockito.mock(DataStoreConfig.class);
        when(config.getType()).thenReturn(null);
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ReviewRepositoryFactory.create(config));
    }
}
