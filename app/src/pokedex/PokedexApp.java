package pokedex;

import javax.swing.*;
import pokedex.controller.AppController;
import pokedex.util.ErrorHandler;

/**
 * Main application entry point for the Pokédex GUI application.
 * Handles initialization, Look & Feel setup, and global exception handling.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public final class PokedexApp {

    private static final String NIMBUS_LAF = "javax.swing.plaf.nimbus.NimbusLookAndFeel";

    /**
     * Prevents instantiation of this application launcher class.
     */
    private PokedexApp() {
        throw new AssertionError("Application launcher should not be instantiated");
    }

    /**
     * Application entry point. Sets up UI theming and initializes the controller.
     * 
     * @param args command line arguments (currently unused)
     */
    public static void main(String[] args) {
        setupGlobalExceptionHandler();
        setupLookAndFeel();
        initializeApplication();
    }

    /**
     * Sets up global exception handler for uncaught exceptions.
     * Logs exceptions to standard error without showing UI dialogs to avoid recursion.
     */
    private static void setupGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
        });
    }

    /**
     * Attempts to set Nimbus Look and Feel for modern UI appearance.
     * Falls back to system default if Nimbus is not available.
     */
    private static void setupLookAndFeel() {
        try {
            UIManager.setLookAndFeel(NIMBUS_LAF);
        } catch (ClassNotFoundException e) {
            handleLookAndFeelError("Nimbus Look and Feel not found, using system default", e);
        } catch (Exception e) {
            handleLookAndFeelError("Failed to set Nimbus Look and Feel", e);
        }
    }

    /**
     * Handles Look and Feel setup errors with appropriate logging and user notification.
     */
    private static void handleLookAndFeelError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        ErrorHandler.showError(null, "Nie można załadować nowoczesnego interfejsu.\n" +
                                    "Aplikacja będzie używać domyślnego wyglądu systemu.");
    }

    /**
     * Initializes the main application controller with error handling and retry logic.
     */
    private static void initializeApplication() {
        SwingUtilities.invokeLater(() -> {
            try {
                new AppController().init();
            } catch (Exception e) {
                handleInitializationError(e);
            }
        });
    }

    /**
     * Handles application initialization errors with user-friendly retry options.
     */
    private static void handleInitializationError(Exception e) {
        ErrorHandler.showError(null, e, "inicjalizacja aplikacji");
        
        int userChoice = showRetryDialog();
        
        if (userChoice == JOptionPane.YES_OPTION) {
            retryInitialization();
        } else {
            exitApplication();
        }
    }

    /**
     * Shows a dialog asking the user if they want to retry initialization.
     */
    private static int showRetryDialog() {
        return JOptionPane.showConfirmDialog(
            null,
            "Nie można uruchomić aplikacji.\nCzy chcesz spróbować ponownie?",
            "Błąd uruchomienia",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Attempts to retry application initialization.
     */
    private static void retryInitialization() {
        try {
            new AppController().init();
        } catch (Exception retryException) {
            ErrorHandler.showError(null, retryException, "ponowna inicjalizacja aplikacji");
            exitApplication();
        }
    }

    /**
     * Exits the application with appropriate cleanup.
     */
    private static void exitApplication() {
        System.exit(1);
    }
}