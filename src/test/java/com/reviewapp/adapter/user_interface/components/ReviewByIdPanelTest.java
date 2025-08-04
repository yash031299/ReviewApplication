package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewByIdPanel} covering construction, component checks, and method invocations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewByIdPanelTest {
    /**
     * Tests that constructing ReviewByIdPanel with a valid service does not throw.
     */
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new ReviewByIdPanel(mockService));
    }

    /**
     * Tests that the panel contains a button component after construction.
     */
    @Test
    void givenPanel_whenGetComponents_thenHasButton() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        // Act
        boolean hasButton = containsButton(panel);
        // Assert
        assertTrue(hasButton, "Panel should have at least one button");
    }

    /**
     * Tests that the nvl method returns an empty string for null input.
     */
    @Test
    void givenNullInput_whenNvl_thenReturnsEmptyString() {
        // Act
        String result = invokeNvl(null);
        // Assert
        assertEquals("", result);
    }

    /**
     * Tests that the nvl method returns the same string for non-null input.
     */
    @Test
    void givenNonNullInput_whenNvl_thenReturnsSameString() {
        // Arrange
        String input = "abc";
        // Act
        String result = invokeNvl(input);
        // Assert
        assertEquals("abc", result);
    }

    /**
     * Tests that the formatReview method does not throw for a review with null fields.
     */
    @Test
    void givenNullFields_whenFormatReview_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        var review = Mockito.mock(com.reviewapp.domain.model.Review.class);
        Mockito.when(review.getReviewId()).thenReturn(1L);
        Mockito.when(review.getReviewTitle()).thenReturn(null);
        Mockito.when(review.getAuthorName()).thenReturn(null);
        Mockito.when(review.getProductName()).thenReturn(null);
        Mockito.when(review.getReviewSource()).thenReturn(null);
        Mockito.when(review.getReviewText()).thenReturn(null);
        Mockito.when(review.getProductRating()).thenReturn(5);
        Mockito.when(review.getReviewedDate()).thenReturn(java.time.LocalDate.now());
        // Act & Assert
        assertDoesNotThrow(() -> invokeFormatReview(review));
    }

    /**
     * Tests that the onFetch method does not throw for an empty input.
     */
    @Test
    void givenEmptyInput_whenOnFetch_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        java.lang.reflect.Field idField = panel.getClass().getDeclaredField("idField");
        idField.setAccessible(true);
        ((javax.swing.JTextField) idField.get(panel)).setText("");
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnFetch(panel));
    }

    // Helper to check for button presence
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
    private String invokeNvl(String s) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.ReviewByIdPanel")
                .getDeclaredMethod("nvl", String.class);
            method.setAccessible(true);
            return (String) method.invoke(null, s);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private static formatReview via reflection
    private String invokeFormatReview(com.reviewapp.domain.model.Review r) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.ReviewByIdPanel")
                .getDeclaredMethod("formatReview", com.reviewapp.domain.model.Review.class);
            method.setAccessible(true);
            return (String) method.invoke(null, r);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to access private method via reflection
    private void invokeOnFetch(ReviewByIdPanel panel) {
        try {
            var method = panel.getClass().getDeclaredMethod("onFetch");
            method.setAccessible(true);
            method.invoke(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
