package com.reviewapp.adapter.user_interface;

import com.reviewapp.application.service.ReviewService;
import com.reviewapp.application.service.StatisticsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ReviewAppMain} covering construction and tab building.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class ReviewAppMainTest {
    /**
     * Tests that constructing ReviewAppMain with valid services does not throw.
     */
    @Test
    void givenValidServices_whenMainConstructed_thenNoException() {
        // Arrange
        ReviewService mockReviewService = Mockito.mock(ReviewService.class);
        StatisticsService mockStatisticsService = Mockito.mock(StatisticsService.class);
        // Act & Assert
        assertDoesNotThrow(() -> new ReviewAppMain(mockReviewService, mockStatisticsService));
    }

    /**
     * Tests that the main window builds all expected tabs.
     */
    @Test
    void givenMainWindow_whenBuildTabs_thenContainsAllTabs() throws Exception {
        // Arrange
        ReviewService mockReviewService = Mockito.mock(ReviewService.class);
        StatisticsService mockStatisticsService = Mockito.mock(StatisticsService.class);
        ReviewAppMain main = new ReviewAppMain(mockReviewService, mockStatisticsService);
        // Act
        // Use reflection to call private buildTabs
        var method = main.getClass().getDeclaredMethod("buildTabs");
        method.setAccessible(true);
        JTabbedPane tabs = (JTabbedPane) method.invoke(main);
        // Assert
        assertNotNull(tabs);
        assertEquals(5, tabs.getTabCount());
        assertEquals("All Reviews", tabs.getTitleAt(0));
        assertEquals("Review by ID", tabs.getTitleAt(1));
        assertEquals("Filter Reviews", tabs.getTitleAt(2));
        assertEquals("Search", tabs.getTitleAt(3));
        assertEquals("Statistics", tabs.getTitleAt(4));
    }
}
