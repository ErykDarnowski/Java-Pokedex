package pokedex.ui;

import pokedex.model.Pokemon;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Main search and browsing interface for Pokemon data.
 * Provides search functionality, grid display, and virtual scrolling
 * for optimal performance with large datasets.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class SearchView extends JPanel {

    // Performance configuration
    private static final int INITIAL_BATCH_SIZE = 50;
    private static final int LAZY_LOAD_DELAY_MS = 1;

    // Data and state
    private final List<Pokemon> allPokemon;
    private final Consumer<Pokemon> selectionHandler;
    private List<Pokemon> filteredPokemon;
    private String lastSearchTerm = "";

    // UI components
    private final JTextField searchField;
    private final JPanel pokemonGrid;
    private final JScrollPane scrollPane;
    private final JPanel gridContainer;
    private final Map<Pokemon, JPanel> pokemonPanelCache;

    /**
     * Creates a new SearchView with the specified Pokemon data and selection handler.
     * 
     * @param pokemonList the complete list of Pokemon to display
     * @param onPokemonSelect callback invoked when a Pokemon is selected
     * @param unused legacy parameter (retained for compatibility)
     */
    public SearchView(List<Pokemon> pokemonList, Consumer<Pokemon> onPokemonSelect, Runnable unused) {
        this.allPokemon = sortPokemonAlphabetically(pokemonList);
        this.selectionHandler = onPokemonSelect;
        this.filteredPokemon = allPokemon;
        this.searchField = new JTextField(30);
        this.pokemonGrid = new JPanel();
        this.pokemonPanelCache = new HashMap<>();
        
        try {
            this.gridContainer = createGridContainer();
            this.scrollPane = createScrollPane();
            initializeView();
        } catch (Exception e) {
            ErrorHandler.showError(this, e, "inicjalizacja widoku wyszukiwania");
            throw new RuntimeException("Failed to initialize search view", e);
        }
    }

    /**
     * Initializes the main view layout and components.
     */
    private void initializeView() {
        setupMainLayout();
        createHeaderSection();
        createContentSection();
        createFooterSection();
        setupEventHandlers();
        populateInitialContent();
    }

    /**
     * Sets up the main layout configuration.
     */
    private void setupMainLayout() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.Colors.BACKGROUND);
    }

    /**
     * Creates the header section with logo and search functionality.
     */
    private void createHeaderSection() {
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
    }

    /**
     * Creates and configures the header panel.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        setupFocusHandling(panel);

        JLabel logoLabel = createLogoLabel();
        JPanel searchPanel = createSearchPanel();

        panel.add(logoLabel, BorderLayout.NORTH);
        panel.add(searchPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates and configures the application logo.
     */
    private JLabel createLogoLabel() {
        JLabel logo = new JLabel();
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        try {
            Image logoImage = ImageIO.read(getClass().getResource("/logo.png"));
            logo.setIcon(new ImageIcon(logoImage));
        } catch (Exception e) {
            ErrorHandler.showError(this, e, "ładowanie logo aplikacji");
            setupFallbackLogo(logo);
        }

        return logo;
    }

    /**
     * Sets up fallback logo when image loading fails.
     */
    private void setupFallbackLogo(JLabel logo) {
        logo.setText(UIConstants.Strings.APP_TITLE_FALLBACK);
        logo.setForeground(UIConstants.Colors.TEXT_PRIMARY);
    }

    /**
     * Creates and configures the search panel.
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        configureSearchField();
        panel.add(searchField);

        return panel;
    }

    /**
     * Configures the search field appearance and behavior.
     */
    private void configureSearchField() {
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setPreferredSize(UIConstants.Sizes.SEARCH_FIELD);
        searchField.setToolTipText(UIConstants.Strings.TOOLTIP_SEARCH);
        
        setupSearchFieldPlaceholder();
        setupSearchFieldListeners();
    }

    /**
     * Sets up the search field placeholder functionality.
     */
    private void setupSearchFieldPlaceholder() {
        searchField.setText(UIConstants.Strings.PLACEHOLDER_SEARCH);
        searchField.setForeground(Color.LIGHT_GRAY);
        
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                handleSearchFieldFocusGained();
            }

            @Override
            public void focusLost(FocusEvent e) {
                handleSearchFieldFocusLost();
            }
        });
    }

    /**
     * Handles search field focus gained events.
     */
    private void handleSearchFieldFocusGained() {
        if (isPlaceholderText()) {
            searchField.setText("");
            searchField.setForeground(Color.BLACK);
        }
    }

    /**
     * Handles search field focus lost events.
     */
    private void handleSearchFieldFocusLost() {
        if (searchField.getText().isEmpty()) {
            searchField.setText(UIConstants.Strings.PLACEHOLDER_SEARCH);
            searchField.setForeground(Color.GRAY);
        }
    }

    /**
     * Sets up search field change listeners.
     */
    private void setupSearchFieldListeners() {
        searchField.addCaretListener(e -> performSearch(searchField.getText()));
    }

    /**
     * Creates the main content section with Pokemon grid.
     */
    private void createContentSection() {
        setupPokemonGrid();
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets up the Pokemon grid layout and container.
     */
    private void setupPokemonGrid() {
        pokemonGrid.setLayout(new GridLayout(0, 1, UIConstants.Sizes.GRID_HGAP, UIConstants.Sizes.GRID_VGAP));
        pokemonGrid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        pokemonGrid.setOpaque(false);
    }

    /**
     * Creates the grid container panel.
     */
    private JPanel createGridContainer() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.CENTER));
        container.setOpaque(false);
        container.add(pokemonGrid);
        return container;
    }

    /**
     * Creates and configures the scroll pane.
     */
    private JScrollPane createScrollPane() {
        JScrollPane pane = new JScrollPane(gridContainer);
        pane.setBorder(null);
        pane.setViewportBorder(null);
        pane.setOpaque(false);
        pane.getViewport().setOpaque(false);
        pane.getVerticalScrollBar().setUnitIncrement(15);
        pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        return pane;
    }

    /**
     * Creates the footer section with author information.
     */
    private void createFooterSection() {
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates and configures the footer panel.
     */
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setOpaque(false);
        setupFocusHandling(panel);

        JLabel authorLabel = new JLabel(UIConstants.Strings.AUTHOR);
        authorLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        panel.add(authorLabel);

        return panel;
    }

    /**
     * Sets up global event handlers for the view.
     */
    private void setupEventHandlers() {
        setupResizeHandler();
        setupGlobalFocusHandling();
    }

    /**
     * Sets up the window resize handler for dynamic column adjustment.
     */
    private void setupResizeHandler() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateGridColumns();
            }
        });
    }

    /**
     * Sets up global focus handling to remove focus from search field.
     */
    private void setupGlobalFocusHandling() {
        MouseAdapter unfocusAdapter = createUnfocusMouseAdapter();
        applyUnfocusHandling(this, unfocusAdapter);
        applyUnfocusHandling(scrollPane.getViewport(), unfocusAdapter);
        applyUnfocusHandling(gridContainer, unfocusAdapter);
        applyUnfocusHandling(pokemonGrid, unfocusAdapter);
    }

    /**
     * Creates a mouse adapter for removing focus from search field.
     */
    private MouseAdapter createUnfocusMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (searchField.hasFocus()) {
                    ((Component) e.getSource()).requestFocusInWindow();
                }
            }
        };
    }

    /**
     * Applies unfocus handling to a component.
     */
    private void applyUnfocusHandling(Component component, MouseAdapter adapter) {
        component.addMouseListener(adapter);
        component.setFocusable(true);
    }

    /**
     * Sets up focus handling for a panel.
     */
    private void setupFocusHandling(JPanel panel) {
        panel.setFocusable(true);
        panel.addMouseListener(createUnfocusMouseAdapter());
    }

    /**
     * Populates the initial content display.
     */
    private void populateInitialContent() {
        updateDisplayedPokemon();
    }

    /**
     * Performs search based on the provided term.
     */
    private void performSearch(String searchTerm) {
        if (searchTerm.equals(lastSearchTerm)) {
            return;
        }
        
        lastSearchTerm = searchTerm;
        List<Pokemon> filtered = filterPokemon(searchTerm);
        
        if (!filtered.equals(filteredPokemon)) {
            filteredPokemon = filtered;
            updateDisplayedPokemon();
        }
    }

    /**
     * Filters Pokemon based on the search term.
     */
    private List<Pokemon> filterPokemon(String searchTerm) {
        String normalizedTerm = normalizeSearchTerm(searchTerm);
        
        return allPokemon.stream()
            .filter(pokemon -> matchesPokemon(pokemon, normalizedTerm))
            .collect(Collectors.toList());
    }

    /**
     * Normalizes a search term for comparison.
     */
    private String normalizeSearchTerm(String term) {
        if (isPlaceholderText(term)) {
            return "";
        }
        return term.trim().toLowerCase().replace("-", "");
    }

    /**
     * Checks if a Pokemon matches the search criteria.
     */
    private boolean matchesPokemon(Pokemon pokemon, String normalizedTerm) {
        if (normalizedTerm.isEmpty()) {
            return true;
        }
        
        String normalizedName = pokemon.getName().toLowerCase().replace("-", "");
        boolean nameMatches = normalizedName.contains(normalizedTerm);
        boolean idMatches = normalizedTerm.matches("\\d+") && 
                           String.valueOf(pokemon.getId()).equals(normalizedTerm);
        
        return nameMatches || idMatches;
    }

    /**
     * Updates the displayed Pokemon in the grid.
     */
    private void updateDisplayedPokemon() {
        clearPokemonGrid();
        displayInitialBatch();
        updateGridColumns();
        refreshGridDisplay();
        
        if (hasRemainingPokemon()) {
            loadRemainingPokemonAsync();
        }
    }

    /**
     * Clears the Pokemon grid.
     */
    private void clearPokemonGrid() {
        pokemonGrid.removeAll();
    }

    /**
     * Displays the initial batch of Pokemon.
     */
    private void displayInitialBatch() {
        int batchSize = Math.min(INITIAL_BATCH_SIZE, filteredPokemon.size());
        for (int i = 0; i < batchSize; i++) {
            Pokemon pokemon = filteredPokemon.get(i);
            JPanel pokemonPanel = getOrCreatePokemonPanel(pokemon);
            pokemonGrid.add(pokemonPanel);
        }
    }

    /**
     * Refreshes the grid display.
     */
    private void refreshGridDisplay() {
        pokemonGrid.revalidate();
        pokemonGrid.repaint();
        gridContainer.revalidate();
        gridContainer.repaint();
    }

    /**
     * Checks if there are remaining Pokemon to load.
     */
    private boolean hasRemainingPokemon() {
        return filteredPokemon.size() > INITIAL_BATCH_SIZE;
    }

    /**
     * Loads remaining Pokemon asynchronously.
     */
    private void loadRemainingPokemonAsync() {
        SwingUtilities.invokeLater(() -> new SwingWorker<Void, JPanel>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = INITIAL_BATCH_SIZE; i < filteredPokemon.size(); i++) {
                    Pokemon pokemon = filteredPokemon.get(i);
                    JPanel panel = getOrCreatePokemonPanel(pokemon);
                    publish(panel);
                    Thread.sleep(LAZY_LOAD_DELAY_MS);
                }
                return null;
            }

            @Override
            protected void process(List<JPanel> chunks) {
                for (JPanel panel : chunks) {
                    pokemonGrid.add(panel);
                }
                refreshGridDisplay();
            }
        }.execute());
    }

    /**
     * Gets or creates a Pokemon panel from cache.
     */
    private JPanel getOrCreatePokemonPanel(Pokemon pokemon) {
        return pokemonPanelCache.computeIfAbsent(pokemon, this::createPokemonPanel);
    }

    /**
     * Creates a new Pokemon display panel.
     */
    private JPanel createPokemonPanel(Pokemon pokemon) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(UIConstants.Sizes.POKEMON_PANEL_DIM);

        JButton imageButton = createPokemonImageButton(pokemon);
        JLabel nameLabel = createPokemonNameLabel(pokemon);

        panel.add(imageButton, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Creates the image button for a Pokemon.
     */
    private JButton createPokemonImageButton(Pokemon pokemon) {
        JButton button = new JButton();
        button.setPreferredSize(new Dimension(UIConstants.Sizes.POKEMON_PANEL_WIDTH - 20, 150));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> selectionHandler.accept(pokemon));

        loadPokemonImageAsync(button, pokemon);
        return button;
    }

    /**
     * Loads Pokemon image asynchronously for the button.
     */
    private void loadPokemonImageAsync(JButton button, Pokemon pokemon) {
        SwingUtilities.invokeLater(() -> new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                return ImageCache.loadScaled(pokemon.getId(), 130);
            }

            @Override
            protected void done() {
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        button.setIcon(icon);
                    } else {
                        showImageError(button);
                    }
                } catch (Exception e) {
                    showImageLoadError(button);
                }
            }
        }.execute());
    }

    /**
     * Shows image error message on button.
     */
    private void showImageError(JButton button) {
        String errorHtml = "<html><center><span style='" + 
                          UIConstants.Styles.IMG_ERR_SEARCH + "'>" + 
                          UIConstants.Strings.NO_IMAGE + 
                          "</span></center></html>";
        button.setText(errorHtml);
    }

    /**
     * Shows image load error message on button.
     */
    private void showImageLoadError(JButton button) {
        String errorHtml = "<html><center><span style='" + 
                          UIConstants.Styles.IMG_ERR_SEARCH + "'>" + 
                          UIConstants.Strings.ERROR_LOADING + 
                          "</span></center></html>";
        button.setText(errorHtml);
    }

    /**
     * Creates the name label for a Pokemon.
     */
    private JLabel createPokemonNameLabel(Pokemon pokemon) {
        JLabel label = new JLabel(pokemon.getName());
        label.setFont(UIConstants.Fonts.NAMES);
        label.setForeground(UIConstants.Colors.TEXT_PRIMARY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    /**
     * Updates the grid column layout based on available width.
     */
    private void updateGridColumns() {
        if (scrollPane == null || scrollPane.getViewport() == null) {
            return;
        }

        int availableWidth = calculateAvailableWidth();
        int optimalColumns = calculateOptimalColumns(availableWidth);
        
        updateGridLayoutIfNeeded(optimalColumns);
    }

    /**
     * Calculates the available width for the grid.
     */
    private int calculateAvailableWidth() {
        int viewportWidth = scrollPane.getViewport().getWidth();
        int scrollBarWidth = scrollPane.getVerticalScrollBar().isVisible() ? 
                            scrollPane.getVerticalScrollBar().getWidth() : 0;
        return viewportWidth - scrollBarWidth;
    }

    /**
     * Calculates the optimal number of columns for the available width.
     */
    private int calculateOptimalColumns(int availableWidth) {
        int itemWidth = UIConstants.Sizes.POKEMON_PANEL_WIDTH;
        int gap = UIConstants.Sizes.GRID_HGAP;
        return Math.max(1, availableWidth / (itemWidth + gap));
    }

    /**
     * Updates the grid layout if the column count has changed.
     */
    private void updateGridLayoutIfNeeded(int newColumns) {
        GridLayout currentLayout = (GridLayout) pokemonGrid.getLayout();
        if (currentLayout.getColumns() != newColumns) {
            pokemonGrid.setLayout(new GridLayout(0, newColumns, 
                                               UIConstants.Sizes.GRID_HGAP, 
                                               UIConstants.Sizes.GRID_VGAP));
            pokemonGrid.revalidate();
            gridContainer.revalidate();
        }
    }

    /**
     * Sorts Pokemon alphabetically by name.
     */
    private List<Pokemon> sortPokemonAlphabetically(List<Pokemon> pokemon) {
        return pokemon.stream()
                     .sorted(Comparator.comparing(Pokemon::getName, String.CASE_INSENSITIVE_ORDER))
                     .collect(Collectors.toList());
    }

    /**
     * Checks if the current search field text is the placeholder.
     */
    private boolean isPlaceholderText() {
        return isPlaceholderText(searchField.getText());
    }

    /**
     * Checks if the given text is the placeholder text.
     */
    private boolean isPlaceholderText(String text) {
        return UIConstants.Strings.PLACEHOLDER_SEARCH.equals(text);
    }
}