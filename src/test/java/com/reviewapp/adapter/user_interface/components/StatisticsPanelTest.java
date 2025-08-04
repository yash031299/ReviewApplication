package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StatisticsPanel} covering construction and component checks.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class StatisticsPanelTest {

    /**
     * Tests that constructing StatisticsPanel with a valid service does not throw.
     */
    @Test
    void givenValidStatisticsService_whenPanelConstructed_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new StatisticsPanel(mockService));
    }

    /**
     * Tests that the panel contains a button component after construction.
     */
    @Test
    void givenPanel_whenGetComponents_thenHasButton() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act
        boolean hasButton = containsButton(panel);
        // Assert
        assertTrue(hasButton, "Panel should have at least one button");
    }

    /**
     * Recursively checks if a container or its children contain a JButton.
     */
    private boolean containsButton(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) return true;
            if (c instanceof Container) {
                if (containsButton((Container) c)) return true;
            }
        }
        return false;
    }

    /**
     * Tests that rendering overview with null input does not throw.
     */
    @Test
    void givenNullInput_whenRenderOverview_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderOverview(panel, null));
    }

    /**
     * Tests that rendering overview with empty input does not throw.
     */
    @Test
    void givenEmptyInput_whenRenderOverview_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderOverview(panel, Collections.emptyMap()));
    }

    /**
     * Tests that rendering distribution with null input does not throw.
     */
    @Test
    void givenNullInput_whenRenderDistribution_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderDistribution(panel, null));
    }

    /**
     * Tests that rendering distribution with empty input does not throw.
     */
    @Test
    void givenEmptyInput_whenRenderDistribution_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderDistribution(panel, Collections.emptyMap()));
    }

    /**
     * Tests that invoking the Swing worker error handler with a simulated error does not throw.
     */
    @Test
    void givenSimulatedError_whenSwingWorkerErrorHandlerInvoked_thenNoException() throws Exception {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnLoadError(panel, new RuntimeException("Simulated error")));
    }

    // Helper to access private method via reflection
    private void invokeRenderOverview(StatisticsPanel panel, Map<?, ?> overview) {
        try {
            var method = panel.getClass().getDeclaredMethod("renderOverview", Map.class);
            method.setAccessible(true);
            method.invoke(panel, overview);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeRenderDistribution(StatisticsPanel panel, Map<?, ?> distribution) {
        try {
            var method = panel.getClass().getDeclaredMethod("renderDistribution", Map.class);
            method.setAccessible(true);
            method.invoke(panel, distribution);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeOnLoadError(StatisticsPanel panel, Throwable t) {
        try {
            var method = panel.getClass().getDeclaredMethod("onLoadError", Throwable.class);
            method.setAccessible(true);
            method.invoke(panel, t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
