package org.hospital.common.controller;

import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hospital.common.exception.DataAccessException;

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

        String userMessage = buildDatabaseErrorMessage(e);

        showError(userMessage);
        e.printStackTrace();
    }

    /**
     * Construye un mensaje de error para mostrar al usuario.
     * Si encuentra un ORA-xxxxx en la cadena de causas, usa ese mensaje;
     * si no, devuelve el genérico.
     */
    private String buildDatabaseErrorMessage(DataAccessException e) {
        // Mensaje genérico por defecto
        String generic = "Database error: " + e.getMessage();

        Throwable cause = e.getCause();
        while (cause != null) {
            String msg = cause.getMessage();
            if (msg != null && msg.contains("ORA-")) {
                String oracleText = extractOracleUserMessage(msg);
                if (oracleText != null && !oracleText.isEmpty()) {
                    return "Database error: " + oracleText;
                } else {
                    // Si no pudimos limpiar bien, devolvemos el genérico + detalle
                    return generic + " (" + msg + ")";
                }
            }
            cause = cause.getCause();
        }
        // Si no había ORA- en ninguna causa, usamos el genérico
        return generic;
    }

    /**
     * A partir de un mensaje largo de Oracle, extrae el texto "útil" para el usuario.
     * Ejemplo de entrada:
     *   "ORA-20001: El médico principal no puede ser la misma persona que el paciente.\nORA-06512: at ..."
     * Devuelve:
     *   "El médico principal no puede ser la misma persona que el paciente."
     */
    private String extractOracleUserMessage(String fullMessage) {
        if (fullMessage == null) {
            return null;
        }

        // Nos quedamos solo con la primera línea (antes del primer salto de línea)
        String[] lines = fullMessage.split("\\R");
        String firstLine = lines.length > 0 ? lines[0] : fullMessage;

        // Buscamos el primer ':' (después del código ORA-xxxxx)
        int idx = firstLine.indexOf(':');
        if (idx != -1 && idx + 1 < firstLine.length()) {
            return firstLine.substring(idx + 1).trim();
        }

        // Si no encontramos ':', devolvemos la línea completa "tal cual" pero recortada
        return firstLine.trim();
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

