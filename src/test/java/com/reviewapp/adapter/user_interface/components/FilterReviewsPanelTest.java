package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.swing.*;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FilterReviewsPanel} covering construction, component checks, and method invocations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class FilterReviewsPanelTest {

    @DisplayName("Tests that constructing FilterReviewsPanel with a valid service does not throw")
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new FilterReviewsPanel(mockService));
    }


    @DisplayName("Tests that the panel contains a button component after construction")
    @Test
    void givenPanel_whenGetComponents_thenHasButton() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        // Act
        boolean hasButton = containsButton(panel);
        // Assert
        assertTrue(hasButton, "Panel should have at least one button");
    }


    @DisplayName("Tests that the nullToEmpty method returns an empty string when given null input")
    @Test
    void givenNullInput_whenNullToEmpty_thenReturnsEmptyString() {
        // Act
        String result = invokeNullToEmpty(null);
        // Assert
        assertEquals("", result);
    }


    @DisplayName("Tests that the nullToEmpty method returns the same string when given non-null input")
    @Test
    void givenNonNullInput_whenNullToEmpty_thenReturnsSameString() {
        // Arrange
        String input = "xyz";
        // Act
        String result = invokeNullToEmpty(input);
        // Assert
        assertEquals("xyz", result);
    }


    @DisplayName("Tests that refreshTable does not throw when given a null list")
    @Test
    void givenNullList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, null));
    }


    @DisplayName("Tests that refreshTable does not throw when given an empty list")
    @Test
    void givenEmptyList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, java.util.Collections.emptyList()));
    }


    @DisplayName("Tests that onFilter does not throw when given an empty input")
    @Test
    void givenEmptyInput_whenOnFilter_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        java.lang.reflect.Field keywordField = panel.getClass().getDeclaredField("keywordField");
        keywordField.setAccessible(true);
        ((javax.swing.JTextField) keywordField.get(panel)).setText("");
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnFilter(panel));
    }


    @DisplayName("Tests that onLoadError does not throw when given a simulated error")
    @Test
    void givenSimulatedError_whenSwingWorkerErrorHandlerInvoked_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnLoadError(panel, new RuntimeException("Simulated error")));
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

    // Helper to access private static method via reflection
    private String invokeNullToEmpty(String s) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.FilterReviewsPanel")
                .getDeclaredMethod("nullToEmpty", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private method via reflection
    private void invokeRefreshTable(FilterReviewsPanel panel, java.util.List<?> reviews) {
        try {
            var method = panel.getClass().getDeclaredMethod("refreshTable", java.util.List.class);
            method.setAccessible(true);
            method.invoke(panel, reviews);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeOnFilter(FilterReviewsPanel panel) {
        try {
            var method = panel.getClass().getDeclaredMethod("onFilter");
            method.setAccessible(true);
            method.invoke(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeOnLoadError(FilterReviewsPanel panel, Throwable t) {
        try {
            var method = panel.getClass().getDeclaredMethod("onLoadError", Throwable.class);
            method.setAccessible(true);
            method.invoke(panel, t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
