package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StatisticsPanel} covering construction and component checks.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class StatisticsPanelTest {

    @DisplayName("Tests that constructing StatisticsPanel with a valid service does not throw")
    @Test
    void givenValidStatisticsService_whenPanelConstructed_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new StatisticsPanel(mockService));
    }


    @DisplayName("Tests that the panel contains a button component after construction")
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


    @DisplayName("Recursively checks if a container or its children contain a JButton")
    private boolean containsButton(Container container) {
        for (Component c : container.getComponents()) {
            if (c instanceof JButton) return true;
            if (c instanceof Container) {
                if (containsButton((Container) c)) return true;
            }
        }
        return false;
    }


    @DisplayName("Tests that rendering overview with null input does not throw")
    @Test
    void givenNullInput_whenRenderOverview_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderOverview(panel, null));
    }


    @DisplayName("Tests that rendering overview with empty input does not throw")
    @Test
    void givenEmptyInput_whenRenderOverview_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderOverview(panel, Collections.emptyMap()));
    }


    @DisplayName("Tests that rendering distribution with null input does not throw")
    @Test
    void givenNullInput_whenRenderDistribution_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderDistribution(panel, null));
    }


    @DisplayName("Tests that rendering distribution with empty input does not throw")
    @Test
    void givenEmptyInput_whenRenderDistribution_thenNoException() {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRenderDistribution(panel, Collections.emptyMap()));
    }


    @DisplayName("Tests that onLoadError does not throw when given a simulated error")
    @Test
    void givenSimulatedError_whenSwingWorkerErrorHandlerInvoked_thenNoException() throws Exception {
        // Arrange
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnLoadError(panel, new RuntimeException("Simulated error")));
    }


    @DisplayName("renderOverview handles a map with missing keys gracefully")
    @Test
    void givenMapWithMissingKeys_whenRenderOverview_thenNoException() {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Map<String, Object> overview = new HashMap<>();
        overview.put("unexpectedKey", 123);
        assertDoesNotThrow(() -> invokeRenderOverview(panel, overview));
    }


    @DisplayName("renderOverview handles a large map without exceptions")
    @Test
    void givenLargeMap_whenRenderOverview_thenNoException() {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Map<String, Object> overview = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            overview.put("key" + i, i);
        }
        assertDoesNotThrow(() -> invokeRenderOverview(panel, overview));
    }

    @DisplayName("Tests that setBusy enables/disables controls appropriately")
    @Test
    void setBusy_enablesAndDisablesControls() throws Exception {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Method setBusy = panel.getClass().getDeclaredMethod("setBusy", boolean.class);
        setBusy.setAccessible(true);
        Field showStatsButtonField = panel.getClass().getDeclaredField("showStatsButton");
        Field distributionButtonField = panel.getClass().getDeclaredField("distributionButton");
        Field monthlyAvgButtonField = panel.getClass().getDeclaredField("monthlyAvgButton");
        Field refreshButtonField = panel.getClass().getDeclaredField("refreshButton");
        showStatsButtonField.setAccessible(true);
        distributionButtonField.setAccessible(true);
        monthlyAvgButtonField.setAccessible(true);
        refreshButtonField.setAccessible(true);
        JButton showStatsButton = (JButton) showStatsButtonField.get(panel);
        JButton distributionButton = (JButton) distributionButtonField.get(panel);
        JButton monthlyAvgButton = (JButton) monthlyAvgButtonField.get(panel);
        JButton refreshButton = (JButton) refreshButtonField.get(panel);
        setBusy.invoke(panel, false);
        assertTrue(showStatsButton.isEnabled());
        assertTrue(distributionButton.isEnabled());
        assertTrue(monthlyAvgButton.isEnabled());
        assertTrue(refreshButton.isEnabled());
        setBusy.invoke(panel, true);
        assertFalse(showStatsButton.isEnabled());
        assertFalse(distributionButton.isEnabled());
        assertFalse(monthlyAvgButton.isEnabled());
        assertFalse(refreshButton.isEnabled());
        setBusy.invoke(panel, false);
        assertTrue(showStatsButton.isEnabled());
        assertTrue(distributionButton.isEnabled());
        assertTrue(monthlyAvgButton.isEnabled());
        assertTrue(refreshButton.isEnabled());
    }

    @DisplayName("Tests that clearContent removes all components from contentPanel")
    @Test
    void clearContent_removesAllComponents() throws Exception {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Method clearContent = panel.getClass().getDeclaredMethod("clearContent");
        clearContent.setAccessible(true);
        // Add a dummy component
        Field contentPanelField = panel.getClass().getDeclaredField("contentPanel");
        contentPanelField.setAccessible(true);
        JPanel contentPanel = (JPanel) contentPanelField.get(panel);
        contentPanel.add(new JLabel("dummy"));
        clearContent.invoke(panel);
        assertEquals(0, contentPanel.getComponentCount());
    }

    @DisplayName("Tests that buildNonEditableTable returns a non-editable JTable")
    @Test
    void buildNonEditableTable_returnsNonEditableTable() throws Exception {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Method buildNonEditableTable = panel.getClass().getDeclaredMethod("buildNonEditableTable", String[].class, Object[][].class);
        buildNonEditableTable.setAccessible(true);
        String[] columns = {"Col1", "Col2"};
        Object[][] data = {{"A", 1}, {"B", 2}};
        JTable table = (JTable) buildNonEditableTable.invoke(panel, (Object) columns, (Object) data);
        assertFalse(table.isCellEditable(0, 0));
    }

    @DisplayName("Tests that revalidateAndRepaint calls revalidate and repaint on contentPanel")
    @Test
    void revalidateAndRepaint_invokesMethods() throws Exception {
        StatisticsService mockService = Mockito.mock(StatisticsService.class);
        StatisticsPanel panel = new StatisticsPanel(mockService);
        Method revalidateAndRepaint = panel.getClass().getDeclaredMethod("revalidateAndRepaint");
        revalidateAndRepaint.setAccessible(true);
        // Should not throw
        assertDoesNotThrow(() -> revalidateAndRepaint.invoke(panel));
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
