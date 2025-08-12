package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.exception.InvalidInputException;
import com.reviewapp.application.service.ReviewService;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReviewByIdPanel} covering construction, component checks, and method invocations.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewByIdPanelTest {

    @DisplayName("Tests that constructing ReviewByIdPanel with a valid service does not throw")
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new ReviewByIdPanel(mockService));
    }

    @DisplayName("Tests that constructing ReviewByIdPanel with null service throws NullPointerException")
    @Test
    void givenNullReviewService_whenPanelConstructed_thenThrowsNullPointerException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> new ReviewByIdPanel(null));
    }

    @DisplayName("Tests that the panel contains a button component after construction")
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

    @DisplayName("Tests that the nvl method returns an empty string for null input")
    @Test
    void givenNullInput_whenNvl_thenReturnsEmptyString() {
        // Act
        String result = invokeNvl(null);
        // Assert
        assertEquals("", result);
    }

    @DisplayName("Tests that the nvl method returns the same string for non-null input")
    @Test
    void givenNonNullInput_whenNvl_thenReturnsSameString() {
        // Arrange
        String input = "abc";
        // Act
        String result = invokeNvl(input);
        // Assert
        assertEquals("abc", result);
    }

    @DisplayName("Tests that the formatReview method does not throw for a review with null fields")
    @Test
    void givenNullFields_whenFormatReview_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        var review = Mockito.mock(Review.class);
        Mockito.when(review.getReviewId()).thenReturn(1L);
        Mockito.when(review.getReviewTitle()).thenReturn(null);
        Mockito.when(review.getAuthorName()).thenReturn(null);
        Mockito.when(review.getProductName()).thenReturn(null);
        Mockito.when(review.getReviewSource()).thenReturn(null);
        Mockito.when(review.getReviewText()).thenReturn(null);
        Mockito.when(review.getProductRating()).thenReturn(5);
        Mockito.when(review.getReviewedDate()).thenReturn(LocalDate.now());
        // Act & Assert
        assertDoesNotThrow(() -> invokeFormatReview(review));
    }

    @DisplayName("Tests that the formatReview method formats correctly for a review with non-null fields")
    @Test
    void givenReviewWithFields_whenFormatReview_thenFormatsCorrectly() {
        // Arrange
        var review = Mockito.mock(Review.class);
        Mockito.when(review.getReviewId()).thenReturn(1L);
        Mockito.when(review.getReviewTitle()).thenReturn("Title");
        Mockito.when(review.getAuthorName()).thenReturn("Author");
        Mockito.when(review.getProductRating()).thenReturn(5);
        Mockito.when(review.getReviewedDate()).thenReturn(LocalDate.of(2025, 8, 12));
        Mockito.when(review.getProductName()).thenReturn("Product");
        Mockito.when(review.getReviewSource()).thenReturn("Store");
        Mockito.when(review.getReviewText()).thenReturn("Text");
        // Act
        String result = invokeFormatReview(review);
        // Assert
        String expected = "ID: 1\nTitle: Title\nAuthor: Author\nRating: 5\nDate: 2025-08-12\nProduct: Product\nStore: Store\n\nReview Text:\nText";
        assertEquals(expected, result);
    }

    @DisplayName("Tests that onFetch does not throw for an empty input")
    @Test
    void givenEmptyInput_whenOnFetch_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JTextField idField = getIdField(panel);
        idField.setText("");
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnFetch(panel));
    }

    @DisplayName("Tests that onFetch handles invalid (non-numeric or negative) input gracefully")
    @Test
    void givenInvalidInput_whenOnFetch_thenShowsWarningOrError() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        // Simulate entering invalid input into the text field
        JTextField idField = getIdField(panel);
        idField.setText("-1");
        // Should not throw, should show warning dialog
        assertDoesNotThrow(() -> invokeOnFetch(panel));
        idField.setText("abc");
        assertDoesNotThrow(() -> invokeOnFetch(panel));
    }

    @DisplayName("Tests that onFetch handles service exceptions gracefully")
    @Test
    void givenServiceThrows_whenOnFetch_thenShowsErrorDialog() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Mockito.when(mockService.getReviewById(Mockito.anyLong())).thenThrow(new RuntimeException("fail"));
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JTextField idField = getIdField(panel);
        idField.setText("123");
        assertDoesNotThrow(() -> invokeOnFetch(panel));
    }

    @DisplayName("Tests that onFetch displays review for valid ID when review found")
    @Test
    void givenValidId_whenOnFetch_thenDisplaysReview() throws InterruptedException {
        // Arrange
        ReviewService mockService = mock(ReviewService.class);
        Review review = mock(Review.class);
        when(review.getReviewId()).thenReturn(1L);
        when(review.getReviewTitle()).thenReturn("Title");
        when(review.getAuthorName()).thenReturn("Author");
        when(review.getProductRating()).thenReturn(5);
        when(review.getReviewedDate()).thenReturn(LocalDate.of(2025, 8, 12));
        when(review.getProductName()).thenReturn("Product");
        when(review.getReviewSource()).thenReturn("Store");
        when(review.getReviewText()).thenReturn("Text");
        when(mockService.getReviewById(1L)).thenReturn(review);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JTextField idField = getIdField(panel);
        JTextArea reviewArea = getReviewArea(panel);
        idField.setText("1");

        // Act
        invokeOnFetch(panel);
        Thread.sleep(500); // Wait for SwingWorker

        // Assert
        String expected = "ID: 1\nTitle: Title\nAuthor: Author\nRating: 5\nDate: 2025-08-12\nProduct: Product\nStore: Store\n\nReview Text:\nText";
        assertEquals(expected, reviewArea.getText());
    }

    @DisplayName("Tests that onFetch shows not found for valid ID when review null")
    @Test
    void givenValidIdButNoReview_whenOnFetch_thenShowsNotFound() throws InterruptedException {
        // Arrange
        ReviewService mockService = mock(ReviewService.class);
        when(mockService.getReviewById(1L)).thenReturn(null);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JTextField idField = getIdField(panel);
        JTextArea reviewArea = getReviewArea(panel);
        idField.setText("1");

        // Act
        invokeOnFetch(panel);
        Thread.sleep(500); // Wait for SwingWorker

        // Assert
        assertEquals("", reviewArea.getText());
    }

    @DisplayName("Tests that onFetch shows warning for InvalidInputException from service")
    @Test
    void givenServiceThrowsInvalidInput_whenOnFetch_thenShowsWarning() throws InterruptedException {
        // Arrange
        ReviewService mockService = mock(ReviewService.class);
        when(mockService.getReviewById(1L)).thenThrow(new InvalidInputException("Invalid"));
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JTextField idField = getIdField(panel);
        JTextArea reviewArea = getReviewArea(panel);
        idField.setText("1");

        // Act
        invokeOnFetch(panel);
        Thread.sleep(500); // Wait for SwingWorker

        // Assert
        assertEquals("", reviewArea.getText());
    }

    @DisplayName("Tests that setBusy enables/disables controls and updates cursor appropriately")
    @Test
    void setBusy_enablesAndDisablesControls() throws Exception {
        // Arrange
        ReviewService mockService = mock(ReviewService.class);
        ReviewByIdPanel panel = new ReviewByIdPanel(mockService);
        JButton fetchButton = getFetchButton(panel);
        JTextField idField = getIdField(panel);

        // Act & Assert: busy = true
        invokeSetBusy(panel, true);
        assertEquals(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR), panel.getCursor());
        assertFalse(fetchButton.isEnabled());
        assertFalse(idField.isEnabled());

        // Act & Assert: busy = false
        invokeSetBusy(panel, false);
        assertEquals(Cursor.getDefaultCursor(), panel.getCursor());
        assertTrue(fetchButton.isEnabled());
        assertTrue(idField.isEnabled());
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
    private String invokeFormatReview(Review r) {
        try {
            var method = Class.forName("com.reviewapp.adapter.user_interface.components.ReviewByIdPanel")
                    .getDeclaredMethod("formatReview", Review.class);
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

    private void invokeSetBusy(ReviewByIdPanel panel, boolean busy) {
        try {
            var method = panel.getClass().getDeclaredMethod("setBusy", boolean.class);
            method.setAccessible(true);
            method.invoke(panel, busy);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JTextField getIdField(ReviewByIdPanel panel) {
        try {
            Field field = panel.getClass().getDeclaredField("idField");
            field.setAccessible(true);
            return (JTextField) field.get(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JTextArea getReviewArea(ReviewByIdPanel panel) {
        try {
            Field field = panel.getClass().getDeclaredField("reviewArea");
            field.setAccessible(true);
            return (JTextArea) field.get(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JButton getFetchButton(ReviewByIdPanel panel) {
        try {
            Field field = panel.getClass().getDeclaredField("fetchButton");
            field.setAccessible(true);
            return (JButton) field.get(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}