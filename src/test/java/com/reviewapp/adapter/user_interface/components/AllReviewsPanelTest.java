package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AllReviewsPanel} covering construction, component checks, and error handling.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class AllReviewsPanelTest {
    /**
     * Tests that constructing AllReviewsPanel with a valid service does not throw.
     */
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new AllReviewsPanel(mockService));
    }

    /**
     * Tests that the panel contains a scroll pane or table component after construction.
     */
    @Test
    void givenPanel_whenGetComponents_thenHasScrollOrTable() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act
        boolean hasScroll = false;
        boolean hasTable = false;
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof javax.swing.JScrollPane) hasScroll = true;
            if (c instanceof javax.swing.JTable) hasTable = true;
        }
        // Assert
        assertTrue(hasScroll || hasTable, "Panel should have scroll or table");
    }

    /**
     * Tests that the nullToEmpty method returns an empty string when given null input.
     */
    @Test
    void givenNullInput_whenNullToEmpty_thenReturnsEmptyString() {
        // Act
        String result = invokeNullToEmpty(null);
        // Assert
        assertEquals("", result);
    }

    /**
     * Tests that the nullToEmpty method returns the same string when given non-null input.
     */
    @Test
    void givenNonNullInput_whenNullToEmpty_thenReturnsSameString() {
        // Arrange
        String input = "abc";
        // Act
        String result = invokeNullToEmpty(input);
        // Assert
        assertEquals("abc", result);
    }

    /**
     * Tests that refreshing the table with a null list does not throw.
     */
    @Test
    void givenNullList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, null));
    }

    /**
     * Tests that refreshing the table with an empty list does not throw.
     */
    @Test
    void givenEmptyList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, java.util.Collections.emptyList()));
    }

    /**
     * Tests that the error handler does not throw when invoked with a simulated error.
     */
    @Test
    void givenSimulatedError_whenSwingWorkerErrorHandlerInvoked_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnLoadError(panel, new RuntimeException("Simulated error")));
    }

    // Helper to access private static method via reflection
    private String invokeNullToEmpty(String s) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.AllReviewsPanel")
                .getDeclaredMethod("nullToEmpty", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private method via reflection
    private void invokeRefreshTable(AllReviewsPanel panel, java.util.List<?> reviews) {
        try {
            var method = panel.getClass().getDeclaredMethod("refreshTable", java.util.List.class);
            method.setAccessible(true);
            method.invoke(panel, reviews);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private error handler via reflection
    private void invokeOnLoadError(AllReviewsPanel panel, Throwable t) {
        try {
            var method = panel.getClass().getDeclaredMethod("onLoadError", Throwable.class);
            method.setAccessible(true);
            method.invoke(panel, t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
