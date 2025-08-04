package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SearchPanel} covering construction and component checks.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class SearchPanelTest {

    /**
     * Tests that constructing SearchPanel with a valid service does not throw.
     */
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new SearchPanel(mockService));
    }

    /**
     * Tests that the panel contains a button component after construction.
     */
    @Test
    void givenPanel_whenGetComponents_thenHasButton() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        // Act
        boolean hasButton = false;
        for (java.awt.Component c : panel.getComponents()) {
            if (c instanceof JButton) hasButton = true;
        }
        // Assert
        assertTrue(hasButton, "Panel should have at least one button");
    }

    /**
     * Tests that the nvl method returns an empty string when given a null input.
     */
    @Test
    void givenNullInput_whenNvl_thenReturnsEmptyString() {
        // Act
        String result = invokeNvl(null);
        // Assert
        assertEquals("", result);
    }

    /**
     * Tests that the nvl method returns the same string when given a non-null input.
     */
    @Test
    void givenNonNullInput_whenNvl_thenReturnsSameString() {
        // Arrange
        String input = "xyz";
        // Act
        String result = invokeNvl(input);
        // Assert
        assertEquals("xyz", result);
    }

    /**
     * Tests that setting results with a null list does not throw.
     */
    @Test
    void givenNullList_whenSetResults_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeSetResults(panel, null));
    }

    /**
     * Tests that setting results with an empty list does not throw.
     */
    @Test
    void givenEmptyList_whenSetResults_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeSetResults(panel, java.util.Collections.emptyList()));
    }

    /**
     * Tests that searching with an empty input does not throw.
     */
    @Test
    void givenEmptyInput_whenOnSearch_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        java.lang.reflect.Field keywordField = panel.getClass().getDeclaredField("keywordField");
        keywordField.setAccessible(true);
        ((javax.swing.JTextField) keywordField.get(panel)).setText("");
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnSearch(panel));
    }

    // Helper to access private static method via reflection
    private String invokeNvl(String s) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.SearchPanel")
                .getDeclaredMethod("nvl", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private method via reflection
    private void invokeSetResults(SearchPanel panel, java.util.List<?> reviews) {
        try {
            var method = panel.getClass().getDeclaredMethod("setResults", java.util.List.class);
            method.setAccessible(true);
            method.invoke(panel, reviews);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeOnSearch(SearchPanel panel) {
        try {
            var method = panel.getClass().getDeclaredMethod("onSearch");
            method.setAccessible(true);
            method.invoke(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
