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
            // Create a minimal error state view
            createErrorStateView(onBack);
        }
    }

    private void initializeView(PokemonDetails d, Runnable onBack) {
        // --- TOP: Back Button ---
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

        // --- LEFT: Label column ---
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setBorder(new EmptyBorder(0, 15, 10, 10));

        try {
            // Safely display Pokemon name and ID
            String pokemonName = d.getName() != null ? d.getName() : "Nieznany";
            JLabel nameAndId = new JLabel(pokemonName + " #" + d.getId());
            nameAndId.setFont(UIConstants.FONT_TITLE);
            nameAndId.setForeground(Color.WHITE);
            nameAndId.setBorder(new EmptyBorder(0, 0, 15, 0));
            left.add(nameAndId);

            // Safely display basic info
            addDataLabel(left, "Gatunek: " + (d.getSpecies() != null ? d.getSpecies() : "Nieznany"));
            addDataLabel(left, String.format("Wzrost: %d cm", d.getHeight() * 10));
            addDataLabel(left, String.format("Waga: %.1f kg", d.getWeight() / 10.0));

            // Safely display abilities
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

            // Safely display stats
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
            // Add fallback content
            JLabel errorLabel = new JLabel("Błąd wyświetlania danych Pokémona");
            errorLabel.setForeground(Color.RED);
            left.add(errorLabel);
        }

        add(left, BorderLayout.WEST);

        // --- RIGHT: Image Panel ---
        try {
            createImagePanel(d);
        } catch (Exception ex) {
            ErrorHandler.showError(this, ex, "tworzenie panelu obrazu");
            // Create a simple placeholder panel
            JPanel placeholder = new JPanel();
            placeholder.setOpaque(false);
            placeholder.add(new JLabel("Brak obrazu"));
            add(placeholder, BorderLayout.EAST);
        }

        // --- Footer: Author Info ---
        try {
            createFooter();
        } catch (Exception ex) {
            // Don't show error for footer, it's not critical
            System.err.println("Failed to create footer: " + ex.getMessage());
        }
    }

    private void createImagePanel(PokemonDetails d) {
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel imgLabel = new JLabel("Ładowanie obrazu...");
        imgLabel.setHorizontalAlignment(JLabel.RIGHT);
        imgLabel.setVerticalAlignment(JLabel.TOP);
        imgLabel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 10));
        imgLabel.setForeground(Color.WHITE);

        right.add(imgLabel, BorderLayout.NORTH);
        add(right, BorderLayout.EAST);

        // Load image async with error handling
        CompletableFuture.supplyAsync(() -> {
            try {
                return ImageCache.load(String.valueOf(d.getId()));
            } catch (Exception ex) {
                // Log the error but don't show dialog for image loading failures
                System.err.println("Failed to load image for Pokemon " + d.getId() + ": " + ex.getMessage());
                return null;
            }
        }).thenAccept(icon -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (icon != null) {
                        Image scaled = icon.getImage().getScaledInstance(310, 310, Image.SCALE_SMOOTH);
                        imgLabel.setIcon(new ImageIcon(scaled));
                        imgLabel.setText(null); // remove loading text
                    } else {
                        imgLabel.setText("Brak obrazu");
                        imgLabel.setForeground(new Color(200, 200, 200));
                    }
                } catch (Exception ex) {
                    ErrorHandler.showError(this, ex, "skalowanie obrazu Pokémona");
                    imgLabel.setText("Błąd ładowania obrazu");
                    imgLabel.setForeground(Color.RED);
                }
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                System.err.println("Async image loading failed: " + throwable.getMessage());
                imgLabel.setText("Błąd ładowania obrazu");
                imgLabel.setForeground(Color.RED);
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
        removeAll(); // Clear any partially created content
        
        setLayout(new BorderLayout());
        
        // Back button
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

        // Error message
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