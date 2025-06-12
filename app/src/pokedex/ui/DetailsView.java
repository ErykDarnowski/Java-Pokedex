// Refactored DetailsView.java to use ImageCache.loadScaledAsync
package pokedex.ui;

import pokedex.model.PokemonDetails;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DetailsView extends JPanel {

    public DetailsView(PokemonDetails d, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.Colors.BACKGROUND);
        setBorder(new EmptyBorder(UIConstants.Sizes.PADDING_SMALL));

        try {
            initializeView(d, onBack);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "inicjalizacja widoku szczegółów Pokémona");
            createErrorStateView(onBack);
        }
    }

    private void initializeView(PokemonDetails d, Runnable onBack) {
        JButton back = new JButton(UIConstants.Strings.BACK_BUTTON);
        back.setFont(UIConstants.Fonts.BUTTON);
        back.setFocusPainted(false);
        back.setBackground(UIConstants.Colors.BUTTON_BACKGROUND);
        back.setForeground(UIConstants.Colors.BUTTON_TEXT);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> onBack.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 15, 10, 10));

        String pokemonName = d.getName() != null ? d.getName() : "Nieznany";
        JLabel nameAndId = new JLabel(pokemonName + " #" + d.getId());
        nameAndId.setFont(UIConstants.Fonts.TITLE);
        nameAndId.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        nameAndId.setBorder(new EmptyBorder(0, 0, 15, 0));
        left.add(nameAndId);

        addStyledDataLabel(left, "Gatunek", d.getSpecies());
        addStyledDataLabel(left, "Wzrost", String.format("%d cm", d.getHeight() * 10));
        addStyledDataLabel(left, "Waga", String.format("%.1f kg", d.getWeight() / 10.0));

        JLabel abilTitle = createTitleLabel("Umiejętności:");
        left.add(abilTitle);
        if (d.getAbilities() != null && !d.getAbilities().isEmpty()) {
            for (String ab : d.getAbilities()) {
                if (ab != null && !ab.trim().isEmpty()) {
                    addStyledSubLabel(left, ab);
                }
            }
        } else {
            addStyledSubLabel(left, "Brak danych");
        }

        JLabel statTitle = createTitleLabel("Statystyki:");
        left.add(statTitle);
        addStyledSubLabel(left, "HP", String.valueOf(d.getHp()));
        addStyledSubLabel(left, "Atak", String.valueOf(d.getAttack()));
        addStyledSubLabel(left, "Obrona", String.valueOf(d.getDefense()));
        addStyledSubLabel(left, "Szybkość", String.valueOf(d.getSpeed()));
        addStyledSubLabel(left, "Atak specjalny", String.valueOf(d.getSpAttack()));
        addStyledSubLabel(left, "Obrona specjalna", String.valueOf(d.getSpDefense()));

        add(left, BorderLayout.WEST);
        createImagePanel(d);
        createFooter();
    }

    private void createImagePanel(PokemonDetails d) {
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(UIConstants.Sizes.PADDING_MEDIUM));

        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        imgLabel.setPreferredSize(UIConstants.Sizes.IMAGE_DETAILS);

        right.add(imgLabel, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        ImageCache.loadScaledAsync(String.valueOf(d.getId()), UIConstants.Sizes.IMAGE_DETAILS.width)
            .thenAccept(icon -> SwingUtilities.invokeLater(() -> {
                if (icon != null) {
                    imgLabel.setIcon(icon);
                    imgLabel.setText(null);
                } else {
                    imgLabel.setText("<html><center><span style='" + UIConstants.Styles.IMG_ERR_DETAILS + "'>" + UIConstants.Strings.NO_IMAGE + "</span></center></html>");
                }
            }));
    }

    private void createFooter() {
        JLabel authorLabel = new JLabel(UIConstants.Strings.AUTHOR);
        authorLabel.setForeground(UIConstants.Colors.TEXT_PRIMARY);

        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        authorPanel.setOpaque(false);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
    }

    private void createErrorStateView(Runnable onBack) {
        removeAll();
        setLayout(new BorderLayout());

        JButton back = new JButton(UIConstants.Strings.BACK_BUTTON);
        back.setFont(UIConstants.Fonts.BUTTON);
        back.setFocusPainted(false);
        back.setBackground(UIConstants.Colors.BUTTON_BACKGROUND);
        back.setForeground(UIConstants.Colors.BUTTON_TEXT);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> onBack.run());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JLabel errorLabel = new JLabel(UIConstants.Strings.ERROR_STATS);
        errorLabel.setFont(UIConstants.Fonts.ERROR);
        errorLabel.setForeground(UIConstants.Colors.ERROR);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(errorLabel);
        add(center, BorderLayout.CENTER);
    }

    private void addStyledDataLabel(JPanel parent, String label, String value) {
        String safeLabel = label != null ? label : "Brak etykiety";
        String safeValue = value != null ? value : "Brak danych";
        String htmlText = String.format("<html>%s: <span style='color: #90EE90; font-weight: bold; font-family: %s;'>%s</span></html>",
                safeLabel, UIConstants.Fonts.HIGHLIGHT_DETAILS.getFontName(), safeValue);
        JLabel lab = new JLabel(htmlText);
        lab.setFont(UIConstants.Fonts.MONO_LARGE);
        lab.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        parent.add(lab);
    }

    private JLabel createTitleLabel(String text) {
        JLabel lab = new JLabel(text);
        lab.setFont(UIConstants.Fonts.MONO_LARGE);
        lab.setForeground(UIConstants.Colors.TEXT_HIGHLIGHT);
        return lab;
    }

    private void addStyledSubLabel(JPanel parent, String text) {
        String safeText = text != null ? text : "Brak danych";
        String htmlText = String.format("<html>&nbsp;&nbsp;&nbsp;&nbsp;• <span style='%s'>%s</span></html>",
                UIConstants.Styles.BULLET_LABEL, safeText);
        JLabel lab = new JLabel(htmlText);
        lab.setFont(UIConstants.Fonts.MONO_MED);
        lab.setForeground(UIConstants.Colors.TEXT_SECONDARY);
        parent.add(lab);
    }

    private void addStyledSubLabel(JPanel parent, String label, String value) {
        String safeLabel = label != null ? label : "Brak etykiety";
        String safeValue = value != null ? value : "Brak danych";
        String htmlText = String.format("<html>&nbsp;&nbsp;&nbsp;&nbsp;• %s: <span style='%s'>%s</span></html>",
                safeLabel, UIConstants.Styles.STATS_LABEL, safeValue);
        JLabel lab = new JLabel(htmlText);
        lab.setFont(UIConstants.Fonts.MONO_MED);
        lab.setForeground(UIConstants.Colors.TEXT_SECONDARY);
        parent.add(lab);
    }
}