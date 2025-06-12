package pokedex.ui;

import pokedex.model.PokemonDetails;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Detailed view component for displaying comprehensive Pokemon information.
 * Shows Pokemon statistics, abilities, physical characteristics, and sprite image
 * with proper error handling and responsive layout.
 * 
 * @author Eryk Darnowski (7741)
 * @version 1.0.0
 */
public class DetailsView extends JPanel {

    private final PokemonDetails pokemonDetails;
    private final Runnable backNavigation;

    /**
     * Creates a new DetailsView for the specified Pokemon.
     * 
     * @param pokemonDetails the Pokemon data to display
     * @param backNavigation callback to execute when back button is pressed
     */
    public DetailsView(PokemonDetails pokemonDetails, Runnable backNavigation) {
        this.pokemonDetails = pokemonDetails;
        this.backNavigation = backNavigation;

        try {
            initializeView();
        } catch (Exception e) {
            ErrorHandler.showError(this, e, "inicjalizacja widoku szczegółów Pokémona");
            createErrorStateView();
        }
    }

    /**
     * Initializes the main view layout and components.
     */
    private void initializeView() {
        setupMainLayout();
        createNavigationPanel();
        createContentPanel();
        createFooterPanel();
    }

    /**
     * Sets up the main layout configuration.
     */
    private void setupMainLayout() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.Colors.BACKGROUND);
        setBorder(new EmptyBorder(UIConstants.Sizes.PADDING_SMALL));
    }

    /**
     * Creates the top navigation panel with back button.
     */
    private void createNavigationPanel() {
        JButton backButton = createBackButton();
        JPanel navigationPanel = createNavigationContainer(backButton);
        add(navigationPanel, BorderLayout.NORTH);
    }

    /**
     * Creates and configures the back button.
     */
    private JButton createBackButton() {
        JButton button = new JButton(UIConstants.Strings.BACK_BUTTON);
        button.setFont(UIConstants.Fonts.BUTTON);
        button.setFocusPainted(false);
        button.setBackground(UIConstants.Colors.BUTTON_BACKGROUND);
        button.setForeground(UIConstants.Colors.BUTTON_TEXT);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        button.addActionListener(e -> backNavigation.run());
        return button;
    }

    /**
     * Creates the container panel for navigation elements.
     */
    private JPanel createNavigationContainer(JButton backButton) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        panel.add(backButton);
        return panel;
    }

    /**
     * Creates the main content panel with Pokemon information and image.
     */
    private void createContentPanel() {
        JPanel informationPanel = createInformationPanel();
        JPanel imagePanel = createImagePanel();

        add(informationPanel, BorderLayout.WEST);
        add(imagePanel, BorderLayout.EAST);
    }

    /**
     * Creates the left panel containing Pokemon information.
     */
    private JPanel createInformationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 15, 10, 10));

        addPokemonHeader(panel);
        addBasicInformation(panel);
        addAbilitiesSection(panel);
        addStatisticsSection(panel);

        return panel;
    }

    /**
     * Adds the Pokemon name and ID header to the information panel.
     */
    private void addPokemonHeader(JPanel parent) {
        String pokemonName = pokemonDetails.getName() != null ? pokemonDetails.getName() : "Nieznany";
        String headerText = pokemonName + " #" + pokemonDetails.getId();
        
        JLabel headerLabel = new JLabel(headerText);
        headerLabel.setFont(UIConstants.Fonts.TITLE);
        headerLabel.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        headerLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        parent.add(headerLabel);
    }

    /**
     * Adds basic Pokemon information (species, height, weight) to the panel.
     */
    private void addBasicInformation(JPanel parent) {
        addDataLabel(parent, "Gatunek", pokemonDetails.getSpecies());
        addDataLabel(parent, "Wzrost", formatHeight(pokemonDetails.getHeight()));
        addDataLabel(parent, "Waga", formatWeight(pokemonDetails.getWeight()));
    }

    /**
     * Adds the abilities section to the information panel.
     */
    private void addAbilitiesSection(JPanel parent) {
        JLabel abilitiesTitle = createSectionTitle("Umiejętności:");
        parent.add(abilitiesTitle);

        List<String> abilities = pokemonDetails.getAbilities();
        if (abilities != null && !abilities.isEmpty()) {
            for (String ability : abilities) {
                if (ability != null && !ability.trim().isEmpty()) {
                    addBulletPoint(parent, ability);
                }
            }
        } else {
            addBulletPoint(parent, "Brak danych");
        }
    }

    /**
     * Adds the statistics section to the information panel.
     */
    private void addStatisticsSection(JPanel parent) {
        JLabel statsTitle = createSectionTitle("Statystyki:");
        parent.add(statsTitle);

        addStatistic(parent, "HP", pokemonDetails.getHp());
        addStatistic(parent, "Atak", pokemonDetails.getAttack());
        addStatistic(parent, "Obrona", pokemonDetails.getDefense());
        addStatistic(parent, "Szybkość", pokemonDetails.getSpeed());
        addStatistic(parent, "Atak specjalny", pokemonDetails.getSpAttack());
        addStatistic(parent, "Obrona specjalna", pokemonDetails.getSpDefense());
    }

    /**
     * Creates the right panel containing the Pokemon image.
     */
    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(UIConstants.Sizes.PADDING_MEDIUM));

        JLabel imageLabel = createImageLabel();
        panel.add(imageLabel, BorderLayout.NORTH);

        loadPokemonImage(imageLabel);
        return panel;
    }

    /**
     * Creates and configures the image label.
     */
    private JLabel createImageLabel() {
        JLabel label = new JLabel();
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setPreferredSize(UIConstants.Sizes.IMAGE_DETAILS);
        return label;
    }

    /**
     * Asynchronously loads the Pokemon image.
     */
    private void loadPokemonImage(JLabel imageLabel) {
        String pokemonId = String.valueOf(pokemonDetails.getId());
        int targetSize = UIConstants.Sizes.IMAGE_DETAILS.width;

        ImageCache.loadScaledAsync(pokemonId, targetSize)
            .thenAccept(icon -> SwingUtilities.invokeLater(() -> {
                if (icon != null) {
                    imageLabel.setIcon(icon);
                    imageLabel.setText(null);
                } else {
                    showImageError(imageLabel);
                }
            }));
    }

    /**
     * Displays an error message when image loading fails.
     */
    private void showImageError(JLabel imageLabel) {
        String errorHtml = "<html><center><span style='" + 
                          UIConstants.Styles.IMG_ERR_DETAILS + "'>" + 
                          UIConstants.Strings.NO_IMAGE + 
                          "</span></center></html>";
        imageLabel.setText(errorHtml);
    }

    /**
     * Creates the footer panel with author information.
     */
    private void createFooterPanel() {
        JLabel authorLabel = new JLabel(UIConstants.Strings.AUTHOR);
        authorLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.setOpaque(false);
        footerPanel.add(authorLabel);
        
        add(footerPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the error state view when initialization fails.
     */
    private void createErrorStateView() {
        removeAll();
        setLayout(new BorderLayout());

        // Add back button
        JButton backButton = createBackButton();
        JPanel topPanel = createNavigationContainer(backButton);
        add(topPanel, BorderLayout.NORTH);

        // Add error message
        JLabel errorLabel = new JLabel(UIConstants.Strings.ERROR_STATS);
        errorLabel.setFont(UIConstants.Fonts.ERROR);
        errorLabel.setForeground(UIConstants.Colors.ERROR);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(errorLabel);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Adds a data label with highlighted value to the parent panel.
     */
    private void addDataLabel(JPanel parent, String label, String value) {
        String safeLabel = label != null ? label : "Brak etykiety";
        String safeValue = value != null ? value : "Brak danych";
        
        String htmlText = String.format(
            "<html>%s: <span style='color: #90EE90; font-weight: bold; font-family: %s;'>%s</span></html>",
            safeLabel, 
            UIConstants.Fonts.HIGHLIGHT_DETAILS.getFontName(), 
            safeValue
        );
        
        JLabel dataLabel = new JLabel(htmlText);
        dataLabel.setFont(UIConstants.Fonts.MONO_LARGE);
        dataLabel.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        parent.add(dataLabel);
    }

    /**
     * Creates a section title label.
     */
    private JLabel createSectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(UIConstants.Fonts.MONO_LARGE);
        title.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        return title;
    }

    /**
     * Adds a bullet point item to the parent panel.
     */
    private void addBulletPoint(JPanel parent, String text) {
        String safeText = text != null ? text : "Brak danych";
        String htmlText = String.format(
            "<html>&nbsp;&nbsp;&nbsp;&nbsp;• <span style='%s'>%s</span></html>",
            UIConstants.Styles.BULLET_LABEL, 
            safeText
        );
        
        JLabel bulletLabel = new JLabel(htmlText);
        bulletLabel.setFont(UIConstants.Fonts.MONO_MED);
        bulletLabel.setForeground(UIConstants.Colors.TEXT_SECONDARY);
        parent.add(bulletLabel);
    }

    /**
     * Adds a statistic item with label and value to the parent panel.
     */
    private void addStatistic(JPanel parent, String label, int value) {
        String safeLabel = label != null ? label : "Brak etykiety";
        String htmlText = String.format(
            "<html>&nbsp;&nbsp;&nbsp;&nbsp;• %s: <span style='%s'>%d</span></html>",
            safeLabel, 
            UIConstants.Styles.STATS_LABEL, 
            value
        );
        
        JLabel statLabel = new JLabel(htmlText);
        statLabel.setFont(UIConstants.Fonts.MONO_MED);
        statLabel.setForeground(UIConstants.Colors.TEXT_SECONDARY);
        parent.add(statLabel);
    }

    /**
     * Formats height from decimeters to a readable string.
     */
    private String formatHeight(int heightInDecimeters) {
        return String.format("%d cm", heightInDecimeters * 10);
    }

    /**
     * Formats weight from hectograms to a readable string.
     */
    private String formatWeight(int weightInHectograms) {
        return String.format("%.1f kg", weightInHectograms / 10.0);
    }
}