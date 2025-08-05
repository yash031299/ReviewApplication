package com.reviewapp.adapter.user_interface.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AlertDialogs} covering dialog methods for info, warning, error, and confirm.
 * Each test follows the Arrange-Act-Assert pattern and documents the scenario tested.
 */
class AlertDialogsTest {

    @SuppressWarnings("Tests that showing info dialog with null parent does not throw")
    @Test
    void givenNullParent_whenShowInfoDialog_thenNoException() {
        // Act & Assert
        assertDoesNotThrow(() -> AlertDialogs.info(null, "Info", "Message"));
    }

    @DisplayName("Tests that showing warning dialog with null parent does not throw")
    @Test
    void givenNullParent_whenShowWarnDialog_thenNoException() {
        // Act & Assert
        assertDoesNotThrow(() -> AlertDialogs.warn(null, "Warning", "Warn message"));
    }


    @DisplayName("Tests that showing error dialog with null parent does not throw")
    @Test
    void givenNullParent_whenShowErrorDialog_thenNoException() {
        // Act & Assert
        assertDoesNotThrow(() -> AlertDialogs.error(null, "Error", "Error message"));
    }


    @DisplayName("Tests that showing confirm dialog with null parent returns a valid option")
    @Test
    void givenNullParent_whenShowConfirmDialog_thenValidOption() {
        // Act
        int result = AlertDialogs.confirm(null, "Confirm", "Are you sure?");
        // Assert
        assertTrue(result == JOptionPane.OK_OPTION || result == JOptionPane.CANCEL_OPTION);
    }


    @DisplayName("Tests that showing dialogs with empty title and message does not throw")
    @Test
    void givenEmptyTitleAndMessage_whenShowDialogs_thenNoException() {
        // Act & Assert
        assertDoesNotThrow(() -> AlertDialogs.info(null, "", ""));
        assertDoesNotThrow(() -> AlertDialogs.warn(null, "", ""));
        assertDoesNotThrow(() -> AlertDialogs.error(null, "", ""));
        assertDoesNotThrow(() -> AlertDialogs.confirm(null, "", ""));
    }
}
