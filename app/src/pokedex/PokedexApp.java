package pokedex;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JOptionPane;

import pokedex.controller.AppController;
import pokedex.util.ErrorHandler;

public class PokedexApp {

    public static void main(String[] args) {
        // Set up global exception handler for uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            System.err.println("Uncaught exception in thread " + thread.getName() + ": " + exception.getMessage());
            exception.printStackTrace();
            
            SwingUtilities.invokeLater(() -> {
                ErrorHandler.showError(
                    null, 
                    "Wystąpił nieoczekiwany błąd aplikacji.\n" +
                    "Aplikacja może działać niestabilnie.\n\n" +
                    "Szczegóły: " + exception.getMessage(),
                    "Błąd krytyczny"
                );
            });
        });

        // Force Nimbus L&F for modern UI
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException ex) {
            System.err.println("Nimbus Look and Feel not found, using system default");
            ErrorHandler.showError(null, "Nie można załadować nowoczesnego interfejsu.\n" +
                                        "Aplikacja będzie używać domyślnego wyglądu systemu.");
        } catch (InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to set Nimbus Look and Feel: " + ex.getMessage());
            ErrorHandler.showError(null, "Nie można zastosować nowoczesnego interfejsu.\n" +
                                        "Aplikacja będzie używać domyślnego wyglądu systemu.");
        }

        // Initialize the application with error handling
        SwingUtilities.invokeLater(() -> {
            try {
                new AppController().init();
            } catch (Exception ex) {
                ErrorHandler.showError(null, ex, "inicjalizacja aplikacji");
                
                // Show critical error and exit
                int choice = JOptionPane.showConfirmDialog(
                    null,
                    "Nie można uruchomić aplikacji.\n" +
                    "Czy chcesz spróbować ponownie?",
                    "Błąd uruchomienia",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
                );
                
                if (choice == JOptionPane.YES_OPTION) {
                    // Retry initialization
                    try {
                        new AppController().init();
                    } catch (Exception retryEx) {
                        ErrorHandler.showError(null, retryEx, "ponowna inicjalizacja aplikacji");
                        System.exit(1);
                    }
                } else {
                    System.exit(1);
                }
            }
        });
    }
}