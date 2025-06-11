package pokedex.util;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

/**
 * Centralized error handling utility for the Pokédex application.
 * Provides user-friendly error dialogs for different types of exceptions.
 */
public class ErrorHandler {
    
    // Error dialog titles
    private static final String NETWORK_ERROR_TITLE = "Błąd połączenia";
    private static final String FILE_ERROR_TITLE = "Błąd pliku";
    private static final String DATA_ERROR_TITLE = "Błąd danych";
    private static final String GENERAL_ERROR_TITLE = "Błąd aplikacji";
    
    /**
     * Shows an appropriate error dialog based on the exception type
     * @param parent Parent component for the dialog
     * @param exception The exception that occurred
     * @param context Additional context about where the error occurred
     */
    public static void showError(Component parent, Exception exception, String context) {
        String title;
        String message;
        
        // Determine error type and create appropriate message
        if (isNetworkError(exception)) {
            title = NETWORK_ERROR_TITLE;
            message = createNetworkErrorMessage(exception, context);
        } else if (isFileError(exception)) {
            title = FILE_ERROR_TITLE;
            message = createFileErrorMessage(exception, context);
        } else if (isDataError(exception)) {
            title = DATA_ERROR_TITLE;
            message = createDataErrorMessage(exception, context);
        } else {
            title = GENERAL_ERROR_TITLE;
            message = createGeneralErrorMessage(exception, context);
        }
        
        showErrorDialog(parent, message, title);
    }
    
    /**
     * Shows an error dialog with custom message
     * @param parent Parent component for the dialog
     * @param message Custom error message
     * @param title Dialog title
     */
    public static void showError(Component parent, String message, String title) {
        showErrorDialog(parent, message, title);
    }
    
    /**
     * Shows a simple error dialog with default title
     * @param parent Parent component for the dialog
     * @param message Error message
     */
    public static void showError(Component parent, String message) {
        showErrorDialog(parent, message, GENERAL_ERROR_TITLE);
    }
    
    /**
     * Shows a network connectivity error
     * @param parent Parent component for the dialog
     * @param context Context where the error occurred
     */
    public static void showNetworkError(Component parent, String context) {
        String message = "Nie można połączyć się z internetem.\n" +
                        "Sprawdź połączenie internetowe i spróbuj ponownie.";
        if (context != null && !context.isEmpty()) {
            message = "Błąd podczas: " + context + "\n\n" + message;
        }
        showErrorDialog(parent, message, NETWORK_ERROR_TITLE);
    }
    
    /**
     * Shows a timeout error
     * @param parent Parent component for the dialog
     * @param context Context where the timeout occurred
     */
    public static void showTimeoutError(Component parent, String context) {
        String message = "Operacja trwała zbyt długo i została przerwana.\n" +
                        "Spróbuj ponownie za chwilę.";
        if (context != null && !context.isEmpty()) {
            message = "Timeout podczas: " + context + "\n\n" + message;
        }
        showErrorDialog(parent, message, NETWORK_ERROR_TITLE);
    }
    
    // Private helper methods
    
    private static void showErrorDialog(Component parent, String message, String title) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                parent,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
            );
        });
    }
    
    private static boolean isNetworkError(Exception e) {
        return e instanceof ConnectException ||
               e instanceof UnknownHostException ||
               e instanceof SocketTimeoutException ||
               e instanceof TimeoutException ||
               e instanceof MalformedURLException ||
               e instanceof URISyntaxException ||
               (e instanceof IOException && e.getMessage() != null && 
                (e.getMessage().contains("timeout") || 
                 e.getMessage().contains("connection") ||
                 e.getMessage().contains("network")));
    }
    
    private static boolean isFileError(Exception e) {
        return e instanceof FileNotFoundException ||
               (e instanceof IOException && e.getMessage() != null &&
                (e.getMessage().contains("file") ||
                 e.getMessage().contains("directory") ||
                 e.getMessage().contains("path")));
    }
    
    private static boolean isDataError(Exception e) {
        return e.getClass().getSimpleName().contains("JSON") ||
               e.getClass().getSimpleName().contains("Parse") ||
               (e instanceof IllegalArgumentException) ||
               (e instanceof NumberFormatException) ||
               (e.getMessage() != null && 
                (e.getMessage().contains("parse") ||
                 e.getMessage().contains("format") ||
                 e.getMessage().contains("invalid data")));
    }
    
    private static String createNetworkErrorMessage(Exception e, String context) {
        StringBuilder message = new StringBuilder();
        
        if (context != null && !context.isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        
        if (e instanceof ConnectException) {
            message.append("Nie można połączyć się z serwerem.\n")
                   .append("Sprawdź połączenie internetowe.");
        } else if (e instanceof UnknownHostException) {
            message.append("Nie można znaleźć serwera.\n")
                   .append("Sprawdź połączenie internetowe i spróbuj ponownie.");
        } else if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
            message.append("Przekroczono limit czasu połączenia.\n")
                   .append("Serwer może być przeciążony. Spróbuj ponownie za chwilę.");
        } else if (e instanceof MalformedURLException) {
            message.append("Nieprawidłowy adres URL.\n")
                   .append("Skontaktuj się z deweloperem aplikacji.");
        } else if (e instanceof URISyntaxException) {
            message.append("Nieprawidłowy adres zasobu.\n")
                   .append("Skontaktuj się z deweloperem aplikacji.");
        } else {
            message.append("Wystąpił błąd połączenia sieciowego.\n")
                   .append("Sprawdź połączenie internetowe i spróbuj ponownie.");
        }
        
        return message.toString();
    }
    
    private static String createFileErrorMessage(Exception e, String context) {
        StringBuilder message = new StringBuilder();
        
        if (context != null && !context.isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        
        if (e instanceof FileNotFoundException) {
            message.append("Nie można znaleźć wymaganego pliku.\n")
                   .append("Plik może zostać pobrany automatycznie przy następnej próbie.");
        } else {
            message.append("Wystąpił błąd podczas operacji na pliku.\n")
                   .append("Sprawdź uprawnienia do zapisu i dostępne miejsce na dysku.");
        }
        
        return message.toString();
    }
    
    private static String createDataErrorMessage(Exception e, String context) {
        StringBuilder message = new StringBuilder();
        
        if (context != null && !context.isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        
        message.append("Otrzymano nieprawidłowe dane z serwera.\n")
               .append("API może być tymczasowo niedostępne. Spróbuj ponownie za chwilę.");
        
        return message.toString();
    }
    
    private static String createGeneralErrorMessage(Exception e, String context) {
        StringBuilder message = new StringBuilder();
        
        if (context != null && !context.isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        
        message.append("Wystąpił nieoczekiwany błąd aplikacji.\n");
        
        // Add specific error message if it's user-friendly
        String errorMsg = e.getMessage();
        if (errorMsg != null && !errorMsg.isEmpty() && errorMsg.length() < 100) {
            message.append("Szczegóły: ").append(errorMsg).append("\n");
        }
        
        message.append("Spróbuj ponownie lub zrestartuj aplikację.");
        
        return message.toString();
    }
}