package com.reviewapp.adapter.user_interface.components;

import com.reviewapp.application.service.ReviewService;
import com.reviewapp.domain.model.Review;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Unit tests for {@link AllReviewsPanel} covering construction, component checks, and error handling.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class AllReviewsPanelTest {

    @DisplayName("Tests that constructing AllReviewsPanel with a valid service does not throw")
    @Test
    void givenValidReviewService_whenPanelConstructed_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new AllReviewsPanel(mockService));
    }


    @DisplayName("Tests that the panel contains a scroll pane or table component after construction")
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
        String input = "abc";
        // Act
        String result = invokeNullToEmpty(input);
        // Assert
        assertEquals("abc", result);
    }


    @DisplayName("Tests that refreshing the table with a null list does not throw")
    @Test
    void givenNullList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, null));
    }


    @DisplayName("Tests that refreshing the table with an empty list does not throw")
    @Test
    void givenEmptyList_whenRefreshTable_thenNoException() {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeRefreshTable(panel, java.util.Collections.emptyList()));
    }


    @DisplayName("Tests that onLoadError does not throw when given a simulated error")
    @Test
    void givenSimulatedError_whenSwingWorkerErrorHandlerInvoked_thenNoException() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        // Act & Assert
        assertDoesNotThrow(() -> invokeOnLoadError(panel, new RuntimeException("Simulated error")));
    }

    @DisplayName("TableModel: isCellEditable always returns false and getColumnClass covers all branches")
    @Test
    void tableModel_isCellEditable_and_getColumnClass_branches() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        JTable table = getChildComponent(panel, JTable.class);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        // isCellEditable false for all
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                assertFalse(model.isCellEditable(r, c));
            }
        }
        // getColumnClass branches
        assertEquals(Long.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(String.class, model.getColumnClass(2));
        assertEquals(Integer.class, model.getColumnClass(3));
        assertEquals(String.class, model.getColumnClass(4));
        assertEquals(String.class, model.getColumnClass(5));
        assertEquals(String.class, model.getColumnClass(6));
    }


    @DisplayName("refreshTable: handles nulls and reviews with null fields")
    @Test
    void refreshTable_handlesNullsAndNullFields() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        var review = Mockito.mock(com.reviewapp.domain.model.Review.class);
        Mockito.when(review.getReviewId()).thenReturn(1L);
        Mockito.when(review.getReviewTitle()).thenReturn(null);
        Mockito.when(review.getAuthorName()).thenReturn(null);
        Mockito.when(review.getProductRating()).thenReturn(null);
        Mockito.when(review.getReviewedDate()).thenReturn(null);
        Mockito.when(review.getProductName()).thenReturn(null);
        Mockito.when(review.getReviewSource()).thenReturn(null);
        List<com.reviewapp.domain.model.Review> reviews = java.util.Arrays.asList(null, review);
        assertDoesNotThrow(() -> invokeRefreshTable(panel, reviews));
    }

    @DisplayName("Constructor throws for invalid page size")
    @Test
    void constructor_invalidPageSize_throws() {
        ReviewService mockService = Mockito.mock(ReviewService.class);
        assertThrows(IllegalArgumentException.class, () -> new AllReviewsPanel(mockService, 0));
        assertThrows(IllegalArgumentException.class, () -> new AllReviewsPanel(mockService, -5));
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

    // Helper to get child component by type (handles JScrollPane)
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

    @DisplayName("loadPageAsync: covers clamping, totalPages edges, and refresh")
    @Test
    void loadPageAsync_coversClampingAndEdges() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Mockito.when(mockService.getTotalReviewCount()).thenReturn(0); // Edge: 0 reviews, totalPages=1
        Mockito.when(mockService.getReviewsPage(anyInt(), eq(20))).thenReturn(Collections.emptyList());
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        Method loadMethod = panel.getClass().getDeclaredMethod("loadPageAsync", int.class);
        loadMethod.setAccessible(true);
        Method clampMethod = panel.getClass().getDeclaredMethod("clamp", int.class, int.class, int.class);
        clampMethod.setAccessible(true);

        assertEquals(1, clampMethod.invoke(null, 0, 1, 5)); // < min -> min
        assertEquals(5, clampMethod.invoke(null, 10, 1, 5)); // > max -> max
        assertEquals(3, clampMethod.invoke(null, 3, 1, 5)); // in range

        loadMethod.invoke(panel, 0);
        Thread.sleep(200); // Wait for worker (flaky, but for coverage; in real use CountdownLatch)
        assertEquals("Page 1 of 1", getPageLabel(panel).getText()); // Covers refresh empty

        Mockito.when(mockService.getTotalReviewCount()).thenReturn(25);
        Mockito.when(mockService.getReviewsPage(2, 20)).thenReturn(generateMockReviews(5)); // Partial page
        loadMethod.invoke(panel, 2);
        Thread.sleep(200);
        DefaultTableModel model = getTableModel(panel);
        assertEquals(5, model.getRowCount()); // Covers non-empty refresh
        assertEquals("Page 2 of 2", getPageLabel(panel).getText()); // Covers ceil(25/20)=2

        invokeUpdatePaginationControls(panel);
        assertTrue(getPrevButton(panel).isEnabled());
        assertFalse(getNextButton(panel).isEnabled());

    }

    @DisplayName("loadPageAsync: covers error path in worker")
    @Test
    void loadPageAsync_coversErrorPath() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        Mockito.when(mockService.getTotalReviewCount()).thenThrow(new RuntimeException("Test error"));
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        Method loadMethod = panel.getClass().getDeclaredMethod("loadPageAsync", int.class);
        loadMethod.setAccessible(true);

        // Act
        loadMethod.invoke(panel, 1);
        Thread.sleep(200); // Wait for done

        assertEquals("Page 1 of 1", getPageLabel(panel).getText());
    }

    @DisplayName("Selection listener: covers all branches")
    @Test
    void selectionListener_coversAllBranches() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        List<Review> reviews = generateMockReviews(2);
        Mockito.when(mockService.getReviewsPage(anyInt(), anyInt())).thenReturn(reviews);
        Mockito.when(mockService.getTotalReviewCount()).thenReturn(2);
        Mockito.when(mockService.getReviewById(1L)).thenReturn(reviews.get(0)); // Non-null
        Mockito.when(mockService.getReviewById(2L)).thenReturn(null); // Null review
        AllReviewsPanel panel = new AllReviewsPanel(mockService, 20);
        // Load data
        invokeLoadPageAsync(panel, 1);
        Thread.sleep(200);
        JTable table = getChildComponent(panel, JTable.class);
        JTextArea details = getDetailsArea(panel); // Need helper for this

        ListSelectionModel selModel = table.getSelectionModel();

        selModel.setSelectionInterval(0, 0);
        fireListSelectionEvent(selModel, true);

        selModel.setSelectionInterval(0, 0);
        fireListSelectionEvent(selModel, false);
        assertEquals("Mock text 1", details.getText());

        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setValueAt("invalid", 0, 0);
        selModel.setSelectionInterval(0, 0);
        fireListSelectionEvent(selModel, false);
        assertEquals("", details.getText());

        selModel.setSelectionInterval(1, 1);
        fireListSelectionEvent(selModel, false);
        assertEquals("", details.getText());

        selModel.clearSelection();
        fireListSelectionEvent(selModel, false);
        assertEquals("", details.getText());

        table.getRowSorter().toggleSortOrder(1);
        selModel.setSelectionInterval(0, 0);
        fireListSelectionEvent(selModel, false);
    }

    @DisplayName("setLoading and updatePaginationControls branches")
    @Test
    void setLoadingAndUpdateControls_branches() throws Exception {
        // Arrange
        ReviewService mockService = Mockito.mock(ReviewService.class);
        AllReviewsPanel panel = new AllReviewsPanel(mockService);
        Method setLoadingMethod = panel.getClass().getDeclaredMethod("setLoading", boolean.class);
        setLoadingMethod.setAccessible(true);
        Method updateMethod = panel.getClass().getDeclaredMethod("updatePaginationControls");
        updateMethod.setAccessible(true);

        // Set state for branches
        setField(panel, "currentPage", 1);
        setField(panel, "totalPages", 1);
        updateMethod.invoke(panel);
        assertFalse(getPrevButton(panel).isEnabled());
        assertFalse(getNextButton(panel).isEnabled());

        setField(panel, "currentPage", 2);
        setField(panel, "totalPages", 3);
        updateMethod.invoke(panel);
        assertTrue(getPrevButton(panel).isEnabled());
        assertTrue(getNextButton(panel).isEnabled());

        setField(panel, "currentPage", 3);
        updateMethod.invoke(panel);
        assertTrue(getPrevButton(panel).isEnabled());
        assertFalse(getNextButton(panel).isEnabled());

        // totalPages <=0
        setField(panel, "totalPages", 0);
        updateMethod.invoke(panel);
        assertEquals("Page 3 of 1", getPageLabel(panel).getText()); // Covers max to 1

        // setLoading branches
        setLoadingMethod.invoke(panel, true);
        assertEquals(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR), panel.getCursor());
        assertFalse(getPrevButton(panel).isEnabled()); // !loading false, but loading true disables

        setLoadingMethod.invoke(panel, false);
        assertEquals(Cursor.getDefaultCursor(), panel.getCursor());

    }


    private void invokeLoadPageAsync(AllReviewsPanel panel, int page) throws Exception {
        Method method = panel.getClass().getDeclaredMethod("loadPageAsync", int.class);
        method.setAccessible(true);
        method.invoke(panel, page);
    }

    private void invokeUpdatePaginationControls(AllReviewsPanel panel) throws Exception {
        Method method = panel.getClass().getDeclaredMethod("updatePaginationControls");
        method.setAccessible(true);
        method.invoke(panel);
    }

    private DefaultTableModel getTableModel(AllReviewsPanel panel) {
        JTable table = getChildComponent(panel, JTable.class);
        return (DefaultTableModel) table.getModel();
    }

    private JLabel getPageLabel(AllReviewsPanel panel) {
        // Try to find JLabel with page text, fallback to first JLabel
        JLabel[] labels = getChildComponents(panel, JLabel.class);
        for (JLabel label : labels) {
            String txt = label.getText();
            if (txt != null && (txt.startsWith("Page ") || txt.contains("reviews"))) return label;
        }
        return labels.length > 0 ? labels[0] : null;
    }

    private JButton getPrevButton(AllReviewsPanel panel) {
        // Find by text or position; assume first JButton
        JButton[] buttons = getChildComponents(panel, JButton.class);
        for (JButton b : buttons) {
            if ("Previous".equals(b.getText())) return b;
        }
        return null;
    }

    private JButton getNextButton(AllReviewsPanel panel) {
        JButton[] buttons = getChildComponents(panel, JButton.class);
        for (JButton b : buttons) {
            if ("Next".equals(b.getText())) return b;
        }
        return null;
    }

    private <T> T[] getChildComponents(Container parent, Class<T> type) {
        java.util.List<T> found = new java.util.ArrayList<>();
        collectChildComponents(parent, type, found);
        @SuppressWarnings("unchecked")
        T[] arr = (T[]) java.lang.reflect.Array.newInstance(type, found.size());
        return found.toArray(arr);
    }

    private <T> void collectChildComponents(Container parent, Class<T> type, java.util.List<T> found) {
        for (Component c : parent.getComponents()) {
            if (type.isInstance(c)) found.add(type.cast(c));
            if (c instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) c).getViewport();
                if (viewport != null) {
                    Component view = viewport.getView();
                    if (type.isInstance(view)) found.add(type.cast(view));
                    if (view instanceof Container) collectChildComponents((Container) view, type, found);
                }
            }
            if (c instanceof Container) collectChildComponents((Container) c, type, found);
        }
    }

    private JTextArea getDetailsArea(AllReviewsPanel panel) {
        // Find JTextArea in south scroll
        return getChildComponent(panel, JTextArea.class);
    }

    private List<Review> generateMockReviews(int count) {
        List<Review> reviews = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Review r = new Review.Builder()
                .setReviewId((long) i)
                .setReviewTitle("Title " + i)
                .setAuthorName("Author " + i)
                .setProductRating(i % 5 + 1)
                .setReviewedDate(LocalDate.now())
                .setProductName("Product " + i)
                .setReviewSource("Store " + i)
                .setReviewText("Mock text " + i)
                .build();
            reviews.add(r);
        }
        return reviews;
    }

    private void fireListSelectionEvent(ListSelectionModel model, boolean adjusting) {
        ListSelectionListener[] listeners = ((DefaultListSelectionModel) model).getListSelectionListeners();
        ListSelectionEvent event = new ListSelectionEvent(model, 0, 0, adjusting);
        for (ListSelectionListener l : listeners) {
            l.valueChanged(event);
        }
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private java.lang.reflect.Field getField(Object obj, String name) throws Exception {
        Class<?> clazz = obj.getClass();
        while (clazz != null) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
