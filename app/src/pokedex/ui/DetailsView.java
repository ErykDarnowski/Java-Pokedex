package pokedex.ui;

import pokedex.model.PokemonDetails;
import pokedex.util.ErrorHandler;
import pokedex.util.ImageCache;
import pokedex.util.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class DetailsView extends JPanel {

    public DetailsView(PokemonDetails d, Runnable onBack) {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND);
        setBorder(new EmptyBorder(5, 5, 5, 5));

        try {
            initializeView(d, onBack);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "inicjalizacja widoku szczegółów Pokémona");
            createErrorStateView(onBack);
        }
    }

    private void initializeView(PokemonDetails d, Runnable onBack) {
        JButton back = new JButton("← Wróć");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setFocusPainted(false);
        back.setBackground(new Color(200, 200, 200));
        back.setForeground(Color.BLACK);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> {
            try {
                onBack.run();
            } catch (Exception ex) {
                ErrorHandler.showError(this, ex, "powrót do widoku wyszukiwania");
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 15, 10, 10));

        try {
            String pokemonName = d.getName() != null ? d.getName() : "Nieznany";
            JLabel nameAndId = new JLabel(pokemonName + " #" + d.getId());
            nameAndId.setFont(UIConstants.FONT_TITLE);
            nameAndId.setForeground(Color.WHITE);
            nameAndId.setBorder(new EmptyBorder(0, 0, 15, 0));
            left.add(nameAndId);

            addDataLabel(left, "Gatunek: " + (d.getSpecies() != null ? d.getSpecies() : "Nieznany"));
            addDataLabel(left, String.format("Wzrost: %d cm", d.getHeight() * 10));
            addDataLabel(left, String.format("Waga: %.1f kg", d.getWeight() / 10.0));

            JLabel abilTitle = createTitleLabel("Umiejętności:");
            left.add(abilTitle);
            if (d.getAbilities() != null && !d.getAbilities().isEmpty()) {
                for (String ab : d.getAbilities()) {
                    if (ab != null && !ab.trim().isEmpty()) {
                        addSubLabel(left, ab);
                    }
                }
            } else {
                addSubLabel(left, "Brak danych");
            }

            JLabel statTitle = createTitleLabel("Statystyki:");
            left.add(statTitle);
            addSubLabel(left, "HP: " + d.getHp());
            addSubLabel(left, "Atak: " + d.getAttack());
            addSubLabel(left, "Obrona: " + d.getDefense());
            addSubLabel(left, "Szybkość: " + d.getSpeed());
            addSubLabel(left, "Atak specjalny: " + d.getSpAttack());
            addSubLabel(left, "Obrona specjalna: " + d.getSpDefense());

        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "wyświetlanie informacji o Pokémonie");
            JLabel errorLabel = new JLabel("Błąd wyświetlania danych Pokémona");
            errorLabel.setForeground(Color.RED);
            left.add(errorLabel);
        }

        add(left, BorderLayout.WEST);

        try {
            createImagePanel(d);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "tworzenie panelu obrazu");

            JLabel errorLabel = new JLabel("<html><center><span style='color:black;'>BRAK OBRAZKA<br>W API</span></center></html>");
            errorLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            errorLabel.setHorizontalAlignment(JLabel.CENTER);
            errorLabel.setVerticalAlignment(JLabel.CENTER);
            errorLabel.setPreferredSize(new Dimension(310, 310));

            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.setOpaque(false);
            placeholder.setPreferredSize(new Dimension(310, 310));
            placeholder.add(errorLabel);

            add(placeholder, BorderLayout.EAST);
        }

        try {
            createFooter();
        } catch (Exception ex) {
            System.err.println("Failed to create footer: " + ex.getMessage());
        }
    }

    private void createImagePanel(PokemonDetails d) {
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel imgLabel = new JLabel("");
        imgLabel.setHorizontalAlignment(JLabel.RIGHT);
        imgLabel.setVerticalAlignment(JLabel.TOP);
        imgLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 10));
        imgLabel.setForeground(Color.WHITE);

        right.add(imgLabel, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        CompletableFuture.supplyAsync(() -> {
            try {
                return ImageCache.load(String.valueOf(d.getId()));
            } catch (Exception ex) {
                System.err.println("Failed to load image for Pokemon " + d.getId() + ": " + ex.getMessage());
                return null;
            }
        }).thenAccept(icon -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (icon != null) {
                        Image scaled = icon.getImage().getScaledInstance(310, 310, Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(scaled));
                        imgLabel.setText(null);
                    } else {
                        imgLabel.setText(String.format("<html><center><span style='%s'>BRAK OBRAZKA<br>W API</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                        imgLabel.setHorizontalAlignment(JLabel.CENTER);
                        imgLabel.setVerticalAlignment(JLabel.CENTER);
                        imgLabel.setPreferredSize(new Dimension(310, 310));
                        imgLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                        imgLabel.setVerticalTextPosition(SwingConstants.CENTER);
                        imgLabel.setIcon(null);
                    }
                } catch (Exception ex) {
                    ErrorHandler.showError(this, ex, "skalowanie obrazu Pokémona");
                    imgLabel.setText(String.format("<html><center><span style='%s'>BŁĄD SKALOWANIA</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("Async image loading failed: " + throwable.getMessage());
	    	imgLabel.setText(String.format("<html><center><span style='%s'>BŁĄD ŁADOWANIA</span></center></html>", UIConstants.IMG_ERR_STYLE_DETAILS));
                imgLabel.setHorizontalAlignment(JLabel.CENTER);
                imgLabel.setVerticalAlignment(JLabel.CENTER);
                imgLabel.setPreferredSize(new Dimension(310, 310));
                imgLabel.setHorizontalTextPosition(SwingConstants.CENTER);
                imgLabel.setVerticalTextPosition(SwingConstants.CENTER);
                imgLabel.setIcon(null);
            });
            return null;
        });
    }

    private void createFooter() {
        JLabel authorLabel = new JLabel("Eryk Darnowski (7741) - II inf. NST (24/25)");
        authorLabel.setForeground(Color.WHITE);

        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        authorPanel.setOpaque(false);
        authorPanel.add(authorLabel);
        add(authorPanel, BorderLayout.SOUTH);
    }

    private void createErrorStateView(Runnable onBack) {
        removeAll();
        setLayout(new BorderLayout());

        JButton back = new JButton("← Wróć");
        back.setFont(new Font("SansSerif", Font.BOLD, 16));
        back.setFocusPainted(false);
        back.setBackground(new Color(200, 200, 200));
        back.setForeground(Color.BLACK);
        back.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        back.addActionListener(e -> {
            try {
                onBack.run();
            } catch (Exception ex) {
                System.err.println("Error in back button: " + ex.getMessage());
            }
        });

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
        top.add(back);
        add(top, BorderLayout.NORTH);

        JLabel errorLabel = new JLabel("Nie można wyświetlić szczegółów Pokémona");
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(JLabel.CENTER);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(errorLabel);
        add(center, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void addDataLabel(JPanel parent, String text) {
        try {
            JLabel lab = new JLabel(text != null ? text : "Brak danych");
            lab.setFont(UIConstants.FONT_MONO_LARGE);
            lab.setForeground(new Color(240, 240, 240));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding data label: " + ex.getMessage());
        }
    }

    private JLabel createTitleLabel(String text) {
        try {
            JLabel lab = new JLabel(text != null ? text : "");
            lab.setFont(UIConstants.FONT_MONO_LARGE);
            lab.setForeground(new Color(240, 240, 240));
            return lab;
        } catch (Exception ex) {
            System.err.println("Error creating title label: " + ex.getMessage());
            return new JLabel("Błąd");
        }
    }

    private void addSubLabel(JPanel parent, String text) {
        try {
            JLabel lab = new JLabel("    • " + (text != null ? text : "Brak danych"));
            lab.setFont(UIConstants.FONT_MONO_MED);
            lab.setForeground(new Color(220, 220, 220));
            parent.add(lab);
        } catch (Exception ex) {
            System.err.println("Error adding sub label: " + ex.getMessage());
        }
    }
}