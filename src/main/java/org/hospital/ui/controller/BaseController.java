package org.hospital.ui.controller;

import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hospital.exception.DataAccessException;

/**
 * Base controller with common functionality for all controllers.
 */
public abstract class BaseController {
    protected final Logger logger = Logger.getLogger(getClass().getName());
    
    /**
     * Show an error message dialog.
     */
    protected void showError(String message) {
        logger.warning("Error shown to user: " + message);
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show a success message dialog.
     */
    protected void showSuccess(String message) {
        logger.info("Success: " + message);
        JOptionPane.showMessageDialog(null, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show a confirmation dialog.
     */
    protected boolean showConfirmation(String message) {
        logger.fine("Confirmation requested: " + message);
        int result = JOptionPane.showConfirmDialog(null, message, "Confirm", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        boolean confirmed = result == JOptionPane.YES_OPTION;
        logger.fine("User " + (confirmed ? "confirmed" : "cancelled"));
        return confirmed;
    }
    
    /**
     * Handle a DataAccessException and show appropriate error message.
     */
    protected void handleDataAccessException(DataAccessException e) {
        logger.severe("Database error occurred: " + e.getMessage());
        showError("Database error: " + e.getMessage());
        e.printStackTrace();
    }
    
    /**
     * Handle a general exception and show appropriate error message.
     */
    protected void handleException(Exception e) {
        logger.severe("Unexpected error occurred: " + e.getMessage());
        showError("Error: " + e.getMessage());
        e.printStackTrace();
    }
}

