package pokedex;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import pokedex.controller.AppController;

public class PokedexApp {

    public static void main(String[] args) {
        // Force Nimbus L&F for modern UI
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException ignored) {}

        SwingUtilities.invokeLater(() -> new AppController().init());
    }
}