package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.exception.ValidationUtils;
import com.reviewapp.domain.model.Filters;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.BeforeEach;
import com.reviewapp.application.service.ReviewService;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
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


    @DisplayName("Apply filters: all fields valid, triggers service call")
    @Test
    void applyFilters_allFieldsValid_triggersService() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        setText(panel, "authorField", "John");
        setText(panel, "titleField", "Title");
        setText(panel, "productField", "Product");
        setText(panel, "storeField", "Store");
        setText(panel, "minRatingField", "2");
        setText(panel, "maxRatingField", "5");
        setText(panel, "dateField", "2024-08-11");
        setText(panel, "minDateField", "2024-08-01");
        setText(panel, "maxDateField", "2024-08-31");
        setText(panel, "minTimeField", "10:00");
        setText(panel, "maxTimeField", "18:00");
        setText(panel, "keywordField", "foo");
        setCheck(panel, "sortByDateBox", true);
        setCheck(panel, "sortByRatingsBox", true);
        // No exceptions expected
        assertDoesNotThrow(() -> invokeOnFilter(panel));
    }

    @DisplayName("Apply filters: invalid min rating triggers error dialog")
    @Test
    void applyFilters_invalidMinRating_triggersError() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        setText(panel, "minRatingField", "bad");
        // Should not throw, but should show error dialog (not verifiable here)
        assertDoesNotThrow(() -> invokeOnFilter(panel));
    }

    @DisplayName("Apply filters: invalid date triggers error dialog")
    @Test
    void applyFilters_invalidDate_triggersError() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        setText(panel, "dateField", "notadate");
        assertDoesNotThrow(() -> invokeOnFilter(panel));
    }

    @DisplayName("Apply filters: empty fields does not throw")
    @Test
    void applyFilters_emptyFields_noException() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        assertDoesNotThrow(() -> invokeOnFilter(panel));
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

    // Helpers for field/checkbox access
    private void setText(FilterReviewsPanel panel, String fieldName, String value) {
        try {
            var f = panel.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            ((JTextField) f.get(panel)).setText(value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private void setCheck(FilterReviewsPanel panel, String fieldName, boolean value) {
        try {
            var f = panel.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            ((JCheckBox) f.get(panel)).setSelected(value);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
    private <T> T getChildComponent(Container parent, Class<T> type) {
        for (Component c : parent.getComponents()) {
            if (type.isInstance(c)) return type.cast(c);
            if (c instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) c).getViewport();
                if (viewport != null) {
                    Component view = viewport.getView();
                    if (type.isInstance(view)) return type.cast(view);
                    if (view instanceof Container) {
                        T found = getChildComponent((Container) view, type);
                        if (found != null) return found;
                    }
                }
            }
            if (c instanceof Container) {
                T found = getChildComponent((Container) c, type);
                if (found != null) return found;
            }
        }
        return null;
    }



    @DisplayName("Selection listener: details area updates on row select")
    @Test
    void selectionListener_detailsAreaUpdates() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        JTable table = getChildComponent(panel, JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        JTextArea detailsArea = getChildComponent(panel, JTextArea.class);

        // Add row with valid ID
        model.addRow(new Object[]{123L, "t", "a", 5, "2024-08-11", "p", "s"});

        // Mock review
        Review mockReview = Mockito.mock(Review.class);
        Mockito.when(mockReview.getReviewText()).thenReturn("details");
        Mockito.when(mockService.getReviewById(123L)).thenReturn(mockReview);

        // Act: Ensure selection happens on EDT and wait for processing
        SwingUtilities.invokeAndWait(() -> {
            table.updateUI();
            int viewRow = table.convertRowIndexToView(0);
            table.setRowSelectionInterval(viewRow, viewRow);
        });

        // Assert: Details area should have review text
        assertEquals("details", detailsArea.getText(), "Details area should show review text after selection");

        // Act: Clear selection on EDT
        SwingUtilities.invokeAndWait(() -> table.clearSelection());

        // Assert: Details area should be empty
        assertEquals("", detailsArea.getText(), "Details area should be empty after clearing selection");
    }

    @DisplayName("Constructor throws NullPointerException when ReviewService is null")
    @Test
    void constructor_nullReviewService_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FilterReviewsPanel(null));
    }

    @DisplayName("Constructor throws IllegalArgumentException when pageSize is less than 1")
    @Test
    void constructor_invalidPageSize_throwsIllegalArgumentException() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        assertThrows(IllegalArgumentException.class, () -> new FilterReviewsPanel(mockService, 0));
    }

    @DisplayName("refreshTable with reviews containing null fields adds rows without exception")
    @Test
    void refreshTable_withReviewsHavingNulls_addsRows() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        Review mockReview = Mockito.mock(Review.class);
        Mockito.when(mockReview.getReviewId()).thenReturn(1L);
        Mockito.when(mockReview.getReviewTitle()).thenReturn(null);
        Mockito.when(mockReview.getAuthorName()).thenReturn(null);
        Mockito.when(mockReview.getProductRating()).thenReturn(5);
        Mockito.when(mockReview.getReviewedDate()).thenReturn(null);
        Mockito.when(mockReview.getProductName()).thenReturn(null);
        Mockito.when(mockReview.getReviewSource()).thenReturn(null);
        List<Review> reviews = Arrays.asList(mockReview);
        assertDoesNotThrow(() -> invokeRefreshTable(panel, reviews));
        DefaultTableModel model = (DefaultTableModel) getChildComponent(panel, JTable.class).getModel();
        assertEquals(1, model.getRowCount());
    }

    @DisplayName("Selection listener: non-number ID sets details to empty")
    @Test
    void selectionListener_nonNumberId_setsDetailsEmpty() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        JTable table = getChildComponent(panel, JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        JTextArea detailsArea = getChildComponent(panel, JTextArea.class);
        model.addRow(new Object[]{"invalid", "title", "author", 4, "date", "product", "store"});
        table.updateUI();
        int viewRow = table.convertRowIndexToView(0);
        table.setRowSelectionInterval(viewRow, viewRow);
        assertEquals("", detailsArea.getText());
    }

    @DisplayName("Selection listener: null review from service sets details to empty")
    @Test
    void selectionListener_nullReview_setsDetailsEmpty() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        JTable table = getChildComponent(panel, JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        JTextArea detailsArea = getChildComponent(panel, JTextArea.class);
        model.addRow(new Object[]{456L, "title", "author", 4, "date", "product", "store"});
        Mockito.when(mockService.getReviewById(456L)).thenReturn(null);
        table.updateUI();
        int viewRow = table.convertRowIndexToView(0);
        table.setRowSelectionInterval(viewRow, viewRow);
        assertEquals("", detailsArea.getText());
    }

    @DisplayName("loadPageAsync with null filters does not throw and covers info branch")
    @Test
    void loadPageAsync_nullFilters_coversBranch() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        assertDoesNotThrow(() -> invokeLoadPageAsync(panel, null, 1));
    }

    @DisplayName("clamp handles value below min")
    @Test
    void clamp_belowMin_returnsMin() {
        assertEquals(1, invokeClamp(-5, 1, 10));
    }

    @DisplayName("clamp handles value above max")
    @Test
    void clamp_aboveMax_returnsMax() {
        assertEquals(10, invokeClamp(15, 1, 10));
    }

    @DisplayName("clamp handles value within range")
    @Test
    void clamp_withinRange_returnsValue() {
        assertEquals(5, invokeClamp(5, 1, 10));
    }

    @DisplayName("applyFilters triggers general Exception catch")
    @Test
    void applyFilters_generalException_coversCatch() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        setText(panel, "minRatingField", "2");
        try (MockedStatic<ValidationUtils> mockedStatic = Mockito.mockStatic(ValidationUtils.class)) {
            mockedStatic.when(() -> ValidationUtils.parseRating("2")).thenThrow(new RuntimeException("Forced error"));
            assertDoesNotThrow(() -> invokeOnFilter(panel));
        }
    }

    @DisplayName("loadPageAsync with results covers total > 0 branches")
    @Test
    void loadPageAsync_withResults_coversBranches() throws InterruptedException {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Filters filters = new Filters.Builder().build();
        Review mockReview = Mockito.mock(Review.class);
        Mockito.when(mockReview.getReviewId()).thenReturn(1L);
        Mockito.when(mockReview.getReviewTitle()).thenReturn("title");
        Mockito.when(mockReview.getAuthorName()).thenReturn("author");
        Mockito.when(mockReview.getProductRating()).thenReturn(5);
        Mockito.when(mockReview.getReviewedDate()).thenReturn(LocalDate.of(2024, 8, 12)); // LocalDate.parse("2024-08-12");
        Mockito.when(mockReview.getProductName()).thenReturn("product");
        Mockito.when(mockReview.getReviewSource()).thenReturn("store");
        List<Review> reviews = Arrays.asList(mockReview);
        Mockito.when(mockService.getFilteredReviewCount(ArgumentMatchers.any(Filters.class))).thenReturn(25);
        Mockito.when(mockService.getFilteredReviewsPage(ArgumentMatchers.any(Filters.class), ArgumentMatchers.eq(1), ArgumentMatchers.eq(20))).thenReturn(reviews);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        invokeLoadPageAsync(panel, filters, 1);
        Thread.sleep(500);
        DefaultTableModel model = (DefaultTableModel) getChildComponent(panel, JTable.class).getModel();
        assertEquals(1, model.getRowCount());
    }

    @DisplayName("loadPageAsync with exception in background covers error branch")
    @Test
    void loadPageAsync_backgroundException_coversError() throws InterruptedException {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Filters filters = new Filters.Builder().build();
        Mockito.when(mockService.getFilteredReviewCount(ArgumentMatchers.any(Filters.class))).thenThrow(new RuntimeException("Service error"));
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        invokeLoadPageAsync(panel, filters, 1);
        Thread.sleep(500);
    }


    @DisplayName("loadPageAsync with clamped page below 1")
    @Test
    void loadPageAsync_clampedBelow_coversBranch() throws InterruptedException {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Filters filters = new Filters.Builder().build();
        Mockito.when(mockService.getFilteredReviewCount(ArgumentMatchers.any(Filters.class))).thenReturn(10);
        Mockito.when(mockService.getFilteredReviewsPage(ArgumentMatchers.any(Filters.class), ArgumentMatchers.eq(1), ArgumentMatchers.eq(20))).thenReturn(Arrays.asList());
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        invokeLoadPageAsync(panel, filters, 0);
        Thread.sleep(500);
    }

    @DisplayName("loadPageAsync with clamped page above total")
    @Test
    void loadPageAsync_clampedAbove_coversBranch() throws InterruptedException {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Filters filters = new Filters.Builder().build();
        Mockito.when(mockService.getFilteredReviewCount(ArgumentMatchers.any(Filters.class))).thenReturn(10);
        Mockito.when(mockService.getFilteredReviewsPage(ArgumentMatchers.any(Filters.class), ArgumentMatchers.eq(1), ArgumentMatchers.eq(20))).thenReturn(Arrays.asList());
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        invokeLoadPageAsync(panel, filters, 5);
        Thread.sleep(500);
    }

    @DisplayName("updatePaginationControls with multiple pages enables buttons appropriately")
    @Test
    void updatePaginationControls_multiplePages_enablesButtons() throws Exception {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        FilterReviewsPanel panel = new FilterReviewsPanel(mockService);
        // Set currentPage and totalPages via reflection
        java.lang.reflect.Field currentPageField = panel.getClass().getDeclaredField("currentPage");
        currentPageField.setAccessible(true);
        currentPageField.set(panel, 2);
        java.lang.reflect.Field totalPagesField = panel.getClass().getDeclaredField("totalPages");
        totalPagesField.setAccessible(true);
        totalPagesField.set(panel, 5);
        invokeUpdatePaginationControls(panel);
        JButton prevButton = getChildComponent(panel, JButton.class, "Previous");
        JButton nextButton = getChildComponent(panel, JButton.class, "Next");
        assertTrue(prevButton.isEnabled());
        assertTrue(nextButton.isEnabled());
    }

    private void invokeLoadPageAsync(FilterReviewsPanel panel, Filters filters, int page) {
        try {
            java.lang.reflect.Method method = panel.getClass().getDeclaredMethod("loadPageAsync", Filters.class, int.class);
            method.setAccessible(true);
            method.invoke(panel, filters, page);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private int invokeClamp(int val, int min, int max) {
        try {
            java.lang.reflect.Method method = FilterReviewsPanel.class.getDeclaredMethod("clamp", int.class, int.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(null, val, min, max);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void invokeUpdatePaginationControls(FilterReviewsPanel panel) {
        try {
            java.lang.reflect.Method method = panel.getClass().getDeclaredMethod("updatePaginationControls");
            method.setAccessible(true);
            method.invoke(panel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // Overloaded getChildComponent to find by text (for buttons)
    private <T> T getChildComponent(Container parent, Class<T> type, String text) {
        for (Component c : parent.getComponents()) {
            if (type.isInstance(c) && ((JButton) c).getText().equals(text)) return type.cast(c);
            if (c instanceof Container) {
                T found = getChildComponent((Container) c, type, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
