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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized error handling utility providing user-friendly error dialogs and logging.
 * Categorizes exceptions by type and presents appropriate messages to users while
 * maintaining detailed logging for debugging purposes.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public final class ErrorHandler {
    
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());
    
    // Dialog titles - centralized for consistency
    private static final String NETWORK_ERROR_TITLE = "Błąd połączenia";
    private static final String FILE_ERROR_TITLE = "Błąd pliku";
    private static final String DATA_ERROR_TITLE = "Błąd danych";
    private static final String GENERAL_ERROR_TITLE = "Błąd aplikacji";
    
    // Prevent instantiation of utility class
    private ErrorHandler() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Displays an appropriate error dialog based on exception type and context.
     * Automatically categorizes the exception and shows a user-friendly message.
     * 
     * @param parent    the parent component for dialog positioning (may be null)
     * @param exception the exception that occurred
     * @param context   descriptive context about where the error occurred
     */
    public static void showError(Component parent, Exception exception, String context) {
        if (exception == null) {
            logger.warning("showError called with null exception");
            return;
        }
        
        logException(exception, context);
        
        ErrorCategory category = categorizeException(exception);
        String message = createMessage(exception, context, category);
        
        showErrorDialog(parent, message, category.getTitle());
    }

    /**
     * Shows an error dialog with a custom message and title.
     * 
     * @param parent  the parent component for dialog positioning (may be null)
     * @param message the error message to display
     * @param title   the dialog title
     */
    public static void showError(Component parent, String message, String title) {
        if (message == null || message.trim().isEmpty()) {
            logger.warning("showError called with empty message");
            return;
        }
        
        logger.info(String.format("Showing custom error: %s - %s", title, message));
        showErrorDialog(parent, message, title != null ? title : GENERAL_ERROR_TITLE);
    }

    /**
     * Shows an error dialog with a custom message and default title.
     * 
     * @param parent  the parent component for dialog positioning (may be null)
     * @param message the error message to display
     */
    public static void showError(Component parent, String message) {
        showError(parent, message, GENERAL_ERROR_TITLE);
    }

    /**
     * Shows a network connectivity error with standard messaging.
     * 
     * @param parent  the parent component for dialog positioning (may be null)
     * @param context descriptive context about where the error occurred
     */
    public static void showNetworkError(Component parent, String context) {
        String message = buildNetworkErrorMessage(context);
        logger.warning(String.format("Network error during: %s", context));
        showErrorDialog(parent, message, NETWORK_ERROR_TITLE);
    }

    /**
     * Shows a timeout error with standard messaging.
     * 
     * @param parent  the parent component for dialog positioning (may be null)
     * @param context descriptive context about where the timeout occurred
     */
    public static void showTimeoutError(Component parent, String context) {
        String message = buildTimeoutErrorMessage(context);
        logger.warning(String.format("Timeout error during: %s", context));
        showErrorDialog(parent, message, NETWORK_ERROR_TITLE);
    }

    /**
     * Exception categories for appropriate error handling and messaging.
     */
    private enum ErrorCategory {
        NETWORK(NETWORK_ERROR_TITLE),
        FILE(FILE_ERROR_TITLE),
        DATA(DATA_ERROR_TITLE),
        GENERAL(GENERAL_ERROR_TITLE);

        private final String title;

        ErrorCategory(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    /**
     * Categorizes an exception based on its type and characteristics.
     */
    private static ErrorCategory categorizeException(Exception exception) {
        if (isNetworkException(exception)) {
            return ErrorCategory.NETWORK;
        } else if (isFileException(exception)) {
            return ErrorCategory.FILE;
        } else if (isDataException(exception)) {
            return ErrorCategory.DATA;
        } else {
            return ErrorCategory.GENERAL;
        }
    }

    /**
     * Creates an appropriate error message based on exception category and context.
     */
    private static String createMessage(Exception exception, String context, ErrorCategory category) {
        StringBuilder message = new StringBuilder();
        
        if (context != null && !context.trim().isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        
        switch (category) {
            case NETWORK -> message.append(createNetworkMessage(exception));
            case FILE -> message.append(createFileMessage(exception));
            case DATA -> message.append(createDataMessage(exception));
            case GENERAL -> message.append(createGeneralMessage(exception));
        }
        
        return message.toString();
    }

    /**
     * Displays the error dialog on the Event Dispatch Thread.
     */
    private static void showErrorDialog(Component parent, String message, String title) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE)
        );
    }

    /**
     * Logs exception details for debugging purposes.
     */
    private static void logException(Exception exception, String context) {
        String logMessage = String.format("Exception during %s: %s", 
                                         context != null ? context : "unknown operation", 
                                         exception.getMessage());
        logger.log(Level.WARNING, logMessage, exception);
    }

    // Exception type classification methods
    private static boolean isNetworkException(Exception e) {
        return e instanceof ConnectException ||
               e instanceof UnknownHostException ||
               e instanceof SocketTimeoutException ||
               e instanceof TimeoutException ||
               e instanceof MalformedURLException ||
               e instanceof URISyntaxException ||
               isNetworkIOException(e);
    }

    private static boolean isNetworkIOException(Exception e) {
        if (!(e instanceof IOException)) return false;
        
        String message = e.getMessage();
        return message != null && (
            message.contains("timeout") || 
            message.contains("connection") ||
            message.contains("network")
        );
    }

    private static boolean isFileException(Exception e) {
        return e instanceof FileNotFoundException ||
               isFileIOException(e);
    }

    private static boolean isFileIOException(Exception e) {
        if (!(e instanceof IOException)) return false;
        
        String message = e.getMessage();
        return message != null && (
            message.contains("file") ||
            message.contains("directory") ||
            message.contains("path")
        );
    }

    private static boolean isDataException(Exception e) {
        return e.getClass().getSimpleName().contains("JSON") ||
               e.getClass().getSimpleName().contains("Parse") ||
               e instanceof IllegalArgumentException ||
               e instanceof NumberFormatException ||
               isDataFormatException(e);
    }

    private static boolean isDataFormatException(Exception e) {
        String message = e.getMessage();
        return message != null && (
            message.contains("parse") ||
            message.contains("format") ||
            message.contains("invalid data")
        );
    }

    // Message creation methods
    private static String createNetworkMessage(Exception e) {
        return switch (e) {
            case ConnectException ex -> 
                "Nie można połączyć się z serwerem.\nSprawdź połączenie internetowe.";
            case UnknownHostException ex -> 
                "Nie można znaleźć serwera.\nSprawdź połączenie internetowe i spróbuj ponownie.";
            case SocketTimeoutException ex -> 
                "Przekroczono limit czasu połączenia.\nSerwer może być przeciążony. Spróbuj ponownie za chwilę.";
            case TimeoutException ex -> 
                "Przekroczono limit czasu połączenia.\nSerwer może być przeciążony. Spróbuj ponownie za chwilę.";
            case MalformedURLException ex -> 
                "Nieprawidłowy adres URL.\nSkontaktuj się z deweloperem aplikacji.";
            case URISyntaxException ex -> 
                "Nieprawidłowy adres zasobu.\nSkontaktuj się z deweloperem aplikacji.";
            default -> 
                "Wystąpił błąd połączenia sieciowego.\nSprawdź połączenie internetowe i spróbuj ponownie.";
        };
    }

    private static String createFileMessage(Exception e) {
        return switch (e) {
            case FileNotFoundException ex -> 
                "Nie można znaleźć wymaganego pliku.\nPlik może zostać pobrany automatycznie przy następnej próbie.";
            default -> 
                "Wystąpił błąd podczas operacji na pliku.\nSprawdź uprawnienia do zapisu i dostępne miejsce na dysku.";
        };
    }

    private static String createDataMessage(Exception e) {
        return "Otrzymano nieprawidłowe dane z serwera.\n" +
               "API może być tymczasowo niedostępne. Spróbuj ponownie za chwilę.";
    }

    private static String createGeneralMessage(Exception e) {
        StringBuilder message = new StringBuilder("Wystąpił nieoczekiwany błąd aplikacji.\n");
        
        String errorMsg = e.getMessage();
        if (errorMsg != null && !errorMsg.isEmpty() && errorMsg.length() < 100) {
            message.append("Szczegóły: ").append(errorMsg).append("\n");
        }
        
        message.append("Spróbuj ponownie lub zrestartuj aplikację.");
        return message.toString();
    }

    private static String buildNetworkErrorMessage(String context) {
        StringBuilder message = new StringBuilder();
        if (context != null && !context.trim().isEmpty()) {
            message.append("Błąd podczas: ").append(context).append("\n\n");
        }
        message.append("Nie można połączyć się z internetem.\n")
               .append("Sprawdź połączenie internetowe i spróbuj ponownie.");
        return message.toString();
    }

    private static String buildTimeoutErrorMessage(String context) {
        StringBuilder message = new StringBuilder();
        if (context != null && !context.trim().isEmpty()) {
            message.append("Timeout podczas: ").append(context).append("\n\n");
        }
        message.append("Operacja trwała zbyt długo i została przerwana.\n")
               .append("Spróbuj ponownie za chwilę.");
        return message.toString();
    }
}