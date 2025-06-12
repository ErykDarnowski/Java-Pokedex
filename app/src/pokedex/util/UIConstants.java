package pokedex.util;

import java.awt.*;

/**
 * Centralized UI constants for consistent styling, dimensions, and text across the application.
 * Provides a single point of configuration for visual elements and user-facing strings.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public final class UIConstants {

    public static final String APP_NAME = "Java Pokédex";
    public static final String APP_VERSION = "1.0.0";

    /**
     * Prevents instantiation of this constants class.
     */
    private UIConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }

    /**
     * Font definitions for consistent typography throughout the application.
     */
    public static final class Fonts {
        public static final Font NAMES = new Font("Segoe UI", Font.BOLD, 12);
        public static final Font MONO_MED = new Font("SansSerif", Font.BOLD, 22);
        public static final Font MONO_LARGE = new Font("SansSerif", Font.BOLD, 26);
        public static final Font HIGHLIGHT_DETAILS = new Font("Monospaced", Font.PLAIN, 14);
        public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 42);
        public static final Font ERROR = new Font("SansSerif", Font.BOLD, 14);
        public static final Font BUTTON = new Font("SansSerif", Font.BOLD, 16);
    }

    /**
     * Color scheme for consistent visual appearance.
     */
    public static final class Colors {
        public static final Color BACKGROUND = new Color(197, 41, 57);
        public static final Color TEXT_PRIMARY = new Color(238, 238, 238);
        public static final Color TEXT_SECONDARY = new Color(220, 220, 220);
        public static final Color TEXT_HIGHLIGHT = new Color(240, 240, 240);
        public static final Color BUTTON_BACKGROUND = new Color(200, 200, 200);
        public static final Color BUTTON_TEXT = Color.BLACK;
        public static final Color ERROR = Color.RED;
    }

    /**
     * Standard dimensions and sizes for UI components.
     */
    public static final class Sizes {
        // Pokemon panel dimensions
        public static final int POKEMON_PANEL_WIDTH = 190;
        public static final int POKEMON_PANEL_HEIGHT = 180;
        public static final Dimension POKEMON_PANEL_DIM = new Dimension(POKEMON_PANEL_WIDTH, POKEMON_PANEL_HEIGHT);

        // Component sizes
        public static final Dimension SEARCH_FIELD = new Dimension(300, 30);
        public static final Dimension IMAGE_DETAILS = new Dimension(310, 310);
        public static final Dimension PROGRESS_BAR = new Dimension(300, 25);

        // Grid layout spacing
        public static final int GRID_HGAP = 10;
        public static final int GRID_VGAP = 10;

        // Standard padding values
        public static final Insets PADDING_SMALL = new Insets(5, 5, 5, 5);
        public static final Insets PADDING_MEDIUM = new Insets(10, 10, 10, 10);
    }

    /**
     * HTML styling strings for rich text components.
     */
    public static final class Styles {
        public static final String BULLET_LABEL = "color: #87CEEB; font-weight: bold; font-family: Monospaced;";
        public static final String STATS_LABEL = "color: #F0E68C; font-weight: bold; font-size: 110%; font-family: Monospaced;";
        public static final String IMG_ERR_SEARCH = "color:red; font-weight: bold; font-size: 14;";
        public static final String IMG_ERR_DETAILS = "color: black; font-family:SansSerif; font-weight: bold; font-size: 16;";
    }

    /**
     * User-facing text and labels for internationalization support.
     */
    public static final class Strings {
        // Application text
        public static final String LOADING = "Ładowanie...";
        public static final String APP_TITLE_FALLBACK = "Pokédex";
        public static final String AUTHOR = "Eryk Darnowski (7741) - II inf. NST (24/25)";
        public static final String BACK_BUTTON = "← Wróć";

        // Error messages
        public static final String NO_IMAGE = "BRAK OBRAZKA<br>W API";
        public static final String ERROR_LOADING = "BŁĄD ŁADOWANIA";
        public static final String ERROR_SCALING = "BŁĄD SKALOWANIA";
        public static final String ERROR_STATS = "Błąd wyświetlania danych Pokémona";

        // Tooltips and placeholders
        public static final String TOOLTIP_SEARCH = "Wyszukaj Pokémona po nazwie...";
        public static final String PLACEHOLDER_SEARCH = "Pikachu";
    }
}