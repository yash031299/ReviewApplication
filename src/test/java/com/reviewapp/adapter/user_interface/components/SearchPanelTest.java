package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.adapter.user_interface.util.AlertDialogs;
import com.reviewapp.application.exception.InvalidInputException;
import com.reviewapp.application.service.ReviewService;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SearchPanel} covering construction and component checks.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class SearchPanelTest {

    @DisplayName("Tests that constructing SearchPanel with a valid service does not throw")
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new SearchPanel(mockService));
    }


    @DisplayName("Tests that the panel contains a button component after construction")
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


    @DisplayName("Tests that the nvl method returns an empty string when given a null input")
    @Test
    void givenNullInput_whenNvl_thenReturnsEmptyString() {
        // Act
        String result = invokeNvl(null);
        // Assert
        assertEquals("", result);
    }


    @DisplayName("Tests that the nvl method returns the same string when given a non-null input")
    @Test
    void givenNonNullInput_whenNvl_thenReturnsSameString() {
        // Arrange
        String input = "xyz";
        // Act
        String result = invokeNvl(input);
        // Assert
        assertEquals("xyz", result);
    }


    @DisplayName("Tests that setting results with a null list does not throw")
    @Test
    void givenNullList_whenSetResults_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeSetResults(panel, null));
    }


    @DisplayName("Tests that setting results with an empty list does not throw")
    @Test
    void givenEmptyList_whenSetResults_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeSetResults(panel, java.util.Collections.emptyList()));
    }


    @DisplayName("Tests that searching with an empty input does not throw")
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


    @DisplayName("Tests that setResults handles a large list without exceptions")
    @Test
    void givenLargeList_whenSetResults_thenNoException() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        java.util.List<Review> reviews = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            Review r = Mockito.mock(Review.class);
            Mockito.when(r.getReviewId()).thenReturn((long) i);
            reviews.add(r);
        }
        assertDoesNotThrow(() -> invokeSetResults(panel, reviews));
    }


    @DisplayName("Tests that setResults handles a list containing nulls without exceptions")
    @Test
    void givenListWithNulls_whenSetResults_thenNoException() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        java.util.List<Review> reviews = java.util.Arrays.asList(null, null);
        assertDoesNotThrow(() -> invokeSetResults(panel, reviews));
    }

    @DisplayName("Tests that setBusy enables/disables controls appropriately")
    @Test
    void setBusy_enablesAndDisablesControls() throws Exception {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        java.lang.reflect.Method setBusy = panel.getClass().getDeclaredMethod("setBusy", boolean.class);
        setBusy.setAccessible(true);
        java.lang.reflect.Field searchButtonField = panel.getClass().getDeclaredField("searchButton");
        java.lang.reflect.Field keywordFieldField = panel.getClass().getDeclaredField("keywordField");
        searchButtonField.setAccessible(true);
        keywordFieldField.setAccessible(true);
        JButton searchButton = (JButton) searchButtonField.get(panel);
        JTextField keywordField = (JTextField) keywordFieldField.get(panel);
        setBusy.invoke(panel, false);
        assertTrue(searchButton.isEnabled());
        assertTrue(keywordField.isEnabled());
        setBusy.invoke(panel, true);
        assertFalse(searchButton.isEnabled());
        assertFalse(keywordField.isEnabled());
        setBusy.invoke(panel, false);
        assertTrue(searchButton.isEnabled());
        assertTrue(keywordField.isEnabled());
    }

    @DisplayName("Tests that configureColumnWidths sets preferred widths without exception")
    @Test
    void configureColumnWidths_setsPreferredWidths() throws Exception {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(0, 7);
        javax.swing.JTable table = new javax.swing.JTable(model);
        java.lang.reflect.Method configureColumnWidths = Class.forName("com.reviewapp.adapter.user_interface.components.SearchPanel").getDeclaredMethod("configureColumnWidths", javax.swing.table.TableColumnModel.class);
        configureColumnWidths.setAccessible(true);
        assertDoesNotThrow(() -> configureColumnWidths.invoke(null, table.getColumnModel()));
    }

    @DisplayName("Table selection with null review clears detailsArea")
    @Test
    void tableSelection_withNullReview_clearsDetailsArea() throws Exception {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Mockito.when(mockService.getReviewById(1L)).thenReturn(null);
        SearchPanel panel = new SearchPanel(mockService);
        Field tableModelField = panel.getClass().getDeclaredField("tableModel");
        tableModelField.setAccessible(true);
        DefaultTableModel model = (DefaultTableModel) tableModelField.get(panel);
        model.addRow(new Object[]{1L, "t", "a", 5, "2025-08-05", "p", "s"});
        Field resultTableField = panel.getClass().getDeclaredField("resultTable");
        resultTableField.setAccessible(true);
        JTable table = (JTable) resultTableField.get(panel);
        table.setRowSelectionInterval(0, 0);
        Field detailsAreaField = panel.getClass().getDeclaredField("detailsArea");
        detailsAreaField.setAccessible(true);
        JTextArea detailsArea = (JTextArea) detailsAreaField.get(panel);
        // Wait for listener
        Thread.sleep(100);
        assertEquals("", detailsArea.getText());
    }

    @DisplayName("searchButton and keywordField action listeners invoke onSearch")
    @Test
    void actionListeners_invokeOnSearch() throws Exception {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        SearchPanel panel = new SearchPanel(mockService);
        java.lang.reflect.Field keywordFieldField = panel.getClass().getDeclaredField("keywordField");
        java.lang.reflect.Field searchButtonField = panel.getClass().getDeclaredField("searchButton");
        keywordFieldField.setAccessible(true);
        searchButtonField.setAccessible(true);
        JTextField keywordField = (JTextField) keywordFieldField.get(panel);
        JButton searchButton = (JButton) searchButtonField.get(panel);
        keywordField.setText("test");
        // Simulate button click
        searchButton.doClick();
        keywordField.setText("test2");
        // Simulate enter key
        for (var l : keywordField.getActionListeners()) l.actionPerformed(new java.awt.event.ActionEvent(keywordField, java.awt.event.ActionEvent.ACTION_PERFORMED, ""));
        // Wait for async workers
        Thread.sleep(250);
        Mockito.verify(mockService, Mockito.times(2)).getReviewsByKeywords(Mockito.anyList());
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
